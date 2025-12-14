package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.service.AttachmentStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnexoControllerTest {

    private com.ecoledger.movimentacao.config.S3Properties s3Properties;
    private AttachmentStorageService attachmentStorageService;
    private S3Client s3Client;
    private AnexoController controller;

    @BeforeEach
    void setup() {
        s3Properties = new com.ecoledger.movimentacao.config.S3Properties("https://s3.example.com", "mybucket", null, null, null, 1024, java.util.List.of(), null, false);
        attachmentStorageService = mock(AttachmentStorageService.class);
        s3Client = mock(S3Client.class);
        controller = new AnexoController(s3Properties, attachmentStorageService, s3Client);
    }

    @Test
    void createUploadUrl_usesEndpoint_whenPublicBaseNull_and_encodesContentType_and_includesBucket() {
        s3Properties = new com.ecoledger.movimentacao.config.S3Properties("https://s3.example.com", "mybucket", null, null, null, 1024, java.util.List.of(), null, false);
        controller = new AnexoController(s3Properties, attachmentStorageService, s3Client);

        var req = new AnexoController.SignedUploadRequest("text/plain;charset=utf-8");
        var resp = controller.createUploadUrl(req).getBody();
        assertNotNull(resp);
        assertNotNull(resp.objectKey());
        assertTrue(resp.uploadUrl().contains("/mybucket/"));
        // verify encoded content type is present
        assertTrue(resp.uploadUrl().contains("contentType="));
        String encoded = resp.uploadUrl().split("contentType=")[1].split("&")[0];
        String decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
        assertEquals("text/plain;charset=utf-8", decoded);
    }

    @Test
    void createUploadUrl_usesPublicBase_without_duplicating_bucket_when_publicBase_contains_bucket() {
        s3Properties = new com.ecoledger.movimentacao.config.S3Properties("https://s3.example.com", "mybucket", null, null, null, 1024, java.util.List.of(), "https://cdn.example.com/mybucket", false);
        controller = new AnexoController(s3Properties, attachmentStorageService, s3Client);

        var req = new AnexoController.SignedUploadRequest(null);
        var resp = controller.createUploadUrl(req).getBody();
        assertNotNull(resp);
        // should not contain double bucket
        assertFalse(resp.uploadUrl().contains("/mybucket/mybucket"));
        // since publicBase already contains bucket, url should be base/key?...
        assertTrue(resp.uploadUrl().contains("cdn.example.com"));
    }

    @Test
    void uploadProxy_success_returnsOk_and_callsS3Client() {
        String key = "k1";
        byte[] body = "hello".getBytes();
        var resp = controller.uploadProxy(key, body, "text/plain");
        assertEquals(200, resp.getStatusCodeValue());
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(software.amazon.awssdk.core.sync.RequestBody.class));
        assertEquals(s3Properties.bucket(), captor.getValue().bucket());
        assertEquals(key, captor.getValue().key());
        assertEquals("text/plain", captor.getValue().contentType());
    }

    @Test
    void uploadProxy_whenS3Throws_returns500() {
        doThrow(new RuntimeException("boom")).when(s3Client).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
        var resp = controller.uploadProxy("k2", null, null);
        assertEquals(500, resp.getStatusCodeValue());
    }

    @Test
    void confirmUpload_returnsAttachmentConfirmation() {
        var expected = new AttachmentStorageService.AttachmentConfirmation("k", "url", "tipo", "hash", 10L);
        when(attachmentStorageService.confirmUpload("k")).thenReturn(expected);
        var resp = controller.confirmUpload(new AnexoController.ConfirmUploadRequest("k"));
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(expected, resp.getBody());
    }


}
