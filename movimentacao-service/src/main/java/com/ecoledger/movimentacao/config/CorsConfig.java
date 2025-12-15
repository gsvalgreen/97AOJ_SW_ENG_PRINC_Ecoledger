package com.ecoledger.movimentacao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir origens do frontend
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",      // Frontend em produção (Docker)
                "http://localhost:5173",      // Frontend dev (Vite)
                "http://localhost:8080"       // Outras origens
        ));

        // Permitir todos os métodos HTTP
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Permitir todos os headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Expor headers customizados
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Idempotency-Key",
                "Idempotency-Key"
        ));

        // Permitir credenciais (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache de configuração CORS por 1 hora
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CorsFilter corsFilter(CorsConfigurationSource corsConfigurationSource) {
        return new CorsFilter(corsConfigurationSource);
    }
}
