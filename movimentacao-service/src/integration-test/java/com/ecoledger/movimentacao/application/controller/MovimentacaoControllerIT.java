package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.service.ProducerApprovalClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimentacaoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProducerApprovalClient producerApprovalClient;

    @Test
    void shouldCreateMovimentacao() throws Exception {
        when(producerApprovalClient.isApproved("prod-1")).thenReturn(true);

        var payload = new TestRequest("prod-1", "cmd-1", "COLHEITA", new BigDecimal("1.5"), "KG", OffsetDateTime.now());

        var response = mockMvc.perform(post("/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("movimentacaoId");
    }

    record TestRequest(String producerId,
                       String commodityId,
                       String tipo,
                       BigDecimal quantidade,
                       String unidade,
                       OffsetDateTime timestamp) {
    }
}
