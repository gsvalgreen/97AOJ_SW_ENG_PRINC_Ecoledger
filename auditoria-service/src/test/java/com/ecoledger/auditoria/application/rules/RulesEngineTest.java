package com.ecoledger.auditoria.application.rules;

import com.ecoledger.auditoria.config.RulesProperties;
import com.ecoledger.auditoria.domain.model.Evidencia;
import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RulesEngineTest {

    private RulesEngine rulesEngine;
    private ValidationRule rule1;
    private ValidationRule rule2;
    private RulesProperties rulesProperties;

    @BeforeEach
    void setUp() {
        rulesProperties = new RulesProperties("1.0.0", null, null, null);
        rule1 = mock(ValidationRule.class);
        rule2 = mock(ValidationRule.class);
        when(rule1.getName()).thenReturn("RULE_1");
        when(rule2.getName()).thenReturn("RULE_2");
        rulesEngine = new RulesEngine(List.of(rule1, rule2), rulesProperties);
    }

    @Test
    @DisplayName("should return APROVADO when all rules pass")
    void shouldReturnAprovadoWhenAllPass() {
        // given
        MovimentacaoCriadaEvent event = createEvent();
        when(rule1.validate(event)).thenReturn(ValidationRule.ValidationResult.success());
        when(rule2.validate(event)).thenReturn(ValidationRule.ValidationResult.success());

        // when
        RulesEngine.AggregatedValidationResult result = rulesEngine.validate(event);

        // then
        assertThat(result.resultado()).isEqualTo(ResultadoAuditoria.APROVADO);
        assertThat(result.evidencias()).isEmpty();
        assertThat(result.versaoRegra()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("should return REPROVADO when any rule fails")
    void shouldReturnReprovadoWhenAnyFails() {
        // given
        MovimentacaoCriadaEvent event = createEvent();
        when(rule1.validate(event)).thenReturn(ValidationRule.ValidationResult.success());
        when(rule2.validate(event)).thenReturn(
                ValidationRule.ValidationResult.failure("TEST", "Test failure"));

        // when
        RulesEngine.AggregatedValidationResult result = rulesEngine.validate(event);

        // then
        assertThat(result.resultado()).isEqualTo(ResultadoAuditoria.REPROVADO);
        assertThat(result.evidencias()).hasSize(1);
    }

    @Test
    @DisplayName("should aggregate all evidences from failed rules")
    void shouldAggregateAllEvidences() {
        // given
        MovimentacaoCriadaEvent event = createEvent();
        when(rule1.validate(event)).thenReturn(
                ValidationRule.ValidationResult.failure("FAIL_1", "Failure 1"));
        when(rule2.validate(event)).thenReturn(
                ValidationRule.ValidationResult.failure(List.of(
                        new Evidencia("FAIL_2A", "Failure 2A"),
                        new Evidencia("FAIL_2B", "Failure 2B")
                )));

        // when
        RulesEngine.AggregatedValidationResult result = rulesEngine.validate(event);

        // then
        assertThat(result.resultado()).isEqualTo(ResultadoAuditoria.REPROVADO);
        assertThat(result.evidencias()).hasSize(3);
    }

    @Test
    @DisplayName("should return REQUER_REVISAO when rule throws exception")
    void shouldReturnRequerRevisaoOnException() {
        // given
        MovimentacaoCriadaEvent event = createEvent();
        when(rule1.validate(event)).thenThrow(new RuntimeException("Unexpected error"));
        when(rule2.validate(event)).thenReturn(ValidationRule.ValidationResult.success());

        // when
        RulesEngine.AggregatedValidationResult result = rulesEngine.validate(event);

        // then
        assertThat(result.resultado()).isEqualTo(ResultadoAuditoria.REQUER_REVISAO);
        assertThat(result.evidencias()).anyMatch(e -> e.getTipo().equals("RULE_ERROR"));
    }

    @Test
    @DisplayName("should continue executing remaining rules after failure")
    void shouldContinueAfterFailure() {
        // given
        MovimentacaoCriadaEvent event = createEvent();
        when(rule1.validate(event)).thenReturn(
                ValidationRule.ValidationResult.failure("FAIL", "First failure"));
        when(rule2.validate(event)).thenReturn(ValidationRule.ValidationResult.success());

        // when
        rulesEngine.validate(event);

        // then
        verify(rule1).validate(event);
        verify(rule2).validate(event);
    }

    @Test
    @DisplayName("should return configured version")
    void shouldReturnConfiguredVersion() {
        assertThat(rulesEngine.getVersion()).isEqualTo("1.0.0");
    }

    private MovimentacaoCriadaEvent createEvent() {
        return new MovimentacaoCriadaEvent(
                UUID.randomUUID(),
                "producer-1",
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
