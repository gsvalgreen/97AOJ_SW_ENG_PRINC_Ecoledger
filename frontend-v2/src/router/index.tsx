import DashboardLayout from '@/components/layout/DashboardLayout';
import AuditoriasPage from '@/pages/AuditoriasPage';
import CadastroPage from '@/pages/CadastroPage';
import CertificacoesPage from '@/pages/CertificacoesPage';
import DashboardPage from '@/pages/DashboardPage';
import LoginPage from '@/pages/LoginPage';
import MovimentacoesPage from '@/pages/MovimentacoesPage';
import NovaMovimentacaoPage from '@/pages/NovaMovimentacaoPage';
import PerfilPage from '@/pages/PerfilPage';
import { useAuthStore } from '@/store/authStore';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
}

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route
          path="/login"
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          }
        />
        <Route
          path="/cadastro"
          element={
            <PublicRoute>
              <CadastroPage />
            </PublicRoute>
          }
        />

        {/* Protected Routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <DashboardLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="movimentacoes" element={<MovimentacoesPage />} />
          <Route path="movimentacoes/nova" element={<NovaMovimentacaoPage />} />
          <Route path="auditorias" element={<AuditoriasPage />} />
          <Route path="certificacoes" element={<CertificacoesPage />} />
          <Route path="perfil" element={<PerfilPage />} />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
