# Docker Setup - Frontend v2

Este documento descreve como executar o frontend-v2 com Docker e Docker Compose.

## Pré-requisitos

- Docker 20.10+
- Docker Compose 2.0+

## Executar com Docker Compose (Recomendado)

O frontend-v2 está integrado ao docker-compose principal do projeto.

### 1. Build e Start de Todo o Stack

```bash
# No diretório raiz do projeto
docker-compose -f docker-compose-ecoledger.yml up -d
```

Isso irá:
1. Subir todos os serviços de infraestrutura (Kafka, Postgres, MinIO, etc)
2. Subir os 4 microserviços backend
3. Aguardar que todos os serviços estejam prontos
4. Subir o frontend-v2 na porta **3001**

### 2. Acessar o Frontend

```
http://localhost:3000
```

### 3. Verificar Status

```bash
# Ver logs do frontend-v2
docker logs -f ecoledger-frontend-v2

# Ver status de todos os serviços
docker-compose -f docker-compose-ecoledger.yml ps
```

### 4. Parar os Serviços

```bash
# Parar todos os serviços
docker-compose -f docker-compose-ecoledger.yml down

# Parar e remover volumes
docker-compose -f docker-compose-ecoledger.yml down -v
```

## Executar Frontend v2 Isoladamente

Se você quiser executar apenas o frontend-v2 (assumindo que os backends já estão rodando):

```bash
# Build da imagem
cd frontend-v2
docker build -t frontend-v2:local .

# Executar container
docker run -d \
  --name ecoledger-frontend-v2 \
  --network ecoledger-fullstack_ecoledger \
  -p 3001:80 \
  frontend-v2:local
```

## Portas Utilizadas

| Serviço | Porta Host | Porta Container |
|---------|-----------|-----------------|
| Frontend v1 | 3000 | 80 |
| **Frontend v2** | **3001** | **80** |
| Users Service | 8084 | 8080 |
| Movimentacao Service | 8082 | 8080 |
| Auditoria Service | 8083 | 8082 |
| Certificacao Service | 8085 | 8085 |

## Dependências

O frontend-v2 depende dos seguintes serviços e **só inicia após eles estarem prontos**:

✅ **users-service** - Porta interna: 8080
✅ **movimentacao-service** - Porta interna: 8080  
✅ **auditoria-service** - Porta interna: 8082 (com healthcheck)
✅ **certificacao-service** - Porta interna: 8085 (com healthcheck)
✅ **postgres** - Banco de dados
✅ **kafka** - Mensageria

## Configuração do Nginx

O Nginx está configurado para fazer proxy reverso para os serviços backend:

- `/api/usuarios/*` → `users-service:8080`
- `/api/movimentacoes/*` → `movimentacao-service:8080`
- `/api/produtores/*` → `movimentacao-service:8080`
- `/api/commodities/*` → `movimentacao-service:8080`
- `/api/anexos/*` → `movimentacao-service:8080`
- `/api/auditorias/*` → `auditoria-service:8082`
- `/api/selos/*` → `certificacao-service:8085`

## Troubleshooting

### Frontend não inicia

1. Verifique se todos os backends estão rodando:
```bash
docker-compose -f docker-compose-ecoledger.yml ps
```

2. Verifique os logs:
```bash
docker logs ecoledger-frontend-v2
```

### Erro 502 Bad Gateway

Isso significa que o Nginx não consegue se conectar aos backends.

Verifique:
1. Se os serviços backend estão rodando
2. Se os serviços estão na mesma rede Docker
3. Se as portas internas estão corretas

```bash
# Verificar rede
docker network inspect ecoledger-fullstack_ecoledger

# Testar conectividade de dentro do container
docker exec ecoledger-frontend-v2 wget -O- http://users-service:8080/usuarios/cadastros
```

### Erro de CORS

O CORS está configurado nos backends. Se houver problemas:

1. Verifique se os backends têm CORS habilitado
2. Verifique se o origin está correto (http://localhost:3000)

### Rebuild do Frontend

Se você fez alterações no código:

```bash
# Rebuild apenas o frontend-v2
docker-compose -f docker-compose-ecoledger.yml up -d --build frontend-v2

# Ou rebuild tudo
docker-compose -f docker-compose-ecoledger.yml up -d --build
```

## Healthchecks

O frontend-v2 não possui healthcheck próprio, mas depende dos healthchecks dos backends:

- **auditoria-service**: `wget http://localhost:8082/actuator/health`
- **certificacao-service**: `wget http://localhost:8085/actuator/health`

## Comandos Úteis

```bash
# Ver logs em tempo real
docker logs -f ecoledger-frontend-v2

# Entrar no container
docker exec -it ecoledger-frontend-v2 sh

# Reiniciar apenas o frontend
docker-compose -f docker-compose-ecoledger.yml restart frontend-v2

# Remover e recriar o frontend
docker-compose -f docker-compose-ecoledger.yml up -d --force-recreate frontend-v2

# Ver uso de recursos
docker stats ecoledger-frontend-v2
```

## Performance

O frontend-v2 é otimizado para produção:

- ✅ Build minificado (Vite)
- ✅ Gzip comprimido (Nginx)
- ✅ Cache de assets estáticos (1 ano)
- ✅ SPA fallback configurado
- ✅ Security headers habilitados

## Variáveis de Ambiente

O frontend-v2 não requer variáveis de ambiente específicas em runtime, pois:

1. O build é feito em tempo de construção da imagem
2. O roteamento de API é feito pelo Nginx
3. Todas as configurações estão no nginx.conf

## Próximos Passos

Após ter o frontend-v2 rodando no Docker:

1. Acesse http://localhost:3000
2. Faça login ou crie uma conta
3. Explore as funcionalidades
4. Verifique os logs dos backends para ver as requisições

## Suporte

Se encontrar problemas:

1. Verifique os logs: `docker logs ecoledger-frontend-v2`
2. Verifique o status: `docker-compose -f docker-compose-ecoledger.yml ps`
3. Teste os backends diretamente nas suas portas
4. Consulte o README.md principal

---

Desenvolvido com ❤️ para ECO LEDGER
