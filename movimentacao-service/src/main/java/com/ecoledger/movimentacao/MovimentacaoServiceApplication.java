package com.ecoledger.movimentacao;

import com.ecoledger.movimentacao.config.AttachmentPolicyProperties;
import com.ecoledger.movimentacao.config.KafkaProperties;
import com.ecoledger.movimentacao.config.ProducerApprovalProperties;
import com.ecoledger.movimentacao.config.S3Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AttachmentPolicyProperties.class, S3Properties.class, KafkaProperties.class, ProducerApprovalProperties.class})
public class MovimentacaoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovimentacaoServiceApplication.class, args);
    }
}
