# language: pt
Funcionalidade: Integração Certificacao (E2E)

  Contexto:
    Dado os serviços de movimentação, auditoria e certificacao estão disponíveis em localhost

  Cenário: Certificacao cria selo ativo após auditoria aprovada
    Quando eu anexo um arquivo valido para o produtor "producer-e2e-1"
    E eu registro uma movimentacao valida para o produtor "producer-e2e-1" via API de movimentacao
    Então a API de movimentacao retorna 201
    E o serviço de auditoria registra uma auditoria para o produtor "producer-e2e-1" dentro de 30 segundos
    Então o selo para o produtor "producer-e2e-1" tem status "ATIVO" dentro de 30 segundos

  Cenário: Certificacao cria selo inativo após auditoria reprovada
    Quando eu registro uma movimentacao invalida para o produtor "producer-e2e-2" via API de movimentacao
    Então a auditoria para o produtor "producer-e2e-2" é publicada com resultado "REPROVADO" dentro de 30 segundos
    Então o selo para o produtor "producer-e2e-2" tem status "INATIVO" dentro de 30 segundos
