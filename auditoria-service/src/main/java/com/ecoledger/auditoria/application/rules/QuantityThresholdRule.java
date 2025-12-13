package com.ecoledger.auditoria.application.rules;

import com.ecoledger.auditoria.config.RulesProperties;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Rule that validates the quantity is within acceptable thresholds.
 */
@Component
public class QuantityThresholdRule implements ValidationRule {

    private final RulesProperties rulesProperties;

    public QuantityThresholdRule(RulesProperties rulesProperties) {
        this.rulesProperties = rulesProperties;
    }

    @Override
    public String getName() {
        return "QUANTITY_THRESHOLD";
    }

    @Override
    public ValidationResult validate(MovimentacaoCriadaEvent event) {
        BigDecimal quantidade = event.quantidade();
        
        if (quantidade == null) {
            return ValidationResult.failure("QUANTITY_VALIDATION", "Quantidade não informada");
        }

        BigDecimal minThreshold = rulesProperties.quantity().minThreshold();
        BigDecimal maxThreshold = rulesProperties.quantity().maxThreshold();

        if (quantidade.compareTo(minThreshold) < 0) {
            return ValidationResult.failure("QUANTITY_VALIDATION",
                    "Quantidade %s está abaixo do limite mínimo de %s".formatted(quantidade, minThreshold));
        }

        if (quantidade.compareTo(maxThreshold) > 0) {
            return ValidationResult.failure("QUANTITY_VALIDATION",
                    "Quantidade %s está acima do limite máximo de %s".formatted(quantidade, maxThreshold));
        }

        return ValidationResult.success();
    }
}
