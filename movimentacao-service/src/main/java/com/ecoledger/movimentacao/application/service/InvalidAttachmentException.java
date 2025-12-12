package com.ecoledger.movimentacao.application.service;

public class InvalidAttachmentException extends RuntimeException {
    public InvalidAttachmentException(String message) {
        super(message);
    }
}

