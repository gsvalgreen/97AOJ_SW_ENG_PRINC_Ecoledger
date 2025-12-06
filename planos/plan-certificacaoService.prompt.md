Visão geral

Plano para o "Serviço de Certificação (Selo Verde)" — consolida resultados de auditoria, decide status do selo por produtor e expõe consultas e históricos.

Responsabilidades

- Consumir `auditoria.concluida` e recalcular elegibilidade do produtor.
- Armazenar o estado do selo (`SeloVerde`) e histórico de mudanças.
- Expor endpoints para consulta do selo e forçar recálculo/substituição manual.
- Publicar `selo.atualizado` para serviços dependentes (Crédito, Notificação).

API REST

- GET /selos/{producerId}
  - Retorna estado atual do selo: { producerId, status: [ATIVO|PENDENTE|INATIVO], nivel?, ultimoCheck, evidencias[] }

- POST /selos/{producerId}/recalcular
  - Força recálculo do selo com base em auditorias disponíveis.
  - Resposta: 200 { novoStatus }

- GET /selos/{producerId}/historico
  - Retorna mudanças históricas do selo com timestamps e motivos.

Modelos

- SeloVerde: { producerId, status, nivel (ex: BRONZE|PRATA|OURO), pontuacao, motivos[], ultimoCheck }
- AlteracaoSelo: { id, producerId, deStatus, paraStatus, motivo, timestamp, evidencia }

Regras e critérios

- Definir thresholds que transformam os resultados de auditoria em pontuação final.
- Regras versionadas e auditáveis (ligar ao versaoRegra do RegistroAuditoria).
- Política de expiração: selo expira após X meses sem novas auditorias.

Eventos

- Consome: `auditoria.concluida`
- Produz: `selo.atualizado` { producerId, statusAntigo, statusNovo, timestamp, detalhes }

Cenários de Teste (mapeados)

Feature: Consultar Status do Selo Verde
  Scenario: Produtor consulta selo ativo
    Given produtor autenticado com selo ativo
    When GET /selos/{producerId}
    Then 200 { status: "ATIVO" }

  Scenario: Produtor consulta selo pendente ou inativo
    Given selo pendente
    When GET /selos/{producerId}
    Then 200 { status: "PENDENTE" | "INATIVO" }

Cenários adicionais
- Ao receber `auditoria.concluida` com REPROVADO, selo reduz prioridade e publica `selo.atualizado`.
- Recalculo manual por auditor gera histórico com motivo.
- Expiração do selo realiza transição para PENDENTE e notifica o produtor.

Implementação técnica

- Store: certDB com tabelas selos, alteracoes_selo
- Consumer robusto para `auditoria.concluida` e processamento idempotente
- Endpoint de recálculo síncrono (executar em background se necessário)

Checklist para o agente

1. Implementar consumer `auditoria.concluida` e lógica de cálculo.
2. Persistir selos e alteracoes_selo com migrations.
3. Implementar endpoints REST e regras de autorização.
4. Publicar `selo.atualizado` e integrar Notificação.
5. Testes unitários para cálculo e integração para reprocessamento.

Configuração

- ENV: DATABASE_URL, KAFKA_BOOTSTRAP, SELO_EXPIRATION_DIAS, RULES_VERSION_STORE
