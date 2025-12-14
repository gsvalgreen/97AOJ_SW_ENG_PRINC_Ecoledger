package com.ecoledger.certificacao.controller;

import com.ecoledger.certificacao.dto.*;
import com.ecoledger.certificacao.model.SeloVerde;
import com.ecoledger.certificacao.service.SeloService;
import com.ecoledger.certificacao.service.exception.SeloNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/selos")
@Tag(name = "Selos")
@SecurityRequirement(name = "bearerAuth")
public class SeloController {

    private final SeloService seloService;

    public SeloController(SeloService seloService) {
        this.seloService = seloService;
    }

    @Operation(
            summary = "Obter selo do produtor",
            description = "Recupera o estado atual do selo verde para o produtor informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Selo encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeloResponse.class))),
            @ApiResponse(responseCode = "404", description = "Selo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDto.class)))
    })
    @GetMapping("/{producerId}")
    public ResponseEntity<SeloResponse> obterSelo(
            @Parameter(description = "Identificador do produtor.", required = true)
            @PathVariable String producerId) {
        SeloVerde selo = seloService.obterSelo(producerId);
        return ResponseEntity.ok(SeloResponse.from(selo));
    }

    @Operation(
            summary = "Forçar recálculo do selo",
            description = "Inicia um novo cálculo do selo verde para o produtor informado.",
            security = {@SecurityRequirement(name = "bearerAuth", scopes = {"admin:selos"})}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recálculo iniciado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecalcularResponse.class))),
            @ApiResponse(responseCode = "404", description = "Selo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDto.class)))
    })
    @PostMapping("/{producerId}/recalcular")
    public ResponseEntity<RecalcularResponse> recalcular(
            @Parameter(description = "Identificador do produtor.", required = true)
            @PathVariable String producerId,
            @Valid @RequestBody(required = false) RecalcularRequest request) {
        String motivo = request != null ? request.motivoOrDefault() : "recalculo-manual";
        var selo = seloService.recalcularSelo(producerId, motivo);
        return ResponseEntity.ok(RecalcularResponse.from(selo));
    }

    @Operation(
            summary = "Histórico de alterações do selo",
            description = "Lista todas as alterações registradas no selo do produtor."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HistoricoSeloResponse.class))),
            @ApiResponse(responseCode = "404", description = "Selo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponseDto.class)))
    })
    @GetMapping("/{producerId}/historico")
    public ResponseEntity<HistoricoSeloResponse> historico(
            @Parameter(description = "Identificador do produtor.", required = true)
            @PathVariable String producerId) {
        var historico = seloService.historico(producerId);
        return ResponseEntity.ok(HistoricoSeloResponse.from(historico));
    }

    @ExceptionHandler(SeloNotFoundException.class)
    public ResponseEntity<ErroResponseDto> handleNotFound(SeloNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponseDto("not_found", ex.getMessage()));
    }
}
