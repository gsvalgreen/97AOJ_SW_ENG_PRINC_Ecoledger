# language: pt
Funcionalidade: Serviço de usuários - cenários básicos

  Cenário: Produtor preenche e envia cadastro valido
    Dado o serviço de usuarios está disponível em localhost:8084
    Quando eu submeter um cadastro valido
    Então o serviço retorna 201 e eu consigo recuperar o cadastro criado

  Cenário: Reenvio com mesma Idempotency-Key retorna o mesmo cadastro
    Dado o serviço de usuarios está disponível em localhost:8084
    Quando eu submeter um cadastro valido
    Então o serviço retorna 201 e eu consigo recuperar o cadastro criado
    Quando eu reenviar o mesmo cadastro com a mesma idempotency key
    Então o serviço retorna o mesmo cadastroId

  Cenário: Produtor tenta enviar cadastro com dados incompletos
    Dado o serviço de usuarios está disponível em localhost:8084
    Quando eu submeter um cadastro inválido
    Então a API retorna 400

  Cenário: Acesso protegido sem token retorna 401
    Dado o serviço de usuarios está disponível em localhost:8084
    Quando eu consulto usuarios any-id sem autenticação
    Então a resposta deve ser 401

  Cenário: Patch de status sem token retorna 401
    Dado o serviço de usuarios está disponível em localhost:8084
    Quando eu atualizo usuarios any-id status sem autenticação
    Então a resposta deve ser 401
