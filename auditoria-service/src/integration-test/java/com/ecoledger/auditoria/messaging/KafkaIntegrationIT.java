package com.ecoledger.auditoria.messaging;

import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;
import com.ecoledger.auditoria.domain.repository.AuditoriaRepository;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
        "auditoria.kafka.enabled=true",
        "auditoria.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "auditoria.kafka.topics.movimentacao-criada=movimentacao.criada",
        "auditoria.kafka.topics.auditoria-concluida=auditoria.concluida"
})
@EmbeddedKafka(
        partitions = 1,
        topics = {"movimentacao.criada", "auditoria.concluida"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:0"}
)
@ActiveProfiles("test")
@DirtiesContext
class KafkaIntegrationIT {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    private Producer<String, Object> producer;
    private Consumer<String, Object> consumer;

    @BeforeEach
    void setUp() {
        auditoriaRepository.deleteAll();
        
        // Setup producer
        Map<String, Object> producerProps = new HashMap<>(
                KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producerProps.put("key.serializer", StringSerializer.class);
        producerProps.put("value.serializer", JsonSerializer.class);
        producer = new DefaultKafkaProducerFactory<String, Object>(producerProps).createProducer();

        // Setup consumer for output topic
        Map<String, Object> consumerProps = new HashMap<>(
                KafkaTestUtils.consumerProps("test-consumer-group", "true", embeddedKafkaBroker));
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        consumer = new DefaultKafkaConsumerFactory<String, Object>(consumerProps).createConsumer();
        consumer.subscribe(List.of("auditoria.concluida"));
    }

    @AfterEach
    void tearDown() {
        if (producer != null) producer.close();
        if (consumer != null) consumer.close();
    }

    @Test
    @DisplayName("should consume movimentacao.criada and produce auditoria.concluida")
    void shouldConsumeAndProduceEvents() throws Exception {
        // given
        UUID movimentacaoId = UUID.randomUUID();
        String producerId = "kafka-test-producer";
        
        MovimentacaoCriadaEvent event = new MovimentacaoCriadaEvent(
                movimentacaoId,
                producerId,
                "commodity-1",
                "ENTRADA",
                new BigDecimal("100"),
                "KG",
                "BR-SP",
                Instant.now(),
                List.of(),
                Instant.now()
        );

        // when
        producer.send(new ProducerRecord<>("movimentacao.criada", 
                movimentacaoId.toString(), event)).get();
        producer.flush();

        // then - verify audit was created in database
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<RegistroAuditoria> auditorias = auditoriaRepository
                    .findByMovimentacaoIdOrderByProcessadoEmDesc(movimentacaoId);
            assertThat(auditorias).hasSize(1);
            assertThat(auditorias.get(0).getResultado()).isEqualTo(ResultadoAuditoria.APROVADO);
        });

        // Verify output event was produced
        ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(10));
        assertThat(records.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("should handle idempotent messages")
    void shouldHandleIdempotentMessages() throws Exception {
        // given
        UUID movimentacaoId = UUID.randomUUID();
        
        MovimentacaoCriadaEvent event = new MovimentacaoCriadaEvent(
                movimentacaoId,
                "producer-1",
                "commodity-1",
                "ENTRADA",
                new BigDecimal("100"),
                "KG",
                "BR-SP",
                Instant.now(),
                List.of(),
                Instant.now()
        );

        // when - send same message twice
        producer.send(new ProducerRecord<>("movimentacao.criada", 
                movimentacaoId.toString(), event)).get();
        producer.flush();
        
        // Wait for first message to be processed
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(auditoriaRepository.existsByMovimentacaoId(movimentacaoId)).isTrue();
        });
        
        // Send duplicate
        producer.send(new ProducerRecord<>("movimentacao.criada", 
                movimentacaoId.toString(), event)).get();
        producer.flush();
        
        // Give time for potential duplicate processing
        Thread.sleep(2000);

        // then - should only have one audit record
        List<RegistroAuditoria> auditorias = auditoriaRepository
                .findByMovimentacaoIdOrderByProcessadoEmDesc(movimentacaoId);
        assertThat(auditorias).hasSize(1);
    }

    @Test
    @DisplayName("should create REPROVADO audit when validation fails")
    void shouldCreateReprovadoAuditOnValidationFailure() throws Exception {
        // given - quantity below minimum (configured as 1 in test profile)
        UUID movimentacaoId = UUID.randomUUID();
        
        MovimentacaoCriadaEvent event = new MovimentacaoCriadaEvent(
                movimentacaoId,
                "producer-1",
                "commodity-1",
                "ENTRADA",
                new BigDecimal("0"), // Below minimum
                "KG",
                "BR-SP",
                Instant.now(),
                List.of(),
                Instant.now()
        );

        // when
        producer.send(new ProducerRecord<>("movimentacao.criada", 
                movimentacaoId.toString(), event)).get();
        producer.flush();

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<RegistroAuditoria> auditorias = auditoriaRepository
                    .findByMovimentacaoIdOrderByProcessadoEmDesc(movimentacaoId);
            assertThat(auditorias).hasSize(1);
            assertThat(auditorias.get(0).getResultado()).isEqualTo(ResultadoAuditoria.REPROVADO);
            assertThat(auditorias.get(0).getEvidencias()).isNotEmpty();
        });
    }
}
