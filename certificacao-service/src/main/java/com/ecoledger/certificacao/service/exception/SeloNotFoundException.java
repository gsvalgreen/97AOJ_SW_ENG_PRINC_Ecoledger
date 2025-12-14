package com.ecoledger.certificacao.service.exception;

public class SeloNotFoundException extends RuntimeException {
    public SeloNotFoundException(String producerId) {
        super("Selo nao encontrado para produtor: " + producerId);
    }
}
