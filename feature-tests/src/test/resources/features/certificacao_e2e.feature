Feature: Integração Certificacao (E2E)

  Background:
    Given os serviços de movimentação, auditoria e certificacao estão disponíveis em localhost

  Scenario: Certificacao cria selo ativo após auditoria aprovada
    When eu anexo um arquivo valido para o produtor "producer-e2e-1"
    And eu registro uma movimentacao valida para o produtor "producer-e2e-1" via API de movimentacao
    Then a API de movimentacao retorna 201
    And o serviço de auditoria registra uma auditoria para o produtor "producer-e2e-1" dentro de 30 segundos
    Then o selo para o produtor "producer-e2e-1" tem status "ATIVO" dentro de 30 segundos

  Scenario: Certificacao cria selo inativo após auditoria reprovada
    When eu registro uma movimentacao invalida para o produtor "producer-e2e-2" via API de movimentacao
    Then a auditoria para o produtor "producer-e2e-2" é publicada com resultado "REPROVADO" dentro de 30 segundos
    Then o selo para o produtor "producer-e2e-2" tem status "INATIVO" dentro de 30 segundos
