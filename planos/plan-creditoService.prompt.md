Visão geral

Plano para o "Serviço de Acesso a Crédito (Financiamento)" — fornece propostas de financiamento, registra solicitações e comunica com instituições financeiras.

Responsabilidades

- Expor propostas de financiamento disponíveis para produtores elegíveis.
- Receber solicitações de crédito e encaminhar às instituições financeiras parceiras.
- Registrar status das solicitações e publicar eventos (`credit.requested`, `credit.result`).
- Requer integração com Certificação para verificar selo ativo.

API REST

- GET /propostas?producerId={id}
  - Retorna propostas aplicáveis (considerando selo e perfil).

- POST /solicitacoes-credito
  - Payload: { producerId, propostaId, valorSolicitado, prazoMeses, documentos? }
  - Valida: produtor com selo ativo
  - Resposta: 201 { solicitacaoId }
  - Efeito: publica `credit.requested` para bancos/parceiros

- GET /solicitacoes-credito/{id}
  - Retorna status e histórico de comunicações

- PATCH /solicitacoes-credito/{id}/status
  - Uso: recebe callback interno ou webhook de instituição financeira com status APROVADO/REPROVADO

Modelos

- Proposta: { id, instituicaoId, valorMaximo, taxa, opcoesPrazo, condicoes }
- SolicitacaoCredito: { id, producerId, propostaId, valor, status, criadoEm, historico[] }

Integrações

- Certificacao: validar selo ativo antes de aceitar requisição.
- Parceiros (Bancos): webhook/callback ou API para envio de propostas.
- Notificacao: avisar produtor sobre resultado.

Cenários de Teste (do Projeto_1.md)

Feature: Solicitar Financiamento com Base na Certificação
  Scenario: Produtor com selo ativo solicita financiamento
    Given produtor com selo ativo
    When GET /propostas e POST /solicitacoes-credito
    Then apresenta propostas e aceita requisição

  Scenario: Produtor sem selo ativo tenta solicitar financiamento
    Given produtor sem selo ativo
    When POST /solicitacoes-credito
    Then 403 Proibido com mensagem explicativa

Cenários adicionais
- Callback de instituição aprova crédito: PATCH /solicitacoes-credito/{id}/status = APROVADO -> Notificação enviada e evento `credit.result` publicado.
- Falha na comunicação com parceiro: retry/backoff e registro em dead-letter.
- Proteção contra reenvio duplicado de solicitações (Idempotency-Key).

Requisitos não-funcionais

- Segurança dos documentos (PII), cifrar em repouso.
- Auditoria completa de decisões de crédito.

Checklist para o agente

1. Criar endpoints e validações.
2. Integrar verificação de selo (chamada síncrona ou estado em cache).
3. Implementar envio para parceiros via webhook/queue com retries.
4. Persistir solicitações e histórico.
5. Testes unitários e integração (stub de parceiro).  

Configuração

- ENV: KAFKA_BOOTSTRAP, DATABASE_URL, PARTNER_WEBHOOKS, NOTIFICATION_ENDPOINT
- Política de retenção de dados de propostas e documentos.
