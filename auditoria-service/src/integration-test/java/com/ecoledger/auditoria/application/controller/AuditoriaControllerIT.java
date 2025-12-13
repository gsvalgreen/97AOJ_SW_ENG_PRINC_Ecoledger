package com.ecoledger.auditoria.application.controller;

import com.ecoledger.auditoria.application.dto.RevisaoRequest;
import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;
import com.ecoledger.auditoria.domain.repository.AuditoriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuditoriaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID auditoriaId;
    private UUID movimentacaoId;
    private String producerId;

    @BeforeEach
    void setUp() {
        auditoriaRepository.deleteAll();
        movimentacaoId = UUID.randomUUID();
        producerId = "producer-test-123";
    }

    @Nested
    @DisplayName("GET /auditorias/{id}")
    class GetAuditoriaTests {

        @Test
        @DisplayName("should return 200 with auditoria when found")
        void shouldReturnAuditoriaWhenFound() throws Exception {
            // given
            RegistroAuditoria auditoria = createAndSaveAuditoria(movimentacaoId, producerId, 
                    ResultadoAuditoria.APROVADO);
            auditoriaId = auditoria.getId();

            // when/then
            mockMvc.perform(get("/auditorias/{id}", auditoriaId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(auditoriaId.toString()))
                    .andExpect(jsonPath("$.movimentacaoId").value(movimentacaoId.toString()))
                    .andExpect(jsonPath("$.producerId").value(producerId))
                    .andExpect(jsonPath("$.resultado").value("APROVADO"))
                    .andExpect(jsonPath("$.versaoRegra").value("1.0.0-test"))
                    .andExpect(jsonPath("$.processadoEm").isNotEmpty());
        }

        @Test
        @DisplayName("should return 404 when auditoria not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // given
            UUID nonExistentId = UUID.randomUUID();

            // when/then
            mockMvc.perform(get("/auditorias/{id}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Auditoria Not Found"))
                    .andExpect(jsonPath("$.detail").value(containsString(nonExistentId.toString())));
        }
    }

    @Nested
    @DisplayName("GET /produtores/{producerId}/historico-auditorias")
    class GetHistoricoTests {

        @Test
        @DisplayName("should return audit history for producer")
        void shouldReturnHistoricoForProducer() throws Exception {
            // given
            createAndSaveAuditoria(UUID.randomUUID(), producerId, ResultadoAuditoria.APROVADO);
            createAndSaveAuditoria(UUID.randomUUID(), producerId, ResultadoAuditoria.REPROVADO);
            createAndSaveAuditoria(UUID.randomUUID(), "other-producer", ResultadoAuditoria.APROVADO);

            // when/then
            mockMvc.perform(get("/produtores/{producerId}/historico-auditorias", producerId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.items", hasSize(2)))
                    .andExpect(jsonPath("$.total").value(2))
                    .andExpect(jsonPath("$.items[*].producerId", everyItem(equalTo(producerId))));
        }

        @Test
        @DisplayName("should return empty history when no audits found")
        void shouldReturnEmptyHistorico() throws Exception {
            // when/then
            mockMvc.perform(get("/produtores/{producerId}/historico-auditorias", "non-existent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(0)))
                    .andExpect(jsonPath("$.total").value(0));
        }
    }

    @Nested
    @DisplayName("POST /auditorias/{id}/revisao")
    class AplicarRevisaoTests {

        @Test
        @DisplayName("should apply revision successfully")
        void shouldApplyRevisionSuccessfully() throws Exception {
            // given
            RegistroAuditoria auditoria = createAndSaveAuditoria(movimentacaoId, producerId,
                    ResultadoAuditoria.REQUER_REVISAO);
            auditoriaId = auditoria.getId();
            
            RevisaoRequest request = new RevisaoRequest(
                    "auditor-123", ResultadoAuditoria.APROVADO, "Manual review passed");

            // when/then
            mockMvc.perform(post("/auditorias/{id}/revisao", auditoriaId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultado").value("APROVADO"))
                    .andExpect(jsonPath("$.auditorId").value("auditor-123"))
                    .andExpect(jsonPath("$.observacoes").value("Manual review passed"))
                    .andExpect(jsonPath("$.revisadoEm").isNotEmpty());
        }

        @Test
        @DisplayName("should return 404 when auditoria not found")
        void shouldReturn404WhenAuditoriaNotFound() throws Exception {
            // given
            RevisaoRequest request = new RevisaoRequest(
                    "auditor-123", ResultadoAuditoria.APROVADO, null);

            // when/then
            mockMvc.perform(post("/auditorias/{id}/revisao", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when auditorId is missing")
        void shouldReturn400WhenAuditorIdMissing() throws Exception {
            // given
            RegistroAuditoria auditoria = createAndSaveAuditoria(movimentacaoId, producerId,
                    ResultadoAuditoria.REQUER_REVISAO);
            
            String requestJson = """
                {
                    "resultado": "APROVADO"
                }
                """;

            // when/then
            mockMvc.perform(post("/auditorias/{id}/revisao", auditoria.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"));
        }

        @Test
        @DisplayName("should return 400 when trying to set REQUER_REVISAO")
        void shouldReturn400WhenSettingRequerRevisao() throws Exception {
            // given
            RegistroAuditoria auditoria = createAndSaveAuditoria(movimentacaoId, producerId,
                    ResultadoAuditoria.REQUER_REVISAO);
            
            RevisaoRequest request = new RevisaoRequest(
                    "auditor-123", ResultadoAuditoria.REQUER_REVISAO, null);

            // when/then
            mockMvc.perform(post("/auditorias/{id}/revisao", auditoria.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when auditoria already revised")
        void shouldReturn400WhenAlreadyRevised() throws Exception {
            // given
            RegistroAuditoria auditoria = createAndSaveAuditoria(movimentacaoId, producerId,
                    ResultadoAuditoria.REQUER_REVISAO);
            auditoria.aplicarRevisao("first-auditor", ResultadoAuditoria.APROVADO, null);
            auditoriaRepository.save(auditoria);
            
            RevisaoRequest request = new RevisaoRequest(
                    "second-auditor", ResultadoAuditoria.REPROVADO, null);

            // when/then
            mockMvc.perform(post("/auditorias/{id}/revisao", auditoria.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Invalid Revision"));
        }
    }

    private RegistroAuditoria createAndSaveAuditoria(UUID movimentacaoId, String producerId,
                                                      ResultadoAuditoria resultado) {
        RegistroAuditoria auditoria = new RegistroAuditoria(
                movimentacaoId, producerId, "1.0.0-test", resultado, List.of());
        return auditoriaRepository.save(auditoria);
    }
}
