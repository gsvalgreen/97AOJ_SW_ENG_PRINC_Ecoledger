package com.ecoledger.movimentacao.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "RespostaMovimentacao", description = "Resposta retornada após a criação da movimentação")
public record MovimentacaoResponse(
        @Schema(description = "Identificador da movimentação criada", example = "9b9dd2d3-7e24-4baf-9ab0-5f53319f0c10")
        UUID movimentacaoId
) {}
