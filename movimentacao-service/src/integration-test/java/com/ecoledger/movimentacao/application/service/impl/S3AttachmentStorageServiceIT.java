package com.ecoledger.movimentacao.application.service.impl;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.application.service.InvalidAttachmentException;
import com.ecoledger.movimentacao.config.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class S3AttachmentStorageServiceIT {

    private static final String BUCKET = "movimentacoes";

    @Container
    private final S3MockContainer s3MockContainer = new S3MockContainer("latest")
            .withInitialBuckets(BUCKET);

    private S3AttachmentStorageService service;
    private S3Client client;
    private String s3MockContainerHttpEndpoint;

    @BeforeEach
    void setup() {
        s3MockContainerHttpEndpoint = s3MockContainer.getHttpEndpoint();
        System.out.println("s3MockContainerHttpEndpoint = " + s3MockContainerHttpEndpoint);
        var serviceConfig = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
        client = S3Client.builder()
                .endpointOverride(URI.create(s3MockContainerHttpEndpoint))
                .region(Region.SA_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("minioadmin", "minioadmin")
                ))
                .serviceConfiguration(serviceConfig)
                .build();
        S3Properties props = new S3Properties(
                s3MockContainerHttpEndpoint,
                BUCKET,
                "sa-east-1",
                "access",
                "secret",
                5,
                List.of("application/pdf"),
                s3MockContainerHttpEndpoint + "/" + BUCKET,
                true
        );
        service = new S3AttachmentStorageService(props, client);
    }

    @Test
    void validateAttachment_shouldPassWhenS3ObjectMatchesMetadata() {
        putObject("doc.pdf", "application/pdf", Map.of("hash", "abc"), 1024);
        var attachment = new MovimentacaoRequest.MovimentacaoRequestAttachment(
                "application/pdf",
                s3MockContainerHttpEndpoint + "/" + BUCKET + "/doc.pdf",
                "abc"
        );

        assertThatNoException().isThrownBy(() -> service.validateAttachment(attachment));
    }

    @Test
    void validateAttachment_shouldThrowWhenHashDiffers() {
        putObject("doc.pdf", "application/pdf", Map.of("hash", "xyz"), 1024);
        var attachment = new MovimentacaoRequest.MovimentacaoRequestAttachment(
                "application/pdf",
                s3MockContainerHttpEndpoint + "/" + BUCKET + "/doc.pdf",
                "abc"
        );

        assertThatThrownBy(() -> service.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("hash mismatch");
    }

    @Test
    void validateAttachment_shouldThrowWhenFileTooLarge() {
        putObject("doc.pdf", "application/pdf", Map.of("hash", "abc"), 10_000);
        var attachment = new MovimentacaoRequest.MovimentacaoRequestAttachment(
                "application/pdf",
                s3MockContainerHttpEndpoint + "/" + BUCKET + "/doc.pdf",
                "abc"
        );

        assertThatThrownBy(() -> service.validateAttachment(attachment))
                .isInstanceOf(InvalidAttachmentException.class)
                .hasMessageContaining("exceeds max");
    }

    private void putObject(String key, String contentType, Map<String, String> metadata, int sizeBytes) {
        client.putObject(PutObjectRequest.builder()
                        .bucket(BUCKET)
                        .key(key)
                        .contentType(contentType)
                        .metadata(metadata)
                        .build(),
                RequestBody.fromBytes(new byte[sizeBytes]));
    }
}
