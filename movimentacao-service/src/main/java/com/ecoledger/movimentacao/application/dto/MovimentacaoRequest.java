package com.ecoledger.movimentacao.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(name = "MovimentacaoCriacao", description = "Payload para criação de uma nova movimentação")
public record MovimentacaoRequest(
        @Schema(description = "Identificador do produtor", example = "producer-123") @NotBlank String producerId,
        @Schema(description = "Identificador da commodity", example = "commodity-1") @NotBlank String commodityId,
        @Schema(description = "Tipo de movimentação (ex: PRODUCAO, PROCESSAMENTO)", example = "PRODUCAO") @NotBlank String tipo,
        @Schema(description = "Quantidade movimentada", example = "10.5") @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantidade,
        @Schema(description = "Unidade de medida", example = "KG") @NotBlank String unidade,
        @Schema(description = "Timestamp ISO-8601 da movimentação", example = "2024-01-01T12:00:00Z") @NotNull OffsetDateTime timestamp,
        @Schema(description = "Localização geográfica associada") @Valid Localizacao localizacao,
        @Schema(description = "Anexos enviados junto com a movimentação") @Valid List<MovimentacaoRequestAttachment> anexos
) {
    @Schema(description = "Dados de um anexo enviado durante a criação da movimentação")
    public record MovimentacaoRequestAttachment(
            @Schema(description = "Tipo do anexo", example = "image/png") @NotBlank String tipo,
            @Schema(description = "URL pública do anexo", example = "https://files.ecoledger.com/anexos/a.png") @NotBlank @Pattern(regexp = "https?://.+") String url,
            @Schema(description = "Hash do conteúdo para verificação", example = "abc123") @NotBlank String hash
    ) {
    }

    @Schema(description = "Coordenadas geográficas da movimentação")
    public record Localizacao(
            @Schema(description = "Latitude", example = "-23.55052") Double lat,
            @Schema(description = "Longitude", example = "-46.633308") Double lon
    ) {}
}
