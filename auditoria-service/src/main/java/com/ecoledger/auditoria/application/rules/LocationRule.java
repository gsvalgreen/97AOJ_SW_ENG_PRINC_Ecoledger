package com.ecoledger.auditoria.application.rules;

import com.ecoledger.auditoria.config.RulesProperties;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.springframework.stereotype.Component;

/**
 * Rule that validates the location coordinates are provided when required.
 */
@Component
public class LocationRule implements ValidationRule {

    private final RulesProperties rulesProperties;

    public LocationRule(RulesProperties rulesProperties) {
        this.rulesProperties = rulesProperties;
    }

    @Override
    public String getName() {
        return "LOCATION_VALIDATION";
    }

    @Override
    public ValidationResult validate(MovimentacaoCriadaEvent event) {
        // Check if location validation is required
        boolean validateCoordinates = rulesProperties.location() != null 
                && rulesProperties.location().validateCoordinates();
        
        // Check if coordinates are provided
        boolean hasCoordinates = event.latitude() != null && event.longitude() != null;
        
        if (validateCoordinates && !hasCoordinates) {
            return ValidationResult.failure("LOCATION_VALIDATION", 
                    "Coordenadas de localização são obrigatórias");
        }

        return ValidationResult.success();
    }
}
