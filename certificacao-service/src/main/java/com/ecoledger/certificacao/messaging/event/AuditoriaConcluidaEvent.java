package com.ecoledger.certificacao.messaging.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuditoriaConcluidaEvent(
        UUID auditoriaId,
        UUID movimentacaoId,
        String producerId,
        ResultadoAuditoria resultado,
        String versaoRegra,
        List<DetalheEvidencia> detalhes,
        Instant timestamp
) {
    public record DetalheEvidencia(String tipo, String detalhe) {}
}
