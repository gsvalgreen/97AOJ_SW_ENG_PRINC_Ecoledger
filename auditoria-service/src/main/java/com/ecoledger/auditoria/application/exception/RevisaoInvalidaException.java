package com.ecoledger.auditoria.application.exception;

/**
 * Exception thrown when a revision operation is not allowed.
 */
public class RevisaoInvalidaException extends RuntimeException {
    
    public RevisaoInvalidaException(String message) {
        super(message);
    }
}
