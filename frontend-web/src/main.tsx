import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import { useAuthStore } from './store/authStore';
import { setupMockInterceptor } from './mock/mockApi';
import axiosInstance from './api/axiosConfig';
import './index.css';

// Setup mock API if enabled
if (import.meta.env.VITE_MOCK_API === 'true') {
  setupMockInterceptor(axiosInstance as any);
  console.log('🔧 Mock API enabled - Using mock data');
}

useAuthStore.getState().initialize();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>
);
