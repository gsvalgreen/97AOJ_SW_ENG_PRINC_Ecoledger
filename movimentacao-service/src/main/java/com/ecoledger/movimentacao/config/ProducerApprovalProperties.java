package com.ecoledger.movimentacao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "movimentacao.producer-approval")
public record ProducerApprovalProperties(String baseUrl, long timeoutMs, String jwtSecret, Duration tokenTtl, String clientId, String scopes) {

    public Duration tokenTtl() {
        return tokenTtl == null ? Duration.ofMinutes(10) : tokenTtl;
    }

    public String clientId() {
        return (clientId == null || clientId.isBlank()) ? "movimentacao-service" : clientId;
    }

    public String scopes() {
        return (scopes == null || scopes.isBlank()) ? "admin:usuarios usuarios:read" : scopes;
    }
}
