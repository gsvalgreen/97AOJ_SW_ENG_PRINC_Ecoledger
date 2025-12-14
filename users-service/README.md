# Users Service (Serviço de Usuários)

Serviço responsável pelo cadastro, autenticação e gerenciamento de perfis (Produtor, Analista, Auditor) do projeto ECO LEDGER.

Resumo
- Expõe APIs REST para: cadastro de candidatos (/cadastros), gestão de usuários (/usuarios), autenticação (/auth/login) e alteração de status (PATCH /usuarios/{id}/status).
- Publica eventos assíncronos em Kafka: `user.registered`, `user.approved`, `user.rejected`.
- Persiste em PostgreSQL (users-db) e segue o plano e contratos descritos em Projeto_1.md e planos/plan-usersService.prompt.md.

Stack e requisitos
- Java 21 + Spring Boot
- Gradle (wrapper incluído)
- Docker & Docker Compose (para infraestrutura auxiliar)

Serviços auxiliares (definidos em docker-compose.yml)
- PostgreSQL (users-db): jdbc:postgresql://localhost:5432/users (user: ecoledger / ecoledger)
- Kafka: bootstrap em localhost:29092 (tópicos criados pelo serviço kafka-init)
- Schema Registry: http://localhost:8081
- Kafka UI: http://localhost:8090
- MinIO: http://localhost:9000 (console: http://localhost:9001) — credenciais: minioadmin / minioadmin
- MailHog: http://localhost:8025

Rodando localmente
1) Subir infraestrutura do projeto (no root do repositório):

   docker-compose up -d

   Isso iniciará Zookeeper, Kafka, Schema Registry, MinIO, bancos Postgres (incluindo users-db), MailHog e utilitários.

2) Construir e executar o serviço (dentro da pasta users-service):

   ./gradlew clean build
   ./gradlew bootRun

   ou executar o jar gerado:

   java -jar build/libs/*.jar

Variáveis de ambiente (exemplos)
- SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/users
- SPRING_DATASOURCE_USERNAME=ecoledger
- SPRING_DATASOURCE_PASSWORD=ecoledger
- KAFKA_BOOTSTRAP=localhost:29092
- SCHEMA_REGISTRY_URL=http://localhost:8081
- S3_ENDPOINT=http://localhost:9000
- S3_ACCESS_KEY=minioadmin
- S3_SECRET_KEY=minioadmin
- JWT_SECRET=<seu-jwt-secret>
- NOTIFY_ENDPOINT=http://notification-service:8080 (ou o endpoint de notificação usado localmente)

Endpoints principais (contratos resumidos)
- POST /cadastros
  - Submete um novo cadastro (candidato). Respostas: 201 Created { cadastroId, status: PENDENTE } | 400 Bad Request
  - Publica evento `user.registered` após sucesso.

- PATCH /usuarios/{id}/status
  - Atualiza status administrativo do usuário (APROVADO|REJEITADO). Requer autorização de Analista.
  - Publica `user.approved` ou `user.rejected` e dispara notificação.

- GET /usuarios/{id}
  - Retorna perfil do usuário (autenticação requerida).

- POST /auth/login
  - Autenticação por email+senha. Retorna accessToken + refreshToken.

Banco de Dados e Migrações
- Banco definido em docker-compose: users-db (Postgres 16).
- Usuário/DB: ecoledger / users. Rodar migrations na inicialização (Flyway/Liquibase) é recomendado; incluir scripts em src/main/resources/db/migration.
- Índices recomendados: email (UNIQUE), documento (UNIQUE).

Eventos e Integrações
- Tópicos Kafka relevantes: `usuarios.events` (ou `usuarios.*` conforme convenção do projeto) — ver kafka-init no docker-compose.
- Integração com serviço de Notificação para envio de e-mails (aprovação/rejeição).

Testes
- Unit tests: ./gradlew test
- Integration tests: ./gradlew integrationTest ou ./gradlew clean check (se task configurada)
- Recomenda-se usar H2 para testes de integração e EmbeddedKafka para testes de mensageria.

Segurança e Boas Práticas
- Autenticação: JWT Bearer (configurar JWT_SECRET)
- Regras de autorização por scopes/roles: usuarios:read, usuarios:write, admin:usuarios
- Criptografia de dados sensíveis (documentos) e TLS para comunicações externas em produção.

Notas importantes
- O docker-compose já cria tópicos via o serviço kafka-init (ver raiz docker-compose.yml).
- Ajustar variáveis de conexão se executar os serviços em portas diferentes ou em hosts remotos.
- Consulte Projeto_1.md e planos/plan-usersService.prompt.md para regras de negócio, cenários de teste (Gherkin) e contratos completos.

Contribuição e CI
- Build e testes são executados via Gradle; pipeline esperado: ./gradlew clean build
- Incluir testes unitários e de integração em PRs; seguir checklist do plano de implementação.

Contato
- Equipe ECO LEDGER — ver Projeto_1.md para membros e responsáveis.
