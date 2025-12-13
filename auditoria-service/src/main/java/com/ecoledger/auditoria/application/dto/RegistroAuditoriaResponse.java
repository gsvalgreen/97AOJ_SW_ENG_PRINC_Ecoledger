package com.ecoledger.auditoria.application.dto;

import com.ecoledger.auditoria.domain.model.Evidencia;
import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a single audit record.
 */
public record RegistroAuditoriaResponse(
    UUID id,
    UUID movimentacaoId,
    String producerId,
    String versaoRegra,
    ResultadoAuditoria resultado,
    List<EvidenciaResponse> evidencias,
    Instant processadoEm,
    String auditorId,
    String observacoes,
    Instant revisadoEm
) {
    public record EvidenciaResponse(String tipo, String detalhe) {
        public static EvidenciaResponse from(Evidencia evidencia) {
            return new EvidenciaResponse(evidencia.getTipo(), evidencia.getDetalhe());
        }
    }

    public static RegistroAuditoriaResponse from(RegistroAuditoria auditoria) {
        return new RegistroAuditoriaResponse(
            auditoria.getId(),
            auditoria.getMovimentacaoId(),
            auditoria.getProducerId(),
            auditoria.getVersaoRegra(),
            auditoria.getResultado(),
            auditoria.getEvidencias().stream()
                .map(EvidenciaResponse::from)
                .toList(),
            auditoria.getProcessadoEm(),
            auditoria.getAuditorId(),
            auditoria.getObservacoes(),
            auditoria.getRevisadoEm()
        );
    }
}
