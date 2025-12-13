package com.ecoledger.movimentacao.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnexoConfirmIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldConfirmUploadUsingNoOpProvider() throws Exception {
        // use a fixed key
        var req = new AnexoController.ConfirmUploadRequest("some-key-123");

        var mvc = mockMvc.perform(post("/anexos/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String content = mvc.getResponse().getContentAsString();
        com.ecoledger.movimentacao.application.service.AttachmentStorageService.AttachmentConfirmation resp =
                objectMapper.readValue(content, com.ecoledger.movimentacao.application.service.AttachmentStorageService.AttachmentConfirmation.class);

        assertThat(resp.objectKey()).isEqualTo("some-key-123");
        assertThat(resp.url()).contains("some-key-123");
    }
}
