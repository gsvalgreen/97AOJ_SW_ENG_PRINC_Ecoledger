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
    cadastro_id UUID PRIMARY KEY,
    usuario_id UUID,
    payload JSONB,
    submetido_em TIMESTAMP WITH TIME ZONE DEFAULT now()
);