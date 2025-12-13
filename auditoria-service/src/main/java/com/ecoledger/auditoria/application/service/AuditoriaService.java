package com.ecoledger.auditoria.application.service;

import com.ecoledger.auditoria.application.dto.HistoricoAuditoriasResponse;
import com.ecoledger.auditoria.application.dto.RegistroAuditoriaResponse;
import com.ecoledger.auditoria.application.dto.RevisaoRequest;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;

import java.util.UUID;

/**
 * Service interface for audit operations.
 */
public interface AuditoriaService {

    /**
     * Retrieves an audit record by its ID.
     * 
     * @param id the audit record ID
     * @return the audit record response
     */
    RegistroAuditoriaResponse findById(UUID id);

    /**
     * Retrieves the audit history for a producer.
     * 
     * @param producerId the producer ID
     * @return the audit history response
     */
    HistoricoAuditoriasResponse findHistoricoByProducerId(String producerId);

    /**
     * Applies a manual revision to an existing audit record.
     * 
     * @param id the audit record ID
     * @param request the revision request
     * @return the updated audit record response
     */
    RegistroAuditoriaResponse aplicarRevisao(UUID id, RevisaoRequest request);

    /**
     * Processes a movimentacao created event and performs automatic validation.
     * 
     * @param event the movimentacao created event
     */
    void processarMovimentacaoCriada(MovimentacaoCriadaEvent event);
}
