Feature: Users Service - basic scenarios

  Scenario: Produtor preenche e envia cadastro valido
    Given o serviço de usuarios está disponível em localhost:8084
    When eu submeter um cadastro valido
    Then o serviço retorna 201 e eu consigo recuperar o cadastro criado

  Scenario: Produtor tenta enviar cadastro com dados incompletos
    Given o serviço de usuarios está disponível em localhost:8084
    When eu submeter um cadastro inválido
    Then a API retorna 400

  Scenario: Acesso protegido sem token retorna 401
    Given o serviço de usuarios está disponível em localhost:8084
    When eu consulto usuarios any-id sem autenticação
    Then a resposta deve ser 401

  Scenario: Patch de status sem token retorna 401
    Given o serviço de usuarios está disponível em localhost:8084
    When eu atualizo usuarios any-id status sem autenticação
    Then a resposta deve ser 401
