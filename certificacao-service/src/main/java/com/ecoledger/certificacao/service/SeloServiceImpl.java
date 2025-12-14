package com.ecoledger.certificacao.service;

import com.ecoledger.certificacao.messaging.event.AuditoriaConcluidaEvent;
import com.ecoledger.certificacao.messaging.event.ResultadoAuditoria;
import com.ecoledger.certificacao.messaging.event.SeloAtualizadoEvent;
import com.ecoledger.certificacao.model.AlteracaoSelo;
import com.ecoledger.certificacao.model.SeloNivel;
import com.ecoledger.certificacao.model.SeloStatus;
import com.ecoledger.certificacao.model.SeloVerde;
import com.ecoledger.certificacao.repository.AlteracaoSeloRepository;
import com.ecoledger.certificacao.repository.SeloRepository;
import com.ecoledger.certificacao.service.config.SeloProperties;
import com.ecoledger.certificacao.service.exception.SeloNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class SeloServiceImpl implements SeloService {

    private static final Logger log = LoggerFactory.getLogger(SeloServiceImpl.class);

    private final SeloRepository seloRepository;
    private final AlteracaoSeloRepository alteracaoSeloRepository;
    private final CertificacaoEventPublisher eventPublisher;
    private final SeloProperties properties;

    public SeloServiceImpl(SeloRepository seloRepository,
                           AlteracaoSeloRepository alteracaoSeloRepository,
                           CertificacaoEventPublisher eventPublisher,
                           SeloProperties properties) {
        this.seloRepository = Objects.requireNonNull(seloRepository);
        this.alteracaoSeloRepository = Objects.requireNonNull(alteracaoSeloRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    @Transactional(readOnly = true)
    public SeloVerde obterSelo(String producerId) {
        return seloRepository.findById(producerId)
                .orElseThrow(() -> new SeloNotFoundException(producerId));
    }

    @Override
    @Transactional
    public SeloVerde processarAuditoriaConcluida(AuditoriaConcluidaEvent event) {
        log.info("Processando auditoria concluida para produtor {}", event.producerId());
        var motivos = mapMotivos(event);
        var decisao = decidir(event.resultado());
        var expiracao = Instant.now().plus(properties.getExpirationDays(), ChronoUnit.DAYS);

        var seloExistente = seloRepository.findById(event.producerId()).orElse(null);
        var statusAnterior = seloExistente != null ? seloExistente.getStatus() : null;

        SeloVerde salvo;
        if (seloExistente == null) {
            salvo = new SeloVerde(
                    event.producerId(),
                    decisao.status(),
                    decisao.nivel(),
                    decisao.pontuacao(),
                    motivos,
                    event.versaoRegra(),
                    event.auditoriaId(),
                    event.resultado(),
                    Instant.now(),
                    expiracao
            );
            salvo = seloRepository.save(salvo);
        } else {
            seloExistente.atualizar(decisao.status(), decisao.nivel(), decisao.pontuacao(), motivos,
                    event.versaoRegra(), event.auditoriaId(), event.resultado(), expiracao);
            salvo = seloRepository.save(seloExistente);
        }

        if (statusChanged(statusAnterior, salvo.getStatus())) {
            registrarAlteracao(salvo.getProducerId(), statusAnterior, salvo.getStatus(), "Resultado auditoria " + event.resultado(), null);
            publicarEvento(statusAnterior, salvo);
        }

        return salvo;
    }

    @Override
    @Transactional
    public SeloVerde recalcularSelo(String producerId, String motivo) {
        var selo = seloRepository.findById(producerId)
                .orElseThrow(() -> new SeloNotFoundException(producerId));

        var statusAnterior = selo.getStatus();
        var agora = Instant.now();

        List<String> motivos = motivo != null ? List.of(motivo) : List.<String>of();

        if (selo.expirado(agora)) {
            selo.atualizar(SeloStatus.PENDENTE, null, 0, motivos, selo.getVersaoRegra(),
                    selo.getUltimaAuditoriaId(), selo.getUltimoResultadoAuditoria(),
                    agora.plus(properties.getExpirationDays(), ChronoUnit.DAYS));
        } else if (selo.getUltimoResultadoAuditoria() != null) {
            var decisao = decidir(selo.getUltimoResultadoAuditoria());
            selo.atualizar(decisao.status(), decisao.nivel(), decisao.pontuacao(), selo.getMotivos(),
                    selo.getVersaoRegra(), selo.getUltimaAuditoriaId(), selo.getUltimoResultadoAuditoria(),
                    agora.plus(properties.getExpirationDays(), ChronoUnit.DAYS));
        }

        var salvo = seloRepository.save(selo);
        if (statusChanged(statusAnterior, salvo.getStatus())) {
            registrarAlteracao(producerId, statusAnterior, salvo.getStatus(), motivo, null);
            publicarEvento(statusAnterior, salvo);
        }

        return salvo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlteracaoSelo> historico(String producerId) {
        return alteracaoSeloRepository.findByProducerIdOrderByCreatedAtDesc(producerId);
    }

    private boolean statusChanged(SeloStatus anterior, SeloStatus atual) {
        return anterior == null || !anterior.equals(atual);
    }

    private void registrarAlteracao(String producerId, SeloStatus de, SeloStatus para, String motivo, String evidencia) {
        var alteracao = AlteracaoSelo.fromTransition(producerId, de, para, motivo, evidencia);
        alteracaoSeloRepository.save(alteracao);
    }

    private void publicarEvento(SeloStatus statusAnterior, SeloVerde selo) {
        var evento = new SeloAtualizadoEvent(
                selo.getProducerId(),
                statusAnterior,
                selo.getStatus(),
                selo.getNivel(),
                selo.getPontuacao(),
                selo.getVersaoRegra(),
                Instant.now()
        );
        eventPublisher.publishSeloAtualizado(evento);
    }

    private List<String> mapMotivos(AuditoriaConcluidaEvent event) {
        if (event.detalhes() == null || event.detalhes().isEmpty()) {
            return new ArrayList<>();
        }
        return event.detalhes().stream()
                .map(det -> det.tipo() + ": " + det.detalhe())
                .toList();
    }

    private SeloDecision decidir(ResultadoAuditoria resultado) {
        int pontuacaoBase = switch (resultado) {
            case APROVADO -> properties.getOuroThreshold() + 5;
            case REQUER_REVISAO -> properties.getBronzeThreshold() - 10;
            case REPROVADO -> 0;
        };

        if (pontuacaoBase >= properties.getOuroThreshold()) {
            return new SeloDecision(SeloStatus.ATIVO, SeloNivel.OURO, pontuacaoBase);
        }
        if (pontuacaoBase >= properties.getPrataThreshold()) {
            return new SeloDecision(SeloStatus.ATIVO, SeloNivel.PRATA, pontuacaoBase);
        }
        if (pontuacaoBase >= properties.getBronzeThreshold()) {
            return new SeloDecision(SeloStatus.ATIVO, SeloNivel.BRONZE, pontuacaoBase);
        }
        if (resultado == ResultadoAuditoria.REQUER_REVISAO) {
            return new SeloDecision(SeloStatus.PENDENTE, null, pontuacaoBase);
        }
        return new SeloDecision(SeloStatus.INATIVO, null, pontuacaoBase);
    }

    private record SeloDecision(SeloStatus status, SeloNivel nivel, int pontuacao) { }
}
