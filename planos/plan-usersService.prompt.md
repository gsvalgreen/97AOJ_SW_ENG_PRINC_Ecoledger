Visão geral
- Token refresh e revogação podem ser adicionados depois como melhoria.
- Fornecer variáveis de configuração para `JWT_SECRET`, `KAFKA_BOOTSTRAP`, `NOTIFY_ENDPOINT`, `DATABASE_URL`.

Notas finais

8. Gerar OpenAPI (yaml) e documentação mínima README.
7. Implementar testes: unit (validação), integração (DB em memória), e2e (fluxo de registro+aprovação usando stubs para Notificação/Kafka).
6. Implementar autenticação JWT e middleware de autorização.
5. Integrar com serviço de Notificação via REST/Queue.
4. Publicar eventos para Kafka (user.registered / user.approved / user.rejected).
3. Persistir em UsersDB e criar migrations.
2. Implementar validação de payloads (JSON Schema).
1. Criar rotas HTTP conforme contratos.

Checklist para o agente (implementação automatizada)

- Migrações: versão inicial com colunas principais; script de backfill para registros antigos.
- Índices: email (único), documento (único), status
- Tabelas sugeridas: usuarios, cadastros, anexos_usuario, eventos_usuario

Estrutura de Dados e Migrações

- Analista sem permissão tenta aprovar retorna 403.
- Tentativa de acessar /usuarios/{id} sem token retorna 401.
- Tentativa de re-submissão idempotente (Idempotency-Key) deve retornar o mesmo cadastroId sem criar duplicata.
Cenários adicionais do serviço

    Then 200 OK, `user.rejected` publicado e e-mail enviado com motivo
    When PATCH /usuarios/{id}/status {status: "REJEITADO", reason}
    Given Analista autenticado
  Scenario: Analista rejeita cadastro com motivo

    Then 200 OK e evento `user.approved` publicado; Notificação enviada
    When PATCH /usuarios/{id}/status {status: "APROVADO"}
    Given Analista autenticado com scope admin:usuarios
  Scenario: Analista aprova cadastro pendente
Feature: Aprovar ou Rejeitar Cadastro de Produtor Rural

    Then 400 Bad Request com mensagem de validação
    When POST /cadastros
    Given payload com campos faltantes
  Scenario: Produtor tenta enviar cadastro com dados incompletos

    Then 201 Created e evento `user.registered` publicado
    When POST /cadastros
    Given formulário válido
  Scenario: Produtor preenche e envia cadastro válido
Feature: Solicitar Cadastro de Produtor Rural

Inclui cenários do `Projeto_1.md` mapeados ao serviço e casos específicos:

Cenários de Teste (Gherkin e unit/integration tests)

- Dados sensíveis (documento) devem ser criptografados-at-rest ou encriptados no DB.
- TLS obrigatório para todas as comunicações externas.
- Scopes: usuarios:read, usuarios:write, cadastros:write, admin:usuarios
- Autenticação via JWT (Bearer). Claims: sub=userId, role, scopes.

Políticas de segurança e autorização

- UsersDB: persistência (ex: PostgreSQL) para usuários e registros de cadastro.
- Kafka (ou outra fila): publicar eventos no tópico `usuarios.*`.
- Notificação: enviar templates para aprovação/rejeição.

Integrações

  - Payload: { usuarioId, status, reason?, timestamp }
  - Emissão: após PATCH /usuarios/{id}/status
- user.approved / user.rejected

  - Payload: { cadastroId, candidatoUsuario: { nome, email, documento, role }, submetidoEm }
  - Emissão: após POST /cadastros com sucesso.
- user.registered

Eventos (assíncronos)

- AuthToken: { accessToken, refreshToken, expiresIn }
- Cadastro: { id, candidatoUsuario: Usuario (parcial), anexos: [ { tipo, url } ], submetidoEm }
- Usuario: { id, nome, email, role: [produtor|analista|auditor], documento, status: [PENDENTE|APROVADO|REJEITADO], criadoEm }

Modelos (JSON Schema simplificado)

  - Respostas: 200 OK { usuario } | 400 | 403
  - Payload: { status: "APROVADO" | "REJEITADO", reason? }
  - Descrição: Atualiza o status do usuário (uso administrativo por Analista).
- PATCH /usuarios/{id}/status

  - Respostas: 200 OK { usuario } | 403 | 404
  - Autorização: Bearer token (scopes por role)
  - Descrição: Obtém perfil do usuário.
- GET /usuarios/{id}

  - Respostas: 200 OK { accessToken, refreshToken, expiresIn } | 401 Unauthorized
  - Payload: { email, password }
  - Descrição: Autenticação com email + senha (ou credenciais definida pelo client).
- POST /auth/login

  - Respostas: 200 OK { cadastro } | 404 Not Found
  - Descrição: Recupera dados do pedido de cadastro.
- GET /cadastros/{id}

  - Cabeçalhos: Idempotency-Key (opcional)
  - Respostas: 201 Created { cadastroId, status: "PENDENTE" } | 400 Bad Request
  - Payload (application/json): { nome, email, documento, role, dadosFazenda?, anexos? }
  - Descrição: Submete novo cadastro de usuário.
- POST /cadastros

API REST (contratos principais)

- Integração com serviço de Notificação para avisos (aprovação, rejeição).
- Emissão de eventos assíncronos: `user.registered`, `user.approved`, `user.rejected`.
- Gestão de perfis (leitura/atualização) e alteração de status (pendente, aprovado, rejeitado).
- Autenticação (login) e gerenciamento de sessão (JWT/OAuth2 bearer).
- Registro de novos usuários (Produtor, Analista, Auditor).

Responsabilidades

Este documento é um plano pronto para alimentar um agente de implementação do "Serviço de Usuários (Cadastro)" do projeto ECO LEDGER. Contém responsabilidades, contratos REST, modelos de dados, eventos assíncronos, cenários de teste (incluindo os do `Projeto_1.md`) e uma checklist detalhada para implementação automática.
