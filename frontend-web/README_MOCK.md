# Sistema de Mock API - ECO LEDGER

## Visão Geral

O sistema de Mock API permite testar toda a aplicação frontend sem precisar do backend implementado. Ele intercepta todas as chamadas de API e retorna dados mock pré-configurados.

## Como Ativar

1. **Crie um arquivo `.env` na raiz do projeto `frontend-web/`**:
   ```bash
   VITE_MOCK_API=true
   ```

2. **Reinicie o servidor de desenvolvimento**:
   ```bash
   npm run dev
   ```

3. **Você verá no console**: `🔧 Mock API enabled - Using mock data`

## Usuários Mock Disponíveis

### Produtor (Aprovado)
- **Email**: `joao@fazenda.com`
- **Senha**: Qualquer senha funciona
- **Role**: `produtor`
- **Status**: `APROVADO`

### Analista
- **Email**: `maria@ecoledger.com`
- **Senha**: Qualquer senha funciona
- **Role**: `analista`
- **Status**: `APROVADO`

### Auditor
- **Email**: `carlos@ecoledger.com`
- **Senha**: Qualquer senha funciona
- **Role**: `auditor`
- **Status**: `APROVADO`

### Produtor (Pendente)
- **Email**: `pedro@fazenda.com`
- **Senha**: Qualquer senha funciona
- **Role**: `produtor`
- **Status**: `PENDENTE`

## Dados Mock Incluídos

- ✅ 3 movimentações de exemplo
- ✅ Selo Verde (Ouro, 95 pontos) com histórico
- ✅ 2 propostas de financiamento
- ✅ 2 solicitações de crédito
- ✅ 2 cadastros pendentes

## Como Desativar

Remova a variável `VITE_MOCK_API` do `.env` ou defina como `false`:

```bash
VITE_MOCK_API=false
```

## Arquivos do Sistema Mock

- `src/mock/data.ts` - Dados mock (usuários, movimentações, etc.)
- `src/mock/mockApi.ts` - Lógica de interceptação e respostas mock
- `MOCK_USERS.md` - Documentação detalhada dos usuários mock

## Notas

- O mock simula um delay de 500ms nas requisições
- Qualquer senha funciona para login nos usuários mock
- Os dados são resetados ao recarregar a página
- Algumas operações retornam IDs mock, mas não persistem entre recarregamentos

