package com.ecoledger.auditoria.application.dto;

import com.ecoledger.auditoria.domain.model.RegistroAuditoria;

import java.util.List;

/**
 * Response DTO for a list of audit records (history).
 */
public record HistoricoAuditoriasResponse(
    List<RegistroAuditoriaResponse> items,
    int total
) {
    public static HistoricoAuditoriasResponse from(List<RegistroAuditoria> auditorias) {
        List<RegistroAuditoriaResponse> items = auditorias.stream()
            .map(RegistroAuditoriaResponse::from)
            .toList();
        return new HistoricoAuditoriasResponse(items, items.size());
    }
}
