# Auditoria Service

Serviço de validação e auditoria do ECO LEDGER. Responsável por consumir eventos de movimentação, aplicar regras de sustentabilidade, registrar auditorias (automáticas e manuais) e publicar resultados para o serviço de Certificação.

## Responsabilidades

- Consumir eventos `movimentacao.criada` e executar validação automática
- Gerar registros de auditoria (APROVADO/REPROVADO/REQUER_REVISAO) com evidências
- Expor endpoints REST para auditores consultarem e registrarem revisões manuais
- Publicar eventos `auditoria.concluida` com resultado e detalhes

## Stack Tecnológica

- **Java 21** + **Spring Boot 3.5.8**
- **Spring Data JPA** com PostgreSQL
- **Spring Kafka** para mensageria
- **Flyway** para migrações de banco de dados
- **JUnit 5** + **Mockito** para testes unitários
- **H2** + **EmbeddedKafka** para testes de integração

## API REST

### Documentação OpenAPI

- Swagger UI: http://localhost:8082/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs (reflete `planos/api-contracts/auditoria.yaml`)

### Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/auditorias/{id}` | Recuperar registro de auditoria por ID |
| GET | `/produtores/{producerId}/historico-auditorias` | Histórico de auditorias de um produtor |
| POST | `/auditorias/{id}/revisao` | Registrar revisão manual por auditor |

### Exemplos

#### GET /auditorias/{id}

```bash
curl -X GET http://localhost:8082/auditorias/123e4567-e89b-12d3-a456-426614174000
```

Response:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "movimentacaoId": "abc-123",
  "producerId": "producer-001",
  "versaoRegra": "1.0.0",
  "resultado": "APROVADO",
  "evidencias": [],
  "processadoEm": "2024-01-15T10:30:00Z"
}
```

#### POST /auditorias/{id}/revisao

```bash
curl -X POST http://localhost:8082/auditorias/123e4567-e89b-12d3-a456-426614174000/revisao \
  -H "Content-Type: application/json" \
  -d '{
    "auditorId": "auditor-001",
    "resultado": "APROVADO",
    "observacoes": "Revisão manual realizada com sucesso"
  }'
```

## Eventos Kafka

### Consumidos

**Topic:** `movimentacao.criada`

```json
{
  "movimentacaoId": "uuid",
  "producerId": "string",
  "commodityId": "string",
  "tipo": "ENTRADA|SAIDA",
  "quantidade": 100.0,
  "unidade": "KG",
  "localizacao": "BR-SP",
  "dataMovimentacao": "2024-01-15T10:00:00Z",
  "anexos": [
    {"tipo": "PHOTO", "url": "...", "hash": "..."}
  ],
  "timestamp": "2024-01-15T10:00:00Z"
}
```

### Produzidos

**Topic:** `auditoria.concluida`

```json
{
  "auditoriaId": "uuid",
  "movimentacaoId": "uuid",
  "producerId": "string",
  "resultado": "APROVADO|REPROVADO|REQUER_REVISAO",
  "versaoRegra": "1.0.0",
  "detalhes": [
    {"tipo": "QUANTITY_VALIDATION", "detalhe": "..."}
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Regras de Validação

As regras são plugáveis e versionadas. Cada execução armazena a `versaoRegra` aplicada.

### Regras Implementadas

1. **QuantityThresholdRule** - Valida se a quantidade está dentro dos limites configurados
2. **LocationRule** - Valida se a localização está nas regiões permitidas
3. **AttachmentRule** - Valida a presença e tipos de anexos obrigatórios

### Configuração de Regras

```yaml
auditoria:
  rules:
    version: "1.0.0"
    quantity:
      max-threshold: 10000
      min-threshold: 0
    location:
      allowed-regions:
        - "BR-SP"
        - "BR-MG"
      validate-coordinates: false
    attachments:
      required: true
      min-count: 1
      required-types:
        - "PHOTO"
```

## Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `SPRING_DATASOURCE_URL` | URL do banco PostgreSQL | `jdbc:postgresql://localhost:5432/auditoria` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `postgres` |
| `KAFKA_ENABLED` | Habilita Kafka | `false` |
| `KAFKA_BOOTSTRAP` | Endereço do Kafka | `localhost:9092` |

## Desenvolvimento Local

### Pré-requisitos

- Java 21
- Docker (opcional, para PostgreSQL e Kafka)

### Executando

```bash
# Rodar testes unitários
./gradlew test

# Rodar testes de integração
./gradlew integrationTest

# Build completo (unit + integration)
./gradlew clean check

# Rodar aplicação
./gradlew bootRun
```

### Docker

```bash
# Build da imagem
docker build -t auditoria-service .

# Executar container
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/auditoria \
  -e KAFKA_ENABLED=true \
  -e KAFKA_BOOTSTRAP=kafka:9092 \
  auditoria-service
```

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/ecoledger/auditoria/
│   │   ├── application/
│   │   │   ├── controller/    # REST controllers
│   │   │   ├── dto/           # Data Transfer Objects
│   │   │   ├── exception/     # Custom exceptions
│   │   │   ├── rules/         # Validation rules engine
│   │   │   └── service/       # Business services
│   │   ├── config/            # Configuration classes
│   │   ├── domain/
│   │   │   ├── model/         # JPA entities
│   │   │   └── repository/    # Spring Data repositories
│   │   └── messaging/         # Kafka consumers/producers
│   │       └── event/         # Event DTOs
│   └── resources/
│       ├── application.yml
│       └── db/migration/      # Flyway migrations
├── test/                      # Unit tests
└── integration-test/          # Integration tests
```

## Testes

### Cenários Cobertos

- ✅ Validação automática de movimentações aprovadas
- ✅ Validação automática de movimentações reprovadas (violação de regras)
- ✅ Registro de revisão manual por auditor
- ✅ Idempotência no processamento de mensagens
- ✅ Publicação de eventos de auditoria concluída

### Executando Testes

```bash
# Apenas unit tests
./gradlew test

# Apenas integration tests
./gradlew integrationTest

# Todos os testes com cobertura
./gradlew check
```

## Health Check

```bash
curl http://localhost:8082/actuator/health
```

## Métricas

Disponíveis em `/actuator/metrics` quando habilitado.
