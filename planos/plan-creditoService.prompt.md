Visão geral

Plano para o "Serviço de Acesso a Crédito (Financiamento)" — fornece propostas de financiamento, registra solicitações e comunica com instituições financeiras.

Responsabilidades

- Expor propostas de financiamento disponíveis para produtores elegíveis.
- Receber solicitações de crédito e encaminhar às instituições financeiras parceiras.
- Registrar status das solicitações e publicar eventos (`credit.requested`, `credit.result`).
- Requer integração com Certificação para verificar selo ativo.

API REST

- GET /proposals?producerId={id}
  - Retorna propostas aplicáveis (considerando selo e perfil).

- POST /credit-requests
  - Payload: { producerId, proposalId, amountRequested, termMonths, documents? }
  - Valida: produtor com selo ativo
  - Resposta: 201 { requestId }
  - Efeito: publica `credit.requested` para bancos/partners

- GET /credit-requests/{id}
  - Retorna status e histórico de comunicações

- PATCH /credit-requests/{id}/status
  - Uso: recebe callback interno ou webhook de instituição financeira com status APPROVED/REJECTED

Modelos

- Proposal: { id, institutionId, maxAmount, rate, termOptions, conditions }
- CreditRequest: { id, producerId, proposalId, amount, status, createdAt, history[] }

Integrações

- Certificação: validar selo ativo antes de aceitar requisição.
- Partners (Bancos): webhook/callback ou API para envio de propostas.
- Notificação: avisar produtor sobre resultado.

Cenários de Teste (do Projeto_1.md)

Feature: Solicitar Financiamento com Base na Certificação
  Scenario: Produtor com selo ativo solicita financiamento
    Given produtor com selo ativo
    When GET /proposals e POST /credit-requests
    Then apresenta propostas e aceita requisição

  Scenario: Produtor sem selo ativo tenta solicitar financiamento
    Given produtor sem selo ativo
    When POST /credit-requests
    Then 403 Forbidden com mensagem explicativa

Cenários adicionais
- Callback de instituição aprova crédito: PATCH /credit-requests/{id}/status = APPROVED -> Notificação enviada e evento `credit.result` publicado.
- Falha na comunicação com parceiro: retry/backoff e registro em dead-letter.
- Proteção contra reenvio duplicado de solicitações (Idempotency-Key).

Requisitos não-funcionais

- Segurança dos documentos (PII), cifrar em repouso.
- Auditoria completa de decisões de crédito.

Checklist para o agente

1. Criar endpoints e validações.
2. Integrar verificação de selo (call sync or cached state).
3. Implementar envio para parceiros via webhook/queue com retries.
4. Persistir requests e histórico.
5. Testes unitários e integração (stub de parceiro).  

Configuração

- ENV: KAFKA_BOOTSTRAP, DATABASE_URL, PARTNER_WEBHOOKS, NOTIFICATION_ENDPOINT
- Política de retenção de dados de propostas e documentos.

