package com.ecoledger.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "Usuario", description = "Perfil básico de um usuário na plataforma.")
public record UsuarioDto(
        @Schema(description = "Identificador único do usuário.", example = "usr_123456")
        String id,
        @Schema(description = "Nome completo do usuário.", example = "João da Silva")
        String nome,
        @Schema(description = "Email do usuário.", format = "email", example = "joao@ecoledger.com")
        String email,
        @Schema(description = "Perfil do usuário na plataforma.", allowableValues = {"produtor", "analista", "auditor"}, example = "produtor")
        String role,
        @Schema(description = "Documento associado ao usuário.", example = "12345678900")
        String documento,
        @Schema(description = "Status atual do usuário.", allowableValues = {"PENDENTE", "APROVADO", "REJEITADO"}, example = "APROVADO")
        String status,
        @Schema(description = "Instante de criação do usuário.", type = "string", format = "date-time")
        Instant criadoEm
) {}
