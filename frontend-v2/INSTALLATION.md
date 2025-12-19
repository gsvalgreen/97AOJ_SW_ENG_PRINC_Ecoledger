# Instalação das Dependências - Frontend v2

## Passo a Passo

### 1. Navegue até o diretório
```bash
cd frontend-v2
```

### 2. Instale as dependências
```bash
npm install
```

### 3. Aguarde a instalação
Isso pode levar alguns minutos dependendo da sua conexão.

### 4. Verifique a instalação
```bash
npm list --depth=0
```

Você deve ver todas as dependências listadas no package.json.

## Dependências Principais

### Produção
- react@^18.2.0
- react-dom@^18.2.0
- react-router-dom@^6.21.1
- @tanstack/react-query@^5.17.19
- axios@^1.6.5
- zustand@^4.4.7
- lucide-react@^0.309.0
- date-fns@^3.0.6
- clsx@^2.1.0
- tailwind-merge@^2.2.0
- class-variance-authority@^0.7.0

### Radix UI (Componentes)
- @radix-ui/react-alert-dialog@^1.0.5
- @radix-ui/react-avatar@^1.0.4
- @radix-ui/react-dialog@^1.0.5
- @radix-ui/react-dropdown-menu@^2.0.6
- @radix-ui/react-label@^2.0.2
- @radix-ui/react-select@^2.0.0
- @radix-ui/react-separator@^1.0.3
- @radix-ui/react-slot@^1.0.2
- @radix-ui/react-tabs@^1.0.4
- @radix-ui/react-toast@^1.1.5

### Desenvolvimento
- @vitejs/plugin-react@^4.2.1
- vite@^5.0.11
- typescript@^5.3.3
- tailwindcss@^3.4.1
- autoprefixer@^10.4.16
- postcss@^8.4.33
- eslint@^8.56.0

## Problemas Comuns

### Erro: EACCES (Permission denied)

**Windows (PowerShell como Administrador):**
```powershell
npm cache clean --force
npm install
```

**Linux/Mac:**
```bash
sudo npm cache clean --force
sudo npm install
```

Ou use nvm para gerenciar Node.js sem sudo:
```bash
# Instalar nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash

# Instalar Node.js via nvm
nvm install 20
nvm use 20

# Agora instale sem sudo
npm install
```

### Erro: network timeout

Tente usar outro registry:
```bash
npm config set registry https://registry.npmjs.org/
npm install
```

Ou aumente o timeout:
```bash
npm config set fetch-timeout 60000
npm install
```

### Erro: peer dependencies

Force a instalação:
```bash
npm install --legacy-peer-deps
```

### Erro: insufficient space

Limpe o cache:
```bash
npm cache clean --force
```

E remova node_modules se existir:
```bash
rm -rf node_modules package-lock.json
npm install
```

## Verificação Final

Após a instalação bem-sucedida, teste se tudo está funcionando:

```bash
# Teste o TypeScript
npx tsc --version

# Teste o Vite
npx vite --version

# Inicie o servidor de desenvolvimento
npm run dev
```

Se o servidor iniciar sem erros e você conseguir acessar http://localhost:3000, a instalação foi bem-sucedida! ✅

## Próximos Passos

Após instalar as dependências, consulte o [QUICKSTART.md](./QUICKSTART.md) para instruções de como executar o projeto.
