package com.ecoledger.movimentacao.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI movimentacaoOpenAPI() {
        var bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Autenticação via JWT com escopos de produtor.");

        return new OpenAPI()
                .info(new Info()
                        .title("EcoLedger - Serviço de Movimentação API")
                        .version("1.0.0")
                        .description("Serviço para registrar e consultar movimentações de commodities."))
                .addServersItem(new Server().url("https://api.ecoledger.local/movimentacoes"))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}

