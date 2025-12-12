package com.ecoledger.movimentacao.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "movimentacao.attachment-policy")
public record AttachmentPolicyProperties(int maxAttachments, List<String> allowedMimeTypes) {
}

