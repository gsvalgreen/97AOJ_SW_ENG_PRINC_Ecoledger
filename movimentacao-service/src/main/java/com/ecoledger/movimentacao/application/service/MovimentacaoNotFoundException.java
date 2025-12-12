package com.ecoledger.movimentacao.application.service;

import java.util.UUID;

public class MovimentacaoNotFoundException extends RuntimeException {
    public MovimentacaoNotFoundException(UUID id) {
        super("Movimentacao not found: " + id);
    }
}
