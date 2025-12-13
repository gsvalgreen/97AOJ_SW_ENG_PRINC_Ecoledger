package com.ecoledger.auditoria.application.service.impl;

import com.ecoledger.auditoria.application.service.AuditoriaEventPublisher;
import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op implementation of AuditoriaEventPublisher for local development/testing.
 */
public class NoOpAuditoriaEventPublisher implements AuditoriaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpAuditoriaEventPublisher.class);

    @Override
    public void publishAuditoriaConcluida(RegistroAuditoria auditoria) {
        log.info("NoOp: Would publish auditoria.concluida event for auditoria: {} with result: {}",
                auditoria.getId(), auditoria.getResultado());
    }
}
