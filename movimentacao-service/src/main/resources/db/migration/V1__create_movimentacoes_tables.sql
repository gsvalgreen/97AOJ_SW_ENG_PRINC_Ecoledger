CREATE TABLE movimentacoes (
    id UUID PRIMARY KEY,
    producer_id VARCHAR(36) NOT NULL,
    commodity_id VARCHAR(36) NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    quantidade NUMERIC(19,4) NOT NULL,
    unidade VARCHAR(10) NOT NULL,
    registro_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE movimentacao_anexos (
    id UUID PRIMARY KEY,
    movimentacao_id UUID NOT NULL REFERENCES movimentacoes(id),
    tipo VARCHAR(40) NOT NULL,
    url TEXT NOT NULL,
    hash VARCHAR(128)
);

CREATE INDEX idx_movimentacoes_producer_timestamp ON movimentacoes (producer_id, registro_timestamp);
CREATE INDEX idx_movimentacoes_commodity_timestamp ON movimentacoes (commodity_id, registro_timestamp);

