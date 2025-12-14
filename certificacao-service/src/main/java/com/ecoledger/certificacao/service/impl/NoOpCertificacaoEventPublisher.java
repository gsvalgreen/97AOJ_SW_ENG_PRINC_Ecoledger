package com.ecoledger.certificacao.service.impl;

import com.ecoledger.certificacao.messaging.event.SeloAtualizadoEvent;
import com.ecoledger.certificacao.service.CertificacaoEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpCertificacaoEventPublisher implements CertificacaoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpCertificacaoEventPublisher.class);

    @Override
    public void publishSeloAtualizado(SeloAtualizadoEvent event) {
        log.debug("Kafka disabled, skipping selo.atualizado publication for producer {}", event.producerId());
    }
}
