package com.ecoledger.certificacao.messaging;

import com.ecoledger.certificacao.messaging.event.SeloAtualizadoEvent;
import com.ecoledger.certificacao.model.SeloNivel;
import com.ecoledger.certificacao.model.SeloStatus;
import com.ecoledger.certificacao.service.CertificacaoEventPublisher;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {"certificacao.kafka.enabled=true","certificacao.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@EmbeddedKafka(partitions = 1, topics = {"certificacao.events"})
@ActiveProfiles("test")
class KafkaCertificacaoEventPublisherIT {

    @Autowired
    private CertificacaoEventPublisher eventPublisher;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @AfterEach
    void cleanup() {
        if (consumer != null) consumer.close();
    }

    @Test
    void shouldPublishSeloAtualizadoEvent() {
        var event = new SeloAtualizadoEvent(
                "producer-x",
                SeloStatus.INATIVO,
                SeloStatus.ATIVO,
                SeloNivel.OURO,
                95,
                "v1",
                Instant.now()
        );

        eventPublisher.publishSeloAtualizado(event);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProps);
        consumer.subscribe(java.util.List.of("certificacao.events"));

        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        assertTrue(records.count() >= 1, "Expected at least one record");
        var record = records.iterator().next();
        assertEquals(event.producerId(), record.key());
        assertTrue(record.value().contains("\"producerId\":\"producer-x\""));
    }
}
