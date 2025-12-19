# ✅ Frontend v2 - Integração Docker Completa

## 🎉 O que foi configurado

O **frontend-v2** foi adicionado ao `docker-compose-ecoledger.yml` com:

### ✅ Dependências Corretas
O frontend-v2 **só inicia** após todos os microserviços estarem prontos:
- ✅ `users-service` (porta 8080 interna)
- ✅ `movimentacao-service` (porta 8080 interna)
- ✅ `auditoria-service` (porta 8082 interna) - com healthcheck
- ✅ `certificacao-service` (porta 8085 interna) - com healthcheck
- ✅ `postgres` - banco de dados
- ✅ `kafka` - mensageria

### ✅ Configuração do Nginx
O Nginx foi corrigido para usar as **portas internas corretas**:
- `/api/usuarios/*` → `users-service:8080`
- `/api/movimentacoes/*` → `movimentacao-service:8080`
- `/api/produtores/*` → `movimentacao-service:8080`
- `/api/commodities/*` → `movimentacao-service:8080`
- `/api/anexos/*` → `movimentacao-service:8080`
- `/api/auditorias/*` → `auditoria-service:8082`
- `/api/selos/*` → `certificacao-service:8085`

### ✅ Porta Externa
- **Frontend v2**: http://localhost:**3001** (porta 3001 no host → porta 80 no container)
- **Frontend v1**: http://localhost:3000 (mantido)

---

## 🚀 Como Usar

### 1. Subir todo o stack

```bash
# Windows PowerShell ou Linux/Mac
docker-compose -f docker-compose-ecoledger.yml up -d
```

### 2. Aguardar inicialização

O frontend-v2 aguardará automaticamente todos os backends ficarem prontos. Isso pode levar **2-5 minutos**.

### 3. Validar instalação

**Windows PowerShell:**
```powershell
.\validate-frontend-v2.ps1
```

**Linux/Mac:**
```bash
bash validate-frontend-v2.sh
```

### 4. Acessar

- **Frontend v2**: http://localhost:3001 ✨
- **Frontend v1**: http://localhost:3000

---

## 📊 Verificar Status

```bash
# Ver todos os serviços
docker-compose -f docker-compose-ecoledger.yml ps

# Ver logs do frontend-v2
docker logs -f ecoledger-frontend-v2

# Ver logs de todos os serviços
docker-compose -f docker-compose-ecoledger.yml logs -f
```

---

## 🔧 Comandos Úteis

### Parar serviços
```bash
docker-compose -f docker-compose-ecoledger.yml down
```

### Parar e limpar tudo (incluindo volumes)
```bash
docker-compose -f docker-compose-ecoledger.yml down -v
```

### Rebuild do frontend-v2 (após alterações)
```bash
# Rebuild apenas frontend-v2
docker-compose -f docker-compose-ecoledger.yml up -d --build frontend-v2

# Rebuild tudo
docker-compose -f docker-compose-ecoledger.yml up -d --build
```

### Reiniciar frontend-v2
```bash
docker-compose -f docker-compose-ecoledger.yml restart frontend-v2
```

### Force recreate (se algo der errado)
```bash
docker-compose -f docker-compose-ecoledger.yml up -d --force-recreate frontend-v2
```

---

## 🐛 Troubleshooting

### Frontend v2 não inicia

1. Verifique se os backends estão rodando:
```bash
docker-compose -f docker-compose-ecoledger.yml ps
```

2. Verifique os logs:
```bash
docker logs ecoledger-frontend-v2
```

3. Verifique os healthchecks dos backends:
```bash
# Auditoria
docker exec ecoledger-auditoria-service wget -O- http://localhost:8082/actuator/health

# Certificação
docker exec ecoledger-certificacao-service wget -O- http://localhost:8085/actuator/health
```

### Erro 502 Bad Gateway

Isso significa que o Nginx não consegue conectar aos backends.

**Possíveis causas:**
1. Backends ainda estão iniciando (aguarde 2-5 min)
2. Backends não estão na mesma rede Docker
3. Portas internas estão incorretas

**Teste conectividade:**
```bash
# De dentro do container do frontend
docker exec ecoledger-frontend-v2 wget -O- http://users-service:8080/actuator/health
docker exec ecoledger-frontend-v2 wget -O- http://movimentacao-service:8080/actuator/health
docker exec ecoledger-frontend-v2 wget -O- http://auditoria-service:8082/actuator/health
docker exec ecoledger-frontend-v2 wget -O- http://certificacao-service:8085/actuator/health
```

### Rebuild não funciona

```bash
# 1. Pare tudo
docker-compose -f docker-compose-ecoledger.yml down

# 2. Remova a imagem antiga
docker rmi frontend-v2:local

# 3. Rebuild e suba novamente
docker-compose -f docker-compose-ecoledger.yml up -d --build
```

---

## 📚 Documentação Adicional

- **[FRONTEND-V2-DOCKER.md](./FRONTEND-V2-DOCKER.md)** - Guia rápido Docker
- **[frontend-v2/DOCKER.md](./frontend-v2/DOCKER.md)** - Documentação completa Docker
- **[frontend-v2/README.md](./frontend-v2/README.md)** - Documentação do projeto
- **[frontend-v2/QUICKSTART.md](./frontend-v2/QUICKSTART.md)** - Guia de desenvolvimento
- **[frontend-v2/INSTALLATION.md](./frontend-v2/INSTALLATION.md)** - Instalação local

---

## 🎯 Portas de Todos os Serviços

| Serviço | Porta Host | Porta Container | URL |
|---------|-----------|-----------------|-----|
| **Frontend v2** | **3001** | **80** | http://localhost:3001 |
| Frontend v1 | 3000 | 80 | http://localhost:3000 |
| Users Service | 8084 | 8080 | http://localhost:8084 |
| Movimentacao Service | 8082 | 8080 | http://localhost:8082 |
| Auditoria Service | 8083 | 8082 | http://localhost:8083 |
| Certificacao Service | 8085 | 8085 | http://localhost:8085 |
| Postgres | 5432 | 5432 | localhost:5432 |
| Kafka | 9092 | 9092 | localhost:9092 |
| Kafka UI | 8090 | 8080 | http://localhost:8090 |
| MinIO API | 9000 | 9000 | http://localhost:9000 |
| MinIO Console | 9001 | 9001 | http://localhost:9001 |
| MailHog | 8025 | 8025 | http://localhost:8025 |
| Schema Registry | 8081 | 8081 | http://localhost:8081 |
| WireMock | 8089 | 8080 | http://localhost:8089 |

---

## ✨ Características do Frontend v2

### 🎨 Design Moderno
- Interface limpa e moderna com Tailwind CSS
- Componentes shadcn/ui (design system de alta qualidade)
- Tema verde sustentável
- 100% responsivo (mobile, tablet, desktop)

### 🚀 Performance
- Build otimizado com Vite
- Gzip comprimido
- Cache de assets estáticos
- Code splitting

### 🔐 Segurança
- Security headers no Nginx
- JWT authentication
- CORS configurado
- XSS protection

### 📱 Funcionalidades
- ✅ Dashboard com métricas
- ✅ Gestão de movimentações
- ✅ Visualização de auditorias
- ✅ Acompanhamento de certificação verde
- ✅ Gerenciamento de perfil

---

## 🎓 Para Desenvolvedores

Se você quer desenvolver localmente (sem Docker):

1. Instale as dependências:
```bash
cd frontend-v2
npm install
```

2. Execute em modo desenvolvimento:
```bash
npm run dev
```

3. Acesse: http://localhost:3000

**Nota**: Certifique-se de que os backends estão rodando nas portas:
- users-service: 8081 (ou configure no vite.config.ts)
- movimentacao-service: 8082
- auditoria-service: 8083
- certificacao-service: 8084

---

## ✅ Checklist de Validação

Após rodar `docker-compose up -d`, verifique:

- [ ] Infraestrutura está rodando (Postgres, Kafka, MinIO)
- [ ] Todos os 4 microserviços estão rodando
- [ ] Healthchecks dos backends estão OK
- [ ] Frontend v2 está rodando
- [ ] Você consegue acessar http://localhost:3001
- [ ] Você consegue fazer login/cadastro
- [ ] As APIs estão respondendo corretamente

---

## 🆘 Suporte

Se encontrar problemas:

1. **Execute o script de validação**:
   - Windows: `.\validate-frontend-v2.ps1`
   - Linux/Mac: `bash validate-frontend-v2.sh`

2. **Verifique os logs**:
   ```bash
   docker logs ecoledger-frontend-v2
   docker logs ecoledger-users-service
   docker logs ecoledger-movimentacao-service
   docker logs ecoledger-auditoria-service
   docker logs ecoledger-certificacao-service
   ```

3. **Verifique o status**:
   ```bash
   docker-compose -f docker-compose-ecoledger.yml ps
   ```

4. **Teste os backends diretamente**:
   - http://localhost:8084/actuator/health (users)
   - http://localhost:8082/actuator/health (movimentacao)
   - http://localhost:8083/actuator/health (auditoria)
   - http://localhost:8085/actuator/health (certificacao)

---

## 🎉 Pronto!

O frontend-v2 está configurado e pronto para uso com Docker! 

**Comando único para subir tudo:**
```bash
docker-compose -f docker-compose-ecoledger.yml up -d
```

**Acesse:**
- Frontend v2: http://localhost:3001 ✨
- Frontend v1: http://localhost:3000

---

Desenvolvido com ❤️ para ECO LEDGER
