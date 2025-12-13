package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.application.dto.MovimentacaoResponse;

public interface IdempotencyService {
    /**
     * Try to handle idempotent create.
     * Returns a stored response if available, otherwise executes the supplier and stores the result.
     */
    java.util.Optional<MovimentacaoResponse> handle(String idempotencyKey, String requestHash, java.util.concurrent.Callable<java.util.UUID> createOperation) throws Exception;
}
