package com.ecoledger.auditoria.messaging.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event representing a newly created movimentacao.
 * Consumed from movimentacao.criada topic.
 * Must match the format sent by movimentacao-service.
 */
public record MovimentacaoCriadaEvent(
    UUID movimentacaoId,
    String producerId,
    String commodityId,
    String tipo,
    BigDecimal quantidade,
    String unidade,
    OffsetDateTime timestamp,
    Double latitude,
    Double longitude,
    OffsetDateTime criadoEm,
    List<Anexo> anexos
) {
    public record Anexo(
        String tipo,
        String url,
        String hash
    ) {}
    
    /**
     * Returns the location as a formatted string.
     */
    public String getLocalizacao() {
        if (latitude != null && longitude != null) {
            return String.format("%.6f,%.6f", latitude, longitude);
        }
        return null;
    }
}
