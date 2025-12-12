package com.ecoledger.movimentacao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "movimentacao.kafka")
public record KafkaProperties(boolean enabled,
                              String bootstrapServers,
                              Topics topics) {

    public record Topics(String movimentacaoCriada,
                         String movimentacaoAtualizada) {
    }
}
