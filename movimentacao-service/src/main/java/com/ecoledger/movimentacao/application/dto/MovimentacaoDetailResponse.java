package com.ecoledger.movimentacao.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(name = "Movimentacao", description = "Representação completa de uma movimentação")
public record MovimentacaoDetailResponse(
        @Schema(description = "Identificador da movimentação", example = "9b9dd2d3-7e24-4baf-9ab0-5f53319f0c10") UUID id,
        @Schema(description = "Identificador do produtor", example = "producer-123") String producerId,
        @Schema(description = "Identificador da commodity", example = "commodity-1") String commodityId,
        @Schema(description = "Tipo da movimentação", example = "PRODUCAO") String tipo,
        @Schema(description = "Quantidade movimentada", example = "10.5") BigDecimal quantidade,
        @Schema(description = "Unidade da quantidade", example = "KG") String unidade,
        @Schema(description = "Data/hora da movimentação", example = "2024-01-01T12:00:00Z") OffsetDateTime timestamp,
        @Schema(description = "Localização onde ocorreu a movimentação") Localizacao localizacao,
        @Schema(description = "Data de criação do registro", example = "2024-01-01T12:05:00Z") OffsetDateTime criadoEm,
        @Schema(description = "Anexos associados") List<MovimentacaoAttachmentResponse> anexos
) {
    @Schema(description = "Dados de um anexo da movimentação")
    public record MovimentacaoAttachmentResponse(
            @Schema(description = "Tipo do anexo", example = "image/png") String tipo,
            @Schema(description = "URL do anexo", example = "https://files.ecoledger.com/anexo.png") String url,
            @Schema(description = "Hash do anexo", example = "abc123") String hash
    ) {}

    @Schema(description = "Localização (latitude/longitude)")
    public record Localizacao(
            @Schema(description = "Latitude", example = "-23.55052") Double lat,
            @Schema(description = "Longitude", example = "-46.633308") Double lon
    ) {}

    public static MovimentacaoDetailResponse fromEntity(com.ecoledger.movimentacao.domain.model.Movimentacao m) {
        List<MovimentacaoAttachmentResponse> anexos = m.getAnexos().stream()
            .map(a -> new MovimentacaoAttachmentResponse(a.getTipo(), a.getUrl(), a.getHash()))
            .toList();
        Localizacao loc = null;
        if (m.getLatitude() != null || m.getLongitude() != null) {
            loc = new Localizacao(m.getLatitude(), m.getLongitude());
        }
        return new MovimentacaoDetailResponse(
            m.getId(),
            m.getProducerId(),
            m.getCommodityId(),
            m.getTipo(),
            m.getQuantidade(),
            m.getUnidade(),
            m.getTimestamp(),
            loc,
            m.getCriadoEm(),
            anexos
        );
    }
}
