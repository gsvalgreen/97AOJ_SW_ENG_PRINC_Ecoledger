package com.ecoledger.movimentacao.config;

import com.ecoledger.movimentacao.application.service.AttachmentStorageService;
import com.ecoledger.movimentacao.application.service.MovimentacaoEventPublisher;
import com.ecoledger.movimentacao.application.service.impl.NoOpAttachmentStorageService;
import com.ecoledger.movimentacao.application.service.impl.NoOpMovimentacaoEventPublisher;
import com.ecoledger.movimentacao.application.service.impl.S3AttachmentStorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @ConditionalOnMissingBean(MovimentacaoEventPublisher.class)
    MovimentacaoEventPublisher movimentacaoEventPublisher() {
        return new NoOpMovimentacaoEventPublisher();
    }
}
