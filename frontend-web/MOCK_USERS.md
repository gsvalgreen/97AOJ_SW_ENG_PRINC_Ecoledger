# Mock Users Guide

Este documento lista os usuários mock disponíveis para testar a aplicação quando o backend não está disponível.

## Como Ativar o Mock API

1. Crie um arquivo `.env` na raiz do projeto `frontend-web/`:
   ```bash
   VITE_MOCK_API=true
   ```

2. Reinicie o servidor de desenvolvimento:
   ```bash
   npm run dev
   ```

3. Você verá a mensagem no console: `🔧 Mock API enabled - Using mock data`

## Usuários Mock Disponíveis

### 1. Produtor (Aprovado)
- **Email**: `joao@fazenda.com`
- **Senha**: Qualquer senha (o mock aceita qualquer senha)
- **Role**: `produtor`
- **Status**: `APROVADO`
- **ID**: `prod-1`

**Funcionalidades disponíveis:**
- Dashboard do Produtor
- Criar movimentações
- Ver movimentações
- Ver Selo Verde (Ouro, 95 pontos)
- Ver propostas de financiamento
- Criar solicitações de crédito

### 2. Analista
- **Email**: `maria@ecoledger.com`
- **Senha**: Qualquer senha
- **Role**: `analista`
- **Status**: `APROVADO`
- **ID**: `anal-1`

**Funcionalidades disponíveis:**
- Dashboard do Analista
- Ver cadastros pendentes
- Aprovar/rejeitar cadastros
- Ver todas as movimentações
- Ver solicitações de crédito
- Aprovar/rejeitar solicitações de crédito

### 3. Auditor
- **Email**: `carlos@ecoledger.com`
- **Senha**: Qualquer senha
- **Role**: `auditor`
- **Status**: `APROVADO`
- **ID**: `aud-1`

**Funcionalidades disponíveis:**
- Dashboard do Auditor
- Revisar auditorias
- Ver histórico de auditorias

### 4. Produtor (Pendente)
- **Email**: `pedro@fazenda.com`
- **Senha**: Qualquer senha
- **Role**: `produtor`
- **Status**: `PENDENTE`
- **ID**: `prod-2`

**Funcionalidades:**
- Login funciona, mas acesso limitado até aprovação
- Aparece na lista de cadastros pendentes para analistas

## Dados Mock Disponíveis

### Movimentações
- 3 movimentações de exemplo para o produtor `prod-1`
- Tipos: COLHEITA, PLANTIO
- Commodities: Soja, Milho, Café

### Selo Verde
- Status: ATIVO
- Nível: OURO
- Pontuação: 95
- Histórico de alterações disponível

### Propostas de Financiamento
- 2 propostas disponíveis
- Valores: R$ 500.000 e R$ 300.000
- Diferentes taxas e prazos

### Solicitações de Crédito
- 2 solicitações de exemplo
- Status: APROVADO e PENDENTE
- Histórico completo de alterações

### Cadastros Pendentes
- 2 cadastros pendentes de aprovação
- Aparecem na lista para analistas

## Fluxo de Teste Recomendado

### Como Produtor:
1. Faça login com `joao@fazenda.com` (qualquer senha)
2. Explore o dashboard
3. Crie uma nova movimentação
4. Veja suas movimentações
5. Consulte seu Selo Verde
6. Veja propostas de financiamento
7. Crie uma solicitação de crédito

### Como Analista:
1. Faça login com `maria@ecoledger.com` (qualquer senha)
2. Veja cadastros pendentes
3. Aprove ou rejeite um cadastro
4. Veja todas as movimentações
5. Veja solicitações de crédito
6. Aprove ou rejeite uma solicitação

### Como Auditor:
1. Faça login com `carlos@ecoledger.com` (qualquer senha)
2. Explore o dashboard do auditor
3. Revise auditorias

## Notas Importantes

- **Qualquer senha funciona** para os usuários mock
- O mock simula um delay de 500ms nas requisições
- Os dados são resetados ao recarregar a página
- Algumas operações (como criar movimentação) retornam IDs mock, mas não persistem entre recarregamentos
- Para desativar o mock, remova ou defina `VITE_MOCK_API=false` no `.env`

## Desativar Mock API

Para usar o backend real, remova a variável `VITE_MOCK_API` do `.env` ou defina como `false`:

```bash
VITE_MOCK_API=false
```

Ou simplesmente remova a linha do arquivo `.env`.

