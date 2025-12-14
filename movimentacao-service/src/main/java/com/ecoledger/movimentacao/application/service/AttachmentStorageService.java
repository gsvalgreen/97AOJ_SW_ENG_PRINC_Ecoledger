package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest.MovimentacaoRequestAttachment;
import io.swagger.v3.oas.annotations.media.Schema;

public interface AttachmentStorageService {

    void validateAttachment(MovimentacaoRequestAttachment attachment);

    AttachmentConfirmation confirmUpload(String objectKey);

    @Schema(description = "Dados retornados após a confirmação do upload de um anexo")
    record AttachmentConfirmation(
            @Schema(description = "Chave do objeto no armazenamento") String objectKey,
            @Schema(description = "URL pública do anexo") String url,
            @Schema(description = "Tipo de conteúdo do anexo") String tipo,
            @Schema(description = "Hash do conteúdo") String hash,
            @Schema(description = "Tamanho do arquivo em bytes") long size
    ) {}
}
