# JWT Fix - Correção do Erro atob

## 🐛 Problema Identificado

**Erro:** `Failed to execute 'atob' on 'Window': The string to be decoded is not correctly encoded.`

**Causa Raiz:** O backend estava retornando um token fake no formato `"access.{userId}"` em vez de um JWT válido.

### Código Problemático (antes):

```java
// UsuarioServiceImpl.java - linha 117
return new TokenAuthDto(
    "access."+u.get().getId().toString(), 
    "refresh."+u.get().getId().toString(), 
    3600L
);
```

### O que acontecia:

1. Backend retornava: `accessToken: "access.123e4567-e89b-12d3-a456-426614174000"`
2. Frontend tentava decodificar como JWT: `atob(token.split('.')[1])`
3. O segundo elemento após split era `"123e4567-e89b-12d3-a456-426614174000"`
4. UUID não é base64 válido → **Erro de decodificação**

---

## ✅ Solução Implementada

### 1. Expandido `JwtService.java`

Adicionados métodos para gerar tokens JWT reais:

```java
@Component
public class JwtService {
    
    @Value("${jwt.expiration:3600000}") // 1 hora
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration:86400000}") // 24 horas
    private Long refreshExpiration;

    public String generateAccessToken(String userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getExpirationInSeconds() {
        return jwtExpiration / 1000;
    }
}
```

### 2. Atualizado `UsuarioServiceImpl.java`

Injetado `JwtService` e usado na autenticação:

```java
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final JwtService jwtService;

    public UsuarioServiceImpl(..., JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public TokenAuthDto authenticate(String email, String password) {
        Optional<UsuarioEntity> u = usuarioRepository.findByEmail(email);
        if (u.isEmpty()) throw new IllegalArgumentException("Credenciais inválidas");
        
        if (!password.equals(u.get().getSenha())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        
        // ✅ Gerar tokens JWT reais
        String accessToken = jwtService.generateAccessToken(
            u.get().getId().toString(),
            u.get().getEmail(),
            u.get().getRole()
        );
        String refreshToken = jwtService.generateRefreshToken(u.get().getId().toString());
        Long expiresIn = jwtService.getExpirationInSeconds();
        
        return new TokenAuthDto(accessToken, refreshToken, expiresIn);
    }
}
```

### 3. Configurado `application.yml`

Adicionadas propriedades JWT:

```yaml
jwt:
  secret: ${JWT_SECRET:ecoledger-secret-key-minimum-256-bits-for-hs256-algorithm-security}
  expiration: ${JWT_EXPIRATION:3600000}  # 1 hora em ms
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:86400000}  # 24 horas em ms
```

---

## 📋 Formato JWT Gerado

### Access Token (exemplo):

```
eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxMjNlNDU2Ny1lODliLTEyZDMtYTQ1Ni00MjY2MTQxNzQwMDAiLCJlbWFpbCI6InRlc3RlQHRlc3QuY29tIiwicm9sZSI6InByb2R1dG9yIiwidHlwZSI6ImFjY2VzcyIsInN1YiI6IjEyM2U0NTY3LWU4OWItMTJkMy1hNDU2LTQyNjYxNDE3NDAwMCIsImlhdCI6MTczNDE5NzY4NCwiZXhwIjoxNzM0MjAxMjg0fQ.X7Y_ZqN9Q3rM8vWpKjLhTcUaVdRe2SiGnFxBwE5Hy4o
```

**Estrutura:**
- **Header** (base64): `eyJhbGciOiJIUzI1NiJ9`
  - `{"alg":"HS256"}`
- **Payload** (base64): `eyJ1c2VySWQiOiIxMjNl...`
  - `{"userId":"...", "email":"...", "role":"produtor", "type":"access", "sub":"...", "iat":..., "exp":...}`
- **Signature**: `X7Y_ZqN9Q3rM8vWpKjLhTcUa...`

### Refresh Token (exemplo):

```
eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6IjEyM2U0NTY3LWU4OWItMTJkMy1hNDU2LTQyNjYxNDE3NDAwMCIsImlhdCI6MTczNDE5NzY4NCwiZXhwIjoxNzM0Mjg0MDg0fQ.K2mN5pQrS8tVuW3xYzAaBcDdE6fFgGhI9jJ0kLlM1nN
```

---

## 🧪 Como o Frontend Decodifica

```typescript
// LoginPage.tsx - linha 48
const tokenPayload = JSON.parse(atob(authData.accessToken.split('.')[1]));
```

**Passo a passo:**
1. `authData.accessToken.split('.')` → `["header", "payload", "signature"]`
2. `split('.')[1]` → `"eyJ1c2VySWQiOiIxMjNl..."`
3. `atob(...)` → `'{"userId":"123e4567-e89b-12d3-a456-426614174000","email":"teste@teste.com",...}'`
4. `JSON.parse(...)` → objeto JavaScript

**Resultado:**
```javascript
{
  userId: "123e4567-e89b-12d3-a456-426614174000",
  email: "teste@teste.com",
  role: "produtor",
  type: "access",
  sub: "123e4567-e89b-12d3-a456-426614174000",
  iat: 1734197684,
  exp: 1734201284
}
```

---

## 🔒 Segurança

### ⚠️ Atenção: Senhas em Texto Plano

Atualmente as senhas são validadas com comparação simples:

```java
if (!password.equals(u.get().getSenha())) {
    throw new IllegalArgumentException("Credenciais inválidas");
}
```

**TODO (Produção):**
```java
// Adicionar ao build.gradle.kts
implementation("org.springframework.security:spring-security-crypto")

// Usar BCrypt
if (!passwordEncoder.matches(password, u.get().getSenha())) {
    throw new IllegalArgumentException("Credenciais inválidas");
}
```

### 🔑 Secret JWT

**Atual:** `changeitchangeitchangeitchangeit` (padrão)

**Produção:** Use variável de ambiente `JWT_SECRET` com valor aleatório seguro:
```bash
# Gerar secret seguro (64 caracteres)
openssl rand -base64 64
```

---

## 🚀 Rebuild do Serviço

```bash
# Parar serviço
docker-compose -f docker-compose-ecoledger.yml stop users-service

# Rebuild sem cache
docker-compose -f docker-compose-ecoledger.yml build --no-cache users-service

# Subir novamente
docker-compose -f docker-compose-ecoledger.yml up -d users-service

# Verificar logs
docker logs ecoledger-users-service -f
```

---

## ✅ Teste de Validação

### 1. Fazer Login

```bash
curl -X POST http://localhost:8084/usuarios/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@teste.com",
    "password": "senha123"
  }'
```

**Response esperada:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQi...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoi...",
  "expiresIn": 3600
}
```

### 2. Decodificar Token Online

Acesse https://jwt.io/ e cole o `accessToken`.

**Payload decodificado deve mostrar:**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "teste@teste.com",
  "role": "produtor",
  "type": "access",
  "sub": "123e4567-e89b-12d3-a456-426614174000",
  "iat": 1734197684,
  "exp": 1734201284
}
```

### 3. Testar no Frontend

```javascript
// Console do navegador (http://localhost:3000/login)
fetch('http://localhost:8084/usuarios/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'teste@teste.com',
    password: 'senha123'
  })
})
.then(r => r.json())
.then(data => {
  console.log('Token:', data.accessToken);
  const payload = JSON.parse(atob(data.accessToken.split('.')[1]));
  console.log('Payload:', payload);
})
.catch(console.error);
```

**Resultado esperado:** Nenhum erro, payload decodificado com sucesso.

---

## 📝 Checklist de Validação

- [ ] JwtService.java atualizado com métodos de geração
- [ ] UsuarioServiceImpl.java usando JwtService
- [ ] application.yml com configurações JWT
- [ ] users-service rebuilded e rodando
- [ ] Login retorna JWT válido (não "access.{uuid}")
- [ ] Frontend consegue decodificar token sem erro atob
- [ ] Payload contém userId, email, role

---

**Data:** 14/12/2025  
**Status:** ✅ Correção implementada, aguardando rebuild do serviço
