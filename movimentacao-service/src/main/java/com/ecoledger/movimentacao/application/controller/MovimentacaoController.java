package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.dto.HistoricoMovimentacaoResponse;
import com.ecoledger.movimentacao.application.dto.MovimentacaoDetailResponse;
import com.ecoledger.movimentacao.application.dto.MovimentacaoListResponse;
import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.application.dto.MovimentacaoResponse;
import com.ecoledger.movimentacao.application.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping
@Tag(name = "Movimentacoes")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "Criar nova movimentação",
            description = "Cria uma nova movimentação e retorna o identificador gerado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movimentação criada",
                    content = @Content(schema = @Schema(implementation = MovimentacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Produtor não aprovado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MovimentacaoResponse> criar(
            @Valid @RequestBody MovimentacaoRequest request,
            @Parameter(name = "X-Idempotency-Key", description = "Chave para garantir idempotência da criação", required = false)
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MovimentacaoController.class);
        try {
            String payloadJson = this.objectMapper.writeValueAsString(request);
            String requestHash = sha256(payloadJson);
            LOG.info("Received create movimentacao request producerId={} commodityId={} idempotencyKey={} traceId={}", request.producerId(), request.commodityId(), idempotencyKey, org.slf4j.MDC.get("traceId"));
            var maybe = this.idempotencyService.handle(idempotencyKey, requestHash, () -> service.registrar(request));
            if (maybe.isPresent()) {
                var resp = maybe.get();
                LOG.info("Idempotency returned existing response movimentacaoId={} idempotencyKey={} traceId={}", resp.movimentacaoId(), idempotencyKey, org.slf4j.MDC.get("traceId"));
                return ResponseEntity.created(URI.create("/movimentacoes/" + resp.movimentacaoId())).body(resp);
            }
            // if empty, fallthrough to normal behaviour
            var id = service.registrar(request);
            LOG.info("Created movimentacao id={} producerId={} traceId={}", id, request.producerId(), org.slf4j.MDC.get("traceId"));
            return ResponseEntity.created(URI.create("/movimentacoes/" + id)).body(new MovimentacaoResponse(id));
        } catch (ProducerNotApprovedException ex) {
            LOG.warn("Producer not approved: {} traceId={}", ex.getMessage(), org.slf4j.MDC.get("traceId"));
            throw ex;
        } catch (InvalidAttachmentException ex) {
            LOG.warn("Invalid attachment: {} traceId={}", ex.getMessage(), org.slf4j.MDC.get("traceId"));
            throw ex;
        } catch (Exception ex) {
            LOG.error("Unhandled error while creating movimentacao: {} traceId={}", ex.getMessage(), org.slf4j.MDC.get("traceId"), ex);
            throw new RuntimeException(ex);
        }
    }

    @GetMapping("/movimentacoes/{id}")
    @Operation(summary = "Obter movimentação por id", description = "Retorna os detalhes completos de uma movimentação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentação encontrada",
                    content = @Content(schema = @Schema(implementation = MovimentacaoDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MovimentacaoDetailResponse> buscarPorId(@PathVariable UUID id) {
        var dto = service.buscarPorIdDto(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/produtores/{producerId}/movimentacoes")
    @Operation(summary = "Listar movimentações de um produtor",
            description = "Retorna paginação de movimentações filtrando por produtor, período e commodity.")
    @ApiResponse(responseCode = "200", description = "Lista paginada",
            content = @Content(schema = @Schema(implementation = MovimentacaoListResponse.class)))
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
    @Operation(summary = "Histórico por commodity", description = "Lista as movimentações associadas a uma commodity.")
    @ApiResponse(responseCode = "200", description = "Histórico encontrado",
            content = @Content(schema = @Schema(implementation = HistoricoMovimentacaoResponse.class)))
    public ResponseEntity<HistoricoMovimentacaoResponse> historicoPorCommodity(@PathVariable String commodityId) {
        var items = service.buscarHistoricoPorCommodity(commodityId);
        return ResponseEntity.ok(new HistoricoMovimentacaoResponse(items));
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

    @Schema(name = "Erro", description = "Modelo de erro padrão da API")
    public record ErrorResponse(
            @Schema(description = "Mensagem de erro", example = "Recurso não encontrado") String mensagem
    ) {
    }
}
