package com.ecoledger.movimentacao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "movimentacao.producer-approval")
public record ProducerApprovalProperties(String baseUrl, long timeoutMs, String authToken) {
}
