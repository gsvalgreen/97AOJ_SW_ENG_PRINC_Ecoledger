package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.domain.model.Movimentacao;

public interface MovimentacaoEventPublisher {
    void publishCreated(Movimentacao movimentacao);
}

