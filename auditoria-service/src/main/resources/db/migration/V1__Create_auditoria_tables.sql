-- V1__Create_auditoria_tables.sql
-- Initial schema for auditoria-service

-- Main audit records table (append-only)
CREATE TABLE registro_auditorias (
    id UUID PRIMARY KEY,
    movimentacao_id UUID NOT NULL,
    producer_id VARCHAR(255) NOT NULL,
    versao_regra VARCHAR(50) NOT NULL,
    resultado VARCHAR(50) NOT NULL,
    processado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    auditor_id VARCHAR(255),
    observacoes VARCHAR(4000),
    revisado_em TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_resultado CHECK (resultado IN ('APROVADO', 'REPROVADO', 'REQUER_REVISAO'))
);

-- Evidencias collection table
CREATE TABLE auditoria_evidencias (
    auditoria_id UUID NOT NULL,
    tipo VARCHAR(100) NOT NULL,
    detalhe VARCHAR(2000) NOT NULL,
    CONSTRAINT fk_auditoria_evidencias FOREIGN KEY (auditoria_id) 
        REFERENCES registro_auditorias(id) ON DELETE CASCADE
);

-- Indexes for common queries
CREATE INDEX idx_auditoria_movimentacao ON registro_auditorias(movimentacao_id);
CREATE INDEX idx_auditoria_producer ON registro_auditorias(producer_id);
CREATE INDEX idx_auditoria_processado_em ON registro_auditorias(processado_em DESC);
CREATE INDEX idx_auditoria_resultado ON registro_auditorias(resultado);
CREATE INDEX idx_auditoria_evidencias_auditoria ON auditoria_evidencias(auditoria_id);

-- Comments
COMMENT ON TABLE registro_auditorias IS 'Audit records for movimentacao validation (append-only)';
COMMENT ON TABLE auditoria_evidencias IS 'Evidence collected during audit validation';
COMMENT ON COLUMN registro_auditorias.versao_regra IS 'Version of validation rules used';
COMMENT ON COLUMN registro_auditorias.processado_em IS 'Timestamp when automatic validation was performed';
COMMENT ON COLUMN registro_auditorias.revisado_em IS 'Timestamp when manual revision was applied';
