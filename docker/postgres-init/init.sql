-- init script to create databases, service users and admin
CREATE USER ecoledger_admin WITH SUPERUSER PASSWORD 'ecoledger_admin';

CREATE DATABASE users;
CREATE DATABASE movimentacao;
CREATE DATABASE auditoria;
CREATE DATABASE certificacao;
CREATE DATABASE credito;
CREATE DATABASE notification;

\connect users
CREATE USER ecoledger_users WITH ENCRYPTED PASSWORD 'ecoledger_users';
GRANT ALL PRIVILEGES ON DATABASE users TO ecoledger_users;

\connect movimentacao
CREATE USER ecoledger_movimentacao WITH ENCRYPTED PASSWORD 'ecoledger_movimentacao';
GRANT ALL PRIVILEGES ON DATABASE movimentacao TO ecoledger_movimentacao;

\connect auditoria
CREATE USER ecoledger_auditoria WITH ENCRYPTED PASSWORD 'ecoledger_auditoria';
GRANT ALL PRIVILEGES ON DATABASE auditoria TO ecoledger_auditoria;

\connect certificacao
CREATE USER ecoledger_certificacao WITH ENCRYPTED PASSWORD 'ecoledger_certificacao';
GRANT ALL PRIVILEGES ON DATABASE certificacao TO ecoledger_certificacao;

\connect credito
CREATE USER ecoledger_credito WITH ENCRYPTED PASSWORD 'ecoledger_credito';
GRANT ALL PRIVILEGES ON DATABASE credito TO ecoledger_credito;

\connect notification
CREATE USER ecoledger_notification WITH ENCRYPTED PASSWORD 'ecoledger_notification';
GRANT ALL PRIVILEGES ON DATABASE notification TO ecoledger_notification;

-- grant admin access to all databases
\connect users
GRANT ALL PRIVILEGES ON DATABASE users TO ecoledger_admin;
\connect movimentacao
GRANT ALL PRIVILEGES ON DATABASE movimentacao TO ecoledger_admin;
\connect auditoria
GRANT ALL PRIVILEGES ON DATABASE auditoria TO ecoledger_admin;
\connect certificacao
GRANT ALL PRIVILEGES ON DATABASE certificacao TO ecoledger_admin;
\connect credito
GRANT ALL PRIVILEGES ON DATABASE credito TO ecoledger_admin;
\connect notification
GRANT ALL PRIVILEGES ON DATABASE notification TO ecoledger_admin;
