package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.ecoledger.movimentacao.domain.model.MovimentacaoAnexo;
import com.ecoledger.movimentacao.domain.repository.MovimentacaoRepository;
import com.ecoledger.movimentacao.config.AttachmentPolicyProperties;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Movimentacao movimentacao = new Movimentacao(
                request.producerId(),
                request.commodityId(),
                request.tipo(),
                request.quantidade(),
                request.unidade(),
                request.timestamp(),
                request.latitude(),
                request.longitude(),
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
}
