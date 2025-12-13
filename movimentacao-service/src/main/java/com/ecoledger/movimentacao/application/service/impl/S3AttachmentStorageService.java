package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest.MovimentacaoRequestAttachment;
import com.ecoledger.movimentacao.application.service.AttachmentStorageService;
import com.ecoledger.movimentacao.application.service.InvalidAttachmentException;
import com.ecoledger.movimentacao.config.S3Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Service
public class S3AttachmentStorageService implements AttachmentStorageService {

    private final S3Properties properties;
    private final S3Client s3Client;

    public S3AttachmentStorageService(S3Properties properties, S3Client s3Client) {
        this.properties = properties;
        this.s3Client = s3Client;
    }

    @Override
    public void validateAttachment(MovimentacaoRequestAttachment attachment) {
        ensureMimeAllowed(attachment.tipo());
        String key = resolveKey(attachment.url());
        HeadObjectResponse metadata = fetchMetadata(key);
        validateContentLength(metadata);
        validateContentType(attachment, metadata);
        validateHash(attachment, metadata);
    }

    @Override
    public AttachmentConfirmation confirmUpload(String objectKey) {
        String key = objectKey;
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .build();
        try {
            HeadObjectResponse metadata = s3Client.headObject(request);
            String contentType = metadata.contentType();
            String hash = (metadata.metadata() == null) ? null : metadata.metadata().get("hash");
            long size = metadata.contentLength();
            String base = properties.publicBaseUrl() != null && !properties.publicBaseUrl().isBlank() ? properties.publicBaseUrl() : properties.endpoint();
            String url = String.format("%s/%s/%s", base, properties.bucket(), key);
            return new AttachmentConfirmation(objectKey, url, contentType, hash, size);
        } catch (NoSuchKeyException ex) {
            throw new com.ecoledger.movimentacao.application.service.InvalidAttachmentException("Attachment not found in storage");
        } catch (AwsServiceException | SdkClientException ex) {
            throw new com.ecoledger.movimentacao.application.service.InvalidAttachmentException("Unable to confirm attachment: " + ex.getMessage());
        }
    }

    private void ensureMimeAllowed(String mime) {
        if (!properties.allowedMimeTypes().contains(mime)) {
            throw new InvalidAttachmentException("Attachment type not allowed: " + mime);
        }
    }

    private HeadObjectResponse fetchMetadata(String key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .build();
        try {
            return s3Client.headObject(request);
        } catch (NoSuchKeyException ex) {
            throw new InvalidAttachmentException("Attachment not found in storage");
        } catch (AwsServiceException | SdkClientException ex) {
            throw new InvalidAttachmentException("Unable to validate attachment: " + ex.getMessage());
        }
    }

    private void validateContentLength(HeadObjectResponse response) {
        long contentLength = response.contentLength();
        if (contentLength > properties.maxAttachmentSizeBytes()) {
            throw new InvalidAttachmentException("Attachment exceeds max allowed size");
        }
    }

    private void validateContentType(MovimentacaoRequestAttachment attachment, HeadObjectResponse response) {
        if (StringUtils.isBlank(response.contentType())) {
            return;
        }
        if (!response.contentType().equalsIgnoreCase(attachment.tipo())) {
            throw new InvalidAttachmentException("Attachment content-type mismatch");
        }
    }

    private void validateHash(MovimentacaoRequestAttachment attachment, HeadObjectResponse response) {
        if (response.metadata() == null || response.metadata().isEmpty()) {
            return;
        }
        String remoteHash = response.metadata().get("hash");
        if (remoteHash == null) {
            return;
        }
        if (!remoteHash.equalsIgnoreCase(attachment.hash())) {
            throw new InvalidAttachmentException("Attachment hash mismatch");
        }
    }

    private String resolveKey(String url) {
        if (StringUtils.isBlank(url)) {
            throw new InvalidAttachmentException("Attachment URL must be provided");
        }
        String normalizedBase = normalizeBase(properties.publicBaseUrl());
        String normalizedUrl = normalizeBase(url);
        if (!normalizedUrl.startsWith(normalizedBase)) {
            throw new InvalidAttachmentException("Attachment URL does not match configured bucket");
        }
        String key = normalizedUrl.substring(normalizedBase.length());
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        if (key.isBlank()) {
            throw new InvalidAttachmentException("Attachment URL missing object key");
        }
        return key;
    }

    private String normalizeBase(String value) {
        if (StringUtils.isBlank(value)) {
            throw new InvalidAttachmentException("S3 public base URL is not configured");
        }
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
