package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.dto.MovimentacaoDetailResponse;
import com.ecoledger.movimentacao.application.dto.MovimentacaoListResponse;
import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.application.dto.MovimentacaoResponse;
import com.ecoledger.movimentacao.application.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping
public class MovimentacaoController {

    private final MovimentacaoService service;
    private final ObjectMapper objectMapper;
    private final IdempotencyService idempotencyService;

    public MovimentacaoController(MovimentacaoService service,
                                  ObjectMapper objectMapper,
                                  IdempotencyService idempotencyService) {
        this.service = service;
        this.objectMapper = objectMapper;
        this.idempotencyService = idempotencyService;
    }

    private static String sha256(String value) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/movimentacoes")
    public ResponseEntity<MovimentacaoResponse> criar(@Valid @RequestBody MovimentacaoRequest request,
                                                      @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        try {
            String payloadJson = this.objectMapper.writeValueAsString(request);
            String requestHash = sha256(payloadJson);
            var maybe = this.idempotencyService.handle(idempotencyKey, requestHash, () -> service.registrar(request));
            if (maybe.isPresent()) {
                var resp = maybe.get();
                return ResponseEntity.created(URI.create("/movimentacoes/" + resp.movimentacaoId())).body(resp);
            }
            // if empty, fallthrough to normal behaviour
            var id = service.registrar(request);
            return ResponseEntity.created(URI.create("/movimentacoes/" + id)).body(new MovimentacaoResponse(id));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @GetMapping("/movimentacoes/{id}")
    public ResponseEntity<MovimentacaoDetailResponse> buscarPorId(@PathVariable UUID id) {
        var m = service.buscarPorId(id);
        return ResponseEntity.ok(MovimentacaoDetailResponse.fromEntity(m));
    }

    @GetMapping("/produtores/{producerId}/movimentacoes")
    public ResponseEntity<MovimentacaoListResponse> listarPorProducer(
            @PathVariable String producerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String commodityId,
            @RequestParam(required = false) java.time.OffsetDateTime fromDate,
            @RequestParam(required = false) java.time.OffsetDateTime toDate
    ) {
        int pageIndex = Math.max(1, page) - 1;
        var p = service.buscarPorProducerDto(producerId, PageRequest.of(pageIndex, size), commodityId, fromDate, toDate);
        var items = p.getContent();
        return ResponseEntity.ok(new MovimentacaoListResponse(items, p.getTotalElements()));
    }

    @GetMapping("/commodities/{commodityId}/historico")
    public ResponseEntity<java.util.List<MovimentacaoDetailResponse>> historicoPorCommodity(@PathVariable String commodityId) {
        var items = service.buscarHistoricoPorCommodity(commodityId);
        return ResponseEntity.ok(items);
    }

    @ExceptionHandler(ProducerNotApprovedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleProducerNotApproved(ProducerNotApprovedException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(InvalidAttachmentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAttachment(InvalidAttachmentException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(MovimentacaoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(MovimentacaoNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    public record ErrorResponse(String mensagem) {
    }
}
