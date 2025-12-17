# ECO LEDGER - Guia de Execução Docker

Este documento descreve como executar o projeto ECO LEDGER utilizando Docker Compose.

## 📋 Pré-requisitos

- Docker 20.10+
- Docker Compose 2.0+
- 8GB RAM disponível (recomendado)
- Portas disponíveis: 5432-5437, 8081-8090, 9000-9001, 9092, 1025, 8025, 22181

## 🐳 Docker Compose Disponíveis

### 1. `docker-compose.yml` (Infraestrutura)

Sobe **apenas a infraestrutura** necessária para desenvolvimento local dos microserviços:

**Componentes:**
- Kafka + Zookeeper + Schema Registry
- Kafka UI (interface web para Kafka)
- MinIO (storage S3-compatible)
- PostgreSQL (6 bancos separados para cada serviço)
- MailHog (servidor SMTP de teste)

**Uso:** Desenvolvimento local dos microserviços rodando via IDE/Gradle.

```bash
docker-compose up -d
```

### 2. `docker-compose-ecoledger.yml` (Stack Completa)

Sobe a **stack completa** com infraestrutura + microserviços containerizados:

**Componentes adicionais:**
- Todos os componentes do docker-compose.yml
- PostgreSQL centralizado com múltiplos databases
- WireMock (mock de APIs externas)
- **Microserviços:**
  - movimentacao-service (porta 8082)
  - auditoria-service (porta 8083)
  - users-service (porta 8084)
  - certificacao-service (porta 8085)

**Uso:** Execução completa do sistema ou testes end-to-end.

```bash
docker-compose -f docker-compose-ecoledger.yml up -d
```

## 🚀 Comandos Rápidos

### Iniciar infraestrutura (desenvolvimento local)
```bash
docker-compose up -d
```

### Iniciar stack completa
```bash
docker-compose -f docker-compose-ecoledger.yml up -d
```

### Ver logs
```bash
# Todos os serviços
docker-compose logs -f

# Serviço específico
docker-compose logs -f kafka
docker-compose -f docker-compose-ecoledger.yml logs -f movimentacao-service
```

### Parar serviços
```bash
docker-compose down
docker-compose -f docker-compose-ecoledger.yml down
```

### Parar e remover volumes (limpar dados)
```bash
docker-compose down -v
docker-compose -f docker-compose-ecoledger.yml down -v
```

### Rebuild de serviços (após mudanças no código)
```bash
docker-compose -f docker-compose-ecoledger.yml up -d --build
```

## 🌐 URLs de Acesso

### Interfaces Web

| Serviço | URL | Descrição |
|---------|-----|-----------|
| Kafka UI | http://localhost:8090 | Interface para visualizar tópicos, mensagens e consumidores |
| MinIO Console | http://localhost:9001 | Console do MinIO (usuário: `minioadmin` / senha: `minioadmin`) |
| MailHog UI | http://localhost:8025 | Interface para visualizar emails enviados |

### APIs (docker-compose-ecoledger.yml)

| Microserviço | URL Base | Health Check |
|--------------|----------|--------------|
| Movimentação | http://localhost:8082 | http://localhost:8082/actuator/health |
| Auditoria | http://localhost:8083 | http://localhost:8083/actuator/health |
| Users | http://localhost:8084 | http://localhost:8084/actuator/health |
| Certificação | http://localhost:8085 | http://localhost:8085/actuator/health |
| WireMock | http://localhost:8089 | http://localhost:8089/__admin |

### Swagger UI (quando configurado)

> **Nota:** Swagger não está configurado ainda. Veja seção "Configurando Swagger" abaixo.

| Microserviço | Swagger UI |
|--------------|------------|
| Movimentação | http://localhost:8082/swagger-ui.html |
| Auditoria | http://localhost:8083/swagger-ui.html |
| Users | http://localhost:8084/swagger-ui.html |
| Certificação | http://localhost:8085/swagger-ui.html |

### Bancos de Dados PostgreSQL

#### docker-compose.yml (bancos separados)

| Banco | Porta | Database | Usuário | Senha |
|-------|-------|----------|---------|-------|
| users-db | 5432 | users | ecoledger | ecoledger |
| movimentacao-db | 5433 | movimentacao | ecoledger | ecoledger |
| auditoria-db | 5434 | auditoria | ecoledger | ecoledger |
| certificacao-db | 5435 | certificacao | ecoledger | ecoledger |
| credito-db | 5436 | credito | ecoledger | ecoledger |
| notification-db | 5437 | notification | ecoledger | ecoledger |

#### docker-compose-ecoledger.yml (PostgreSQL centralizado)

| Porta | Databases | Usuário Admin | Senha Admin |
|-------|-----------|---------------|-------------|
| 5432 | users, movimentacao, auditoria, certificacao | ecoledger_admin | ecoledger_admin |

**Usuários por database:**
- `ecoledger_users` / `ecoledger_users`
- `ecoledger_movimentacao` / `ecoledger_movimentacao`
- `ecoledger_auditoria` / `ecoledger_auditoria`
- `ecoledger_certificacao` / `ecoledger_certificacao`

### Kafka

| Componente | Porta | Descrição |
|------------|-------|-----------|
| Kafka Broker | 9092 | Porta para aplicações (PLAINTEXT_HOST) |
| Schema Registry | 8081 | Registro de schemas Avro/JSON |
| Zookeeper | 22181 | Coordenação do Kafka |

**Tópicos criados automaticamente:**
- `usuarios.events`
- `movimentacao.criada`
- `movimentacao.atualizada`
- `movimentacao.events`
- `auditoria.concluida`
- `auditoria.events`
- `certificacao.events`
- `credito.events`
- `notificacao.events`

### MinIO (S3)

| Tipo | Valor |
|------|-------|
| Endpoint | http://localhost:9000 |
| Access Key | minioadmin |
| Secret Key | minioadmin |
| **Buckets criados** | `movimentacoes`, `anexos` |

## 🔧 Desenvolvimento Local

### Cenário 1: Desenvolvimento de um microserviço

1. Suba apenas a infraestrutura:
```bash
docker-compose up -d
```

2. Execute o microserviço via IDE ou Gradle:
```bash
cd movimentacao-service
./gradlew bootRun
```

3. O serviço se conectará à infraestrutura local (Kafka, PostgreSQL, MinIO).

### Cenário 2: Testar integração completa

1. Suba a stack completa:
```bash
docker-compose -f docker-compose-ecoledger.yml up -d
```

2. Aguarde os serviços iniciarem (30-60 segundos):
```bash
docker-compose -f docker-compose-ecoledger.yml ps
```

3. Teste os health checks:
```bash
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

## 🧪 Executando Testes

### Testes Unitários
```bash
cd movimentacao-service
./gradlew test
```

### Testes de Integração
```bash
# Com infraestrutura rodando
docker-compose up -d
cd movimentacao-service
./gradlew integrationTest
```

### Testes End-to-End
```bash
# Stack completa rodando
docker-compose -f docker-compose-ecoledger.yml up -d

# Feature tests
cd feature-tests
./gradlew test
```

## 🔍 Troubleshooting

### Verificar status dos containers
```bash
docker-compose ps
docker-compose -f docker-compose-ecoledger.yml ps
```

### Ver logs de um serviço específico
```bash
docker-compose logs -f kafka
docker-compose logs -f movimentacao-db
docker-compose -f docker-compose-ecoledger.yml logs -f movimentacao-service
```

### Serviço não inicia
```bash
# Verificar logs
docker-compose logs <service-name>

# Verificar healthcheck
docker inspect ecoledger-movimentacao-service | grep -A 10 Health
```

### Banco de dados não conecta
```bash
# Verificar se o PostgreSQL está rodando
docker-compose ps postgres

# Testar conexão
docker exec -it ecoledger-postgres psql -U ecoledger_admin -d movimentacao -c "SELECT 1;"
```

### Kafka não conecta
```bash
# Verificar tópicos criados
docker exec -it ecoledger-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Ver consumer groups
docker exec -it ecoledger-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

### Limpar tudo e recomeçar
```bash
# Parar todos os containers
docker-compose down -v
docker-compose -f docker-compose-ecoledger.yml down -v

# Remover imagens antigas (opcional)
docker rmi movimentacao-service:local auditoria-service:local users-service:local certificacao-service:local

# Limpar volumes órfãos
docker volume prune -f

# Subir novamente
docker-compose up -d
# ou
docker-compose -f docker-compose-ecoledger.yml up -d --build
```

## 📊 Monitoramento

### Verificar uso de recursos
```bash
docker stats
```

### Ver rede dos containers
```bash
docker network inspect ecoledger-stack_ecoledger
# ou
docker network inspect ecoledger-fullstack_ecoledger
```

## 🔐 Credenciais Padrão

| Serviço | Usuário | Senha |
|---------|---------|-------|
| MinIO | minioadmin | minioadmin |
| PostgreSQL (admin) | ecoledger_admin | ecoledger_admin |
| PostgreSQL (users) | ecoledger / ecoledger_users | ecoledger / ecoledger_users |
| MailHog | - | - (sem autenticação) |

## 📦 Configurando Swagger

Para habilitar o Swagger UI nos microserviços:

### 1. Adicionar dependência no `build.gradle.kts`:
```kotlin
dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}
```

### 2. Adicionar no `application.yml`:
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### 3. Rebuild e restart:
```bash
docker-compose -f docker-compose-ecoledger.yml up -d --build
```

## 🎯 Próximos Passos

- [X] Configurar Swagger em todos os microserviços
- [ ] Adicionar frontend-web ao docker-compose-ecoledger.yml
- [ ] Configurar credito-service e notification-service
- [ ] Adicionar traefik/nginx como API Gateway
- [ ] Configurar métricas com Prometheus/Grafana

## 📚 Documentação Adicional

- [AGENTS.md](AGENTS.md) - Guia de desenvolvimento e convenções
- [README-minio.md](README-minio.md) - Configuração detalhada do MinIO
- [README-teams-integration.md](README-teams-integration.md) - Integração com MS Teams para notificações de CI/CD
- [planos/api-contracts/](planos/api-contracts/) - Contratos OpenAPI dos serviços

---

**Projeto:** ECO LEDGER  
**Stack:** Java 21 + Spring Boot 3.5.8 + Kafka + PostgreSQL + MinIO
