package com.ecoledger.auditoria.application.dto;

import com.ecoledger.auditoria.domain.model.Evidencia;
import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RegistroAuditoriaResponseTest {

    @Test
    @DisplayName("should map from entity correctly")
    void shouldMapFromEntity() {
        // given
        UUID movimentacaoId = UUID.randomUUID();
        String producerId = "producer-1";
        List<Evidencia> evidencias = List.of(
                new Evidencia("TYPE_1", "Detail 1"),
                new Evidencia("TYPE_2", "Detail 2")
        );
        
        RegistroAuditoria auditoria = new RegistroAuditoria(
                movimentacaoId, producerId, "1.0.0",
                ResultadoAuditoria.APROVADO, evidencias);

        // when
        RegistroAuditoriaResponse response = RegistroAuditoriaResponse.from(auditoria);

        // then
        assertThat(response.movimentacaoId()).isEqualTo(movimentacaoId);
        assertThat(response.producerId()).isEqualTo(producerId);
        assertThat(response.versaoRegra()).isEqualTo("1.0.0");
        assertThat(response.resultado()).isEqualTo(ResultadoAuditoria.APROVADO);
        assertThat(response.evidencias()).hasSize(2);
        assertThat(response.evidencias().get(0).tipo()).isEqualTo("TYPE_1");
        assertThat(response.processadoEm()).isNotNull();
    }

    @Test
    @DisplayName("should map empty evidencias list")
    void shouldMapEmptyEvidencias() {
        // given
        RegistroAuditoria auditoria = new RegistroAuditoria(
                UUID.randomUUID(), "producer-1", "1.0.0",
                ResultadoAuditoria.APROVADO, List.of());

        // when
        RegistroAuditoriaResponse response = RegistroAuditoriaResponse.from(auditoria);

        // then
        assertThat(response.evidencias()).isEmpty();
    }

    @Test
    @DisplayName("should map revisao fields when present")
    void shouldMapRevisaoFields() {
        // given
        RegistroAuditoria auditoria = new RegistroAuditoria(
                UUID.randomUUID(), "producer-1", "1.0.0",
                ResultadoAuditoria.REQUER_REVISAO, List.of());
        auditoria.aplicarRevisao("auditor-1", ResultadoAuditoria.APROVADO, "Manual approval");

        // when
        RegistroAuditoriaResponse response = RegistroAuditoriaResponse.from(auditoria);

        // then
        assertThat(response.resultado()).isEqualTo(ResultadoAuditoria.APROVADO);
        assertThat(response.auditorId()).isEqualTo("auditor-1");
        assertThat(response.observacoes()).isEqualTo("Manual approval");
        assertThat(response.revisadoEm()).isNotNull();
    }
}
