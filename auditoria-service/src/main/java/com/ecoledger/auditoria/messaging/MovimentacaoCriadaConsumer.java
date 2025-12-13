package com.ecoledger.auditoria.messaging;

import com.ecoledger.auditoria.application.service.AuditoriaService;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for movimentacao.criada events.
 */
@Component
@ConditionalOnProperty(name = "auditoria.kafka.enabled", havingValue = "true")
public class MovimentacaoCriadaConsumer {

    private static final Logger log = LoggerFactory.getLogger(MovimentacaoCriadaConsumer.class);

    private final AuditoriaService auditoriaService;

    public MovimentacaoCriadaConsumer(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @KafkaListener(
            topics = "${auditoria.kafka.topics.movimentacao-criada}",
            groupId = "${auditoria.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload MovimentacaoCriadaEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received movimentacao.criada event: key={}, partition={}, offset={}, movimentacaoId={}",
                key, partition, offset, event.movimentacaoId());
        
        try {
            auditoriaService.processarMovimentacaoCriada(event);
            acknowledgment.acknowledge();
            log.debug("Successfully processed and acknowledged movimentacao.criada event for movimentacao: {}",
                    event.movimentacaoId());
        } catch (Exception e) {
            log.error("Error processing movimentacao.criada event for movimentacao: {}",
                    event.movimentacaoId(), e);
            // Do not acknowledge - the error handler will handle retries
            throw e;
        }
    }
}
