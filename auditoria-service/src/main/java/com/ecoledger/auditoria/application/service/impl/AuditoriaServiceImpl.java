package com.ecoledger.auditoria.application.service.impl;

import com.ecoledger.auditoria.application.dto.HistoricoAuditoriasResponse;
import com.ecoledger.auditoria.application.dto.RegistroAuditoriaResponse;
import com.ecoledger.auditoria.application.dto.RevisaoRequest;
import com.ecoledger.auditoria.application.exception.AuditoriaNotFoundException;
import com.ecoledger.auditoria.application.exception.RevisaoInvalidaException;
import com.ecoledger.auditoria.application.rules.RulesEngine;
import com.ecoledger.auditoria.application.service.AuditoriaEventPublisher;
import com.ecoledger.auditoria.application.service.AuditoriaService;
import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;
import com.ecoledger.auditoria.domain.repository.AuditoriaRepository;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of the AuditoriaService.
 */
@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaServiceImpl.class);

    private final AuditoriaRepository auditoriaRepository;
    private final RulesEngine rulesEngine;
    private final AuditoriaEventPublisher eventPublisher;

    public AuditoriaServiceImpl(AuditoriaRepository auditoriaRepository, 
                                RulesEngine rulesEngine,
                                AuditoriaEventPublisher eventPublisher) {
        this.auditoriaRepository = auditoriaRepository;
        this.rulesEngine = rulesEngine;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroAuditoriaResponse findById(UUID id) {
        log.debug("Finding auditoria by id: {}", id);
        return auditoriaRepository.findById(id)
                .map(RegistroAuditoriaResponse::from)
                .orElseThrow(() -> new AuditoriaNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public HistoricoAuditoriasResponse findHistoricoByProducerId(String producerId) {
        log.debug("Finding audit history for producer: {}", producerId);
        var auditorias = auditoriaRepository.findByProducerIdOrderByProcessadoEmDesc(producerId);
        return HistoricoAuditoriasResponse.from(auditorias);
    }

    @Override
    @Transactional
    public RegistroAuditoriaResponse aplicarRevisao(UUID id, RevisaoRequest request) {
        log.info("Applying revision to auditoria {} by auditor {}", id, request.auditorId());
        
        request.validate();
        
        RegistroAuditoria auditoria = auditoriaRepository.findById(id)
                .orElseThrow(() -> new AuditoriaNotFoundException(id));

        // Validate that revision can be applied
        if (auditoria.foiRevisado()) {
            throw new RevisaoInvalidaException("Auditoria já foi revisada anteriormente");
        }

        try {
            auditoria.aplicarRevisao(request.auditorId(), request.resultado(), request.observacoes());
            auditoria = auditoriaRepository.save(auditoria);
            
            log.info("Revision applied successfully to auditoria {}: new result = {}", 
                    id, auditoria.getResultado());
            
            // Publish event for revised audit
            eventPublisher.publishAuditoriaConcluida(auditoria);
            
            return RegistroAuditoriaResponse.from(auditoria);
        } catch (IllegalArgumentException e) {
            throw new RevisaoInvalidaException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void processarMovimentacaoCriada(MovimentacaoCriadaEvent event) {
        log.info("Processing movimentacao.criada event for movimentacao: {}", event.movimentacaoId());
        
        // Check for idempotency - if audit already exists for this movimentacao, skip
        if (auditoriaRepository.existsByMovimentacaoId(event.movimentacaoId())) {
            log.warn("Audit already exists for movimentacao {}, skipping", event.movimentacaoId());
            return;
        }

        // Execute validation rules
        RulesEngine.AggregatedValidationResult validationResult = rulesEngine.validate(event);
        
        // Create audit record
        RegistroAuditoria auditoria = new RegistroAuditoria(
                event.movimentacaoId(),
                event.producerId(),
                validationResult.versaoRegra(),
                validationResult.resultado(),
                validationResult.evidencias()
        );
        
        auditoria = auditoriaRepository.save(auditoria);
        
        log.info("Created audit record {} for movimentacao {} with result: {}",
                auditoria.getId(), event.movimentacaoId(), auditoria.getResultado());
        
        // Publish audit completed event
        eventPublisher.publishAuditoriaConcluida(auditoria);
    }
}
