# Guia de Teste - Integração Frontend + Backend

## ✅ Configuração Aplicada

### Backend (users-service)
- ✅ Coluna `senha` adicionada na tabela `usuarios`
- ✅ DTO `CadastroCriacaoDto` aceita campo `senha`
- ✅ Entidade `UsuarioEntity` armazena senha
- ✅ Validação de senha implementada no método `authenticate()`
- ✅ Serviço rodando em: http://localhost:8084

### Frontend
- ✅ Formulário de cadastro inclui campos "Senha" e "Confirmar Senha"
- ✅ Validação de senha mínima (6 caracteres)
- ✅ Validação de senhas coincidentes
- ✅ Mock API desabilitado (`VITE_MOCK_API=false`)
- ✅ URLs apontando para backend real (localhost:8084)
- ✅ Frontend rodando em: http://localhost:3000

---

## 🧪 Testes a Executar

### 1. Teste de Cadastro

**Acesse:** http://localhost:3000/register

**Passo a Passo:**
1. Preencha o formulário:
   - Nome: `João Silva`
   - Email: `joao@email.com`
   - CPF/CNPJ: `12345678900`
   - **Senha: `senha123`** ⭐
   - **Confirmar Senha: `senha123`** ⭐
   - Tipo de Usuário: `Produtor Rural`

2. Clique em "Próximo"

3. Preencha dados adicionais (se Produtor):
   - Nome da Fazenda: `Fazenda Teste`
   - Área: `100`
   - Localização: `São Paulo - SP`

4. Clique em "Enviar Cadastro"

**Resultado Esperado:**
- ✅ Mensagem de sucesso
- ✅ Redirecionamento para página de login
- ✅ Dados salvos no banco de dados

### 2. Verificar no Banco de Dados

```bash
docker exec -it ecoledger-postgres psql -U ecoledger_users -d users -c "SELECT id, nome, email, role, senha FROM usuarios;"
```

**Resultado Esperado:**
- ✅ Registro do usuário criado
- ✅ Senha armazenada (em texto plano - para produção usar BCrypt)

### 3. Teste de Login

**Acesse:** http://localhost:3000/login

**Passo a Passo:**
1. Preencha:
   - Email: `joao@email.com`
   - Senha: `senha123`

2. Clique em "Entrar"

**Resultado Esperado:**
- ✅ Login bem-sucedido
- ✅ Token JWT armazenado no localStorage
- ✅ Redirecionamento para dashboard

### 4. Teste de Senha Incorreta

**Acesse:** http://localhost:3000/login

**Passo a Passo:**
1. Preencha:
   - Email: `joao@email.com`
   - Senha: `senhaerrada`

2. Clique em "Entrar"

**Resultado Esperado:**
- ❌ Erro "Credenciais inválidas"
- ❌ Não deve permitir login

---

## 🔍 Debug e Troubleshooting

### Ver logs do Frontend
```bash
docker logs ecoledger-frontend-web -f
```

### Ver logs do Backend
```bash
docker logs ecoledger-users-service -f
```

### Verificar requisições HTTP no navegador
1. Abrir DevTools (F12)
2. Aba "Network"
3. Filtrar por "XHR"
4. Fazer cadastro/login
5. Verificar:
   - URL chamada: `http://localhost:8084/usuarios/...`
   - Status: 201 (cadastro) ou 200 (login)
   - Payload enviado
   - Response recebida

### Problema: Erro de CORS
Se ver erro de CORS no console:
```bash
Access to XMLHttpRequest at 'http://localhost:8084/usuarios/cadastros' 
from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Solução:** Precisamos adicionar configuração CORS no users-service.

### Problema: Mock API ainda ativa
Se os dados mockados aparecerem:
1. Limpar cache do navegador (Ctrl+Shift+Del)
2. Abrir em janela anônima
3. Verificar console: não deve mostrar "🔧 Mock API enabled"

---

## 📊 Endpoints do Backend

### POST /usuarios/cadastros
**Request:**
```json
{
  "nome": "João Silva",
  "email": "joao@email.com",
  "documento": "12345678900",
  "senha": "senha123",
  "role": "produtor",
  "dadosFazenda": {
    "nomeFazenda": "Fazenda Teste",
    "area": 100,
    "localizacao": "São Paulo"
  },
  "anexos": []
}
```

**Headers:**
```
Content-Type: application/json
Idempotency-Key: unique-key-12345
```

**Response (201):**
```json
{
  "cadastroId": "uuid-aqui",
  "status": "PENDENTE"
}
```

### POST /usuarios/auth/login
**Request:**
```json
{
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Response (200):**
```json
{
  "accessToken": "access.uuid-aqui",
  "refreshToken": "refresh.uuid-aqui",
  "expiresIn": 3600
}
```

---

## 🔐 Segurança (Para Produção)

### ⚠️ Atenção: Senhas em Texto Plano

Atualmente as senhas estão sendo armazenadas em **texto plano**. Para produção:

1. **Adicionar BCrypt no backend:**
```java
// Adicionar dependência no build.gradle.kts
implementation("org.springframework.security:spring-security-crypto")

// No UsuarioServiceImpl.java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

// Ao salvar usuário:
user.setSenha(passwordEncoder.encode(dto.senha()));

// Ao autenticar:
if (!passwordEncoder.matches(password, u.get().getSenha())) {
    throw new IllegalArgumentException("Credenciais inválidas");
}
```

2. **Adicionar HTTPS**
3. **Implementar refresh token real**
4. **Adicionar rate limiting**
5. **Validar força da senha**

---

## 📝 Resumo das Mudanças

### Arquivos Modificados

**Backend:**
- `CadastroCriacaoDto.java` - Campo senha adicionado
- `UsuarioEntity.java` - Coluna senha + getters/setters
- `UsuarioServiceImpl.java` - Salvar e validar senha
- `V2__add_senha_column.sql` - Migration criada (aplicada manualmente)

**Frontend:**
- `RegisterPage.tsx` - Campos de senha + validação
- `types/index.ts` - Interface atualizada
- `Dockerfile` - Mock desabilitado + URLs corretas

**Banco de Dados:**
```sql
ALTER TABLE usuarios ADD COLUMN senha VARCHAR(255);
```

---

**Data:** 14/12/2025  
**Status:** ✅ Integração Completa  
**Testado:** Aguardando validação do usuário
