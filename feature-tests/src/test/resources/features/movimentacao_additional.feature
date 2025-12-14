# language: pt
Funcionalidade: Cenários adicionais de movimentacao

  Contexto:
    Dado os serviços de movimentação e auditoria estão disponíveis em localhost

  Cenário: Idempotency - mesmo X-Idempotency-Key retorna mesmo id
    Quando eu registro uma movimentacao valida para o produtor "idempo-1" com idempotency key "key-123"
    Então a API de movimentacao retorna 201
    E eu registro novamente a mesma movimentacao com idempotency key "key-123"
    Então a API de movimentacao retorna 201
    E os dois ids retornados são iguais

  Cenário: Anexo com hash inválido é rejeitado
    Quando eu registro uma movimentacao com anexo e hash invalido para o produtor "producer-err"
    Então a API de movimentacao retorna 400

  Cenário: Consultar movimentacao por id
    Quando eu registro uma movimentacao valida para o produtor "prod-get"
    Então a API de movimentacao retorna 201
    E eu consulto a movimentacao criada e recebo 200

  Cenário: Listagem paginada de movimentacoes por produtor
    Dado o banco está limpo para produtor "prod-list"
    Quando eu crio 3 movimentacoes validas para o produtor "prod-list"
    E eu solicito GET /produtores/prod-list/movimentacoes?page=0&size=2
    Então a resposta contém no máximo 2 itens e total >= 3

  Cenário: Histórico por commodity retorna movimentacoes recentes
    Dado o banco está limpo para produtor "prod-hist"
    Quando eu crio 2 movimentacoes validas para o produtor "prod-hist"
    E eu consulto o historico da commodity "commodity-1"
    Então o historico retorna pelo menos 2 movimentacoes
