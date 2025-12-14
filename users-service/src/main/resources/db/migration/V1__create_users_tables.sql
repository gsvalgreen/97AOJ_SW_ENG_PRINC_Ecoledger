CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(300) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    documento VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE cadastros (
    id UUID PRIMARY KEY,
    usuario_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    payload JSONB,
    submetido_em TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_cadastro_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    cadastro_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_idempotency_cadastro FOREIGN KEY (cadastro_id) REFERENCES cadastros(id)
);
