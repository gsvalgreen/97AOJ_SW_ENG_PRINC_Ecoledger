package com.ecoledger.auditoria.application.dto;

import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for manual audit revision.
 */
public record RevisaoRequest(
    @NotBlank(message = "auditorId is required")
    String auditorId,
    
    @NotNull(message = "resultado is required")
    ResultadoAuditoria resultado,
    
    String observacoes
) {
    /**
     * Validates that resultado is not REQUER_REVISAO.
     */
    public void validate() {
        if (resultado == ResultadoAuditoria.REQUER_REVISAO) {
            throw new IllegalArgumentException("Cannot set result to REQUER_REVISAO in manual revision");
        }
    }
}
