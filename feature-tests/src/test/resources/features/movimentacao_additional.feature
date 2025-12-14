Feature: Movimentacao additional scenarios

  Background:
    Given os serviços de movimentação e auditoria estão disponíveis em localhost

  Scenario: Idempotency - mesmo X-Idempotency-Key retorna mesmo id
    When eu registro uma movimentacao valida para o produtor "idempo-1" com idempotency key "key-123"
    Then a API de movimentacao retorna 201
    And eu registro novamente a mesma movimentacao com idempotency key "key-123"
    Then a API de movimentacao retorna 201
    And os dois ids retornados são iguais

  Scenario: Anexo com hash inválido é rejeitado
    When eu registro uma movimentacao com anexo e hash invalido para o produtor "producer-err"
    Then a API de movimentacao retorna 400

  Scenario: Consultar movimentacao por id
    When eu registro uma movimentacao valida para o produtor "prod-get"
    Then a API de movimentacao retorna 201
    And eu consulto a movimentacao criada e recebo 200

  Scenario: Listagem paginada de movimentacoes por produtor
    Given o banco está limpo para produtor "prod-list"
    When eu crio 3 movimentacoes validas para o produtor "prod-list"
    And eu solicito GET /produtores/prod-list/movimentacoes?page=0&size=2
    Then a resposta contém no máximo 2 itens e total >= 3
