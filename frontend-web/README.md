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
VITE_API_BASE_URL=http://localhost:8080
```

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

## Notas

- A aplicação requer que o backend esteja rodando e acessível na URL configurada em `VITE_API_BASE_URL`
- O token JWT é armazenado no localStorage
- As rotas são protegidas baseadas no status de autenticação e role do usuário
