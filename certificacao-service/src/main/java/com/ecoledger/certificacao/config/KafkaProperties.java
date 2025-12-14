package com.ecoledger.certificacao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "certificacao.kafka")
public record KafkaProperties(
        boolean enabled,
        String bootstrapServers,
        Topics topics,
        Consumer consumer
) {
    public record Topics(String auditoriaConcluida, String seloAtualizado) {}

    public record Consumer(String groupId, int maxRetries, long retryBackoffMs) {}
}
