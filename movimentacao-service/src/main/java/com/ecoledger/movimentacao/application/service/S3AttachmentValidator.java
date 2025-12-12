package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.config.S3Properties;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class S3AttachmentValidator {

    private final S3Properties properties;

    public S3AttachmentValidator(S3Properties properties) {
        this.properties = properties;
    }

    public void validate(List<MovimentacaoRequest.MovimentacaoRequestAttachment> anexos) {
        if (anexos == null) {
            return;
        }

        for (var anexo : anexos) {
            if (!properties.allowedMimeTypes().contains(anexo.tipo())) {
                throw new InvalidAttachmentException("Attachment type not allowed: " + anexo.tipo());
            }
        }
    }
}

