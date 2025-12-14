package com.ecoledger.certificacao.messaging;

import com.ecoledger.certificacao.messaging.event.AuditoriaConcluidaEvent;
import com.ecoledger.certificacao.service.SeloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "certificacao.kafka.enabled", havingValue = "true")
public class AuditoriaConcluidaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaConcluidaConsumer.class);

    private final SeloService seloService;

    public AuditoriaConcluidaConsumer(SeloService seloService) {
        this.seloService = seloService;
    }

    @KafkaListener(
            topics = "${certificacao.kafka.topics.auditoria-concluida}",
            groupId = "${certificacao.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload AuditoriaConcluidaEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        log.info("Recebido auditoria.concluida key={} partition={} offset={} produtor={}",
                key, partition, offset, event.producerId());
        try {
            seloService.processarAuditoriaConcluida(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Erro ao processar auditoria.concluida para produtor {}", event.producerId(), e);
            throw e;
        }
    }
}
