package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest.MovimentacaoRequestAttachment;

public interface AttachmentStorageService {

    void validateAttachment(MovimentacaoRequestAttachment attachment);

    AttachmentConfirmation confirmUpload(String objectKey);

    record AttachmentConfirmation(String objectKey, String url, String tipo, String hash, long size) {}
}
