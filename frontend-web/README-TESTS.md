# Guia de Testes - ECO LEDGER Frontend

## Visão Geral

Este projeto utiliza **Vitest** como framework de testes, com **React Testing Library** para testes de componentes e **happy-dom** como ambiente de DOM.

## Estrutura de Testes

```
src/
├── test/
│   ├── setup.ts              # Configuração global dos testes
│   └── testUtils.tsx         # Helpers customizados para render
├── store/
│   └── *.test.ts             # Testes unitários das stores Zustand
├── api/
│   └── *.test.ts             # Testes unitários das APIs (com mocks)
├── utils/
│   └── *.test.ts             # Testes de utilitários
├── features/
│   └── **/
│       └── *.test.tsx        # Testes de componentes React
└── router/
    └── *.test.tsx            # Testes de rotas
```

## Executando Testes

### Comandos Disponíveis

```bash
# Executar todos os testes uma vez
npm test

# Executar em modo watch (desenvolvimento)
npm test -- --watch

# Executar com cobertura
npm run test:coverage

# Executar com UI interativa
npm run test:ui

# Executar apenas um arquivo
npm test -- src/store/authStore.test.ts
```

## Cobertura Atual

### ✅ 100% Coverage
- **Stores (Zustand)**: authStore, movimentacoesStore, certificacaoStore
- **APIs**: Todas as APIs (usersApi, movimentacoesApi, creditoApi, certificacaoApi, auditoriaApi, notificacoesApi)
- **Utils**: constants
- **Theme**: theme configuration
- **Types**: Type definitions validation

### ⚠️ Parcial Coverage
- **Components**: ~70-80% (alguns componentes complexos precisam de mais testes)
- **Pages**: ~60-70% (páginas principais têm testes básicos)

## Estratégia de Testes

### 1. Testes Unitários (Stores)
- Testam lógica de estado isoladamente
- Mock de localStorage quando necessário
- Validação de todas as ações e estados

### 2. Testes de API
- Mock completo do axiosInstance
- Testam todos os métodos de cada API
- Validam parâmetros e respostas

### 3. Testes de Componentes
- Renderização básica
- Interações do usuário
- Validação de formulários
- Estados de loading e erro

### 4. Testes de Integração
- Fluxos completos (ex: login → dashboard)
- Navegação entre rotas
- Integração entre componentes

## Exemplos de Testes

### Teste de Store
```typescript
import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from './authStore';

describe('authStore', () => {
  beforeEach(() => {
    useAuthStore.getState().logout();
  });

  it('should set auth data', () => {
    const { setAuth } = useAuthStore.getState();
    setAuth(mockAuthData, mockUser);
    
    expect(useAuthStore.getState().isAuthenticated).toBe(true);
  });
});
```

### Teste de API
```typescript
import { describe, it, expect, vi } from 'vitest';
import { usersApi } from './usersApi';

vi.mock('./axiosConfig');

describe('usersApi', () => {
  it('should call login endpoint', async () => {
    (axiosInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: mockResponse
    });

    const result = await usersApi.login({ email: 'test@example.com', password: 'pass' });
    
    expect(axiosInstance.post).toHaveBeenCalledWith('/usuarios/auth/login', {
      email: 'test@example.com',
      password: 'pass',
    });
  });
});
```

### Teste de Componente
```typescript
import { describe, it, expect } from 'vitest';
import { render, screen } from '../../../test/testUtils';
import LoginPage from './LoginPage';

describe('LoginPage', () => {
  it('should render login form', () => {
    render(<LoginPage />);
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
  });
});
```

## Boas Práticas

1. **Isolamento**: Cada teste deve ser independente
2. **Mocking**: Mockar dependências externas (APIs, localStorage, etc.)
3. **Naming**: Usar nomes descritivos: `should [action] when [condition]`
4. **Arrange-Act-Assert**: Estruturar testes claramente
5. **Coverage**: Buscar alta cobertura, mas focar em testes significativos

## Troubleshooting

### Testes falhando com "Cannot find module"
- Verificar se os mocks estão configurados corretamente
- Verificar imports relativos

### Testes de componentes não renderizam
- Verificar se está usando `render` do `testUtils`
- Verificar se os providers necessários estão configurados

### Erros de tipo TypeScript
- Verificar se os tipos estão corretos nos mocks
- Usar `as ReturnType<typeof vi.fn>` para mocks

## Próximos Passos

- [ ] Adicionar testes E2E com Playwright
- [ ] Aumentar cobertura de componentes para 90%+
- [ ] Adicionar testes de acessibilidade
- [ ] Adicionar testes de performance
- [ ] Configurar CI/CD com testes automatizados

