package com.ecoledger.certificacao.dto;

import com.ecoledger.certificacao.model.SeloNivel;
import com.ecoledger.certificacao.model.SeloStatus;
import com.ecoledger.certificacao.model.SeloVerde;

import java.time.Instant;
import java.util.List;

public record SeloResponse(
        String producerId,
        SeloStatus status,
        SeloNivel nivel,
        int pontuacao,
        String versaoRegra,
        Instant ultimoCheck,
        Instant expiracaoEm,
        List<String> motivos
) {
    public static SeloResponse from(SeloVerde selo) {
        return new SeloResponse(
                selo.getProducerId(),
                selo.getStatus(),
                selo.getNivel(),
                selo.getPontuacao(),
                selo.getVersaoRegra(),
                selo.getUltimoCheck(),
                selo.getExpiracaoEm(),
                selo.getMotivos()
        );
    }
}
