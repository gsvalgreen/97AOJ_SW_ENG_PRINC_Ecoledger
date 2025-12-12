package com.ecoledger.movimentacao.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.ecoledger.movimentacao.domain.repository.MovimentacaoRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MovimentacaoServiceTest {

    @Mock
    private MovimentacaoRepository repository;

    @InjectMocks
    private MovimentacaoService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registrar_shouldPersistMovimentacao() {
        MovimentacaoRequest request = new MovimentacaoRequest(
                "prod-1",
                "cmd-1",
                "COLHEITA",
                new BigDecimal("10.5"),
                "KG",
                OffsetDateTime.now(),
                null,
                null,
                null
        );

        when(repository.save(org.mockito.Mockito.any(Movimentacao.class)))
                .thenAnswer(invocation -> {
                    Movimentacao saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        UUID id = service.registrar(request);

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(repository).save(captor.capture());
        Movimentacao persisted = captor.getValue();

        assertThat(persisted.getProducerId()).isEqualTo("prod-1");
        assertThat(id).isNotNull();
    }
}

