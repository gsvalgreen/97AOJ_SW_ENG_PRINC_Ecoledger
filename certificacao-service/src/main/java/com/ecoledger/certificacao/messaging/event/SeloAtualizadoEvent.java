package com.ecoledger.certificacao.messaging.event;

import com.ecoledger.certificacao.model.SeloNivel;
import com.ecoledger.certificacao.model.SeloStatus;

import java.time.Instant;

public record SeloAtualizadoEvent(
        String producerId,
        SeloStatus statusAnterior,
        SeloStatus statusAtual,
        SeloNivel nivelAtual,
        int pontuacao,
        String versaoRegra,
        Instant timestamp
) {}
