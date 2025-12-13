package com.ecoledger.auditoria.application.exception;

import java.util.UUID;

/**
 * Exception thrown when an audit record is not found.
 */
public class AuditoriaNotFoundException extends RuntimeException {
    
    private final UUID auditoriaId;

    public AuditoriaNotFoundException(UUID auditoriaId) {
        super("Auditoria not found with id: " + auditoriaId);
        this.auditoriaId = auditoriaId;
    }

    public UUID getAuditoriaId() {
        return auditoriaId;
    }
}
