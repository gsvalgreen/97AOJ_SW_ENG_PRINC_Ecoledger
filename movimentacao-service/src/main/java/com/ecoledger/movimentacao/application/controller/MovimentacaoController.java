package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.application.dto.MovimentacaoResponse;
import com.ecoledger.movimentacao.application.dto.MovimentacaoDetailResponse;
import com.ecoledger.movimentacao.application.dto.MovimentacaoListResponse;
import com.ecoledger.movimentacao.application.service.InvalidAttachmentException;
import com.ecoledger.movimentacao.application.service.MovimentacaoNotFoundException;
import com.ecoledger.movimentacao.application.service.MovimentacaoService;
import com.ecoledger.movimentacao.application.service.ProducerNotApprovedException;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class MovimentacaoController {

    private final MovimentacaoService service;

    public MovimentacaoController(MovimentacaoService service) {
        this.service = service;
    }

    @PostMapping("/movimentacoes")
    public ResponseEntity<MovimentacaoResponse> criar(@Valid @RequestBody MovimentacaoRequest request) {
        var id = service.registrar(request);
        return ResponseEntity.created(URI.create("/movimentacoes/" + id))
                .body(new MovimentacaoResponse(id));
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

    public record ErrorResponse(String mensagem) {}
}
