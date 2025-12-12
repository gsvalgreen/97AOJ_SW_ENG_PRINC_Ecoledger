package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest.MovimentacaoRequestAttachment;
import com.ecoledger.movimentacao.application.service.AttachmentStorageService;

public class NoOpAttachmentStorageService implements AttachmentStorageService {

    @Override
    public void validateAttachment(MovimentacaoRequestAttachment attachment) {
        // no-op fallback for local tests
    }
}
