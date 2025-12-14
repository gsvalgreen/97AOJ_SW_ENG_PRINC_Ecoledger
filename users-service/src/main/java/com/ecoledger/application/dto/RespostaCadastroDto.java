package com.ecoledger.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RespostaCadastro", description = "Resumo do resultado da criação de um cadastro.")
public record RespostaCadastroDto(
        @Schema(description = "Identificador único do cadastro.", example = "cad_123456")
        String cadastroId,
        @Schema(description = "Status atual do cadastro.", example = "PENDENTE")
        String status
) {}
