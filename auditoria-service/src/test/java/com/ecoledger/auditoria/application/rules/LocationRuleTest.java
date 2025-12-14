package com.ecoledger.auditoria.application.rules;

import com.ecoledger.auditoria.config.RulesProperties;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LocationRuleTest {

    @Test
    @DisplayName("should pass when location is not required")
    void shouldPassWhenLocationNotRequired() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null,
                new RulesProperties.LocationRules(List.of(), false), null);
        LocationRule rule = new LocationRule(properties);
        MovimentacaoCriadaEvent event = createEventWithCoords(null, null);

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("should pass when location is required and coordinates provided")
    void shouldPassWhenRequiredAndProvided() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null,
                new RulesProperties.LocationRules(List.of(), true), null);  // validateCoordinates=true
        LocationRule rule = new LocationRule(properties);
        MovimentacaoCriadaEvent event = createEventWithCoords(-23.5505, -46.6333);

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("should fail when location is required but not provided")
    void shouldFailWhenRequiredButNotProvided() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null,
                new RulesProperties.LocationRules(List.of(), true), null);  // validateCoordinates=true
        LocationRule rule = new LocationRule(properties);
        MovimentacaoCriadaEvent event = createEventWithCoords(null, null);

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isFalse();
        assertThat(result.evidencias().get(0).getTipo()).isEqualTo("LOCATION_VALIDATION");
        assertThat(result.evidencias().get(0).getDetalhe()).contains("obrigatórias");
    }

    @Test
    @DisplayName("should fail when only latitude is provided")
    void shouldFailWhenOnlyLatitudeProvided() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null,
                new RulesProperties.LocationRules(List.of(), true), null);  // validateCoordinates=true
        LocationRule rule = new LocationRule(properties);
        MovimentacaoCriadaEvent event = createEventWithCoords(-23.5505, null);

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isFalse();
    }

    @Test
    @DisplayName("should pass when location config is null")
    void shouldPassWhenLocationConfigIsNull() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null, null, null);
        LocationRule rule = new LocationRule(properties);
        MovimentacaoCriadaEvent event = createEventWithCoords(null, null);

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("should return correct rule name")
    void shouldReturnCorrectName() {
        RulesProperties properties = new RulesProperties("1.0.0", null, null, null);
        LocationRule rule = new LocationRule(properties);
        assertThat(rule.getName()).isEqualTo("LOCATION_VALIDATION");
    }

    private MovimentacaoCriadaEvent createEventWithCoords(Double lat, Double lon) {
        return new MovimentacaoCriadaEvent(
                UUID.randomUUID(),
                "producer-1",
                "commodity-1",
                "ENTRADA",
                new BigDecimal("100"),
                "KG",
                OffsetDateTime.now(),
                lat,
                lon,
                OffsetDateTime.now(),
                List.of()
        );
    }
}