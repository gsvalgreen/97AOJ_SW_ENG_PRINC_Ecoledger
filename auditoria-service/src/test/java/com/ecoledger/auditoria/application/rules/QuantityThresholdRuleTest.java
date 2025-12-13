package com.ecoledger.auditoria.application.rules;

import com.ecoledger.auditoria.config.RulesProperties;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuantityThresholdRuleTest {

    private QuantityThresholdRule rule;

    @BeforeEach
    void setUp() {
        RulesProperties properties = new RulesProperties(
                "1.0.0",
                new RulesProperties.QuantityRules(new BigDecimal("1000"), BigDecimal.ONE),
                null,
                null
        );
        rule = new QuantityThresholdRule(properties);
    }

    @Test
    @DisplayName("should pass when quantity is within threshold")
    void shouldPassWhenQuantityWithinThreshold() {
        // given
        MovimentacaoCriadaEvent event = createEvent(new BigDecimal("500"));

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isTrue();
        assertThat(result.evidencias()).isEmpty();
    }

    @Test
    @DisplayName("should fail when quantity exceeds max threshold")
    void shouldFailWhenQuantityExceedsMax() {
        // given
        MovimentacaoCriadaEvent event = createEvent(new BigDecimal("1500"));

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isFalse();
        assertThat(result.evidencias()).hasSize(1);
        assertThat(result.evidencias().get(0).getTipo()).isEqualTo("QUANTITY_VALIDATION");
        assertThat(result.evidencias().get(0).getDetalhe()).contains("acima do limite");
    }

    @Test
    @DisplayName("should fail when quantity is below min threshold")
    void shouldFailWhenQuantityBelowMin() {
        // given
        MovimentacaoCriadaEvent event = createEvent(new BigDecimal("0"));

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isFalse();
        assertThat(result.evidencias().get(0).getDetalhe()).contains("abaixo do limite");
    }

    @Test
    @DisplayName("should fail when quantity is null")
    void shouldFailWhenQuantityIsNull() {
        // given
        MovimentacaoCriadaEvent event = createEvent(null);

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isFalse();
        assertThat(result.evidencias().get(0).getDetalhe()).contains("não informada");
    }

    @Test
    @DisplayName("should pass when quantity equals max threshold")
    void shouldPassWhenQuantityEqualsMax() {
        // given
        MovimentacaoCriadaEvent event = createEvent(new BigDecimal("1000"));

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("should return correct rule name")
    void shouldReturnCorrectName() {
        assertThat(rule.getName()).isEqualTo("QUANTITY_THRESHOLD");
    }

    private MovimentacaoCriadaEvent createEvent(BigDecimal quantidade) {
        return new MovimentacaoCriadaEvent(
                UUID.randomUUID(),
                "producer-1",
                "commodity-1",
                "ENTRADA",
                quantidade,
                "KG",
                OffsetDateTime.now(),
                -23.5505,
                -46.6333,
                OffsetDateTime.now(),
                List.of()
        );
    }
}
