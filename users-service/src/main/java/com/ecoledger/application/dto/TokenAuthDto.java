package com.ecoledger.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TokenAuth", description = "Tokens retornados após autenticação bem sucedida.")
public record TokenAuthDto(
        @Schema(description = "Token de acesso (JWT).", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "Token utilizado para renovação do acesso.", example = "def50200d3...")
        String refreshToken,
        @Schema(description = "Tempo de expiração do token em segundos.", example = "3600")
        Long expiresIn
) {}
