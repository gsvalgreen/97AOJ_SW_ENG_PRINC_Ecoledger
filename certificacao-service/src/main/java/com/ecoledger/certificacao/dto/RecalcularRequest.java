package com.ecoledger.certificacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload opcional utilizado para justificar o recálculo do selo.")
public record RecalcularRequest(
        @Schema(description = "Motivo do recálculo manual.", example = "Solicitação do auditor.")
        @NotBlank(message = "motivo é obrigatório")
        String motivo
) {
    public String motivoOrDefault() {
        return motivo != null ? motivo : "recalculo-manual";
    }
}
