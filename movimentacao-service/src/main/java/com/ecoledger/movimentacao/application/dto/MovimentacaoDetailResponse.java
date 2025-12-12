package com.ecoledger.movimentacao.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MovimentacaoDetailResponse(
    UUID id,
    String producerId,
    String commodityId,
    String tipo,
    BigDecimal quantidade,
    String unidade,
    OffsetDateTime timestamp,
    Localizacao localizacao,
    OffsetDateTime criadoEm,
    List<MovimentacaoAttachmentResponse> anexos
) {
    public record MovimentacaoAttachmentResponse(String tipo, String url, String hash) {}
    public record Localizacao(Double lat, Double lon) {}

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
