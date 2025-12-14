package com.ecoledger.auditoria.application.dto;

import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for a list of audit records (history).
 */
@Schema(description = "Histórico de auditorias de um produtor.")
public record HistoricoAuditoriasResponse(
    @Schema(description = "Lista de registros de auditoria.")
    List<RegistroAuditoriaResponse> items,
    @Schema(description = "Total de registros retornados.", example = "2")
    int total
) {
    public static HistoricoAuditoriasResponse from(List<RegistroAuditoria> auditorias) {
        List<RegistroAuditoriaResponse> items = auditorias.stream()
            .map(RegistroAuditoriaResponse::from)
            .toList();
        return new HistoricoAuditoriasResponse(items, items.size());
    }
}
