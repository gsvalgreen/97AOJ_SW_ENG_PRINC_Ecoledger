package com.ecoledger.auditoria.application.controller;

import com.ecoledger.auditoria.application.dto.HistoricoAuditoriasResponse;
import com.ecoledger.auditoria.application.dto.RegistroAuditoriaResponse;
import com.ecoledger.auditoria.application.dto.RevisaoRequest;
import com.ecoledger.auditoria.application.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for audit operations.
 */
@RestController
@RequestMapping
@Tag(name = "Auditorias")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "Recuperar registro de auditoria",
            description = "Retorna um registro de auditoria existente pelo seu identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro de auditoria encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegistroAuditoriaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Auditoria não encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/auditorias/{id}")
    public ResponseEntity<RegistroAuditoriaResponse> getAuditoria(
            @Parameter(description = "Identificador único da auditoria.", required = true)
            @PathVariable UUID id) {
        log.debug("GET /auditorias/{}", id);
        RegistroAuditoriaResponse response = auditoriaService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get audit history for a producer.
     * GET /produtores/{producerId}/historico-auditorias
     */
    @Operation(
            summary = "Histórico de auditorias de um produtor",
            description = "Retorna a lista de auditorias realizadas para um produtor específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico recuperado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HistoricoAuditoriasResponse.class)))
    })
    @GetMapping("/produtores/{producerId}/historico-auditorias")
    public ResponseEntity<HistoricoAuditoriasResponse> getHistoricoAuditorias(
            @Parameter(description = "Identificador do produtor.", required = true)
            @PathVariable String producerId) {
        log.debug("GET /produtores/{}/historico-auditorias", producerId);
        HistoricoAuditoriasResponse response = auditoriaService.findHistoricoByProducerId(producerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Apply manual revision to an audit record.
     * POST /auditorias/{id}/revisao
     */
    @Operation(
            summary = "Registrar revisão manual por auditor",
            description = "Permite registrar o resultado de uma revisão manual.",
            security = {@SecurityRequirement(name = "bearerAuth", scopes = {"auditor:write"})}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Revisão registrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegistroAuditoriaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/auditorias/{id}/revisao")
    public ResponseEntity<RegistroAuditoriaResponse> aplicarRevisao(
            @Parameter(description = "Identificador da auditoria.", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody RevisaoRequest request) {
        log.debug("POST /auditorias/{}/revisao by auditor {}", id, request.auditorId());
        RegistroAuditoriaResponse response = auditoriaService.aplicarRevisao(id, request);
        return ResponseEntity.ok(response);
    }
}
