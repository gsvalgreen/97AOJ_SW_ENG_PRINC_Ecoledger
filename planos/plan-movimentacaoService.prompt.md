Visão geral

Plano pronto para o "Serviço de Movimentação (Rastreabilidade)". Inclui endpoints, modelos, integração com Auditoria, tópicos/events e cenários de teste alinhados ao `Projeto_1.md`.

Responsabilidades

- Registrar movimentações de commodities.
- Fornecer consultas por produtor, por commodity e histórico.
- Publicar eventos: `movement.created`, `movement.updated`.
- Referenciar documentos/anexos (armazenamento em S3) e garantir imutabilidade dos registros (hash opcional).

API REST (principais contratos)

- POST /movements
  - Payload: { producerId, commodityId, type, quantity, unit, timestamp, location?, attachments? }
  - Validações: campos obrigatórios, producer autenticado e aprovado.
  - Respostas: 201 Created { movementId } | 400
  - Efeito: publica `movement.created` no Kafka

- GET /movements/{id}
  - Resposta: 200 { movement } | 404

- GET /producers/{producerId}/movements
  - Query: page, size, fromDate, toDate, commodityId
  - Resposta: 200 { items[], total }

- GET /commodities/{commodityId}/history
  - Resposta: 200 { movements[] }

Modelos

- Movement: { id, producerId, commodityId, type, quantity, unit, timestamp, location, attachments: [ { type, url, hash } ], createdAt }
- MovementHistory: lista paginada

Eventos e Integração Assíncrona

- movement.created
  - Payload: full movement record
  - Consumidores: Auditoria service, Notifications (summary)

- movement.updated
  - Payload: { movementId, changes }

Regras de negócio importantes

- Apenas produtores com cadastro aprovado podem registrar movimentações.
- Movimentações devem incluir timestamp e quantidade não-negativa.
- Anexos: armazenados em serviço de arquivos; somente referência salva no MovDB.

Cenários de Teste (inclui do Projeto_1.md)

Feature: Registrar Movimentações na Produção da Commodity
  Scenario: Produtor registra movimentação válida
    Given produtor autenticado e cadastro aprovado
    When POST /movements com payload válido
    Then 201 Created e `movement.created` publicado

  Scenario: Registro inválido de movimentação
    Given produtor autenticado
    When POST /movements com dados incompletos
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

- Tabelas: movements, movement_attachments, movement_events
- Índices: producerId+timestamp, commodityId+timestamp

Checklist para o agente

1. Criar rotas e validação JSON Schema.
2. Persistir movimentos em MovDB (migrations).
3. Upload de arquivos para S3 compatível (mock em CI).
4. Gerar e publicar `movement.created` para Kafka.
5. Implementar paginação e filtros.
6. Testes unitários, integração (DB), e2e com consumidor Auditoria mockado.
7. Documentar OpenAPI.

Configuração

- ENVs: DATABASE_URL, S3_ENDPOINT, KAFKA_BOOTSTRAP, SERVICE_JWT_AUDIENCE
- Parâmetros: maxAttachmentSize, allowedMimeTypes

