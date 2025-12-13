package com.ecoledger.auditoria.application.controller;

import com.ecoledger.auditoria.application.dto.HistoricoAuditoriasResponse;
import com.ecoledger.auditoria.application.dto.RegistroAuditoriaResponse;
import com.ecoledger.auditoria.application.dto.RevisaoRequest;
import com.ecoledger.auditoria.application.service.AuditoriaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for audit operations.
 */
@RestController
@RequestMapping
public class AuditoriaController {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaController.class);

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /**
     * Get an audit record by ID.
     * GET /auditorias/{id}
     */
    @GetMapping("/auditorias/{id}")
    public ResponseEntity<RegistroAuditoriaResponse> getAuditoria(@PathVariable UUID id) {
        log.debug("GET /auditorias/{}", id);
        RegistroAuditoriaResponse response = auditoriaService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get audit history for a producer.
     * GET /produtores/{producerId}/historico-auditorias
     */
    @GetMapping("/produtores/{producerId}/historico-auditorias")
    public ResponseEntity<HistoricoAuditoriasResponse> getHistoricoAuditorias(
            @PathVariable String producerId) {
        log.debug("GET /produtores/{}/historico-auditorias", producerId);
        HistoricoAuditoriasResponse response = auditoriaService.findHistoricoByProducerId(producerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Apply manual revision to an audit record.
     * POST /auditorias/{id}/revisao
     */
    @PostMapping("/auditorias/{id}/revisao")
    public ResponseEntity<RegistroAuditoriaResponse> aplicarRevisao(
            @PathVariable UUID id,
            @Valid @RequestBody RevisaoRequest request) {
        log.debug("POST /auditorias/{}/revisao by auditor {}", id, request.auditorId());
        RegistroAuditoriaResponse response = auditoriaService.aplicarRevisao(id, request);
        return ResponseEntity.ok(response);
    }
}
