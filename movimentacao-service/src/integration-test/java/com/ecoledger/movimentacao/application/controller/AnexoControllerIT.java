package com.ecoledger.movimentacao.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnexoControllerIT {

    private static final WireMockServer wireMock = new WireMockServer(options().dynamicPort());

    @BeforeAll
    static void startWireMock() { wireMock.start(); }

    @AfterAll
    static void stopWireMock() { wireMock.stop(); }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // no external properties required for this test
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnSignedUrl() throws Exception {
        var req = new AnexoController.SignedUploadRequest("image/png");

        var mvc = mockMvc.perform(post("/anexos/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvc.getResponse().getContentAsString();
        AnexoController.SignedUploadResponse resp = objectMapper.readValue(content, AnexoController.SignedUploadResponse.class);

        assertThat(resp.objectKey()).isNotBlank();
        assertThat(resp.uploadUrl()).contains("signature=simulated");
    }
}
