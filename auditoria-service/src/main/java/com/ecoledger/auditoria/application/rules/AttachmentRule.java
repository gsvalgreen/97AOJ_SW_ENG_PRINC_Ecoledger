package com.ecoledger.auditoria.application.rules;

import com.ecoledger.auditoria.config.RulesProperties;
import com.ecoledger.auditoria.domain.model.Evidencia;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rule that validates attachments are present and valid.
 */
@Component
public class AttachmentRule implements ValidationRule {

    private final RulesProperties rulesProperties;

    public AttachmentRule(RulesProperties rulesProperties) {
        this.rulesProperties = rulesProperties;
    }

    @Override
    public String getName() {
        return "ATTACHMENT_VALIDATION";
    }

    @Override
    public ValidationResult validate(MovimentacaoCriadaEvent event) {
        RulesProperties.AttachmentRules attachmentRules = rulesProperties.attachments();
        
        // If attachments are not required, validation passes
        if (!attachmentRules.required()) {
            return ValidationResult.success();
        }

        List<MovimentacaoCriadaEvent.Anexo> anexos = event.anexos();
        List<Evidencia> failures = new ArrayList<>();

        // Check minimum count
        int anexoCount = anexos != null ? anexos.size() : 0;
        if (anexoCount < attachmentRules.minCount()) {
            failures.add(new Evidencia("ATTACHMENT_COUNT",
                    "Número de anexos (%d) é menor que o mínimo exigido (%d)"
                            .formatted(anexoCount, attachmentRules.minCount())));
        }

        // Check required types
        List<String> requiredTypes = attachmentRules.requiredTypes();
        if (requiredTypes != null && !requiredTypes.isEmpty() && anexos != null) {
            Set<String> presentTypes = anexos.stream()
                    .map(MovimentacaoCriadaEvent.Anexo::tipo)
                    .collect(Collectors.toSet());

            List<String> missingTypes = requiredTypes.stream()
                    .filter(type -> !presentTypes.contains(type))
                    .toList();

            if (!missingTypes.isEmpty()) {
                failures.add(new Evidencia("ATTACHMENT_TYPES",
                        "Tipos de anexo obrigatórios ausentes: %s".formatted(String.join(", ", missingTypes))));
            }
        }

        if (!failures.isEmpty()) {
            return ValidationResult.failure(failures);
        }

        return ValidationResult.success();
    }
}
