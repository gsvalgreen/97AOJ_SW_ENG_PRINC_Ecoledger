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
- [x] Endpoint POST `/movimentacoes` com validação de produtor, política de anexos e retorno 201.
- [x] Cliente `ProducerApprovalClient` consumindo `GET /usuarios/{id}` com stubs WireMock em testes.
- [x] Publicação Kafka (`KafkaMovimentacaoEventPublisher`) com tópicos configuráveis, `KafkaTemplate` e testes unitários + EmbeddedKafka IT.
- [x] Testes `MovimentacaoServiceTest`, `MovimentacaoControllerIT` (MockMvc + WireMock) e `KafkaMovimentacaoEventPublisherIT` executados via `integrationTest`.
- [x] Validação de anexos em S3 (`S3AttachmentStorageService`) com políticas de MIME/tamanho/hash, client AWS SDK e testes unitários dedicados.

### Próximos Passos
- [ ] Disponibilizar GET `/movimentacoes/{id}`, `/produtores/{producerId}/movimentacoes` (paginação/filtros) e `/commodities/{commodityId}/historico`, incluindo consultas JPA, DTOs e cobertura de testes (unit + integração H2/WireMock).
- [ ] Implementar fluxo completo de upload (endpoint ou serviço interno) gerando URLs assinadas/definitivas, versionamento e documentação de variáveis S3 restantes (ex.: buckets público/privado, uso do MinIO conforme `README-minio.md`).
- [ ] Enriquecer validação/erros (Bean Validation adicional, Problem Details) e alinhar OpenAPI com `movimentacao.yaml` + exemplos reais.
- [ ] Observabilidade e CI: revisar logs/metrics/healthchecks, atualizar README com comandos `./gradlew clean build` e variáveis obrigatórias.
- [ ] Planejar/eventar `movimentacao.atualizada` e consumidores (Auditoria, Notificações) com testes de contrato ou WireMock específicos.