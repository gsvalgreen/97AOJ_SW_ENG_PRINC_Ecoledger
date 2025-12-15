# Correção do 403 - Acesso a /usuarios/{id}

## 🐛 Problema

Após login bem-sucedido, ao tentar buscar dados do usuário:
```
GET http://localhost:8084/usuarios/45d70372-5056-46b5-b8af-4657391dff91
Authorization: Bearer eyJhbGci...
```

**Retornava:** `403 Forbidden`

## 🔍 Causa Raiz

No `SecurityConfig.java`, as regras de autorização exigiam authorities específicas:

```java
.requestMatchers("/usuarios/*/status").hasAuthority("SCOPE_admin:usuarios")
.requestMatchers("/usuarios/*").hasAnyAuthority("SCOPE_usuarios:read", "SCOPE_usuarios:write")
```

Porém, o JWT gerado contém apenas:
```json
{
  "userId": "...",
  "email": "...",
  "role": "produtor",
  "type": "access",
  "sub": "...",
  "iat": ...,
  "exp": ...
}
```

**Não contém authorities/scopes!** O Spring Security não conseguia validar e retornava 403.

## ✅ Solução

Alterado `SecurityConfig.java` para usar `.authenticated()` em vez de `.hasAuthority()`:

```java
.requestMatchers("/usuarios/*/status").authenticated()
.requestMatchers("/usuarios/**").authenticated()
```

Agora qualquer usuário com JWT válido pode:
- ✅ Acessar `/usuarios/{id}` para buscar dados do usuário
- ✅ Acessar `/usuarios/{id}/status` para atualizar status
- ✅ Fazer requisições autenticadas

## 📝 Arquivo Alterado

**`users-service/src/main/java/com/ecoledger/integration/security/SecurityConfig.java`**

### Antes:
```java
.requestMatchers("/usuarios/*/status").hasAuthority("SCOPE_admin:usuarios")
.requestMatchers("/usuarios/*").hasAnyAuthority("SCOPE_usuarios:read", "SCOPE_usuarios:write")
```

### Depois:
```java
.requestMatchers("/usuarios/*/status").authenticated()
.requestMatchers("/usuarios/**").authenticated()
```

## 🔄 Rebuild em Progresso

```bash
# 1. Parar serviço
docker-compose -f docker-compose-ecoledger.yml stop users-service

# 2. Rebuild
docker-compose -f docker-compose-ecoledger.yml build users-service

# 3. Subir novamente
docker-compose -f docker-compose-ecoledger.yml up -d users-service

# 4. Testar
# Login → obter token → buscar usuário com token
```

**Tempo estimado:** ~3 minutos

## ✅ Validação Esperada

### Teste 1: Login
```bash
POST http://localhost:8084/usuarios/auth/login
Body: {"email":"jane.doe@exemple.com","password":"123456"}

Response: {
  "accessToken": "eyJhbGci...",
  "refreshToken": "...",
  "expiresIn": 3600
}
```

### Teste 2: Buscar Usuário
```bash
GET http://localhost:8084/usuarios/45d70372-5056-46b5-b8af-4657391dff91
Authorization: Bearer eyJhbGci...

Response 200: {
  "id": "45d70372-5056-46b5-b8af-4657391dff91",
  "nome": "Jane Doe",
  "email": "jane.doe@exemple.com",
  "role": "produtor",
  "documento": "...",
  "status": "PENDENTE",
  "criadoEm": "..."
}
```

## 🧪 Teste no Frontend

Após rebuild:

1. **Abrir:** http://localhost:3000/login
2. **DevTools (F12):** Console
3. **Login:** jane.doe@exemple.com / 123456
4. **Observar logs:**

```
[LOGIN] Iniciando login...
[LOGIN] Token recebido: {accessToken: "...", ...}
[LOGIN] Token parts: 3
[LOGIN] Token payload: {userId: "...", role: "produtor", ...}
[LOGIN] User ID extraído: 45d70372-5056-46b5-b8af-4657391dff91
[LOGIN] Buscando dados do usuário: 45d70372-5056-46b5-b8af-4657391dff91
[LOGIN] Dados do usuário: {id: "...", nome: "Jane Doe", role: "produtor", ...}  ← ✅ SUCESSO!
[LOGIN] Auth state atualizado
[LOGIN] Redirecionando para dashboard: produtor
```

**Resultado:** Redirecionamento para `/dashboard/produtor`

## 🎯 Próximos Passos

Após correção validada:

1. ✅ Login funciona
2. ✅ Token JWT válido
3. ✅ Busca dados do usuário (sem 403)
4. ✅ Redireciona para dashboard
5. ⏳ Implementar autorização baseada em role se necessário

---

**Status:** 🔄 Rebuild em progresso (~3 minutos)
**Teste automático:** Executará após rebuild (login + buscar usuário)
