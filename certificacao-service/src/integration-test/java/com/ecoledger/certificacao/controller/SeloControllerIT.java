package com.ecoledger.certificacao.controller;

import com.ecoledger.certificacao.messaging.event.AuditoriaConcluidaEvent;
import com.ecoledger.certificacao.messaging.event.ResultadoAuditoria;
import com.ecoledger.certificacao.service.SeloService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeloControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SeloService seloService;

    private final String producerId = "producer-it";

    @BeforeEach
    void setup() {
        var event = new AuditoriaConcluidaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                producerId,
                ResultadoAuditoria.APROVADO,
                "v-it",
                List.of(new AuditoriaConcluidaEvent.DetalheEvidencia("origem", "teste")),
                Instant.now()
        );
        seloService.processarAuditoriaConcluida(event);
    }

    @Test
    void shouldReturnCurrentSelo() throws Exception {
        mockMvc.perform(get("/selos/{producerId}", producerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.producerId").value(producerId))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.nivel").value("OURO"));
    }

    @Test
    void shouldRecalculateSelo() throws Exception {
        mockMvc.perform(post("/selos/{producerId}/recalcular", producerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"verificacao-periodica\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.novoStatus").isNotEmpty());

        mockMvc.perform(get("/selos/{producerId}/historico", producerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }
}
