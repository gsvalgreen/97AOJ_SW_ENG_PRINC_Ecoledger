package com.ecoledger.certificacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CertificacaoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertificacaoServiceApplication.class, args);
    }
}
