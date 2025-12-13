package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.dto.MovimentacaoListResponse;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
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
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimentacaoListIT {

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
    private com.ecoledger.movimentacao.domain.repository.MovimentacaoRepository repository;

    @BeforeEach
    void setup() {
        wireMock.resetAll();
        wireMock.stubFor(WireMock.get(urlEqualTo("/usuarios/prod-1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"role\":\"produtor\",\"status\":\"APROVADO\"}")));
        repository.deleteAll();

        repository.save(new Movimentacao(
                "prod-1",
                "cmd-1",
                "COLHEITA",
                new BigDecimal("1"),
                "KG",
                OffsetDateTime.now().minusDays(2),
                null,
                null,
                List.of()
        ));

        repository.save(new Movimentacao(
                "prod-1",
                "cmd-2",
                "COLHEITA",
                new BigDecimal("2"),
                "KG",
                OffsetDateTime.now().minusDays(1),
                null,
                null,
                List.of()
        ));

        repository.save(new Movimentacao(
                "prod-1",
                "cmd-3",
                "COLHEITA",
                new BigDecimal("3"),
                "KG",
                OffsetDateTime.now(),
                null,
                null,
                List.of()
        ));
    }

    @Test
    void shouldReturnPagedList() throws Exception {
        var mvcResult = mockMvc.perform(get("/produtores/prod-1/movimentacoes")
                        .param("page", "1")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        MovimentacaoListResponse resp =
                objectMapper.readValue(content, MovimentacaoListResponse.class);

        assertThat(resp.items()).hasSize(2);
        assertThat(resp.total()).isEqualTo(3L);
    }

    @Test
    void shouldFilterByCommodityId() throws Exception {
        var mvcResult = mockMvc.perform(get("/produtores/prod-1/movimentacoes")
                        .param("commodityId", "cmd-2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        MovimentacaoListResponse resp =
                objectMapper.readValue(content, MovimentacaoListResponse.class);

        assertThat(resp.items()).hasSize(1);
        assertThat(resp.total()).isEqualTo(1L);
    }

    @Test
    void shouldReturnSecondPage() throws Exception {
        var mvcResult = mockMvc.perform(get("/produtores/prod-1/movimentacoes")
                        .param("page", "2")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        com.ecoledger.movimentacao.application.dto.MovimentacaoListResponse resp =
                objectMapper.readValue(content, com.ecoledger.movimentacao.application.dto.MovimentacaoListResponse.class);

        assertThat(resp.items()).hasSize(1);
        assertThat(resp.total()).isEqualTo(3L);
    }
}
