package com.ecoledger.movimentacao.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Histórico de movimentações por commodity")
public record HistoricoMovimentacaoResponse(
        @Schema(description = "Lista de movimentações ordenadas por data mais recente")
        List<MovimentacaoDetailResponse> movimentacoes
) {}

