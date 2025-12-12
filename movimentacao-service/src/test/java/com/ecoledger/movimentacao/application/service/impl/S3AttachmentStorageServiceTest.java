package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.application.service.InvalidAttachmentException;
import com.ecoledger.movimentacao.config.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
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

    @Test
    void shouldRejectWhenMimeNotAllowed() {
        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("image/png", "http://localhost:9000/movimentacoes/doc.png", "h1");

        assertThatThrownBy(() -> service.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("type not allowed");
    }

    @Test
    void shouldRejectWhenContentLengthTooLarge() {
        // create properties with very small max size (1 KB) to force the exceed condition
        S3Properties smallProps = new S3Properties(
                "http://localhost:9000",
                "movimentacoes",
                "sa-east-1",
                "access",
                "secret",
                1,
                List.of("application/pdf"),
                "http://localhost:9000/movimentacoes",
                true
        );
        S3AttachmentStorageService smallService = new S3AttachmentStorageService(smallProps, s3Client);

        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://localhost:9000/movimentacoes/large.pdf", "h1");

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentLength(2048L)
                .contentType("application/pdf")
                .metadata(Map.of("hash", "h1"))
                .build());

        assertThatThrownBy(() -> smallService.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("exceeds max allowed size");
    }

    @Test
    void shouldRejectWhenContentTypeMismatch() {
        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://localhost:9000/movimentacoes/doc.pdf", "h1");

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentLength(100L)
                .contentType("application/octet-stream")
                .metadata(Map.of("hash", "h1"))
                .build());

        assertThatThrownBy(() -> service.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("content-type mismatch");
    }

    @Test
    void shouldAcceptWhenContentTypeBlank() {
        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://localhost:9000/movimentacoes/doc.pdf", "h1");

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentLength(100L)
                .contentType("")
                .metadata(Map.of("hash", "h1"))
                .build());

        assertThatNoException().isThrownBy(() -> service.validateAttachment(attachment));
    }

    @Test
    void shouldAcceptWhenNoHashMetadata() {
        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://localhost:9000/movimentacoes/doc.pdf", null);

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentLength(100L)
                .contentType("application/pdf")
                .build());

        assertThatNoException().isThrownBy(() -> service.validateAttachment(attachment));
    }

    @Test
    void shouldRejectWhenUrlBlank() {
        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "", null);

        assertThatThrownBy(() -> service.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("URL must be provided");
    }

    @Test
    void shouldRejectWhenPublicBaseUrlNotConfigured() {
        S3Properties properties = new S3Properties(
                "http://localhost:9000",
                "movimentacoes",
                "sa-east-1",
                "access",
                "secret",
                1024,
                List.of("application/pdf"),
                "",
                true
        );
        S3AttachmentStorageService s = new S3AttachmentStorageService(properties, s3Client);

        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://localhost:9000/movimentacoes/doc.pdf", null);

        assertThatThrownBy(() -> s.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("S3 public base URL is not configured");
    }

    @Test
    void shouldRejectWhenSdkClientExceptionOccurs() {
        MovimentacaoRequest.MovimentacaoRequestAttachment attachment =
                new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://localhost:9000/movimentacoes/doc.pdf", null);

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(SdkClientException.create("fail"));

        assertThatThrownBy(() -> service.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("Unable to validate attachment");
    }
}

