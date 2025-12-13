package com.ecoledger.auditoria.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistroAuditoriaTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create with valid parameters")
        void shouldCreateWithValidParams() {
            // given
            UUID movimentacaoId = UUID.randomUUID();
            String producerId = "producer-1";
            List<Evidencia> evidencias = List.of(new Evidencia("TYPE", "Detail"));

            // when
            RegistroAuditoria auditoria = new RegistroAuditoria(
                    movimentacaoId, producerId, "1.0.0",
                    ResultadoAuditoria.APROVADO, evidencias);

            // then
            assertThat(auditoria.getMovimentacaoId()).isEqualTo(movimentacaoId);
            assertThat(auditoria.getProducerId()).isEqualTo(producerId);
            assertThat(auditoria.getVersaoRegra()).isEqualTo("1.0.0");
            assertThat(auditoria.getResultado()).isEqualTo(ResultadoAuditoria.APROVADO);
            assertThat(auditoria.getEvidencias()).hasSize(1);
            assertThat(auditoria.getProcessadoEm()).isNotNull();
            assertThat(auditoria.foiRevisado()).isFalse();
        }

        @Test
        @DisplayName("should throw when movimentacaoId is null")
        void shouldThrowWhenMovimentacaoIdNull() {
            assertThatThrownBy(() -> new RegistroAuditoria(
                    null, "producer", "1.0.0", ResultadoAuditoria.APROVADO, List.of()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("movimentacaoId");
        }

        @Test
        @DisplayName("should throw when producerId is null")
        void shouldThrowWhenProducerIdNull() {
            assertThatThrownBy(() -> new RegistroAuditoria(
                    UUID.randomUUID(), null, "1.0.0", ResultadoAuditoria.APROVADO, List.of()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("producerId");
        }

        @Test
        @DisplayName("should handle null evidencias list")
        void shouldHandleNullEvidencias() {
            // when
            RegistroAuditoria auditoria = new RegistroAuditoria(
                    UUID.randomUUID(), "producer", "1.0.0",
                    ResultadoAuditoria.APROVADO, null);

            // then
            assertThat(auditoria.getEvidencias()).isEmpty();
        }
    }

    @Nested
    @DisplayName("aplicarRevisao")
    class AplicarRevisaoTests {

        @Test
        @DisplayName("should apply revision successfully")
        void shouldApplyRevision() {
            // given
            RegistroAuditoria auditoria = new RegistroAuditoria(
                    UUID.randomUUID(), "producer", "1.0.0",
                    ResultadoAuditoria.REQUER_REVISAO, List.of());

            // when
            auditoria.aplicarRevisao("auditor-1", ResultadoAuditoria.APROVADO, "All good");

            // then
            assertThat(auditoria.getResultado()).isEqualTo(ResultadoAuditoria.APROVADO);
            assertThat(auditoria.getAuditorId()).isEqualTo("auditor-1");
            assertThat(auditoria.getObservacoes()).isEqualTo("All good");
            assertThat(auditoria.getRevisadoEm()).isNotNull();
            assertThat(auditoria.foiRevisado()).isTrue();
        }

        @Test
        @DisplayName("should throw when setting REQUER_REVISAO")
        void shouldThrowWhenSettingRequerRevisao() {
            // given
            RegistroAuditoria auditoria = new RegistroAuditoria(
                    UUID.randomUUID(), "producer", "1.0.0",
                    ResultadoAuditoria.REQUER_REVISAO, List.of());

            // when/then
            assertThatThrownBy(() -> auditoria.aplicarRevisao(
                    "auditor", ResultadoAuditoria.REQUER_REVISAO, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("REQUER_REVISAO");
        }

        @Test
        @DisplayName("should throw when auditorId is null")
        void shouldThrowWhenAuditorIdNull() {
            // given
            RegistroAuditoria auditoria = new RegistroAuditoria(
                    UUID.randomUUID(), "producer", "1.0.0",
                    ResultadoAuditoria.REQUER_REVISAO, List.of());

            // when/then
            assertThatThrownBy(() -> auditoria.aplicarRevisao(
                    null, ResultadoAuditoria.APROVADO, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should allow null observacoes")
        void shouldAllowNullObservacoes() {
            // given
            RegistroAuditoria auditoria = new RegistroAuditoria(
                    UUID.randomUUID(), "producer", "1.0.0",
                    ResultadoAuditoria.REQUER_REVISAO, List.of());

            // when
            auditoria.aplicarRevisao("auditor", ResultadoAuditoria.APROVADO, null);

            // then
            assertThat(auditoria.getObservacoes()).isNull();
        }
    }

    @Nested
    @DisplayName("Evidencias immutability")
    class EvidenciasImmutabilityTests {

        @Test
        @DisplayName("should return unmodifiable evidencias list")
        void shouldReturnUnmodifiableList() {
            // given
            RegistroAuditoria auditoria = new RegistroAuditoria(
                    UUID.randomUUID(), "producer", "1.0.0",
                    ResultadoAuditoria.APROVADO, List.of(new Evidencia("TYPE", "Detail")));

            // when/then
            assertThatThrownBy(() -> auditoria.getEvidencias().add(
                    new Evidencia("NEW", "New")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
