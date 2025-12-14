import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import { useAuthStore } from './store/authStore';
import { setupMockInterceptor } from './mock/mockApi';
import {
  usersApiInstance,
  movimentacoesApiInstance,
  certificacaoApiInstance,
  auditoriaApiInstance,
  creditoApiInstance,
  notificacoesApiInstance,
} from './api/axiosConfig';
import './index.css';

// Setup mock API if enabled - intercept all service instances
if (import.meta.env.VITE_MOCK_API === 'true') {
  setupMockInterceptor(usersApiInstance as any);
  setupMockInterceptor(movimentacoesApiInstance as any);
  setupMockInterceptor(certificacaoApiInstance as any);
  setupMockInterceptor(auditoriaApiInstance as any);
  setupMockInterceptor(creditoApiInstance as any);
  setupMockInterceptor(notificacoesApiInstance as any);
  console.log('🔧 Mock API enabled - Using mock data');
}

useAuthStore.getState().initialize();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>
);
