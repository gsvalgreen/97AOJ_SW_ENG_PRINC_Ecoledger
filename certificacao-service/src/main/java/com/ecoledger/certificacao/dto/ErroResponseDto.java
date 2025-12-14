package com.ecoledger.certificacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Erro", description = "Modelo padrão de erro retornado pela API de certificação.")
public record ErroResponseDto(
        @Schema(description = "Código único que representa o tipo do erro.", example = "not_found")
        String codigo,
        @Schema(description = "Mensagem explicativa do erro.", example = "Selo do produtor não encontrado.")
        String mensagem
) {
}
