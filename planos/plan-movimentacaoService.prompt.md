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

### Concluído (status verificado)
- [x] Projeto Gradle com Java 21/Spring Boot 3.5.8, migrations e repositório `MovimentacaoRepository` configurados.
- [x] Endpoint POST `/movimentacoes` com validação de produtor (ProducerApprovalClient), política de anexos e retorno 201.
- [x] Cliente `ProducerApprovalClient` consumindo `GET /usuarios/{id}` com stubs WireMock em testes.
- [x] Publicação Kafka (`KafkaMovimentacaoEventPublisher`) com tópicos configuráveis, `KafkaTemplate` e testes unitários + EmbeddedKafka IT.
- [x] Testes `MovimentacaoServiceTest`, `MovimentacaoControllerIT` (MockMvc + WireMock) e `KafkaMovimentacaoEventPublisherIT` executados via `integrationTest`.
- [x] Validação de anexos em S3 (`S3AttachmentStorageService`) com políticas de MIME/tamanho/hash, client AWS SDK e testes unitários dedicados.

### Pendências (prioridade alta -> baixa)
- [x] Implementar e disponibilizar GET `/movimentacoes/{id}` (detalhe) com DTO de resposta e testes unit + integração H2.
- [x] Implementar lista paginada `/produtores/{producerId}/movimentacoes` com filtros (fromDate, toDate, commodityId) e testes.
- [x] Implementar `/commodities/{commodityId}/historico` e cobertura de integração.
- [x] Completar fluxo de upload: endpoint para anexos, geração de URLs assinadas, confirmação de upload e versionamento (MinIO em CI).
- [x] Idempotência para criação (Idempotency-Key optional + duplicate-hash fallback).
- [x] Enriquecer tratamento de erros (Problem Details), validações JSR-380 adicionais e mensagens de erro padronizadas.
- [x] OpenAPI: alinhar `movimentacao.yaml`, incluir exemplos e documentar erros/response codes.
- [ ] Observabilidade: healthchecks, métricas básicas (request count, errors), logs estruturados e configuração para CI/Prod.
- [ ] Testes de contrato/consumidor para Auditoria e Notificações (PACT/WireMock/contract tests).
- [ ] Revisar índices DB (producerId+timestamp, commodityId+timestamp) e adicionar migrations faltantes se necessário.
- [ ] Segurança: validar configuração JWT (audience), permissões por papel (produtor/analista/auditor) e rate limiting.
- [ ] CI: incluir MinIO mock e EmbeddedKafka nas pipelines de integração; documentar variáveis obrigatórias no README.

### Itens identificados na revisão do plano (gaps e riscos)
- Idempotência não está formalizada no design; risco de duplicidade de movimentações em retries.
- Controle de acesso e roles (Analista/Auditor) precisa ser explicitado nos endpoints de consulta.
- Retenção/versão de anexos e política de hash/immutability requer especificação (onde e por quanto tempo manter anexos originais).
- Falta de testes de contrato para consumidores (Auditoria/Notificações) pode gerar quebra em integração.
- Testes E2E dependem de infra (MinIO, Kafka); CI atual precisa rodar estes mocks de forma determinística.
- Observabilidade e SLAs não detalhados (logs, tracing, SLOs).

### Ações recomendadas (próximos passos imediatos)
1. Priorizar endpoints GET (id e listagens) + migrations/índices (1 sprint)
2. Implementar fluxo de upload com MinIO em CI e testes que cubram hash/size/MIME (1 sprint)
3. Adicionar suporte a Idempotency-Key e teste de conflito/duplication (short task)
4. Especificar RBAC mínimo (produtor, analista, auditor) e aplicar em endpoints sensíveis
5. Incluir health/metrics e atualizar README com variáveis de ambiente e comandos de build/test

> Nota: o escopo principal (registro e publicação de eventos) está implementado; pontos acima são pendências e riscos identificados na revisão.