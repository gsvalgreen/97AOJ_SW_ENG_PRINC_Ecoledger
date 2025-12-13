package com.ecoledger.auditoria.messaging.event;

import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Event representing a completed audit.
 * Published to auditoria.concluida topic.
 */
public record AuditoriaConcluidaEvent(
    UUID auditoriaId,
    UUID movimentacaoId,
    String producerId,
    ResultadoAuditoria resultado,
    String versaoRegra,
    List<DetalheEvidencia> detalhes,
    Instant timestamp
) {
    public record DetalheEvidencia(
        String tipo,
        String detalhe
    ) {}

    public static AuditoriaConcluidaEvent from(RegistroAuditoria auditoria) {
        List<DetalheEvidencia> detalhes = auditoria.getEvidencias().stream()
                .map(e -> new DetalheEvidencia(e.getTipo(), e.getDetalhe()))
                .toList();
        
        return new AuditoriaConcluidaEvent(
                auditoria.getId(),
                auditoria.getMovimentacaoId(),
                auditoria.getProducerId(),
                auditoria.getResultado(),
                auditoria.getVersaoRegra(),
                detalhes,
                Instant.now()
        );
    }
}
