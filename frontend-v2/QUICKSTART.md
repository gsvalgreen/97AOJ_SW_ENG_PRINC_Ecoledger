# Guia de Início Rápido - ECO LEDGER Frontend v2

Este guia vai ajudá-lo a configurar e executar o frontend v2 do ECO LEDGER.

## Pré-requisitos

- Node.js 20 ou superior
- npm ou yarn
- Os 4 microserviços backend rodando (users, movimentacao, auditoria, certificacao)

## Passo 1: Instalação

```bash
cd frontend-v2
npm install
```

## Passo 2: Configuração

### Opção A: Desenvolvimento Local (Recomendado)

O Vite já está configurado para fazer proxy das requisições. Certifique-se de que os serviços estão rodando nas portas:

- users-service: `http://localhost:8081`
- movimentacao-service: `http://localhost:8082`
- auditoria-service: `http://localhost:8083`
- certificacao-service: `http://localhost:8084`

### Opção B: Configuração Manual

Crie um arquivo `.env` baseado no `.env.example`:

```bash
cp .env.example .env
```

Edite o arquivo `.env` se necessário.

## Passo 3: Executar

```bash
npm run dev
```

O frontend estará disponível em `http://localhost:3000`

## Passo 4: Teste

### Criar uma conta
1. Acesse `http://localhost:3000/cadastro`
2. Preencha o formulário de cadastro
3. Escolha o tipo de usuário (Produtor, Analista ou Auditor)

### Fazer login
1. Acesse `http://localhost:3000/login`
2. Use as credenciais criadas
3. Você será redirecionado para o dashboard

### Testar funcionalidades

#### Para Produtores:
- ✅ Visualizar dashboard com métricas
- ✅ Criar nova movimentação
- ✅ Ver histórico de movimentações
- ✅ Acompanhar auditorias
- ✅ Ver certificação verde

#### Para Auditores:
- ✅ Ver dashboard de auditorias
- ✅ Revisar movimentações

#### Para Analistas:
- ✅ Ver métricas gerais
- ✅ Acompanhar certificações

## Build para Produção

```bash
# Build
npm run build

# Preview da build
npm run preview
```

## Docker

```bash
# Build da imagem
docker build -t ecoledger-frontend-v2 .

# Executar
docker run -p 3000:80 ecoledger-frontend-v2
```

## Troubleshooting

### Erro de conexão com backend

Verifique se os serviços backend estão rodando:

```bash
# Testar users-service
curl http://localhost:8081/usuarios/cadastros

# Testar movimentacao-service
curl http://localhost:8082/movimentacoes

# Testar auditoria-service
curl http://localhost:8083/auditorias

# Testar certificacao-service
curl http://localhost:8084/selos
```

### Erro de CORS

Se você estiver tendo problemas de CORS, certifique-se de que os serviços backend têm CORS habilitado para `http://localhost:3000`.

### Página em branco

1. Abra o console do navegador (F12)
2. Verifique se há erros JavaScript
3. Limpe o cache do navegador
4. Tente em modo anônimo/privado

## Funcionalidades Principais

### 🔐 Autenticação
- Login com email e senha
- JWT token armazenado localmente
- Auto-redirect em caso de sessão expirada

### 📊 Dashboard
- Métricas em tempo real
- Visualização de certificação
- Ações rápidas

### 📦 Movimentações
- Criar movimentações de commodities
- Filtrar por período e tipo
- Upload de anexos (em desenvolvimento)

### 🔍 Auditorias
- Histórico completo
- Status de conformidade
- Detalhes de evidências

### 🏆 Certificações
- Visualização do selo verde
- Histórico de alterações
- Recálculo manual

### 👤 Perfil
- Edição de dados pessoais
- Visualização de status da conta

## Tecnologias Utilizadas

- **React 18** - Framework UI
- **TypeScript** - Tipagem
- **Vite** - Build tool
- **Tailwind CSS** - Estilização
- **shadcn/ui** - Componentes
- **Zustand** - State management
- **React Router** - Roteamento
- **Axios** - HTTP client
- **React Query** - Data fetching

## Próximos Passos

Após ter o frontend rodando:

1. Explore todas as funcionalidades
2. Teste os diferentes tipos de usuário
3. Verifique a integração com os backends
4. Reporte bugs ou sugestões

## Suporte

Se encontrar problemas:

1. Verifique os logs do console do navegador
2. Verifique os logs dos serviços backend
3. Consulte o README.md principal
4. Abra uma issue no repositório

Desenvolvido com ❤️ para ECO LEDGER
