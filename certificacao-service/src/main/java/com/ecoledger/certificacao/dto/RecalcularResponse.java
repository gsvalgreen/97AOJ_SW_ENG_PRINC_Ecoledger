package com.ecoledger.certificacao.dto;

import com.ecoledger.certificacao.model.SeloNivel;
import com.ecoledger.certificacao.model.SeloStatus;
import com.ecoledger.certificacao.model.SeloVerde;

public record RecalcularResponse(
        SeloStatus status,
        SeloNivel nivel,
        int pontuacao
) {
    public static RecalcularResponse from(SeloVerde selo) {
        return new RecalcularResponse(selo.getStatus(), selo.getNivel(), selo.getPontuacao());
    }
}
