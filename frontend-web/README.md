# ECO LEDGER - Frontend Web

Aplicação web frontend para o ECO LEDGER (A green Hub) - Plataforma de rastreabilidade para certificação e crédito verde.

## Stack Tecnológica

- **React 19** com TypeScript
- **Vite** - Build tool
- **React Router v7** - Roteamento
- **Zustand** - State management
- **Material-UI (MUI)** - UI library
- **Axios** - HTTP client
- **React Hook Form** + **Zod** - Form handling e validação

## Estrutura do Projeto

```
src/
├── api/              # Clientes HTTP para todos os serviços
├── features/         # Features organizadas por domínio
│   ├── auth/        # Autenticação (login, registro)
│   └── dashboard/   # Dashboards por perfil
├── router/           # Configuração de rotas e guards
├── store/            # State management (Zustand)
├── theme/            # Tema Material-UI
├── types/            # TypeScript types/interfaces
└── utils/            # Helpers e constantes
```

## Instalação

```bash
npm install
```

## Configuração

Crie um arquivo `.env` baseado no `.env.example`:

```bash
# URLs dos serviços backend (portas padrão)
VITE_USERS_API_URL=http://localhost:8084
VITE_MOVIMENTACOES_API_URL=http://localhost:8082
VITE_CERTIFICACAO_API_URL=http://localhost:8085
VITE_AUDITORIA_API_URL=http://localhost:8082
VITE_CREDITO_API_URL=http://localhost:8086
VITE_NOTIFICACOES_API_URL=http://localhost:8087

# Habilitar mock API para desenvolvimento sem backend
VITE_MOCK_API=false
```

### URLs dos Serviços Backend

- **Users Service**: `http://localhost:8084` (porta 8084, base path `/usuarios`)
- **Movimentação Service**: `http://localhost:8082` (porta 8082, base path `/`)
- **Certificação Service**: `http://localhost:8085` (porta 8085, base path `/selos`)
- **Auditoria Service**: `http://localhost:8082` (porta 8082, base path `/`)
- **Crédito Service**: `http://localhost:8086` (porta 8086, base path `/`)
- **Notificações Service**: `http://localhost:8087` (porta 8087, base path `/`)

## Desenvolvimento

```bash
npm run dev
```

A aplicação estará disponível em `http://localhost:5173`

## Build

```bash
npm run build
```

## Lint

```bash
npm run lint
```

## Funcionalidades Implementadas (Fase 1)

- ✅ Setup do projeto React com TypeScript e Vite
- ✅ Configuração de roteamento com React Router
- ✅ Configuração de autenticação (JWT, interceptors, guards)
- ✅ Clientes API para todos os serviços backend
- ✅ State management básico (Zustand)
- ✅ Design system / UI library (Material-UI)
- ✅ Telas de login e cadastro

## Próximos Passos (Fase 2)

- [ ] Dashboard do produtor completo
- [ ] Cadastro de commodities
- [ ] Registro de movimentações
- [ ] Lista e detalhe de movimentações
- [ ] Upload de anexos
- [ ] Consulta do selo verde

## Integração com Backend

O frontend está configurado para se comunicar com múltiplos microserviços backend. Cada serviço tem sua própria instância Axios configurada com a URL base apropriada.

### Estrutura de URLs

- **Users**: `/usuarios/*` (ex: `/usuarios/auth/login`, `/usuarios/{id}`)
- **Movimentações**: `/movimentacoes`, `/produtores/{id}/movimentacoes`, `/commodities/{id}/historico`, `/anexos/*`
- **Certificação**: `/selos/{producerId}`, `/selos/{producerId}/historico`
- **Auditoria**: `/auditorias/{id}`, `/produtores/{id}/historico-auditorias`
- **Crédito**: `/propostas`, `/solicitacoes-credito/*`
- **Notificações**: `/notificacoes/enviar`, `/preferencias/{userId}`

## Notas

- A aplicação requer que os serviços backend estejam rodando e acessíveis nas URLs configuradas
- O token JWT é armazenado no localStorage e enviado automaticamente em todas as requisições
- As rotas são protegidas baseadas no status de autenticação e role do usuário
- Para desenvolvimento sem backend, configure `VITE_MOCK_API=true` no `.env`
