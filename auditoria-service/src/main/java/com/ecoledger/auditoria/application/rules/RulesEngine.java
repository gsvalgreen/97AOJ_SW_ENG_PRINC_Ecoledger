package com.ecoledger.auditoria.application.rules;

import com.ecoledger.auditoria.config.RulesProperties;
import com.ecoledger.auditoria.domain.model.Evidencia;
import com.ecoledger.auditoria.domain.model.ResultadoAuditoria;
import com.ecoledger.auditoria.messaging.event.MovimentacaoCriadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Engine that orchestrates the execution of validation rules.
 * All rules are pluggable and the engine tracks the version of rules used.
 */
@Service
public class RulesEngine {

    private static final Logger log = LoggerFactory.getLogger(RulesEngine.class);

    private final List<ValidationRule> rules;
    private final RulesProperties rulesProperties;

    public RulesEngine(List<ValidationRule> rules, RulesProperties rulesProperties) {
        this.rules = rules;
        this.rulesProperties = rulesProperties;
        log.info("RulesEngine initialized with {} rules, version: {}", rules.size(), getVersion());
    }

    /**
     * Returns the current version of the rules configuration.
     */
    public String getVersion() {
        return rulesProperties.version();
    }

    /**
     * Executes all validation rules against the event.
     * 
     * @param event the movimentacao event to validate
     * @return the aggregated validation result
     */
    public AggregatedValidationResult validate(MovimentacaoCriadaEvent event) {
        log.debug("Validating movimentacao {} with rules version {}", event.movimentacaoId(), getVersion());
        
        List<Evidencia> allEvidencias = new ArrayList<>();
        boolean allPassed = true;

        for (ValidationRule rule : rules) {
            try {
                ValidationRule.ValidationResult result = rule.validate(event);
                
                if (!result.passed()) {
                    allPassed = false;
                    allEvidencias.addAll(result.evidencias());
                    log.debug("Rule {} failed for movimentacao {}: {}", 
                            rule.getName(), event.movimentacaoId(), result.evidencias());
                } else {
                    log.debug("Rule {} passed for movimentacao {}", rule.getName(), event.movimentacaoId());
                }
            } catch (Exception e) {
                log.error("Error executing rule {} for movimentacao {}", 
                        rule.getName(), event.movimentacaoId(), e);
                allPassed = false;
                allEvidencias.add(new Evidencia("RULE_ERROR", 
                        "Erro ao executar regra %s: %s".formatted(rule.getName(), e.getMessage())));
            }
        }

        ResultadoAuditoria resultado = determineResult(allPassed, allEvidencias);
        
        log.info("Validation completed for movimentacao {}: result={}, evidencias={}", 
                event.movimentacaoId(), resultado, allEvidencias.size());
        
        return new AggregatedValidationResult(resultado, allEvidencias, getVersion());
    }

    private ResultadoAuditoria determineResult(boolean allPassed, List<Evidencia> evidencias) {
        if (allPassed) {
            return ResultadoAuditoria.APROVADO;
        }
        
        // If there are errors or critical failures, mark as requiring review
        boolean hasErrors = evidencias.stream()
                .anyMatch(e -> "RULE_ERROR".equals(e.getTipo()));
        
        if (hasErrors) {
            return ResultadoAuditoria.REQUER_REVISAO;
        }
        
        return ResultadoAuditoria.REPROVADO;
    }

    /**
     * Aggregated result from running all validation rules.
     */
    public record AggregatedValidationResult(
        ResultadoAuditoria resultado,
        List<Evidencia> evidencias,
        String versaoRegra
    ) {}
}
