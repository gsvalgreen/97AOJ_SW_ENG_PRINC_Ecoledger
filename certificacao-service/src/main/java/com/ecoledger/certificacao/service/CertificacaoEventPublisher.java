package com.ecoledger.certificacao.service;

import com.ecoledger.certificacao.messaging.event.SeloAtualizadoEvent;

public interface CertificacaoEventPublisher {
    void publishSeloAtualizado(SeloAtualizadoEvent event);
}
