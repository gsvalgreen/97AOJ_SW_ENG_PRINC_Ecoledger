package com.ecoledger.auditoria.domain.model;

/**
 * Enum representing the possible results of an audit.
 */
public enum ResultadoAuditoria {
    /**
     * The movimentacao passed all validation rules.
     */
    APROVADO,
    
    /**
     * The movimentacao failed one or more validation rules.
     */
    REPROVADO,
    
    /**
     * The movimentacao requires manual review by an auditor.
     */
    REQUER_REVISAO
}
