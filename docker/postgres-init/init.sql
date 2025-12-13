-- idempotent init script: creates databases and roles only if they do not exist

-- create admin role if missing
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ecoledger_admin') THEN
    CREATE ROLE ecoledger_admin WITH SUPERUSER LOGIN PASSWORD 'ecoledger_admin';
  END IF;
END
$$;

-- create databases if missing (use psql \gexec to run the CREATE statements)
SELECT 'CREATE DATABASE users;' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'users'); \gexec
SELECT 'CREATE DATABASE movimentacao;' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'movimentacao'); \gexec
SELECT 'CREATE DATABASE auditoria;' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'auditoria'); \gexec
SELECT 'CREATE DATABASE certificacao;' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'certificacao'); \gexec
SELECT 'CREATE DATABASE credito;' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'credito'); \gexec
SELECT 'CREATE DATABASE notification;' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification'); \gexec

-- create service users (roles) if missing
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ecoledger_users') THEN
    CREATE USER ecoledger_users WITH ENCRYPTED PASSWORD 'ecoledger_users';
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ecoledger_movimentacao') THEN
    CREATE USER ecoledger_movimentacao WITH ENCRYPTED PASSWORD 'ecoledger_movimentacao';
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ecoledger_auditoria') THEN
    CREATE USER ecoledger_auditoria WITH ENCRYPTED PASSWORD 'ecoledger_auditoria';
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ecoledger_certificacao') THEN
    CREATE USER ecoledger_certificacao WITH ENCRYPTED PASSWORD 'ecoledger_certificacao';
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ecoledger_credito') THEN
    CREATE USER ecoledger_credito WITH ENCRYPTED PASSWORD 'ecoledger_credito';
  END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'ecoledger_notification') THEN
    CREATE USER ecoledger_notification WITH ENCRYPTED PASSWORD 'ecoledger_notification';
  END IF;
END
$$;

-- grant privileges (no-op if already granted at DB level)
GRANT ALL PRIVILEGES ON DATABASE users TO ecoledger_users;
GRANT ALL PRIVILEGES ON DATABASE movimentacao TO ecoledger_movimentacao;
GRANT ALL PRIVILEGES ON DATABASE auditoria TO ecoledger_auditoria;
GRANT ALL PRIVILEGES ON DATABASE certificacao TO ecoledger_certificacao;
GRANT ALL PRIVILEGES ON DATABASE credito TO ecoledger_credito;
GRANT ALL PRIVILEGES ON DATABASE notification TO ecoledger_notification;

-- ensure admin has access to all DBs
GRANT ALL PRIVILEGES ON DATABASE users TO ecoledger_admin;
GRANT ALL PRIVILEGES ON DATABASE movimentacao TO ecoledger_admin;
GRANT ALL PRIVILEGES ON DATABASE auditoria TO ecoledger_admin;
GRANT ALL PRIVILEGES ON DATABASE certificacao TO ecoledger_admin;
GRANT ALL PRIVILEGES ON DATABASE credito TO ecoledger_admin;
GRANT ALL PRIVILEGES ON DATABASE notification TO ecoledger_admin;

-- Ensure service users can create objects in the public schema of their DBs
\connect users
-- grant schema privileges
GRANT ALL ON SCHEMA public TO ecoledger_users;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_users;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_users;

\connect movimentacao
GRANT ALL ON SCHEMA public TO ecoledger_movimentacao;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_movimentacao;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_movimentacao;

\connect auditoria
GRANT ALL ON SCHEMA public TO ecoledger_auditoria;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_auditoria;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_auditoria;

\connect certificacao
GRANT ALL ON SCHEMA public TO ecoledger_certificacao;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_certificacao;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_certificacao;

\connect credito
GRANT ALL ON SCHEMA public TO ecoledger_credito;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_credito;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_credito;

\connect notification
GRANT ALL ON SCHEMA public TO ecoledger_notification;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_notification;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_notification;

-- ensure admin can also operate on schemas
\connect users
GRANT ALL ON SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_admin;

\connect movimentacao
GRANT ALL ON SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_admin;

\connect auditoria
GRANT ALL ON SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_admin;

\connect certificacao
GRANT ALL ON SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_admin;

\connect credito
GRANT ALL ON SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_admin;

\connect notification
GRANT ALL ON SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecoledger_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecoledger_admin;