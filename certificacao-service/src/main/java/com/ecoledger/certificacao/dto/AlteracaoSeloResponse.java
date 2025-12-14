package com.ecoledger.certificacao.dto;

import com.ecoledger.certificacao.model.AlteracaoSelo;
import com.ecoledger.certificacao.model.SeloStatus;

import java.time.Instant;

public record AlteracaoSeloResponse(
        SeloStatus deStatus,
        SeloStatus paraStatus,
        String motivo,
        Instant criadoEm,
        String evidencia
) {
    public static AlteracaoSeloResponse from(AlteracaoSelo alteracao) {
        return new AlteracaoSeloResponse(
                alteracao.getDeStatus(),
                alteracao.getParaStatus(),
                alteracao.getMotivo(),
                alteracao.getCreatedAt(),
                alteracao.getEvidencia()
        );
    }
}
