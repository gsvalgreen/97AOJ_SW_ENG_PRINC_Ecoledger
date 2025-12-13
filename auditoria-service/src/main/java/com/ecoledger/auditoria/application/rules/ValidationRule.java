package com.ecoledger.auditoria.application.rules;

import com.ecoledger.auditoria.domain.model.Evidencia;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;

import java.util.List;

/**
 * Interface for validation rules that can be applied to movimentacao events.
 * Rules are pluggable and versionable.
 */
public interface ValidationRule {

    /**
     * Returns the name/identifier of this rule.
     */
    String getName();

    /**
     * Validates the event and returns validation results.
     * 
     * @param event the movimentacao event to validate
     * @return validation result containing success status and any evidence
     */
    ValidationResult validate(MovimentacaoCriadaEvent event);

    /**
     * Result of a validation rule execution.
     */
    record ValidationResult(
        boolean passed,
        List<Evidencia> evidencias
    ) {
        public static ValidationResult success() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult failure(String tipo, String detalhe) {
            return new ValidationResult(false, List.of(new Evidencia(tipo, detalhe)));
        }

        public static ValidationResult failure(List<Evidencia> evidencias) {
            return new ValidationResult(false, evidencias);
        }

        public static ValidationResult requiresReview(String tipo, String detalhe) {
            return new ValidationResult(false, List.of(new Evidencia(tipo, detalhe)));
        }
    }
}
