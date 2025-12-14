package com.ecoledger.auditoria.application.dto;

import com.ecoledger.auditoria.domain.model.Evidencia;
import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a single audit record.
 */
@Schema(name = "RegistroAuditoria", description = "Detalhes completos de uma auditoria processada.")
public record RegistroAuditoriaResponse(
    @Schema(description = "Identificador único da auditoria.", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,
    @Schema(description = "Identificador da movimentação auditada.", example = "987e6543-e21b-12d3-a456-426614174111")
    UUID movimentacaoId,
    @Schema(description = "Identificador do produtor associado.", example = "producer-001")
    String producerId,
    @Schema(description = "Versão da regra utilizada para processar a auditoria.", example = "1.0.0")
    String versaoRegra,
    @Schema(description = "Resultado da auditoria.", allowableValues = {"APROVADO", "REPROVADO", "REQUER_REVISAO"})
    ResultadoAuditoria resultado,
    @Schema(description = "Evidências coletadas durante a auditoria.")
    List<EvidenciaResponse> evidencias,
    @Schema(description = "Momento em que a auditoria foi processada.", type = "string", format = "date-time")
    Instant processadoEm,
    @Schema(description = "Identificador do auditor que realizou revisão manual, quando existente.", example = "auditor-001")
    String auditorId,
    @Schema(description = "Observações adicionadas pela auditoria manual.", example = "Documentação revisada manualmente.")
    String observacoes,
    @Schema(description = "Timestamp da revisão manual.", type = "string", format = "date-time")
    Instant revisadoEm
) {
    @Schema(name = "Evidencia", description = "Detalhe de evidência levantada durante a auditoria.")
    public record EvidenciaResponse(
            @Schema(description = "Tipo da evidência.", example = "QUANTITY_VALIDATION")
            String tipo,
            @Schema(description = "Descrição detalhada da evidência.", example = "Quantidade reportada excede limite máximo.")
            String detalhe
    ) {
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
