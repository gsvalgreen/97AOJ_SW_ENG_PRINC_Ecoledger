package com.ecoledger.movimentacao.application.dto;

import java.util.List;

public record MovimentacaoListResponse(List<MovimentacaoDetailResponse> items, long total) {}
