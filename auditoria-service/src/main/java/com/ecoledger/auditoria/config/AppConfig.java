package com.ecoledger.auditoria.config;

import com.ecoledger.auditoria.application.service.AuditoriaEventPublisher;
import com.ecoledger.auditoria.application.service.impl.NoOpAuditoriaEventPublisher;
import com.ecoledger.auditoria.messaging.KafkaAuditoriaEventPublisher;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Application configuration for beans and conditional components.
 */
@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    @ConditionalOnProperty(name = "auditoria.kafka.enabled", havingValue = "false", matchIfMissing = true)
    public AuditoriaEventPublisher noOpAuditoriaEventPublisher() {
        log.info("Kafka disabled - using NoOp event publisher");
        return new NoOpAuditoriaEventPublisher();
    }

    @Bean
    @ConditionalOnProperty(name = "auditoria.kafka.enabled", havingValue = "true")
    public AuditoriaEventPublisher kafkaAuditoriaEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaProperties kafkaProperties) {
        log.info("Kafka enabled - using Kafka event publisher");
        return new KafkaAuditoriaEventPublisher(kafkaTemplate, kafkaProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "auditoria.kafka.enabled", havingValue = "true")
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @ConditionalOnProperty(name = "auditoria.kafka.enabled", havingValue = "true")
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnProperty(name = "auditoria.kafka.enabled", havingValue = "true")
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.consumer().groupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, MovimentacaoCriadaEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    @ConditionalOnProperty(name = "auditoria.kafka.enabled", havingValue = "true")
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            KafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Configure error handler with retry and dead-letter
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(kafkaProperties.consumer().retryBackoffMs(), 
                                 kafkaProperties.consumer().maxRetries())
        );
        factory.setCommonErrorHandler(errorHandler);
        
        return factory;
    }
}
