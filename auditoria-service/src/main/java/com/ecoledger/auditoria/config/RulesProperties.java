package com.ecoledger.auditoria.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Configuration properties for audit rules.
 */
@ConfigurationProperties(prefix = "auditoria.rules")
public record RulesProperties(
    String version,
    QuantityRules quantity,
    LocationRules location,
    AttachmentRules attachments
) {
    public record QuantityRules(
        BigDecimal maxThreshold,
        BigDecimal minThreshold
    ) {
        public QuantityRules {
            if (maxThreshold == null) maxThreshold = new BigDecimal("10000");
            if (minThreshold == null) minThreshold = BigDecimal.ZERO;
        }
    }

    public record LocationRules(
        List<String> allowedRegions,
        boolean validateCoordinates
    ) {
        public LocationRules {
            if (allowedRegions == null) allowedRegions = List.of();
        }
    }

    public record AttachmentRules(
        boolean required,
        int minCount,
        List<String> requiredTypes
    ) {
        public AttachmentRules {
            if (requiredTypes == null) requiredTypes = List.of();
        }
    }

    public RulesProperties {
        if (version == null || version.isBlank()) version = "1.0.0";
        if (quantity == null) quantity = new QuantityRules(null, null);
        if (location == null) location = new LocationRules(null, false);
        if (attachments == null) attachments = new AttachmentRules(false, 0, null);
    }
}
