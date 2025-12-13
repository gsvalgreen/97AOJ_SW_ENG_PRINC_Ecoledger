package com.ecoledger.auditoria.application.service;

import com.ecoledger.auditoria.domain.model.RegistroAuditoria;

/**
 * Interface for publishing audit events to external systems.
 */
public interface AuditoriaEventPublisher {

    /**
     * Publishes an event when an audit is completed.
     * 
     * @param auditoria the completed audit record
     */
    void publishAuditoriaConcluida(RegistroAuditoria auditoria);
}
