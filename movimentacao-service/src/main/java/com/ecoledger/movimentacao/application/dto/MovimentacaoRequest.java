package com.ecoledger.movimentacao.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record MovimentacaoRequest(
        @NotBlank String producerId,
        @NotBlank String commodityId,
        @NotBlank String tipo,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantidade,
        @NotBlank String unidade,
        @NotNull OffsetDateTime timestamp,
        Double latitude,
        Double longitude,
        @Valid List<MovimentacaoRequestAttachment> anexos
) {
    public record MovimentacaoRequestAttachment(
            @NotBlank String tipo,
            @NotBlank @Pattern(regexp = "https?://.+") String url,
            @NotBlank String hash
    ) {
    }
}
