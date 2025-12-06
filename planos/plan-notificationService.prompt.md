Visão geral

Plano para o "Serviço de Notificação" — centraliza envio de mensagens (email, push, webhooks) e gerencia preferências de usuários.

Responsabilidades

- Receber solicitações de envio de notificações (síncronas ou por eventos).
- Gerenciar templates e preferências do usuário (opt-in/out).
- Integrar com provedores (SMTP, Firebase, SES) e expor webhooks para parceiros.
- Fornecer relatórios de entrega e lógica de retry.

API REST

- POST /notificacoes/enviar
  - Payload: { paraUsuarioId?, canal: [email|push|webhook], templateId, data }
  - Resposta: 202 Aceito { notificacaoId }

- GET /preferencias/{userId}
  - Retorna preferências do usuário (canais habilitados)

- PATCH /preferencias/{userId}
  - Atualiza preferências

Eventos consumidos

- usuarios.* (user.approved, user.rejected)
- auditoria.concluida
- selo.atualizado
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
- Delivery report store: tabela entregas_notificacao com status, tentativas, ultimoErro.
- Suporte para webhooks com retries e assinaturas HMAC para callbacks.

Checklist para o agente

1. Implementar endpoints e validações.
2. Integrar provedores (drivers mock para CI).
3. Implementar preferências de usuário e respeito ao opt-out.
4. Workers para processamento assíncrono com retries e DLQ.
5. Tests: renderer, integração com provedores mock, end-to-end com eventos.

Configuração

- ENV: SMTP_URL, FIREBASE_CREDENTIALS, HMAC_SECRET, KAFKA_BOOTSTRAP
- Throttling e rate-limits por canal e por usuário
