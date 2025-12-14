Feature: Integração Movimentacao -> Auditoria (E2E)

  Background:
    Given os serviços de movimentação e auditoria estão disponíveis em localhost

  Scenario: Sistema valida movimentacao e publica auditoria concluida
    When eu anexo um arquivo valido para o produtor "producer-123"
    And eu registro uma movimentacao valida para o produtor "producer-123" via API de movimentacao
    Then a API de movimentacao retorna 201
    And o serviço de auditoria registra uma auditoria para o produtor "producer-123" dentro de 30 segundos

  Scenario: Movimentacao inválida gera auditoria reprovada
    When eu registro uma movimentacao invalida para o produtor "producer-456" via API de movimentacao
    Then a auditoria para o produtor "producer-456" é publicada com resultado "REPROVADO" dentro de 30 segundos
