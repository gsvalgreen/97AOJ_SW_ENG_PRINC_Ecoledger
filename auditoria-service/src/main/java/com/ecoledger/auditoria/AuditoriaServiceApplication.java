package com.ecoledger.auditoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AuditoriaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditoriaServiceApplication.class, args);
    }
}
