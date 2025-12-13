package com.ecoledger.auditoria.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafka configuration properties for the auditoria service.
 */
@ConfigurationProperties(prefix = "auditoria.kafka")
public record KafkaProperties(
    boolean enabled,
    String bootstrapServers,
    Topics topics,
    Consumer consumer
) {
    public record Topics(
        String movimentacaoCriada,
        String auditoriaConcluida
    ) {}

    public record Consumer(
        String groupId,
        int maxRetries,
        long retryBackoffMs
    ) {
        public Consumer {
            if (maxRetries < 0) maxRetries = 3;
            if (retryBackoffMs <= 0) retryBackoffMs = 1000L;
        }
    }
}
