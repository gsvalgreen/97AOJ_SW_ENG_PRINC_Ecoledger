package com.ecoledger.certificacao.dto;

import com.ecoledger.certificacao.model.AlteracaoSelo;

import java.util.List;

public record HistoricoSeloResponse(List<AlteracaoSeloResponse> alteracoes) {
    public static HistoricoSeloResponse from(List<AlteracaoSelo> alteracoes) {
        return new HistoricoSeloResponse(alteracoes.stream().map(AlteracaoSeloResponse::from).toList());
    }
}
