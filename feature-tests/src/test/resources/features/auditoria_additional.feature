Feature: Auditoria additional scenarios

  Background:
    Given os serviços de movimentação e auditoria estão disponíveis em localhost

  Scenario: Revisão manual altera resultado da auditoria
    When eu registro uma movimentacao valida para o produtor "producer-review"
    Then a API de movimentacao retorna 201
    And o serviço de auditoria registra uma auditoria para o produtor "producer-review" dentro de 30 seconds
    When eu aplico uma revisao manual para a primeira auditoria do produtor "producer-review" com auditor "auditor-1" e resultado "REPROVADO"
    Then a auditoria para o produtor "producer-review" é publicada com resultado "REPROVADO" dentro de 30 seconds
