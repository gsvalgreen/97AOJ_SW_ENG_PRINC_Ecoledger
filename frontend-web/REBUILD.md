# Comandos para Rebuild do Frontend

## Problema
Quando você faz alterações no código do frontend, o Docker pode usar a imagem em cache e não refletir as mudanças.

## Solução: Rebuild Forçado

### Opção 1: Rebuild apenas o frontend (mais rápido)

```bash
# 1. Parar todos os serviços
docker-compose -f docker-compose-ecoledger.yml down

# 2. Remover a imagem antiga do frontend
docker rmi frontend-web:local

# 3. Rebuild sem cache
docker-compose -f docker-compose-ecoledger.yml build --no-cache frontend-web

# 4. Subir novamente
docker-compose -f docker-compose-ecoledger.yml up -d
```

### Opção 2: Rebuild de um serviço específico e restart

```bash
# Rebuild e restart apenas o frontend (mantém outros serviços rodando)
docker-compose -f docker-compose-ecoledger.yml up -d --build --force-recreate --no-deps frontend-web
```

### Opção 3: Rebuild completo de tudo (demorado)

```bash
# Rebuild sem cache de TODOS os serviços
docker-compose -f docker-compose-ecoledger.yml build --no-cache
docker-compose -f docker-compose-ecoledger.yml up -d
```

## Verificar se o Frontend está Rodando

```bash
# Ver logs do frontend
docker-compose -f docker-compose-ecoledger.yml logs -f frontend-web

# Verificar se o container está rodando
docker ps | grep frontend-web

# Acessar o frontend
http://localhost:3000
```

## Dica: Desenvolvimento Local

Para desenvolver sem precisar rebuild a cada mudança:

```bash
# Na pasta frontend-web
npm install
npm run dev

# O frontend rodará em http://localhost:5173 com hot-reload
# Use VITE_MOCK_API=true para trabalhar com mocks
```

## Problemas Comuns

### Frontend não atualiza após rebuild
- Limpe o cache do navegador (Ctrl+Shift+R ou Cmd+Shift+R)
- Abra em janela anônima
- Verifique se a imagem foi realmente recriada: `docker images | grep frontend-web`

### Erro de build "ENOENT" ou "permission denied"
- Feche o VSCode e outros editores que possam estar travando arquivos
- Execute: `docker system prune -a` (cuidado: remove todas as imagens não usadas)

### Container não inicia
```bash
# Ver logs detalhados
docker logs ecoledger-frontend-web

# Verificar se a porta 3000 está ocupada
netstat -ano | findstr :3000  # Windows
lsof -i :3000                 # Linux/Mac
```

## Limpeza Completa (quando tudo falhar)

```bash
# Parar tudo
docker-compose -f docker-compose-ecoledger.yml down -v

# Remover todas as imagens do projeto
docker rmi frontend-web:local movimentacao-service:local auditoria-service:local users-service:local certificacao-service:local

# Rebuild e start
docker-compose -f docker-compose-ecoledger.yml up -d --build
```

---

**Data:** 14/12/2025  
**Causa Raiz:** Docker usa cache de layers para otimizar builds. Quando você altera o código fonte, precisa forçar o rebuild sem cache.
