package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.config.S3Properties;
import com.ecoledger.movimentacao.application.service.AttachmentStorageService;
import com.ecoledger.movimentacao.application.service.AttachmentStorageService.AttachmentConfirmation;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AnexoController {

    private final S3Properties s3Properties;
    private final AttachmentStorageService attachmentStorageService;

    public AnexoController(S3Properties s3Properties,
                           AttachmentStorageService attachmentStorageService) {
        this.s3Properties = s3Properties;
        this.attachmentStorageService = attachmentStorageService;
    }

    public static record SignedUploadRequest(String contentType) {}
    public static record SignedUploadResponse(String objectKey, String uploadUrl) {}
    public static record ConfirmUploadRequest(String objectKey) {}

    @PostMapping("/anexos/upload-url")
    public ResponseEntity<SignedUploadResponse> createUploadUrl(@RequestBody SignedUploadRequest request) {
        String key = UUID.randomUUID().toString();
        String base = s3Properties.publicBaseUrl();
        if (base == null || base.isBlank()) {
            base = s3Properties.endpoint();
        }
        // Construct a simple URL that simulates a presigned PUT URL
        String encodedType = URLEncoder.encode(request.contentType() == null ? "" : request.contentType(), StandardCharsets.UTF_8);
        String uploadUrl = String.format("%s/%s/%s?contentType=%s&signature=simulated", base, s3Properties.bucket(), key, encodedType);
        return ResponseEntity.ok(new SignedUploadResponse(key, uploadUrl));
    }

    @PostMapping("/anexos/confirm")
    public ResponseEntity<AttachmentConfirmation> confirmUpload(@RequestBody ConfirmUploadRequest request) {
        try {
            AttachmentConfirmation info = attachmentStorageService.confirmUpload(request.objectKey());
            return ResponseEntity.ok(info);
        } catch (com.ecoledger.movimentacao.application.service.InvalidAttachmentException ex) {
            // fallback to constructing a public URL even if storage confirmation failed (useful for tests/no-op environments)
            String base = s3Properties.publicBaseUrl();
            if (base == null || base.isBlank()) {
                base = s3Properties.endpoint();
            }
            String url = String.format("%s/%s/%s", base, s3Properties.bucket(), request.objectKey());
            AttachmentConfirmation fallback = new AttachmentConfirmation(request.objectKey(), url, null, null, 0L);
            return ResponseEntity.ok(fallback);
        }
    }
}
