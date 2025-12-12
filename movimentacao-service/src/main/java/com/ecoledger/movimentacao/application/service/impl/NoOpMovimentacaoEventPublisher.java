package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.service.MovimentacaoEventPublisher;
import com.ecoledger.movimentacao.domain.model.Movimentacao;

public class NoOpMovimentacaoEventPublisher implements MovimentacaoEventPublisher {
    @Override
    public void publishCreated(Movimentacao movimentacao) {
        // intentionally left blank until Kafka integration is implemented
    }
}

