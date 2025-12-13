package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest.MovimentacaoRequestAttachment;
import com.ecoledger.movimentacao.application.service.AttachmentStorageService;

public class NoOpAttachmentStorageService implements AttachmentStorageService {

    @Override
    public void validateAttachment(MovimentacaoRequestAttachment attachment) {
        // no-op fallback for local tests
    }

    @Override
    public AttachmentConfirmation confirmUpload(String objectKey) {
        String url = "https://no-op.storage/" + objectKey;
        return new AttachmentConfirmation(objectKey, url, null, null, 0L);
    }
}
