package com.ecoledger.certificacao.controller;

import com.ecoledger.certificacao.dto.HistoricoSeloResponse;
import com.ecoledger.certificacao.dto.RecalcularRequest;
import com.ecoledger.certificacao.dto.RecalcularResponse;
import com.ecoledger.certificacao.dto.SeloResponse;
import com.ecoledger.certificacao.model.SeloVerde;
import com.ecoledger.certificacao.service.SeloService;
import com.ecoledger.certificacao.service.exception.SeloNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/selos")
public class SeloController {

    private final SeloService seloService;

    public SeloController(SeloService seloService) {
        this.seloService = seloService;
    }

    @GetMapping("/{producerId}")
    public ResponseEntity<SeloResponse> obterSelo(@PathVariable String producerId) {
        SeloVerde selo = seloService.obterSelo(producerId);
        return ResponseEntity.ok(SeloResponse.from(selo));
    }

    @PostMapping("/{producerId}/recalcular")
    public ResponseEntity<RecalcularResponse> recalcular(@PathVariable String producerId,
                                                         @Valid @RequestBody(required = false) RecalcularRequest request) {
        String motivo = request != null ? request.motivoOrDefault() : "recalculo-manual";
        var selo = seloService.recalcularSelo(producerId, motivo);
        return ResponseEntity.ok(RecalcularResponse.from(selo));
    }

    @GetMapping("/{producerId}/historico")
    public ResponseEntity<HistoricoSeloResponse> historico(@PathVariable String producerId) {
        var historico = seloService.historico(producerId);
        return ResponseEntity.ok(HistoricoSeloResponse.from(historico));
    }

    @ExceptionHandler(SeloNotFoundException.class)
    public ResponseEntity<Void> handleNotFound() {
        return ResponseEntity.notFound().build();
    }
}
