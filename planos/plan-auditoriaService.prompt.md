Visão geral

Plano para o "Serviço de Validação/Auditoria". Focado em consumir eventos de movimentação, aplicar regras de sustentabilidade, registrar auditorias (automáticas e manuais) e publicar resultados para o serviço de Certificação.

Responsabilidades

- Consumir `movement.created` e executar validação automática.
- Gerar registros de auditoria (pass/fail/requires_manual) com evidências.
- Expor endpoints para auditores consultarem e registrarem revisões manuais.
- Publicar `audit.completed` com resultado e detalhes.

API REST (contratos)

- GET /audits/{id}
  - Retorna registro de auditoria.

- GET /producers/{producerId}/audit-history
  - Retorna histórico de auditorias para um produtor.

- POST /audits/{id}/review
  - Payload: { auditorId, result: [PASS|FAIL], observations }
  - Uso: auditor humano grava sua revisão e evidências.

Modelos

- AuditRecord: { id, movementId, producerId, ruleVersion, result: [PASS|FAIL|REQUIRES_MANUAL], evidence: [ { type, detail } ], processedAt }

Regras de validação

- As regras devem ser pluggable e versionadas. Cada execução armazena a `ruleVersion` aplicada.
- Exemplo de regras automáticas: quantity <= threshold, location within allowed area, attachments present and valid.

Eventos

- Consome: `movement.created`
- Produz: `audit.completed` { auditId, movementId, producerId, result, details, timestamp }

Cenários de Teste (mapear do Projeto_1.md)

Feature: Validar e Auditar Dados para Certificação
  Scenario: Sistema valida movimentação e atualiza selo verde
    Given `movement.created` consumido
    When as regras automáticas são satisfeitas
    Then publica `audit.completed` com result PASS

  Scenario: Registro inválido de movimentação
    Given `movement.created` com violação de regras
    When processado
    Then publica `audit.completed` com FAIL e evidencia

Cenários adicionais do serviço
- Auditor registra revisão manual e altera resultado; evento `audit.completed` reflete revisão.
- Reprocessamento de movement após correção (endpoint reprocess) gera novo AuditRecord com nova ruleVersion.
- Falha temporária na validação (ex: serviços externos) deve acionar retry com backoff e marcar em erro após N tentativas.

Implementação técnica

- Consumidor Kafka com idempotência (usar movementId como key) e rastreamento de offset.
- Store: auditoriaDB (append-only audit_records table).
- Engine de regras: executar sandboxed (ex: WASM, Lua, or safe JS) para evitar falhas de execução.

Checklist para o agente

1. Implementar consumidor `movement.created` com retries e dead-letter.
2. Implementar engine de regras pluggable e versionada.
3. Criar endpoints REST para consulta e revisão manual.
4. Persistir AuditRecords e versionar regras.
5. Publicar `audit.completed` para Kafka.
6. Escrever testes unitários para regras e integração para consumer/producer.

Configuração

- ENV: KAFKA_BOOTSTRAP, AUDIT_RULES_PATH, DATABASE_URL, MAX_RETRIES
- Recomendação: manter rules em repositório de configuração com CI para validar alterações de regras.

