package com.ecoledger.movimentacao.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "MovimentacaoLista", description = "Lista paginada de movimentações de um produtor")
public record MovimentacaoListResponse(
        @Schema(description = "Itens retornados na página atual")
        List<MovimentacaoDetailResponse> items,
        @Schema(description = "Quantidade total de movimentações") long total
) {}
