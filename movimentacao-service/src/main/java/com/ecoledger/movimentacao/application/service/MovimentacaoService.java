package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
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
        validateProducer(request.producerId());
        validateAnexos(request.anexos());
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
        eventPublisher.publishCreated(saved);
        return saved.getId();
    }

    private void validateProducer(String producerId) {
        if (!approvalClient.isApproved(producerId)) {
            throw new ProducerNotApprovedException(producerId);
        }
    }

    private void validateAnexos(List<MovimentacaoRequest.MovimentacaoRequestAttachment> anexos) {
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

    private List<MovimentacaoAnexo> buildAnexos(List<MovimentacaoRequest.MovimentacaoRequestAttachment> anexos) {
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

    public Movimentacao buscarPorId(java.util.UUID id) {
        return repository.findById(id).orElseThrow(() -> new MovimentacaoNotFoundException(id));
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.ecoledger.movimentacao.application.dto.MovimentacaoDetailResponse> buscarPorProducerDto(String producerId, Pageable pageable, String commodityId, OffsetDateTime fromDate, OffsetDateTime toDate) {
        var page = buscarPorProducer(producerId, pageable, commodityId, fromDate, toDate);
        return page.map(com.ecoledger.movimentacao.application.dto.MovimentacaoDetailResponse::fromEntity);
    }

    public List<Movimentacao> buscarHistoricoPorCommodity(String commodityId) {
        return repository.findByCommodityIdOrderByTimestampDesc(commodityId);
    }
}
