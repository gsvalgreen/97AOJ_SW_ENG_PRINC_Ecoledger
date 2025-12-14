CREATE TABLE selos (
    producer_id VARCHAR(64) PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    nivel VARCHAR(20),
    pontuacao INTEGER NOT NULL,
    ultimo_resultado_auditoria VARCHAR(30),
    ultimo_check TIMESTAMP NOT NULL,
    versao_regra VARCHAR(50),
    ultima_auditoria_id UUID,
    expiracao_em TIMESTAMP
);

CREATE TABLE selo_motivos (
    selo_producer_id VARCHAR(64) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    CONSTRAINT fk_selo_motivos_selo FOREIGN KEY (selo_producer_id) REFERENCES selos(producer_id) ON DELETE CASCADE
);

CREATE TABLE alteracoes_selo (
    id UUID PRIMARY KEY,
    producer_id VARCHAR(64) NOT NULL,
    de_status VARCHAR(20),
    para_status VARCHAR(20) NOT NULL,
    motivo VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    evidencia VARCHAR(500)
);

CREATE INDEX idx_alteracoes_selo_producer ON alteracoes_selo(producer_id, created_at DESC);
