package com.ecoledger.movimentacao.config;

import com.ecoledger.movimentacao.application.service.AttachmentStorageService;
import com.ecoledger.movimentacao.application.service.MovimentacaoEventPublisher;
import com.ecoledger.movimentacao.application.service.impl.NoOpAttachmentStorageService;
import com.ecoledger.movimentacao.application.service.impl.NoOpMovimentacaoEventPublisher;
import com.ecoledger.movimentacao.application.service.impl.S3AttachmentStorageService;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class IntegrationInfrastructureConfiguration {

    @Bean
    @ConditionalOnProperty(name = "movimentacao.attachments.provider", havingValue = "s3", matchIfMissing = true)
    AttachmentStorageService s3AttachmentStorageService(S3Properties s3Properties) {
        return new S3AttachmentStorageService(s3Properties);
    }

    @Bean
    @ConditionalOnMissingBean(AttachmentStorageService.class)
    AttachmentStorageService noOpAttachmentStorageService() {
        return new NoOpAttachmentStorageService();
    }

    @Bean
    @ConditionalOnProperty(name = "movimentacao.kafka.enabled", havingValue = "true", matchIfMissing = true)
    ProducerFactory<String, Object> movimentacaoProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @ConditionalOnProperty(name = "movimentacao.kafka.enabled", havingValue = "true", matchIfMissing = true)
    KafkaTemplate<String, Object> movimentacaoKafkaTemplate(ProducerFactory<String, Object> movimentacaoProducerFactory) {
        return new KafkaTemplate<>(movimentacaoProducerFactory);
    }

    @Bean
    @ConditionalOnMissingBean(MovimentacaoEventPublisher.class)
    MovimentacaoEventPublisher movimentacaoEventPublisherFallback() {
        return new NoOpMovimentacaoEventPublisher();
    }
}
