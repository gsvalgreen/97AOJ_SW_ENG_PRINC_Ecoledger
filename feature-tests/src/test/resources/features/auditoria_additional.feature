# language: pt
Funcionalidade: Cenários adicionais de auditoria

  Contexto:
    Dado os serviços de movimentação e auditoria estão disponíveis em localhost

  Cenário: Revisão manual altera o resultado da auditoria
    Quando eu registro uma movimentacao valida para o produtor "producer-review"
    Então a API de movimentacao retorna 201
    E o serviço de auditoria registra uma auditoria para o produtor "producer-review" dentro de 30 segundos
    Quando eu aplico uma revisao manual para a primeira auditoria do produtor "producer-review" com auditor "auditor-1" e resultado "REPROVADO"
    Então a auditoria para o produtor "producer-review" é publicada com resultado "REPROVADO" dentro de 30 segundos
