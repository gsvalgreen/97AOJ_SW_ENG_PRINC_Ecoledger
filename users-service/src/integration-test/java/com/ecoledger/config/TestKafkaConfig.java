package com.ecoledger.config;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

@TestConfiguration
public class TestKafkaConfig {

    // Provide a ProducerFactory<String,Object> and KafkaTemplate<String,Object> for tests that target the EmbeddedKafka broker
    @Bean
    @Primary
    public ProducerFactory<String, Object> testProducerFactory(EmbeddedKafkaBroker embeddedKafka) {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafka);
        // keep key as String, value serialized as JSON to match main configuration
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), new JsonSerializer<>());
    }

    @Bean(name = "testKafkaTemplate")
    @Primary
    public KafkaTemplate<String, Object> testKafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }

}
