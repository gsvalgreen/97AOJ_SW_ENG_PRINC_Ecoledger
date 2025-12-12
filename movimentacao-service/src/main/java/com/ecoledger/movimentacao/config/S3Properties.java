package com.ecoledger.movimentacao.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "s3")
public record S3Properties(
        String endpoint,
        String bucket,
        String region,
        String accessKey,
        String secretKey,
        int maxAttachmentSizeKb,
        List<String> allowedMimeTypes
) {
    public long maxAttachmentSizeBytes() {
        return maxAttachmentSizeKb * 1024L;
    }
}
