# language: pt

Funcionalidade: Integração Movimentacao -> Auditoria (E2E)

  Contexto:
    Dado os serviços de movimentação e auditoria estão disponíveis em localhost

  Cenário: Sistema valida movimentacao e publica auditoria concluida
    Quando eu anexo um arquivo valido para o produtor "producer-123"
    E eu registro uma movimentacao valida para o produtor "producer-123" via API de movimentacao
    Então a API de movimentacao retorna 201
    E o serviço de auditoria registra uma auditoria para o produtor "producer-123" dentro de 30 segundos

  Cenário: Movimentacao inválida gera auditoria reprovada
    Quando eu registro uma movimentacao invalida para o produtor "producer-456" via API de movimentacao
    Então a auditoria para o produtor "producer-456" é publicada com resultado "REPROVADO" dentro de 30 segundos
