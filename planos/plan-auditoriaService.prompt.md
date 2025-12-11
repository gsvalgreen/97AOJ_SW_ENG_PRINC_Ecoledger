Visão geral

Plano para o "Serviço de Validação/Auditoria". Focado em consumir eventos de movimentação, aplicar regras de sustentabilidade, registrar auditorias (automáticas e manuais) e publicar resultados para o serviço de Certificação.

Responsabilidades

- Consumir `movimentacao.criada` e executar validação automática.
- Gerar registros de auditoria (APROVADO/REPROVADO/REQUER_REVISAO) com evidências.
- Expor endpoints para auditores consultarem e registrarem revisões manuais.
- Publicar `auditoria.concluida` com resultado e detalhes.

API REST (contratos)

- GET /auditorias/{id}
  - Retorna registro de auditoria.

- GET /produtores/{producerId}/historico-auditorias
  - Retorna histórico de auditorias para um produtor.

- POST /auditorias/{id}/revisao
  - Payload: { auditorId, resultado: [APROVADO|REPROVADO], observacoes }
  - Uso: auditor humano grava sua revisão e evidências.

Modelos

- RegistroAuditoria: { id, movimentacaoId, producerId, versaoRegra, resultado: [APROVADO|REPROVADO|REQUER_REVISAO], evidencias: [ { tipo, detalhe } ], processadoEm }

Regras de validação

- As regras devem ser pluggable e versionadas. Cada execução armazena a `versaoRegra` aplicada.
- Exemplo de regras automáticas: quantidade <= threshold, localizacao dentro da area permitida, anexos presentes e válidos.

Eventos

- Consome: `movimentacao.criada`
- Produz: `auditoria.concluida` { auditoriaId, movimentacaoId, producerId, resultado, detalhes, timestamp }

Cenários de Teste (mapear do Projeto_1.md)

Feature: Validar e Auditar Dados para Certificação
  Scenario: Sistema valida movimentação e atualiza selo verde
    Given `movimentacao.criada` consumido
    When as regras automáticas são satisfeitas
    Then publica `auditoria.concluida` com resultado APROVADO

  Scenario: Registro inválido de movimentação
    Given `movimentacao.criada` com violação de regras
    When processado
    Then publica `auditoria.concluida` com REPROVADO e evidencia

Cenários adicionais do serviço
- Auditor registra revisão manual e altera resultado; evento `auditoria.concluida` reflete revisão.
- Reprocessamento de movimentacao após correção (endpoint reprocessar) gera novo RegistroAuditoria com nova versaoRegra.
- Falha temporária na validação (ex: serviços externos) deve acionar retry com backoff e marcar em erro após N tentativas.

Implementação técnica

- Consumidor Kafka com idempotência (usar movimentacaoId como key) e rastreamento de offset.
- Store: auditoriaDB (tabela append-only registro_auditorias).
- Engine de regras: executar em sandbox (ex: WASM, Lua, or safe JS) para evitar falhas de execução.

Checklist para o agente

1. Implementar consumidor `movimentacao.criada` com retries e dead-letter.
2. Implementar engine de regras pluggable e versionada.
3. Criar endpoints REST para consulta e revisão manual.
4. Persistir RegistroAuditoria e versionar regras.
5. Publicar `auditoria.concluida` para Kafka.
6. Escrever testes unitários para regras e integração para consumer/producer.

Configuração

- ENV: KAFKA_BOOTSTRAP, AUDIT_RULES_PATH, DATABASE_URL, MAX_RETRIES
- Recomendação: manter regras em repositório de configuração com CI para validar alterações de regras.