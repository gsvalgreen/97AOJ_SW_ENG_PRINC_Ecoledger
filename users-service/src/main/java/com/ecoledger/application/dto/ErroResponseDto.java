package com.ecoledger.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Erro", description = "Modelo padrão de erro retornado pela API.")
public record ErroResponseDto(
        @Schema(description = "Código único que representa o erro.", example = "not_found")
        String codigo,
        @Schema(description = "Descrição detalhada do erro.", example = "Cadastro não encontrado.")
        String mensagem
) {
}
