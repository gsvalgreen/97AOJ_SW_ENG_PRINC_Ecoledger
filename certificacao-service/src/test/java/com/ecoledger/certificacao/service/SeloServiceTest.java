package com.ecoledger.certificacao.service;

import com.ecoledger.certificacao.messaging.event.AuditoriaConcluidaEvent;
import com.ecoledger.certificacao.messaging.event.ResultadoAuditoria;
import com.ecoledger.certificacao.model.AlteracaoSelo;
import com.ecoledger.certificacao.model.SeloNivel;
import com.ecoledger.certificacao.model.SeloStatus;
import com.ecoledger.certificacao.model.SeloVerde;
import com.ecoledger.certificacao.repository.AlteracaoSeloRepository;
import com.ecoledger.certificacao.repository.SeloRepository;
import com.ecoledger.certificacao.service.config.SeloProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeloServiceTest {

    @Mock
    private SeloRepository seloRepository;

    @Mock
    private AlteracaoSeloRepository alteracaoSeloRepository;

    @Mock
    private CertificacaoEventPublisher eventPublisher;

    private SeloProperties properties;
    private SeloServiceImpl seloService;

    @BeforeEach
    void setup() {
        properties = new SeloProperties();
        properties.setExpirationDays(30);
        properties.setBronzeThreshold(70);
        properties.setPrataThreshold(80);
        properties.setOuroThreshold(90);

        seloService = new SeloServiceImpl(seloRepository, alteracaoSeloRepository, eventPublisher, properties);
    }

    @Test
    void shouldCreateActiveSeloWhenAuditApproved() {
        var event = sampleEvent(ResultadoAuditoria.APROVADO, "producer-1");
        when(seloRepository.findById(event.producerId())).thenReturn(Optional.empty());
        when(seloRepository.save(any(SeloVerde.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = seloService.processarAuditoriaConcluida(event);

        assertThat(result.getStatus()).isEqualTo(SeloStatus.ATIVO);
        assertThat(result.getNivel()).isEqualTo(SeloNivel.OURO);
        assertThat(result.getPontuacao()).isGreaterThanOrEqualTo(properties.getOuroThreshold());

        verify(alteracaoSeloRepository).save(any(AlteracaoSelo.class));
        verify(eventPublisher).publishSeloAtualizado(any());
    }

    @Test
    void shouldMarkInactiveWhenAuditRejected() {
        var event = sampleEvent(ResultadoAuditoria.REPROVADO, "producer-1");
        when(seloRepository.findById(event.producerId())).thenReturn(Optional.empty());
        when(seloRepository.save(any(SeloVerde.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = seloService.processarAuditoriaConcluida(event);

        assertThat(result.getStatus()).isEqualTo(SeloStatus.INATIVO);
        assertThat(result.getNivel()).isNull();
        verify(alteracaoSeloRepository).save(any(AlteracaoSelo.class));
    }

    @Test
    void shouldExpireSeloOnManualRecalculation() {
        var selo = new SeloVerde("producer-1", SeloStatus.ATIVO, SeloNivel.BRONZE, 75,
                List.of("initial"), "v1", UUID.randomUUID(), ResultadoAuditoria.APROVADO,
                Instant.now().minus(45, ChronoUnit.DAYS), Instant.now().minus(15, ChronoUnit.DAYS));

        when(seloRepository.findById(selo.getProducerId())).thenReturn(Optional.of(selo));
        when(seloRepository.save(any(SeloVerde.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = seloService.recalcularSelo(selo.getProducerId(), "expiracao-verificacao");

        assertThat(result.getStatus()).isEqualTo(SeloStatus.PENDENTE);
        assertThat(result.getNivel()).isNull();

        var historyCaptor = ArgumentCaptor.forClass(AlteracaoSelo.class);
        verify(alteracaoSeloRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getParaStatus()).isEqualTo(SeloStatus.PENDENTE);
        verify(eventPublisher).publishSeloAtualizado(any());
    }

    @Test
    void shouldNotCreateHistoryWhenStatusUnchanged() {
        var seloAtual = new SeloVerde("producer-2", SeloStatus.ATIVO, SeloNivel.PRATA, 85,
                List.of("ok"), "v1", UUID.randomUUID(), ResultadoAuditoria.APROVADO, Instant.now(), Instant.now().plus(10, ChronoUnit.DAYS));
        when(seloRepository.findById(seloAtual.getProducerId())).thenReturn(Optional.of(seloAtual));
        when(seloRepository.save(any(SeloVerde.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var event = sampleEvent(ResultadoAuditoria.APROVADO, seloAtual.getProducerId());

        seloService.processarAuditoriaConcluida(event);

        verify(alteracaoSeloRepository, never()).save(any());
    }

    private AuditoriaConcluidaEvent sampleEvent(ResultadoAuditoria resultado, String producerId) {
        return new AuditoriaConcluidaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                producerId,
                resultado,
                "v1",
                List.of(new AuditoriaConcluidaEvent.DetalheEvidencia("origem", "mock")),
                Instant.now()
        );
    }
}
