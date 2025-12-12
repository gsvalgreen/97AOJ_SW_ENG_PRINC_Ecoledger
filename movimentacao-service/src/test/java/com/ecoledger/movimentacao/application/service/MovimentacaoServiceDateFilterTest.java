package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.ecoledger.movimentacao.domain.repository.MovimentacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class MovimentacaoServiceDateFilterTest {

    @Mock
    private MovimentacaoRepository repository;
    @Mock
    private ProducerApprovalClient approvalClient;
    @Mock
    private AttachmentStorageService attachmentStorageService;
    @Mock
    private MovimentacaoEventPublisher eventPublisher;

    @InjectMocks
    private MovimentacaoService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new MovimentacaoService(repository, approvalClient, new com.ecoledger.movimentacao.config.AttachmentPolicyProperties(2, List.of("application/pdf")), attachmentStorageService, eventPublisher);
    }

    @Test
    void whenFromAndToDate_shouldCallRepositoryBetween() {
        var from = OffsetDateTime.now().minusDays(5);
        var to = OffsetDateTime.now();
        when(repository.findByProducerIdAndTimestampBetween(eq("prod-1"), eq(from), eq(to), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var page = service.buscarPorProducer("prod-1", PageRequest.of(0, 10), null, from, to);

        verify(repository).findByProducerIdAndTimestampBetween(eq("prod-1"), eq(from), eq(to), any(PageRequest.class));
        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    @Test
    void whenFromDateOnly_shouldCallRepositoryAfter() {
        var from = OffsetDateTime.now().minusDays(2);
        when(repository.findByProducerIdAndTimestampAfter(eq("prod-1"), eq(from), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var page = service.buscarPorProducer("prod-1", PageRequest.of(0, 10), null, from, null);

        verify(repository).findByProducerIdAndTimestampAfter(eq("prod-1"), eq(from), any(PageRequest.class));
    }

    @Test
    void whenToDateOnly_shouldCallRepositoryBefore() {
        var to = OffsetDateTime.now().plusDays(1);
        when(repository.findByProducerIdAndTimestampBefore(eq("prod-1"), eq(to), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var page = service.buscarPorProducer("prod-1", PageRequest.of(0, 10), null, null, to);

        verify(repository).findByProducerIdAndTimestampBefore(eq("prod-1"), eq(to), any(PageRequest.class));
    }
}
