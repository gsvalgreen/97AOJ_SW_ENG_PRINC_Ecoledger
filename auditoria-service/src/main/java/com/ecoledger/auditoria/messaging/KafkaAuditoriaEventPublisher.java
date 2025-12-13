package com.ecoledger.auditoria.messaging;

import com.ecoledger.auditoria.application.service.AuditoriaEventPublisher;
import com.ecoledger.auditoria.config.KafkaProperties;
import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import com.ecoledger.auditoria.messaging.event.AuditoriaConcluidaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Kafka implementation of AuditoriaEventPublisher.
 */
public class KafkaAuditoriaEventPublisher implements AuditoriaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaAuditoriaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public KafkaAuditoriaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                        KafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    @Override
    public void publishAuditoriaConcluida(RegistroAuditoria auditoria) {
        if (!kafkaProperties.enabled()) {
            log.debug("Kafka is disabled, skipping event publication for auditoria: {}", auditoria.getId());
            return;
        }

        String topic = kafkaProperties.topics().auditoriaConcluida();
        AuditoriaConcluidaEvent event = AuditoriaConcluidaEvent.from(auditoria);
        String key = auditoria.getId().toString();

        log.info("Publishing auditoria.concluida event for auditoria: {} to topic: {}", 
                auditoria.getId(), topic);
        
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish auditoria.concluida event for auditoria: {}", 
                                auditoria.getId(), ex);
                    } else {
                        log.debug("Successfully published auditoria.concluida event for auditoria: {} " +
                                "to partition: {} at offset: {}", 
                                auditoria.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
