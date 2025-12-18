# ECO LEDGER - Frontend v2

Interface web moderna para o sistema ECO LEDGER de gestão sustentável e certificação verde.

## 🚀 Tecnologias

- **React 18** - Biblioteca UI
- **TypeScript** - Tipagem estática
- **Vite** - Build tool rápido
- **Tailwind CSS** - Framework CSS utility-first
- **shadcn/ui** - Componentes UI modernos
- **React Router** - Roteamento
- **Zustand** - Gerenciamento de estado
- **Axios** - Cliente HTTP
- **React Query** - Gerenciamento de dados assíncronos

## 📋 Funcionalidades

### Para Produtores
- ✅ Dashboard com métricas e certificação
- ✅ Gestão de movimentações de commodities
- ✅ Visualização de auditorias
- ✅ Acompanhamento de certificação verde
- ✅ Gerenciamento de perfil

### Para Auditores
- ✅ Dashboard de auditorias
- ✅ Revisão de movimentações
- ✅ Aplicação de aprovações/reprovações

### Para Analistas
- ✅ Visualização de métricas gerais
- ✅ Acompanhamento de certificações
- ✅ Análise de auditorias

## 🛠️ Instalação e Execução

### Pré-requisitos
- Node.js 20+
- npm ou yarn

### Desenvolvimento Local

```bash
# Instalar dependências
npm install

# Executar em modo desenvolvimento
npm run dev

# Build para produção
npm run build

# Preview da build
npm run preview
```

O frontend estará disponível em `http://localhost:3000`

### Com Docker

```bash
# Build da imagem
docker build -t ecoledger-frontend-v2 .

# Executar container
docker run -p 3000:80 ecoledger-frontend-v2
```

## 🔗 Integração com Backend

O frontend se conecta aos seguintes serviços:

- **users-service** (porta 8081) - Autenticação e gestão de usuários
- **movimentacao-service** (porta 8082) - Movimentações de commodities
- **auditoria-service** (porta 8083) - Auditorias e conformidade
- **certificacao-service** (porta 8084) - Selos verdes

### Configuração de Proxy (Desenvolvimento)

O Vite está configurado para fazer proxy das requisições `/api/*` para os serviços backend.
Veja configuração em `vite.config.ts`.

### Configuração Nginx (Produção)

O Nginx está configurado para rotear as requisições para os serviços corretos.
Veja configuração em `nginx.conf`.

## 📁 Estrutura do Projeto

```
frontend-v2/
├── src/
│   ├── components/
│   │   ├── layout/         # Layouts (Dashboard, etc)
│   │   └── ui/             # Componentes shadcn/ui
│   ├── pages/              # Páginas da aplicação
│   │   ├── LoginPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── MovimentacoesPage.tsx
│   │   ├── AuditoriasPage.tsx
│   │   ├── CertificacoesPage.tsx
│   │   └── PerfilPage.tsx
│   ├── services/           # API clients
│   │   ├── api.ts
│   │   ├── authService.ts
│   │   ├── movimentacaoService.ts
│   │   ├── auditoriaService.ts
│   │   └── certificacaoService.ts
│   ├── store/              # Estado global (Zustand)
│   │   └── authStore.ts
│   ├── router/             # Configuração de rotas
│   │   └── index.tsx
│   ├── lib/                # Utilitários
│   │   └── utils.ts
│   ├── index.css           # Estilos globais
│   └── main.tsx            # Entry point
├── public/                 # Assets estáticos
├── Dockerfile              # Build Docker
├── nginx.conf              # Configuração Nginx
├── vite.config.ts          # Configuração Vite
├── tailwind.config.js      # Configuração Tailwind
└── package.json
```

## 🎨 Design System

### Cores

- **Primary** (Verde): `hsl(142, 76%, 36%)` - Representa sustentabilidade
- **Secondary**: `hsl(210, 40%, 96.1%)`
- **Destructive** (Vermelho): `hsl(0, 84.2%, 60.2%)`
- **Muted**: `hsl(210, 40%, 96.1%)`

### Componentes UI

Utilizamos componentes do [shadcn/ui](https://ui.shadcn.com/):
- Button, Input, Label
- Card, Dialog, Toast
- Avatar, Separator, Tabs

## 🔐 Autenticação

O sistema utiliza JWT para autenticação:

1. Login via `/api/usuarios/auth/login`
2. Token armazenado em localStorage
3. Interceptor Axios adiciona token em todas requisições
4. Redirect automático para login em caso de 401

## 📱 Responsividade

O frontend é totalmente responsivo e funciona em:
- 📱 Mobile (320px+)
- 📱 Tablet (768px+)
- 💻 Desktop (1024px+)
- 🖥️ Large Desktop (1440px+)

## 🚦 Rotas

### Públicas
- `/login` - Página de login
- `/cadastro` - Página de cadastro

### Protegidas (requer autenticação)
- `/dashboard` - Dashboard principal
- `/movimentacoes` - Lista de movimentações
- `/movimentacoes/nova` - Nova movimentação
- `/auditorias` - Histórico de auditorias
- `/certificacoes` - Certificação verde
- `/perfil` - Perfil do usuário

## 🧪 Scripts Disponíveis

```bash
npm run dev          # Desenvolvimento
npm run build        # Build produção
npm run preview      # Preview da build
npm run lint         # Lint do código
```

## 📄 Licença

© 2024 ECO LEDGER. Todos os direitos reservados.

## 👥 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📞 Suporte

Para suporte, entre em contato através do sistema de issues do repositório.
