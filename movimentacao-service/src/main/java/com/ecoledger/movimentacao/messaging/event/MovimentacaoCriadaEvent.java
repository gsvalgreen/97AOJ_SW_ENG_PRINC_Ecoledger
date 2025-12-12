package com.ecoledger.movimentacao.messaging.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MovimentacaoCriadaEvent(UUID movimentacaoId,
                                      String producerId,
                                      String commodityId,
                                      String tipo,
                                      BigDecimal quantidade,
                                      String unidade,
                                      OffsetDateTime timestamp,
                                      Double latitude,
                                      Double longitude,
                                      OffsetDateTime criadoEm,
                                      List<MovimentacaoCriadaEventAnexo> anexos) {

    public record MovimentacaoCriadaEventAnexo(String tipo,
                                               String url,
                                               String hash) {
    }
}

