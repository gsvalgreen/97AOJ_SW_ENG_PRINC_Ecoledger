package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.dto.MovimentacaoResponse;
import com.ecoledger.movimentacao.domain.repository.MovimentacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimentacaoIdempotencyIT {

    private static final WireMockServer wireMock = new WireMockServer(options().dynamicPort());

    @BeforeAll
    static void startWireMock() {
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("movimentacao.producer-approval.base-url", wireMock::baseUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovimentacaoRepository repository;

    @BeforeEach
    void setup() {
        wireMock.resetAll();
        wireMock.stubFor(get(urlEqualTo("/usuarios/prod-1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"role\":\"produtor\",\"status\":\"APROVADO\"}")));
        repository.deleteAll();
    }

    @Test
    void shouldReturnSameIdWhenUsingSameIdempotencyKey() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("producerId", "prod-1");
        payload.put("commodityId", "cmd-1");
        payload.put("tipo", "COLHEITA");
        payload.put("quantidade", new BigDecimal("1.5"));
        payload.put("unidade", "KG");
        payload.put("timestamp", OffsetDateTime.now());

        String json = objectMapper.writeValueAsString(payload);

        var mvc1 = mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "idemp-1")
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String c1 = mvc1.getResponse().getContentAsString();
        MovimentacaoResponse r1 =
                objectMapper.readValue(c1, MovimentacaoResponse.class);

        var mvc2 = mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "idemp-1")
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String c2 = mvc2.getResponse().getContentAsString();
        MovimentacaoResponse r2 =
                objectMapper.readValue(c2, MovimentacaoResponse.class);

        assertThat(r1.movimentacaoId()).isEqualTo(r2.movimentacaoId());

        List<com.ecoledger.movimentacao.domain.model.Movimentacao> all = repository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getId()).isEqualTo(r1.movimentacaoId());
    }

    @Test
    void shouldReturnSameIdWhenNoIdempotencyKeyUsesPayloadHash() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("producerId", "prod-1");
        payload.put("commodityId", "cmd-1");
        payload.put("tipo", "COLHEITA");
        payload.put("quantidade", new BigDecimal("1.5"));
        payload.put("unidade", "KG");
        payload.put("timestamp", OffsetDateTime.now());

        String json = objectMapper.writeValueAsString(payload);

        var mvc1 = mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String c1 = mvc1.getResponse().getContentAsString();
        MovimentacaoResponse r1 =
                objectMapper.readValue(c1, MovimentacaoResponse.class);

        var mvc2 = mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String c2 = mvc2.getResponse().getContentAsString();
        MovimentacaoResponse r2 =
                objectMapper.readValue(c2, MovimentacaoResponse.class);

        assertThat(r1.movimentacaoId()).isEqualTo(r2.movimentacaoId());

        List<com.ecoledger.movimentacao.domain.model.Movimentacao> all = repository.findAll();
        assertThat(all).hasSize(1);
    }
}
