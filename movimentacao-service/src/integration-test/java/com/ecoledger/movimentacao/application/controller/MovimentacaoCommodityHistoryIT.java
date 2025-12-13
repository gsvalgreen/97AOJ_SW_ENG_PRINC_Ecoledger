package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.dto.MovimentacaoDetailResponse;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.ecoledger.movimentacao.domain.repository.MovimentacaoRepository;
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
class MovimentacaoCommodityHistoryIT {

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
        wireMock.stubFor(WireMock.get(urlEqualTo("/usuarios/prod-1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"role\":\"produtor\",\"status\":\"APROVADO\"}")));
        repository.deleteAll();

        // Two entries for cmd-A with different timestamps
        repository.save(new Movimentacao(
                "prod-1",
                "cmd-A",
                "COLHEITA",
                new BigDecimal("5"),
                "KG",
                OffsetDateTime.now().minusHours(1),
                null,
                null,
                List.of()
        ));

        repository.save(new Movimentacao(
                "prod-1",
                "cmd-A",
                "COLHEITA",
                new BigDecimal("10"),
                "KG",
                OffsetDateTime.now(),
                null,
                null,
                List.of()
        ));

        // Another entry for different commodity
        repository.save(new Movimentacao(
                "prod-1",
                "cmd-B",
                "COLHEITA",
                new BigDecimal("1"),
                "KG",
                OffsetDateTime.now(),
                null,
                null,
                List.of()
        ));
    }

    @Test
    void shouldReturnHistoryForCommoditySortedDesc() throws Exception {
        var mvcResult = mockMvc.perform(get("/commodities/cmd-A/historico")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        MovimentacaoDetailResponse[] arr =
                objectMapper.readValue(content, MovimentacaoDetailResponse[].class);

        List<MovimentacaoDetailResponse> items = List.of(arr);
        assertThat(items).hasSize(2);
        // First item should have the most recent timestamp
        assertThat(items.get(0).timestamp()).isAfterOrEqualTo(items.get(1).timestamp());
        // Ensure commodityId matches
        assertThat(items.get(0).commodityId()).isEqualTo("cmd-A");
    }
}
