# 🚀 Como Executar o Frontend v2 com Docker

## Início Rápido

```bash
# No diretório raiz do projeto
docker-compose -f docker-compose-ecoledger.yml up -d
```

Aguarde alguns minutos até todos os serviços subirem, então acesse:

**Frontend v2:** http://localhost:3001
**Frontend v1:** http://localhost:3000

## Ordem de Inicialização

O Docker Compose garante esta ordem:

1. ✅ Infraestrutura (Postgres, Kafka, Zookeeper, MinIO)
2. ✅ Kafka Init (criação de tópicos)
3. ✅ Microserviços Backend
   - users-service (porta 8084)
   - movimentacao-service (porta 8082)
   - auditoria-service (porta 8083) - com healthcheck
   - certificacao-service (porta 8085) - com healthcheck
4. ✅ **Frontend v2 (porta 3001)** - só inicia quando todos os backends estiverem prontos!

## Verificar Status

```bash
# Ver todos os serviços
docker-compose -f docker-compose-ecoledger.yml ps

# Ver logs do frontend-v2
docker logs -f ecoledger-frontend-v2

# Ver logs de todos os serviços
docker-compose -f docker-compose-ecoledger.yml logs -f
```

## Parar os Serviços

```bash
# Parar todos
docker-compose -f docker-compose-ecoledger.yml down

# Parar e remover volumes (limpa dados)
docker-compose -f docker-compose-ecoledger.yml down -v
```

## Rebuild do Frontend v2

Se você fez alterações no código:

```bash
# Rebuild e reiniciar apenas o frontend-v2
docker-compose -f docker-compose-ecoledger.yml up -d --build frontend-v2

# Rebuild tudo
docker-compose -f docker-compose-ecoledger.yml up -d --build
```

## Portas Utilizadas

| Serviço | URL |
|---------|-----|
| Frontend v2 | http://localhost:3001 |
| Frontend v1 | http://localhost:3000 |
| Users Service | http://localhost:8084 |
| Movimentacao Service | http://localhost:8082 |
| Auditoria Service | http://localhost:8083 |
| Certificacao Service | http://localhost:8085 |
| Kafka UI | http://localhost:8090 |
| MinIO Console | http://localhost:9001 |
| MailHog | http://localhost:8025 |

## Troubleshooting

### Frontend v2 não inicia

```bash
# 1. Verifique se os backends estão rodando
docker-compose -f docker-compose-ecoledger.yml ps

# 2. Verifique os logs
docker logs ecoledger-frontend-v2

# 3. Verifique os healthchecks
docker inspect ecoledger-auditoria-service | grep -A 20 Health
docker inspect ecoledger-certificacao-service | grep -A 20 Health
```

### Erro 502 Bad Gateway

Os backends ainda não estão prontos. Aguarde alguns minutos ou verifique os logs.

### Rebuild não funciona

```bash
# Force recreate
docker-compose -f docker-compose-ecoledger.yml up -d --force-recreate --build frontend-v2

# Ou remova e recrie
docker-compose -f docker-compose-ecoledger.yml rm -f frontend-v2
docker-compose -f docker-compose-ecoledger.yml up -d --build frontend-v2
```

## Documentação Completa

Para mais detalhes, consulte:
- [frontend-v2/DOCKER.md](./frontend-v2/DOCKER.md) - Documentação Docker completa
- [frontend-v2/README.md](./frontend-v2/README.md) - Documentação do projeto
- [frontend-v2/QUICKSTART.md](./frontend-v2/QUICKSTART.md) - Guia de desenvolvimento

---

✅ **O frontend-v2 está configurado para iniciar APENAS quando todos os microserviços estiverem prontos!**
