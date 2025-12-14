package com.ecoledger.auditoria.application.dto;

import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for manual audit revision.
 */
@Schema(name = "RevisaoRequest", description = "Payload utilizado para registrar uma revisão manual por auditor.")
public record RevisaoRequest(
    @Schema(description = "Identificador do auditor responsável pela revisão.", example = "auditor-001")
    @NotBlank(message = "auditorId is required")
    String auditorId,
    
    @Schema(description = "Resultado final após a revisão.", allowableValues = {"APROVADO", "REPROVADO"}, example = "APROVADO")
    @NotNull(message = "resultado is required")
    ResultadoAuditoria resultado,
    
    @Schema(description = "Observações adicionais registradas pelo auditor.", example = "Revisão manual realizada com sucesso.")
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
