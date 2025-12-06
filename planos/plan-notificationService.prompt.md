Visão geral

Plano para o "Serviço de Notificação" — centraliza envio de mensagens (email, push, webhooks) e gerencia preferências de usuários.

Responsabilidades

- Receber solicitações de envio de notificações (sincronas ou por eventos).
- Gerenciar templates e preferências do usuário (opt-in/out).
- Integrar com provedores (SMTP, Firebase, SES) e expor webhooks para parceiros.
- Fornecer relatórios de entrega e retry logic.

API REST

- POST /notifications/send
  - Payload: { toUserId?, channel: [email|push|webhook], templateId, data }
  - Resposta: 202 Accepted { notificationId }

- GET /preferences/{userId}
  - Retorna preferências do usuário (channels enabled)

- PATCH /preferences/{userId}
  - Atualiza preferências

Eventos consumidos

- users.* (user.approved, user.rejected)
- audit.completed
- seal.updated
- credit.result

Cenários de Teste (do Projeto_1.md)

Feature: Notificar Aprovação de Financiamento
  Scenario: Sistema notifica aprovação de empréstimo
    Given credit.result APPROVED
    When consumer processa evento
    Then envia notificação ao Produtor e instituições financeiras

  Scenario: Sistema notifica rejeição de empréstimo
    Given credit.result REJECTED
    When processa
    Then envia notificação ao Produtor com detalhe e link

Cenários adicionais
- Preferência de usuário bloqueia canal (opt-out) e mensagem não é enviada por esse canal.
- Falha no provedor (SMTP down) causa retry e depois persistência em dead-letter com observability.
- Templates parametrizados e testes de renderização (unit tests para substituição de placeholders).

Implementação técnica

- Queueing interno para cada canal com workers separados.
- Delivery report store: notification_deliveries table com status, attempts, lastError.
- Support for webhooks with retries and HMAC signatures for callbacks.

Checklist para o agente

1. Implementar endpoints e validações.
2. Integrar provedores (mock drivers para CI).
3. Implementar preferências de usuário e respect of opt-out.
4. Worker para processamento assíncrono com retries e DLQ.
5. Tests: renderer, integration with mock providers, end-to-end with events.

Configuração

- ENV: SMTP_URL, FIREBASE_CREDENTIALS, HMAC_SECRET, KAFKA_BOOTSTRAP
- Throttling e rate-limits por canal e por usuário

