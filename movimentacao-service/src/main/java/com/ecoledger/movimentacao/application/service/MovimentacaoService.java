package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.application.dto.MovimentacaoDetailResponse;
import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest.MovimentacaoRequestAttachment;
import com.ecoledger.movimentacao.config.AttachmentPolicyProperties;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.ecoledger.movimentacao.domain.model.MovimentacaoAnexo;
import com.ecoledger.movimentacao.domain.repository.MovimentacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository repository;
    private final ProducerApprovalClient approvalClient;
    private final AttachmentPolicyProperties attachmentPolicyProperties;
    private final AttachmentStorageService attachmentStorageService;
    private final MovimentacaoEventPublisher eventPublisher;

    public MovimentacaoService(MovimentacaoRepository repository,
                               ProducerApprovalClient approvalClient,
                               AttachmentPolicyProperties attachmentPolicyProperties,
                               AttachmentStorageService attachmentStorageService,
                               MovimentacaoEventPublisher eventPublisher) {
        this.repository = repository;
        this.approvalClient = approvalClient;
        this.attachmentPolicyProperties = attachmentPolicyProperties;
        this.attachmentStorageService = attachmentStorageService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UUID registrar(MovimentacaoRequest request) {
        org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MovimentacaoService.class);
        LOG.info("Starting registrar for producerId={} commodityId={} tipo={} timestamp={} traceId={}", request.producerId(), request.commodityId(), request.tipo(), request.timestamp(), org.slf4j.MDC.get("traceId"));
        validateProducer(request.producerId());
        LOG.debug("Producer validated for producerId={} traceId={}", request.producerId(), org.slf4j.MDC.get("traceId"));
        validateAnexos(request.anexos());
        LOG.debug("Attachments validated count={} traceId={}", request.anexos() == null ? 0 : request.anexos().size(), org.slf4j.MDC.get("traceId"));
        Double lat = null;
        Double lon = null;
        if (request.localizacao() != null) {
            lat = request.localizacao().lat();
            lon = request.localizacao().lon();
        }
        Movimentacao movimentacao = new Movimentacao(
                request.producerId(),
                request.commodityId(),
                request.tipo(),
                request.quantidade(),
                request.unidade(),
                request.timestamp(),
                lat,
                lon,
                buildAnexos(request.anexos())
        );
        Movimentacao saved = repository.save(movimentacao);
        LOG.info("Saved movimentacao id={} producerId={} traceId={}", saved.getId(), saved.getProducerId(), org.slf4j.MDC.get("traceId"));
        try {
            eventPublisher.publishCreated(saved);
            LOG.info("Published movimentacao.criada for id={} traceId={}", saved.getId(), org.slf4j.MDC.get("traceId"));
        } catch (Exception ex) {
            LOG.error("Failed to publish event for movimentacao id={} traceId={} error={}", saved.getId(), org.slf4j.MDC.get("traceId"), ex.getMessage());
        }
        return saved.getId();
    }

    private void validateProducer(String producerId) {
        if (!approvalClient.isApproved(producerId)) {
            throw new ProducerNotApprovedException(producerId);
        }
    }

    private void validateAnexos(List<MovimentacaoRequestAttachment> anexos) {
        if (anexos == null || anexos.isEmpty()) {
            return;
        }
        if (anexos.size() > attachmentPolicyProperties.maxAttachments()) {
            throw new InvalidAttachmentException("Maximum attachments exceeded");
        }
        var allowed = attachmentPolicyProperties.allowedMimeTypes();
        for (var anexo : anexos) {
            if (!allowed.contains(anexo.tipo())) {
                throw new InvalidAttachmentException("Attachment type not allowed: " + anexo.tipo());
            }
            attachmentStorageService.validateAttachment(anexo);
        }
    }

    private List<MovimentacaoAnexo> buildAnexos(List<MovimentacaoRequestAttachment> anexos) {
        if (anexos == null) {
            return List.of();
        }
        return anexos.stream()
                .map(anexo -> {
                    MovimentacaoAnexo entity = new MovimentacaoAnexo();
                    entity.setTipo(anexo.tipo());
                    entity.setUrl(anexo.url());
                    entity.setHash(anexo.hash());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    public Movimentacao buscarPorId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new MovimentacaoNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public MovimentacaoDetailResponse buscarPorIdDto(UUID id) {
        var m = repository.findById(id).orElseThrow(() -> new MovimentacaoNotFoundException(id));
        return MovimentacaoDetailResponse.fromEntity(m);
    }

    public Page<Movimentacao> buscarPorProducer(String producerId, Pageable pageable) {
        return repository.findByProducerId(producerId, pageable);
    }

    public Page<Movimentacao> buscarPorProducer(String producerId, Pageable pageable, String commodityId, OffsetDateTime fromDate, OffsetDateTime toDate) {
        if (commodityId != null && !commodityId.isBlank()) {
            return repository.findByProducerIdAndCommodityId(producerId, commodityId, pageable);
        }
        if (fromDate != null && toDate != null) {
            return repository.findByProducerIdAndTimestampBetween(producerId, fromDate, toDate, pageable);
        }
        if (fromDate != null) {
            return repository.findByProducerIdAndTimestampAfter(producerId, fromDate, pageable);
        }
        if (toDate != null) {
            return repository.findByProducerIdAndTimestampBefore(producerId, toDate, pageable);
        }
        return repository.findByProducerId(producerId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoDetailResponse> buscarPorProducerDto(String producerId, Pageable pageable, String commodityId, OffsetDateTime fromDate, OffsetDateTime toDate) {
        var page = buscarPorProducer(producerId, pageable, commodityId, fromDate, toDate);
        return page.map(MovimentacaoDetailResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoDetailResponse> buscarHistoricoPorCommodity(String commodityId) {
        var list = repository.findByCommodityIdOrderByTimestampDesc(commodityId);
        return list.stream()
                .map(MovimentacaoDetailResponse::fromEntity)
                .toList();
    }
}
