package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.application.service.InvalidAttachmentException;
import com.ecoledger.movimentacao.config.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class S3AttachmentStorageServiceTest {

    private S3Client s3Client;
    private S3AttachmentStorageService service;

    @BeforeEach
    void setup() {
        s3Client = mock(S3Client.class);
        S3Properties properties = new S3Properties(
                "http://localhost:9000",
                "movimentacoes",
                "sa-east-1",
                "access",
                "secret",
                1024,
                List.of("application/pdf"),
                "http://localhost:9000/movimentacoes",
                true
        );
        service = new S3AttachmentStorageService(properties, s3Client);
    }

    @Test
    void shouldValidateWhenMetadataMatchesRequest() {
        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://localhost:9000/movimentacoes/doc.pdf", "abc123");

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentLength(512L)
                .contentType("application/pdf")
                .metadata(Map.of("hash", "abc123"))
                .build());

        service.validateAttachment(attachment);
    }

    @Test
    void shouldRejectWhenUrlOutsideConfiguredBase() {
        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://malicious/doc.pdf", "abc123");

        assertThatThrownBy(() -> service.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("does not match configured bucket");
    }

    @Test
    void shouldRejectWhenHeadObjectFails() {
        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://localhost:9000/movimentacoes/missing.pdf", "abc123");
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());

        assertThatThrownBy(() -> service.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("Attachment not found");
    }

    @Test
    void shouldRejectWhenHashMismatch() {
        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://localhost:9000/movimentacoes/doc.pdf", "abc123");
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentLength(200L)
                .contentType("application/pdf")
                .metadata(Map.of("hash", "different"))
                .build());

        assertThatThrownBy(() -> service.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("hash mismatch");
    }
}

