package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.config.AttachmentPolicyProperties;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.ecoledger.movimentacao.domain.repository.MovimentacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MovimentacaoServiceTest {

    @Mock
    private MovimentacaoRepository repository;
    @Mock
    private ProducerApprovalClient approvalClient;
    @Mock
    private AttachmentStorageService attachmentStorageService;
    @Mock
    private MovimentacaoEventPublisher eventPublisher;

    private AttachmentPolicyProperties attachmentPolicyProperties;

    @InjectMocks
    private MovimentacaoService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        attachmentPolicyProperties = new AttachmentPolicyProperties(2, List.of("application/pdf"));
        service = new MovimentacaoService(repository,
                approvalClient,
                attachmentPolicyProperties,
                attachmentStorageService,
                eventPublisher);
    }

    @Test
    void registrar_shouldPersistMovimentacao() {
        when(approvalClient.isApproved("prod-1")).thenReturn(true);
        when(repository.save(org.mockito.Mockito.any(Movimentacao.class)))
                .thenAnswer(invocation -> {
                    Movimentacao saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        UUID id = service.registrar(defaultRequest());

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(repository).save(captor.capture());
        Movimentacao persisted = captor.getValue();

        assertThat(persisted.getProducerId()).isEqualTo("prod-1");
        assertThat(id).isNotNull();

        verify(approvalClient).isApproved("prod-1");
        verify(eventPublisher).publishCreated(persisted);
    }

    @Test
    void registrar_shouldRejectWhenProducerNotApproved() {
        when(approvalClient.isApproved("prod-1")).thenReturn(false);

        assertThatThrownBy(() -> service.registrar(defaultRequest()))
                .isInstanceOf(ProducerNotApprovedException.class);

        verify(repository, never()).save(org.mockito.Mockito.any());
        verify(eventPublisher, never()).publishCreated(org.mockito.Mockito.any());
    }

    @Test
    void registrar_shouldRejectWhenAttachmentNotAllowed() {
        when(approvalClient.isApproved("prod-1")).thenReturn(true);

        var request = withAttachment("image/png");

        assertThatThrownBy(() -> service.registrar(request))
                .isInstanceOf(InvalidAttachmentException.class);

        verify(attachmentStorageService, never()).validateAttachment(org.mockito.Mockito.any());
    }

    @Test
    void registrar_shouldValidateAttachmentsWhenProvided() {
        when(approvalClient.isApproved("prod-1")).thenReturn(true);
        when(repository.save(org.mockito.Mockito.any(Movimentacao.class)))
                .thenAnswer(invocation -> {
                    Movimentacao saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        var request = withAttachment("application/pdf");

        service.registrar(request);

        verify(attachmentStorageService).validateAttachment(new MovimentacaoRequest.MovimentacaoRequestAttachment("application/pdf", "http://localhost:9000/movimentacoes/doc.pdf", "hash"));
    }

    private MovimentacaoRequest defaultRequest() {
        return new MovimentacaoRequest(
                "prod-1",
                "cmd-1",
                "COLHEITA",
                new BigDecimal("10.5"),
                "KG",
                OffsetDateTime.now(),
                null,
                null
        );
    }

    private MovimentacaoRequest withAttachment(String mimeType) {
        var attachment = new MovimentacaoRequest.MovimentacaoRequestAttachment(mimeType, "http://localhost:9000/movimentacoes/doc.pdf", "hash");
        return new MovimentacaoRequest(
                "prod-1",
                "cmd-1",
                "COLHEITA",
                new BigDecimal("10.5"),
                "KG",
                OffsetDateTime.now(),
                null,
                List.of(attachment)
        );
    }
}
