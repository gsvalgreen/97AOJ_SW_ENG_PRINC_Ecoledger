# Teste de CORS - ECO LEDGER

## ✅ Configurações CORS Aplicadas

### Serviços Atualizados:
1. ✅ **users-service** - SecurityConfig.java (rebuild necessário)
2. ✅ **movimentacao-service** - CorsConfig.java (rebuild necessário)
3. ✅ **auditoria-service** - CorsConfig.java (rebuild necessário)
4. ✅ **certificacao-service** - CorsConfig.java (rebuild necessário)

### Origens Permitidas:
- `http://localhost:3000` - Frontend em produção (Docker)
- `http://localhost:5173` - Frontend dev (Vite)
- `http://localhost:8080` - Outras origens

### Métodos Permitidos:
- GET, POST, PUT, PATCH, DELETE, OPTIONS

### Headers Permitidos:
- Todos (`*`)
- Headers expostos: Authorization, Content-Type, Idempotency-Key

---

## 🔧 Rebuild dos Serviços

### Opção 1: Rebuild Completo (Recomendado)
```bash
# Parar todos os serviços de backend
docker-compose -f docker-compose-ecoledger.yml stop users-service movimentacao-service auditoria-service certificacao-service

# Remover containers
docker-compose -f docker-compose-ecoledger.yml rm -f users-service movimentacao-service auditoria-service certificacao-service

# Rebuild sem cache
docker-compose -f docker-compose-ecoledger.yml build --no-cache users-service movimentacao-service auditoria-service certificacao-service

# Subir novamente
docker-compose -f docker-compose-ecoledger.yml up -d users-service movimentacao-service auditoria-service certificacao-service
```

### Opção 2: Rebuild Individual (Mais Rápido para Testar)
```bash
# Apenas users-service para testar cadastro/login
docker-compose -f docker-compose-ecoledger.yml stop users-service
docker-compose -f docker-compose-ecoledger.yml rm -f users-service
docker-compose -f docker-compose-ecoledger.yml build --no-cache users-service
docker-compose -f docker-compose-ecoledger.yml up -d users-service
```

---

## 🧪 Teste de CORS

### 1. Teste Manual no Navegador

Abra o navegador em: http://localhost:3000/register

**Abra o DevTools (F12) → Console** e execute:

```javascript
// Teste de cadastro
fetch('http://localhost:8084/usuarios/cadastros', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Idempotency-Key': 'test-' + Date.now()
  },
  credentials: 'include',
  body: JSON.stringify({
    nome: 'Teste CORS',
    email: 'cors@test.com',
    documento: '99999999999',
    senha: 'senha123',
    role: 'produtor',
    dadosFazenda: {},
    anexos: []
  })
})
.then(r => r.json())
.then(console.log)
.catch(console.error);
```

**Resultado Esperado:**
- ✅ Não deve mostrar erro de CORS
- ✅ Response com `cadastroId` e `status`

**Erro de CORS (antes da correção):**
```
Access to fetch at 'http://localhost:8084/usuarios/cadastros' from origin 
'http://localhost:3000' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' 
header is present on the requested resource.
```

### 2. Verificar Headers de CORS

No DevTools (F12) → Network → Selecione a requisição → Headers

**Verifique Response Headers:**
```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Max-Age: 3600
```

### 3. Teste de Preflight (OPTIONS)

Requisições com headers customizados primeiro fazem um preflight request (OPTIONS):

```bash
curl -X OPTIONS http://localhost:8084/usuarios/cadastros \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,Idempotency-Key" \
  -v
```

**Resultado Esperado:**
```
< HTTP/1.1 200 
< Access-Control-Allow-Origin: http://localhost:3000
< Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
< Access-Control-Allow-Headers: *
< Access-Control-Allow-Credentials: true
< Access-Control-Max-Age: 3600
```

---

## 🐛 Troubleshooting

### Problema: Ainda aparece erro de CORS

**Causa:** Serviço não foi rebuilded com a nova configuração.

**Solução:**
```bash
# Verificar se o serviço está rodando com a nova imagem
docker ps | findstr users

# Ver logs para confirmar startup
docker logs ecoledger-users-service -f

# Se necessário, force rebuild
docker-compose -f docker-compose-ecoledger.yml up -d --build --force-recreate users-service
```

### Problema: Erro 403 Forbidden

**Causa:** Spring Security bloqueando a requisição.

**Solução:** Verificar se o endpoint está permitido no SecurityConfig:
```java
.requestMatchers("/usuarios/auth/**", "/usuarios/cadastros").permitAll()
```

### Problema: Erro de credenciais mesmo com CORS OK

**Causa:** Validação de senha ou dados inválidos.

**Solução:** Verificar logs do backend:
```bash
docker logs ecoledger-users-service --tail 50
```

---

## 📊 Status das Configurações

### SecurityConfig.java (users-service)
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(
        "http://localhost:3000",
        "http://localhost:5173",
        "http://localhost:8080"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    // ...
}

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // ...
}
```

### CorsConfig.java (outros serviços)
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Mesma configuração
    }
}
```

---

## ✅ Checklist de Validação

- [ ] Rebuild do users-service completado
- [ ] Container rodando: `docker ps | findstr users`
- [ ] Frontend acessível em http://localhost:3000
- [ ] Console do navegador sem erros de CORS
- [ ] Cadastro funcionando pelo frontend
- [ ] Login funcionando pelo frontend

---

**Data:** 14/12/2025  
**Status:** ✅ Configuração CORS Completa  
**Aguardando:** Rebuild dos serviços
