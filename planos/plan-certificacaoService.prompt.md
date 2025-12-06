Visão geral

Plano para o "Serviço de Certificação (Selo Verde)" — consolida resultados de auditoria, decide status do selo por produtor e expõe consultas e históricos.

Responsabilidades

- Consumir `audit.completed` e recalcular elegibilidade do produtor.
- Armazenar o estado do selo (`GreenSeal`) e histórico de mudanças.
- Expor endpoints para consulta do selo e forçar recálculo/manual override.
- Publicar `seal.updated` para serviços dependentes (Crédito, Notificação).

API REST

- GET /seals/{producerId}
  - Retorna estado atual do selo: { producerId, status: [ACTIVE|PENDING|INACTIVE], level?, lastUpdated, evidence[] }

- POST /seals/{producerId}/recalculate
  - Força recálculo do selo com base em auditorias disponíveis.
  - Resposta: 200 { newStatus }

- GET /seals/{producerId}/history
  - Retorna mudanças históricas do selo com timestamps e motivos.

Modelos

- GreenSeal: { producerId, status, level (e.g., BRONZE|SILVER|GOLD), score, reasons[], lastChecked }
- SealChange: { id, producerId, fromStatus, toStatus, reason, timestamp, evidence }

Regras e critérios

- Definir thresholds que transformam os resultados de auditoria em score final.
- Regras versionadas e auditable (ligar ao ruleVersion do AuditRecord).
- Política de expiração: selo expira após X meses sem novas auditorias.

Eventos

- Consome: `audit.completed`
- Produz: `seal.updated` { producerId, oldStatus, newStatus, timestamp, details }

Cenários de Teste (mapeados)

Feature: Consultar Status do Selo Verde
  Scenario: Produtor consulta selo ativo
    Given produtor autenticado com selo ativo
    When GET /seals/{producerId}
    Then 200 { status: "ACTIVE" }

  Scenario: Produtor consulta selo pendente ou rejeitado
    Given selo pendente
    When GET /seals/{producerId}
    Then 200 { status: "PENDING" | "INACTIVE" }

Cenários adicionais
- Ao receber `audit.completed` com FAIL, selo reduz prioridade e publica `seal.updated`.
- Recalculation manual por auditor gera histórico com motivo.
- Expiração do selo realiza transição para PENDING e notifica o produtor.

Implementação técnica

- Store: certDB com tabelas seals, seal_changes
- Consumer robusto para `audit.completed` e processamento idempotente
- Endpoint de recálculo síncrono (não bloquear demais; executar em background se necessário)

Checklist para o agente

1. Implementar consumer `audit.completed` e lógica de cálculo.
2. Persistir seals e seal_changes com migrations.
3. Implementar REST endpoints e regras de autorização.
4. Publicar `seal.updated` e integrar Notificação.
5. Testes unitários para cálculo e integração para reprocessamento.

Configuração

- ENV: DATABASE_URL, KAFKA_BOOTSTRAP, SEAL_EXPIRATION_DAYS, RULES_VERSION_STORE

