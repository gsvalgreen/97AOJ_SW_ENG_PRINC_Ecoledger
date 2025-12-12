package com.ecoledger.movimentacao.messaging;

import com.ecoledger.movimentacao.application.service.MovimentacaoEventPublisher;
import com.ecoledger.movimentacao.config.KafkaProperties;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {KafkaMovimentacaoEventPublisherIT.TEST_TOPIC})
@TestPropertySource(properties = {
        "movimentacao.kafka.enabled=true",
        "movimentacao.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "movimentacao.kafka.topics.movimentacao-criada=" + KafkaMovimentacaoEventPublisherIT.TEST_TOPIC
})
class KafkaMovimentacaoEventPublisherIT {

    static final String TEST_TOPIC = "movimentacao.criada.test";

    @Autowired
    private MovimentacaoEventPublisher publisher;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void shouldPublishMessageToConfiguredTopic() {
        Movimentacao movimentacao = new Movimentacao(
                "prod-1",
                "cmd-1",
                "COLHEITA",
                new BigDecimal("50"),
                "KG",
                OffsetDateTime.now(),
                null,
                null,
                List.of()
        );
        movimentacao.setId(UUID.randomUUID());

        publisher.publishCreated(movimentacao);

        var factory = new DefaultKafkaConsumerFactory<String, Map<String, Object>>(
                Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
                        ConsumerConfig.GROUP_ID_CONFIG, "movimentacao-it",
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
                new StringDeserializer(),
                new JsonDeserializer<>(Map.class, false));

        var consumer = factory.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, TEST_TOPIC);
        var records = consumer.poll(Duration.ofSeconds(10));
        consumer.close();

        assertThat(records).hasSize(1);
        var record = records.iterator().next();
        assertThat(record.key()).isEqualTo(movimentacao.getId().toString());
        assertThat(record.value().get("commodityId")).isEqualTo("cmd-1");
    }
}
