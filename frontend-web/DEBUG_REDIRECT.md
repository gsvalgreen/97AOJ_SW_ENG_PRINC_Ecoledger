# Debug: Problema de Redirecionamento

## O que foi implementado

1. **RootRedirect Component**: Componente que verifica autenticação e redireciona
   - Se autenticado → `/dashboard`
   - Se não autenticado → `/login`
   - Mostra loading enquanto verifica

2. **Inicialização do Store**: O `authStore.initialize()` é chamado no `main.tsx` antes de renderizar o App

## Como debugar

### 1. Verificar Console do Navegador
Abra o DevTools (F12) e verifique:
- Erros JavaScript (console vermelho)
- Warnings (console amarelo)
- Network errors

### 2. Verificar localStorage
No console do navegador, execute:
```javascript
localStorage.getItem('ecoledger_access_token')
localStorage.getItem('ecoledger_user')
```

### 3. Verificar se o componente está sendo renderizado
Adicione um console.log temporário no `RootRedirect.tsx`:
```typescript
export const RootRedirect = () => {
  console.log('RootRedirect renderizado');
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  console.log('isAuthenticated:', isAuthenticated);
  // ... resto do código
};
```

### 4. Verificar se o router está funcionando
No console do navegador:
```javascript
// Verificar se o React Router está funcionando
window.location.href
```

### 5. Testar diretamente
Tente acessar diretamente:
- `http://localhost:5173/login` - Deve mostrar a página de login
- `http://localhost:5173/register` - Deve mostrar a página de registro

## Possíveis problemas

1. **Store não inicializado**: O `initialize()` pode não estar sendo executado a tempo
2. **React Router não configurado**: Verificar se o `RouterProvider` está funcionando
3. **Erro JavaScript**: Algum erro pode estar impedindo a renderização
4. **Cache do navegador**: Limpar cache e recarregar (Ctrl+Shift+R ou Cmd+Shift+R)

## Solução alternativa

Se o problema persistir, podemos usar uma abordagem diferente:

1. Remover o `RootRedirect` e fazer o redirecionamento diretamente no `DashboardPage`
2. Ou usar um componente de loading global que verifica a autenticação antes de renderizar qualquer rota

