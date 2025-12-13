package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.service.AttachmentStorageService;
import com.ecoledger.movimentacao.application.service.AttachmentStorageService.AttachmentConfirmation;
import com.ecoledger.movimentacao.config.S3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static software.amazon.awssdk.core.sync.RequestBody.fromBytes;

@RestController
@RequestMapping
public class AnexoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnexoController.class);

    private final S3Properties s3Properties;
    private final AttachmentStorageService attachmentStorageService;
    private final S3Client s3Client;

    public AnexoController(S3Properties s3Properties,
                           AttachmentStorageService attachmentStorageService,
                           S3Client s3Client) {
        this.s3Properties = s3Properties;
        this.attachmentStorageService = attachmentStorageService;
        this.s3Client = s3Client;
    }

    public record SignedUploadRequest(String contentType) {
    }

    public record SignedUploadResponse(String objectKey, String uploadUrl) {
    }

    public record ConfirmUploadRequest(String objectKey) {
    }

    @PostMapping("/anexos/upload-url")
    public ResponseEntity<SignedUploadResponse> createUploadUrl(@RequestBody SignedUploadRequest request) {
        String key = UUID.randomUUID().toString();
        String base = s3Properties.publicBaseUrl();
        if (base == null || base.isBlank()) {
            base = s3Properties.endpoint();
        }
        // normalize base to avoid duplicating bucket path when publicBaseUrl already contains it
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String bucket = s3Properties.bucket();
        String encodedType = URLEncoder.encode(request.contentType() == null ? "" : request.contentType(), StandardCharsets.UTF_8);
        String uploadUrl;
        if (base.endsWith("/" + bucket) || base.endsWith(bucket)) {
            uploadUrl = String.format("%s/%s?contentType=%s&signature=simulated", base, key, encodedType);
        } else {
            uploadUrl = String.format("%s/%s/%s?contentType=%s&signature=simulated", base, bucket, key, encodedType);
        }
        return ResponseEntity.ok(new SignedUploadResponse(key, uploadUrl));
    }

    @PostMapping("/anexos/upload-proxy")
    public ResponseEntity<Void> uploadProxy(@RequestParam String objectKey,
                                            @RequestBody byte[] body,
                                            @RequestHeader(name = "Content-Type", required = false) String contentType) {
        try {
            var put = PutObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();
            var rb = fromBytes(body == null ? new byte[0] : body);
            s3Client.putObject(put, rb);
            LOGGER.info("Uploaded object to S3 bucket={} key={} contentType={}", s3Properties.bucket(), objectKey, contentType);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            LOGGER.error("Failed to upload proxy object key={} error={}", objectKey, ex.getMessage(), ex);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/anexos/confirm")
    public ResponseEntity<AttachmentConfirmation> confirmUpload(@RequestBody ConfirmUploadRequest request) {
        AttachmentConfirmation info = attachmentStorageService.confirmUpload(request.objectKey());
        return ResponseEntity.ok(info);
    }
}
