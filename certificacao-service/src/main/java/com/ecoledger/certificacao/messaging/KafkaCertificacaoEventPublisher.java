package com.ecoledger.certificacao.messaging;

import com.ecoledger.certificacao.config.KafkaProperties;
import com.ecoledger.certificacao.messaging.event.SeloAtualizadoEvent;
import com.ecoledger.certificacao.service.CertificacaoEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaCertificacaoEventPublisher implements CertificacaoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaCertificacaoEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public KafkaCertificacaoEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                           KafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    @Override
    public void publishSeloAtualizado(SeloAtualizadoEvent event) {
        if (!kafkaProperties.enabled()) {
            log.debug("Kafka disabled, skipping selo.atualizado for producer {}", event.producerId());
            return;
        }

        String topic = kafkaProperties.topics().seloAtualizado();
        String key = event.producerId();

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Erro ao publicar selo.atualizado para produtor {}", event.producerId(), ex);
                    } else {
                        log.debug("selo.atualizado publicado para produtor {} particao {} offset {}",
                                event.producerId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
