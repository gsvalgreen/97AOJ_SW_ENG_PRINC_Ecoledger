Visão geral

Plano pronto para o "Serviço de Movimentação (Rastreabilidade)". Inclui endpoints, modelos, integração com Auditoria, tópicos/events e cenários de teste alinhados ao `Projeto_1.md`.

Responsabilidades

- Registrar movimentações de commodities.
- Fornecer consultas por produtor, por commodity e histórico.
- Publicar eventos: `movimentacao.criada`, `movimentacao.atualizada`.
- Referenciar documentos/anexos (armazenamento em S3) e garantir imutabilidade dos registros (hash opcional).

API REST (principais contratos)

- POST /movimentacoes
  - Payload: { producerId, commodityId, tipo, quantidade, unidade, timestamp, localizacao?, anexos? }
  - Validações: campos obrigatórios, produtor autenticado e aprovado.
  - Respostas: 201 Created { movimentacaoId } | 400
  - Efeito: publica `movimentacao.criada` no Kafka

- GET /movimentacoes/{id}
  - Resposta: 200 { movimentacao } | 404

- GET /produtores/{producerId}/movimentacoes
  - Query: page, size, fromDate, toDate, commodityId
  - Resposta: 200 { items[], total }

- GET /commodities/{commodityId}/historico
  - Resposta: 200 { movimentacoes[] }

Modelos

- Movimentacao: { id, producerId, commodityId, tipo, quantidade, unidade, timestamp, localizacao, anexos: [ { tipo, url, hash } ], criadoEm }
- MovimentoHistorico: lista paginada

Eventos e Integração Assíncrona

- movimentacao.criada
  - Payload: registro completo da movimentacao
  - Consumidores: Serviço de Auditoria, Notificações (resumo)

- movimentacao.atualizada
  - Payload: { movimentacaoId, changes }

Regras de negócio importantes

- Apenas produtores com cadastro aprovado podem registrar movimentações.
- Movimentações devem incluir timestamp e quantidade não-negativa.
- Anexos: armazenados em serviço de arquivos; somente referência salva no MovDB.

Cenários de Teste (inclui do Projeto_1.md)

Feature: Registrar Movimentações na Produção da Commodity
  Scenario: Produtor registra movimentação válida
    Given produtor autenticado e cadastro aprovado
    When POST /movimentacoes com payload válido
    Then 201 Created e `movimentacao.criada` publicado

  Scenario: Registro inválido de movimentação
    Given produtor autenticado
    When POST /movimentacoes com dados incompletos
    Then 400 Bad Request

Cenários adicionais do serviço
- Registro duplicado (mesmo hash de conteúdo) retorna 409 Conflict ou é idempotente quando Idempotency-Key fornecido.
- Anexo com hash mismatch rejeitado.
- Consulta de histórico por Analista com permissão retorna 200; sem permissão retorna 403.
- Filtro por período e commodity retorna resultados corretos.

Não-funcionais

- Throughput: projetar para bursts (ex: 200 req/s) com filas e backpressure.
- Paginação obrigatória para endpoints de lista.
- Auditoria imutável: gravar versão original (append-only) e permitir reprocessamento.

Estrutura de Dados e Índices

- Tabelas: movimentacoes, anexos_movimentacao, eventos_movimentacao
- Índices: producerId+timestamp, commodityId+timestamp

Checklist para o agente

1. Criar rotas e validação JSON Schema.
2. Persistir movimentos em MovDB (migrations).
3. Upload de arquivos para S3 compatível (mock em CI).
4. Gerar e publicar `movimentacao.criada` para Kafka.
5. Implementar paginação e filtros.
6. Testes unitários, integração (DB), e2e com consumidor Auditoria mockado.
7. Documentar OpenAPI.

Configuração

- ENVs: DATABASE_URL, S3_ENDPOINT, KAFKA_BOOTSTRAP, SERVICE_JWT_AUDIENCE
- Parâmetros: maxAttachmentSize, allowedMimeTypes

## Checklist de Implementação

### Concluído
- [x] Projeto Gradle com Java 21/Spring Boot 3.5.8, migrations e repositório `MovimentacaoRepository` configurados.
- [x] Endpoint POST `/movimentacoes` com validação de produtor, política de anexos e publicação (no-op) de evento.
- [x] Testes `MovimentacaoServiceTest` (unitário) e `MovimentacaoControllerIT` (H2) cobrindo fluxo feliz e regras de negócio.
- [x] `IntegrationInfrastructureConfiguration` provê `AttachmentStorageService` (S3 vs no-op) e `MovimentacaoEventPublisher` no-op.

### Próximos Passos
- [ ] Implementar `KafkaMovimentacaoEventPublisher`, payload (`movimentacao.criada`/`movimentacao.atualizada`) e testes com Embedded Kafka.
- [ ] Substituir `NoOpAttachmentStorageService` por integração real com S3/MinIO (validação de tamanho/MIME/hash + upload) e testes de integração.
- [ ] Disponibilizar GET `/movimentacoes/{id}`, `/produtores/{producerId}/movimentacoes` (com paginação/filtros) e `/commodities/{commodityId}/historico`, com consultas JPA e cobertura de testes.
- [ ] Adicionar validações complementares (Bean Validation/Problem Details) e documentação OpenAPI alinhada ao contrato `movimentacao.yaml`.
- [ ] Atualizar observabilidade/CI (logs mínimos, comandos `./gradlew clean build`) e documentar variáveis de ambiente faltantes.
