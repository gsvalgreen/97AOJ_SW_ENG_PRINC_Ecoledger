# Movimentacao Service

Serviço responsável por registrar e consultar movimentações de commodities.

Requisitos
- Docker e Docker Compose
- Java 21 (usado pelo projeto) e Gradle (wrapper incluído)

Serviços auxiliares (definidos em docker-compose.yml)
- Kafka: bootstrap em localhost:29092 (broker dentro do compose)
- Schema Registry: http://localhost:8081
- Kafka UI: http://localhost:8080
- MinIO: http://localhost:9000 (console em http://localhost:9001) — credenciais: minioadmin / minioadmin. Buckets criados: `movimentacoes`, `anexos`.
- Postgres (movimentacao-db): jdbc:postgresql://localhost:5433/movimentacao (user: ecoledger / ecoledger)
- MailHog: http://localhost:8025

Rodando localmente
1. Subir a infraestrutura necessária:

   docker-compose up -d

   Isso iniciará Zookeeper, Kafka, Schema Registry, MinIO, os bancos Postgres usados pelos serviços e outras dependências.

2. Executar o serviço:

   ./gradlew clean build
   ./gradlew bootRun

   Ou executar o jar gerado:

   java -jar build/libs/*.jar

Variáveis de configuração úteis
- SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/movimentacao
- SPRING_DATASOURCE_USERNAME=ecoledger
- SPRING_DATASOURCE_PASSWORD=ecoledger
- KAFKA_BOOTSTRAP=localhost:29092
- S3_ENDPOINT=http://localhost:9000
- S3_ACCESS_KEY=minioadmin
- S3_SECRET_KEY=minioadmin

Testes
- Testes unitários: ./gradlew test
- Testes de integração (se configurados): ./gradlew integrationTest ou ./gradlew clean check

Notas
- O serviço kafka-init no compose cria automaticamente os tópicos: usuarios.events, movimentacao.events, auditoria.events, certificacao.events, credito.events e notificacao.events.
- Se precisar inspecionar filas e tópicos, acesse o Kafka UI em http://localhost:8080.
