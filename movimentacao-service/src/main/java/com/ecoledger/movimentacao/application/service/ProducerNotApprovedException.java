package com.ecoledger.movimentacao.application.service;

public class ProducerNotApprovedException extends RuntimeException {
    public ProducerNotApprovedException(String producerId) {
        super("Producer " + producerId + " is not approved");
    }
}

