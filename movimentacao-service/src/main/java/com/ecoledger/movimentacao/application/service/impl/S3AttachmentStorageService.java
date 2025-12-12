package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest.MovimentacaoRequestAttachment;
import com.ecoledger.movimentacao.application.service.AttachmentStorageService;
import com.ecoledger.movimentacao.config.S3Properties;

public class S3AttachmentStorageService implements AttachmentStorageService {

    private final S3Properties properties;

    public S3AttachmentStorageService(S3Properties properties) {
        this.properties = properties;
    }

    @Override
    public void validateAttachment(MovimentacaoRequestAttachment attachment) {
        // TODO implement real S3 validation/upload
        if (!properties.allowedMimeTypes().contains(attachment.tipo())) {
            throw new IllegalArgumentException("Attachment type not allowed: " + attachment.tipo());
        }
    }
}

