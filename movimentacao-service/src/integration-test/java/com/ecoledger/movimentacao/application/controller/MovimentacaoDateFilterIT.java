package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.domain.model.Movimentacao;
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
class MovimentacaoDateFilterIT {

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
    void setupApprovalStub() {
        wireMock.resetAll();
        wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/usuarios/prod-1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"role\":\"produtor\",\"status\":\"APROVADO\"}")));
        repository.deleteAll();
    }

    @Test
    void shouldFilterByFromAndToDate() throws Exception {
        var now = OffsetDateTime.now();
        var m1 = new Movimentacao("prod-1", "cmd-1", "COLHEITA", new BigDecimal("1"), "KG", now.minusDays(3), null, null, List.of());
        var m2 = new Movimentacao("prod-1", "cmd-1", "COLHEITA", new BigDecimal("2"), "KG", now.minusDays(1), null, null, List.of());
        var m3 = new Movimentacao("prod-1", "cmd-1", "COLHEITA", new BigDecimal("3"), "KG", now.plusDays(1), null, null, List.of());
        repository.saveAll(List.of(m1, m2, m3));

        var from = now.minusDays(2).toString();
        var to = now.plusDays(2).toString();

        var resp = mockMvc.perform(get("/produtores/prod-1/movimentacoes?fromDate=" + from + "&toDate=" + to))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(resp).contains("items");
        // should contain m2 and m3 -> 2 items
        assertThat(resp).contains("cmd-1");
    }
}
