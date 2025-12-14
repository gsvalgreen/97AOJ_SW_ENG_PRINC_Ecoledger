package com.ecoledger.certificacao.dto;

import jakarta.validation.constraints.NotBlank;

public record RecalcularRequest(
        @NotBlank(message = "motivo é obrigatório")
        String motivo
) {
    public String motivoOrDefault() {
        return motivo != null ? motivo : "recalculo-manual";
    }
}
