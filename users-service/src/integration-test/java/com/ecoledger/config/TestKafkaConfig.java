package com.ecoledger.config;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

@TestConfiguration
public class TestKafkaConfig {

    // Provide a KafkaTemplate<String,String> that targets the EmbeddedKafka broker used in tests
    @Bean
    @Primary
    public ProducerFactory<String, String> stringProducerFactory(EmbeddedKafkaBroker embeddedKafka) {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafka);
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), new StringSerializer());
    }

    @Bean(name = "stringKafkaTemplate")
    @Primary
    public KafkaTemplate<String, String> stringKafkaTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }

}
