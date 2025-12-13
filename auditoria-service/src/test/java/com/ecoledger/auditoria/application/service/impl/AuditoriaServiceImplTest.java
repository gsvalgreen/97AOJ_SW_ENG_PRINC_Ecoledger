package com.ecoledger.auditoria.application.service.impl;

import com.ecoledger.auditoria.application.dto.HistoricoAuditoriasResponse;
import com.ecoledger.auditoria.application.dto.RegistroAuditoriaResponse;
import com.ecoledger.auditoria.application.dto.RevisaoRequest;
import com.ecoledger.auditoria.application.exception.AuditoriaNotFoundException;
import com.ecoledger.auditoria.application.exception.RevisaoInvalidaException;
import com.ecoledger.auditoria.application.rules.RulesEngine;
import com.ecoledger.auditoria.application.service.AuditoriaEventPublisher;
import com.ecoledger.auditoria.domain.model.Evidencia;
import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;
import com.ecoledger.auditoria.domain.repository.AuditoriaRepository;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceImplTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @Mock
    private RulesEngine rulesEngine;

    @Mock
    private AuditoriaEventPublisher eventPublisher;

    @InjectMocks
    private AuditoriaServiceImpl auditoriaService;

    private UUID auditoriaId;
    private UUID movimentacaoId;
    private String producerId;

    @BeforeEach
    void setUp() {
        auditoriaId = UUID.randomUUID();
        movimentacaoId = UUID.randomUUID();
        producerId = "producer-123";
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("should return auditoria when found")
        void shouldReturnAuditoriaWhenFound() {
            // given
            RegistroAuditoria auditoria = createAuditoria(auditoriaId, movimentacaoId, producerId,
                    ResultadoAuditoria.APROVADO);
            when(auditoriaRepository.findById(auditoriaId)).thenReturn(Optional.of(auditoria));

            // when
            RegistroAuditoriaResponse response = auditoriaService.findById(auditoriaId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(auditoriaId);
            assertThat(response.movimentacaoId()).isEqualTo(movimentacaoId);
            assertThat(response.producerId()).isEqualTo(producerId);
            assertThat(response.resultado()).isEqualTo(ResultadoAuditoria.APROVADO);
        }

        @Test
        @DisplayName("should throw AuditoriaNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            // given
            when(auditoriaRepository.findById(auditoriaId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> auditoriaService.findById(auditoriaId))
                    .isInstanceOf(AuditoriaNotFoundException.class)
                    .hasMessageContaining(auditoriaId.toString());
        }
    }

    @Nested
    @DisplayName("findHistoricoByProducerId")
    class FindHistoricoTests {

        @Test
        @DisplayName("should return audit history for producer")
        void shouldReturnHistoricoForProducer() {
            // given
            RegistroAuditoria audit1 = createAuditoria(UUID.randomUUID(), movimentacaoId, producerId,
                    ResultadoAuditoria.APROVADO);
            RegistroAuditoria audit2 = createAuditoria(UUID.randomUUID(), UUID.randomUUID(), producerId,
                    ResultadoAuditoria.REPROVADO);
            
            when(auditoriaRepository.findByProducerIdOrderByProcessadoEmDesc(producerId))
                    .thenReturn(List.of(audit1, audit2));

            // when
            HistoricoAuditoriasResponse response = auditoriaService.findHistoricoByProducerId(producerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.items()).hasSize(2);
            assertThat(response.total()).isEqualTo(2);
        }

        @Test
        @DisplayName("should return empty history when no audits found")
        void shouldReturnEmptyHistorico() {
            // given
            when(auditoriaRepository.findByProducerIdOrderByProcessadoEmDesc(producerId))
                    .thenReturn(List.of());

            // when
            HistoricoAuditoriasResponse response = auditoriaService.findHistoricoByProducerId(producerId);

            // then
            assertThat(response.items()).isEmpty();
            assertThat(response.total()).isZero();
        }
    }

    @Nested
    @DisplayName("aplicarRevisao")
    class AplicarRevisaoTests {

        @Test
        @DisplayName("should apply revision successfully")
        void shouldApplyRevisionSuccessfully() {
            // given
            RegistroAuditoria auditoria = createAuditoria(auditoriaId, movimentacaoId, producerId,
                    ResultadoAuditoria.REQUER_REVISAO);
            RevisaoRequest request = new RevisaoRequest("auditor-1", ResultadoAuditoria.APROVADO, 
                    "Revisão manual aprovada");
            
            when(auditoriaRepository.findById(auditoriaId)).thenReturn(Optional.of(auditoria));
            when(auditoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // when
            RegistroAuditoriaResponse response = auditoriaService.aplicarRevisao(auditoriaId, request);

            // then
            assertThat(response.resultado()).isEqualTo(ResultadoAuditoria.APROVADO);
            assertThat(response.auditorId()).isEqualTo("auditor-1");
            assertThat(response.observacoes()).isEqualTo("Revisão manual aprovada");
            verify(eventPublisher).publishAuditoriaConcluida(any());
        }

        @Test
        @DisplayName("should throw exception when auditoria not found")
        void shouldThrowWhenAuditoriaNotFound() {
            // given
            RevisaoRequest request = new RevisaoRequest("auditor-1", ResultadoAuditoria.APROVADO, null);
            when(auditoriaRepository.findById(auditoriaId)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> auditoriaService.aplicarRevisao(auditoriaId, request))
                    .isInstanceOf(AuditoriaNotFoundException.class);
        }

        @Test
        @DisplayName("should throw exception when auditoria already revised")
        void shouldThrowWhenAlreadyRevised() {
            // given
            RegistroAuditoria auditoria = createAuditoria(auditoriaId, movimentacaoId, producerId,
                    ResultadoAuditoria.REQUER_REVISAO);
            auditoria.aplicarRevisao("previous-auditor", ResultadoAuditoria.APROVADO, null);
            
            RevisaoRequest request = new RevisaoRequest("auditor-1", ResultadoAuditoria.REPROVADO, null);
            when(auditoriaRepository.findById(auditoriaId)).thenReturn(Optional.of(auditoria));

            // when/then
            assertThatThrownBy(() -> auditoriaService.aplicarRevisao(auditoriaId, request))
                    .isInstanceOf(RevisaoInvalidaException.class)
                    .hasMessageContaining("já foi revisada");
        }

        @Test
        @DisplayName("should throw exception when trying to set REQUER_REVISAO")
        void shouldThrowWhenSettingRequerRevisao() {
            // given
            RevisaoRequest request = new RevisaoRequest("auditor-1", ResultadoAuditoria.REQUER_REVISAO, null);

            // when/then
            assertThatThrownBy(() -> auditoriaService.aplicarRevisao(auditoriaId, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("processarMovimentacaoCriada")
    class ProcessarMovimentacaoCriadaTests {

        @Test
        @DisplayName("should process movimentacao and create audit record")
        void shouldProcessMovimentacaoAndCreateAudit() {
            // given
            MovimentacaoCriadaEvent event = createMovimentacaoEvent(movimentacaoId, producerId);
            RulesEngine.AggregatedValidationResult validationResult = 
                    new RulesEngine.AggregatedValidationResult(
                            ResultadoAuditoria.APROVADO, List.of(), "1.0.0");
            
            when(auditoriaRepository.existsByMovimentacaoId(movimentacaoId)).thenReturn(false);
            when(rulesEngine.validate(event)).thenReturn(validationResult);
            when(auditoriaRepository.save(any())).thenAnswer(inv -> {
                RegistroAuditoria a = inv.getArgument(0);
                // Simulate ID generation
                return a;
            });

            // when
            auditoriaService.processarMovimentacaoCriada(event);

            // then
            ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
            verify(auditoriaRepository).save(captor.capture());
            
            RegistroAuditoria savedAuditoria = captor.getValue();
            assertThat(savedAuditoria.getMovimentacaoId()).isEqualTo(movimentacaoId);
            assertThat(savedAuditoria.getProducerId()).isEqualTo(producerId);
            assertThat(savedAuditoria.getResultado()).isEqualTo(ResultadoAuditoria.APROVADO);
            assertThat(savedAuditoria.getVersaoRegra()).isEqualTo("1.0.0");
            
            verify(eventPublisher).publishAuditoriaConcluida(any());
        }

        @Test
        @DisplayName("should skip processing when audit already exists (idempotency)")
        void shouldSkipWhenAuditAlreadyExists() {
            // given
            MovimentacaoCriadaEvent event = createMovimentacaoEvent(movimentacaoId, producerId);
            when(auditoriaRepository.existsByMovimentacaoId(movimentacaoId)).thenReturn(true);

            // when
            auditoriaService.processarMovimentacaoCriada(event);

            // then
            verify(auditoriaRepository, never()).save(any());
            verify(eventPublisher, never()).publishAuditoriaConcluida(any());
        }

        @Test
        @DisplayName("should create REPROVADO audit when validation fails")
        void shouldCreateReprovadoWhenValidationFails() {
            // given
            MovimentacaoCriadaEvent event = createMovimentacaoEvent(movimentacaoId, producerId);
            List<Evidencia> evidencias = List.of(
                    new Evidencia("QUANTITY_VALIDATION", "Quantidade acima do limite"));
            RulesEngine.AggregatedValidationResult validationResult = 
                    new RulesEngine.AggregatedValidationResult(
                            ResultadoAuditoria.REPROVADO, evidencias, "1.0.0");
            
            when(auditoriaRepository.existsByMovimentacaoId(movimentacaoId)).thenReturn(false);
            when(rulesEngine.validate(event)).thenReturn(validationResult);
            when(auditoriaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // when
            auditoriaService.processarMovimentacaoCriada(event);

            // then
            ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
            verify(auditoriaRepository).save(captor.capture());
            
            RegistroAuditoria savedAuditoria = captor.getValue();
            assertThat(savedAuditoria.getResultado()).isEqualTo(ResultadoAuditoria.REPROVADO);
            assertThat(savedAuditoria.getEvidencias()).hasSize(1);
        }
    }

    // Helper methods
    private RegistroAuditoria createAuditoria(UUID id, UUID movimentacaoId, String producerId,
                                               ResultadoAuditoria resultado) {
        RegistroAuditoria auditoria = new RegistroAuditoria(movimentacaoId, producerId, "1.0.0",
                resultado, List.of());
        // Use reflection to set ID for testing
        try {
            var field = RegistroAuditoria.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(auditoria, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return auditoria;
    }

    private MovimentacaoCriadaEvent createMovimentacaoEvent(UUID movimentacaoId, String producerId) {
        return new MovimentacaoCriadaEvent(
                movimentacaoId,
                producerId,
                "commodity-1",
                "ENTRADA",
                new BigDecimal("100"),
                "KG",
                OffsetDateTime.now(),
                -23.5505,
                -46.6333,
                OffsetDateTime.now(),
                List.of()
        );
    }
}
