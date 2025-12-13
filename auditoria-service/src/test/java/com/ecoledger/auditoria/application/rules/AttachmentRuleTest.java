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

class AttachmentRuleTest {

    @Test
    @DisplayName("should pass when attachments are not required")
    void shouldPassWhenAttachmentsNotRequired() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null, null,
                new RulesProperties.AttachmentRules(false, 0, List.of()));
        AttachmentRule rule = new AttachmentRule(properties);
        MovimentacaoCriadaEvent event = createEvent(List.of());

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("should pass when minimum attachments count is met")
    void shouldPassWhenMinCountMet() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null, null,
                new RulesProperties.AttachmentRules(true, 1, List.of()));
        AttachmentRule rule = new AttachmentRule(properties);
        MovimentacaoCriadaEvent event = createEvent(List.of(
                new MovimentacaoCriadaEvent.Anexo("PHOTO", "http://s3/photo.jpg", "hash1")
        ));

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("should fail when minimum attachments count is not met")
    void shouldFailWhenMinCountNotMet() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null, null,
                new RulesProperties.AttachmentRules(true, 2, List.of()));
        AttachmentRule rule = new AttachmentRule(properties);
        MovimentacaoCriadaEvent event = createEvent(List.of(
                new MovimentacaoCriadaEvent.Anexo("PHOTO", "http://s3/photo.jpg", "hash1")
        ));

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isFalse();
        assertThat(result.evidencias().stream()
                .anyMatch(e -> e.getTipo().equals("ATTACHMENT_COUNT"))).isTrue();
    }

    @Test
    @DisplayName("should pass when required attachment types are present")
    void shouldPassWhenRequiredTypesPresent() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null, null,
                new RulesProperties.AttachmentRules(true, 0, List.of("PHOTO", "DOCUMENT")));
        AttachmentRule rule = new AttachmentRule(properties);
        MovimentacaoCriadaEvent event = createEvent(List.of(
                new MovimentacaoCriadaEvent.Anexo("PHOTO", "http://s3/photo.jpg", "hash1"),
                new MovimentacaoCriadaEvent.Anexo("DOCUMENT", "http://s3/doc.pdf", "hash2")
        ));

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("should fail when required attachment types are missing")
    void shouldFailWhenRequiredTypesMissing() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null, null,
                new RulesProperties.AttachmentRules(true, 0, List.of("PHOTO", "DOCUMENT")));
        AttachmentRule rule = new AttachmentRule(properties);
        MovimentacaoCriadaEvent event = createEvent(List.of(
                new MovimentacaoCriadaEvent.Anexo("PHOTO", "http://s3/photo.jpg", "hash1")
        ));

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isFalse();
        assertThat(result.evidencias().stream()
                .anyMatch(e -> e.getTipo().equals("ATTACHMENT_TYPES"))).isTrue();
        assertThat(result.evidencias().stream()
                .anyMatch(e -> e.getDetalhe().contains("DOCUMENT"))).isTrue();
    }

    @Test
    @DisplayName("should collect multiple failures")
    void shouldCollectMultipleFailures() {
        // given
        RulesProperties properties = new RulesProperties("1.0.0", null, null,
                new RulesProperties.AttachmentRules(true, 3, List.of("PHOTO", "DOCUMENT")));
        AttachmentRule rule = new AttachmentRule(properties);
        MovimentacaoCriadaEvent event = createEvent(List.of(
                new MovimentacaoCriadaEvent.Anexo("VIDEO", "http://s3/video.mp4", "hash1")
        ));

        // when
        ValidationRule.ValidationResult result = rule.validate(event);

        // then
        assertThat(result.passed()).isFalse();
        assertThat(result.evidencias()).hasSize(2); // count failure + types failure
    }

    @Test
    @DisplayName("should return correct rule name")
    void shouldReturnCorrectName() {
        RulesProperties properties = new RulesProperties("1.0.0", null, null, null);
        AttachmentRule rule = new AttachmentRule(properties);
        assertThat(rule.getName()).isEqualTo("ATTACHMENT_VALIDATION");
    }

    private MovimentacaoCriadaEvent createEvent(List<MovimentacaoCriadaEvent.Anexo> anexos) {
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
                anexos
        );
    }
}
