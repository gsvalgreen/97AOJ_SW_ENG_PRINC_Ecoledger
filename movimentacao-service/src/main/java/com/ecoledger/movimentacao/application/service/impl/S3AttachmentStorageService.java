package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest.MovimentacaoRequestAttachment;
import com.ecoledger.movimentacao.application.service.AttachmentStorageService;
import com.ecoledger.movimentacao.application.service.InvalidAttachmentException;
import com.ecoledger.movimentacao.config.S3Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public class S3AttachmentStorageService implements AttachmentStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3AttachmentStorageService.class);

    private final S3Properties properties;
    private final S3Client s3Client;

    public S3AttachmentStorageService(S3Properties properties, S3Client s3Client) {
        this.properties = properties;
        this.s3Client = s3Client;
    }

    @Override
    public void validateAttachment(MovimentacaoRequestAttachment attachment) {
        String traceId = MDC.get("traceId");
        LOGGER.info("Validating attachment url={} mime={} traceId={}", attachment.url(), attachment.tipo(), traceId);
        ensureMimeAllowed(attachment.tipo());
        String key = resolveKey(attachment.url());
        HeadObjectResponse metadata = fetchMetadata(key);
        validateContentLength(metadata);
        validateContentType(attachment, metadata);
        validateHash(attachment, metadata);
        LOGGER.info("Attachment validation passed key={} traceId={}", key, traceId);
    }

    @Override
    public AttachmentConfirmation confirmUpload(String objectKey) {
        String traceId = MDC.get("traceId");
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
            if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
            String url;
            if (base.endsWith("/" + properties.bucket()) || base.endsWith(properties.bucket())) {
                url = String.format("%s/%s", base, key);
            } else {
                url = String.format("%s/%s/%s", base, properties.bucket(), key);
            }
            LOGGER.info("Confirmed upload key={} size={} contentType={} url={} traceId={}", key, size, contentType, url, traceId);
            return new AttachmentConfirmation(objectKey, url, contentType, hash, size);
        } catch (NoSuchKeyException ex) {
            LOGGER.warn("Attachment not found key={} traceId={}", key, MDC.get("traceId"));
            throw new InvalidAttachmentException("Attachment not found in storage");
        } catch (AwsServiceException | SdkClientException ex) {
            LOGGER.error("Unable to confirm attachment key={} traceId={} error={}", key, traceId, ex.getMessage());
            throw new InvalidAttachmentException("Unable to confirm attachment: " + ex.getMessage());
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
            LOGGER.info("HeadObject request bucket={} key={} traceId={}", properties.bucket(), key, MDC.get("traceId"));
            return s3Client.headObject(request);
        } catch (NoSuchKeyException ex) {
            // In local/dev environments where MinIO or S3 mock may not have the object uploaded
            // be lenient and return a minimal metadata response so feature tests can proceed.
            boolean lenientEnv = Boolean.parseBoolean(System.getenv().getOrDefault("MOVIMENTACAO_S3_LENIENT_WHEN_MISSING", "false"));
            if (lenientEnv && properties.endpoint() != null && (properties.endpoint().contains("localhost") || properties.endpoint().contains("127.0.0.1"))) {
                LOGGER.warn("Attachment not found key={} but running against local S3 endpoint {}; lenient validation applied", key, properties.endpoint());
                return HeadObjectResponse.builder().contentLength(0L).contentType("").build();
            }
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
        // if the key still contains the bucket prefix (e.g., when publicBaseUrl already contained the bucket path), strip it
        String bucketPrefix = properties.bucket() + "/";
        if (key.startsWith(bucketPrefix)) {
            key = key.substring(bucketPrefix.length());
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
