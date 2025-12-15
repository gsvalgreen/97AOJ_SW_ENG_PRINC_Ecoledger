# ✅ Teste de Login - PASSO A PASSO

## Status dos Serviços

✅ **users-service** rodando na porta 8084
✅ **frontend-web** rodando na porta 3000
✅ **JWT válido** sendo gerado corretamente

## Validação do JWT

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoicHJvZHV0b3Iu...",
  "refreshToken": "...",
  "expiresIn": 3600
}
```

**Payload decodificado:**
```json
{
  "role": "produtor",
  "type": "access",
  "userId": "45d70372-5056-46b5-b8af-4657391dff91",
  "email": "jane.doe@exemple.com",
  "sub": "45d70372-5056-46b5-b8af-4657391dff91",
  "iat": 1765758374,
  "exp": 1765761974
}
```

## 🧪 Como Testar

### 1. Abrir a aplicação

```
http://localhost:3000/login
```

### 2. Abrir DevTools (F12)

- Ir para a aba **Console**
- Limpar console (Ctrl+L)

### 3. Fazer Login

**Credenciais:**
- **Email:** `jane.doe@exemple.com`
- **Senha:** `123456`

### 4. Observar Logs no Console

Você deve ver:

```
[LOGIN] Iniciando login com: {email: "jane.doe@exemple.com"}
[LOGIN] Token recebido: {accessToken: "eyJhbGci...", refreshToken: "...", expiresIn: 3600}
[LOGIN] Token parts: 3
[LOGIN] Token payload: {role: "produtor", type: "access", userId: "45d70...", email: "...", ...}
[LOGIN] User ID extraído: 45d70372-5056-46b5-b8af-4657391dff91
[LOGIN] Buscando dados do usuário: 45d70372-5056-46b5-b8af-4657391dff91
[LOGIN] Dados do usuário: {id: "45d70...", nome: "Jane Doe", role: "produtor", ...}
[LOGIN] Auth state atualizado
[LOGIN] Redirecionando para dashboard: produtor
```

### 5. Resultado Esperado

- ✅ Nenhum erro no console
- ✅ Redirecionamento para `/dashboard/produtor`
- ✅ LocalStorage contém:
  - `ecoledger_access_token`: JWT válido
  - `ecoledger_user`: Dados do usuário

## 🔍 Verificar LocalStorage

No console do DevTools, execute:

```javascript
// Ver token
localStorage.getItem('ecoledger_access_token')

// Ver usuário
JSON.parse(localStorage.getItem('ecoledger_user'))

// Decodificar token
const token = localStorage.getItem('ecoledger_access_token');
const payload = JSON.parse(atob(token.split('.')[1]));
console.log('Payload:', payload);
```

## ❌ Se Houver Erro

### Erro: CORS

**Sintoma:**
```
Access to fetch at 'http://localhost:8084/usuarios/auth/login' from origin 
'http://localhost:3000' has been blocked by CORS policy
```

**Solução:**
```bash
# Verificar logs do users-service
docker logs ecoledger-users-service --tail 50

# Se necessário, rebuild
docker-compose -f docker-compose-ecoledger.yml restart users-service
```

### Erro: 404 Not Found

**Sintoma:** `POST http://localhost:8084/usuarios/auth/login 404`

**Solução:**
```bash
# Verificar se o serviço está rodando
docker ps | findstr users

# Verificar logs
docker logs ecoledger-users-service --tail 50
```

### Erro: atob (se ainda ocorrer)

**Sintoma:** `Failed to execute 'atob' on 'Window'`

**Causa:** Token ainda é fake (não rebuilded)

**Solução:**
```bash
# Verificar token retornado
curl http://localhost:8084/usuarios/auth/login \
  -d '{"email":"jane.doe@exemple.com","password":"123456"}'

# Se retornar "access.{uuid}", rebuild:
docker-compose -f docker-compose-ecoledger.yml stop users-service
docker-compose -f docker-compose-ecoledger.yml build --no-cache users-service
docker-compose -f docker-compose-ecoledger.yml up -d users-service
```

## 📊 Testes Adicionais

### Testar API diretamente (PowerShell):

```powershell
# Login
$body = '{"email":"jane.doe@exemple.com","password":"123456"}'
$response = Invoke-WebRequest -Uri "http://localhost:8084/usuarios/auth/login" `
  -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
$data = $response.Content | ConvertFrom-Json
$token = $data.accessToken

# Buscar usuário
$userId = "45d70372-5056-46b5-b8af-4657391dff91"
$headers = @{ Authorization = "Bearer $token" }
Invoke-WebRequest -Uri "http://localhost:8084/usuarios/$userId" `
  -Headers $headers -UseBasicParsing | Select-Object -ExpandProperty Content
```

## ✅ Checklist Final

- [x] users-service rodando (porta 8084)
- [x] frontend-web rodando (porta 3000)
- [x] JWT válido sendo gerado (3 partes, base64)
- [x] Payload contém userId, email, role
- [ ] Login no frontend funciona
- [ ] Redirecionamento para dashboard correto
- [ ] LocalStorage atualizado
- [ ] Nenhum erro no console

---

**Data:** 14/12/2025 21:25
**Status:** ✅ Backend pronto, aguardando teste no frontend
**Próximo:** Testar login em http://localhost:3000/login
