package com.ecoledger.certificacao.service;

import com.ecoledger.certificacao.messaging.event.AuditoriaConcluidaEvent;
import com.ecoledger.certificacao.model.AlteracaoSelo;
import com.ecoledger.certificacao.model.SeloVerde;

import java.util.List;

public interface SeloService {
    SeloVerde obterSelo(String producerId);

    SeloVerde processarAuditoriaConcluida(AuditoriaConcluidaEvent event);

    SeloVerde recalcularSelo(String producerId, String motivo);

    List<AlteracaoSelo> historico(String producerId);
}
