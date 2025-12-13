import { createBrowserRouter } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { PublicRoute } from './PublicRoute';
import { RootRedirect } from './RootRedirect';
import { ROUTES } from '../utils/constants';
import LoginPage from '../features/auth/pages/LoginPage';
import RegisterPage from '../features/auth/pages/RegisterPage';
import DashboardPage from '../features/dashboard/pages/DashboardPage';
import DashboardProdutorPage from '../features/dashboard/pages/DashboardProdutorPage';
import DashboardAnalistaPage from '../features/dashboard/pages/DashboardAnalistaPage';
import MovimentacoesListPage from '../features/movimentacoes/pages/MovimentacoesListPage';
import NovaMovimentacaoPage from '../features/movimentacoes/pages/NovaMovimentacaoPage';
import MovimentacaoDetailPage from '../features/movimentacoes/pages/MovimentacaoDetailPage';
import CertificacaoPage from '../features/certificacao/pages/CertificacaoPage';
import CadastrosListPage from '../features/cadastros/pages/CadastrosListPage';
import CadastroDetailPage from '../features/cadastros/pages/CadastroDetailPage';
import SolicitacoesCreditoPage from '../features/credito/pages/SolicitacoesCreditoPage';
import SolicitacaoCreditoDetailPage from '../features/credito/pages/SolicitacaoCreditoDetailPage';
import PropostasFinanciamentoPage from '../features/credito/pages/PropostasFinanciamentoPage';

export const router = createBrowserRouter([
  {
    path: ROUTES.HOME,
    element: <RootRedirect />,
  },
  {
    path: ROUTES.LOGIN,
    element: (
      <PublicRoute>
        <LoginPage />
      </PublicRoute>
    ),
  },
  {
    path: ROUTES.REGISTER,
    element: (
      <PublicRoute>
        <RegisterPage />
      </PublicRoute>
    ),
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        path: ROUTES.DASHBOARD,
        element: <DashboardPage />,
      },
      {
        path: ROUTES.DASHBOARD_PRODUTOR,
        element: <DashboardProdutorPage />,
      },
      {
        path: ROUTES.DASHBOARD_ANALISTA,
        element: <DashboardAnalistaPage />,
      },
      {
        path: ROUTES.DASHBOARD_AUDITOR,
        element: <DashboardPage />,
      },
      {
        path: ROUTES.MOVIMENTACOES,
        element: <MovimentacoesListPage />,
      },
      {
        path: ROUTES.MOVIMENTACOES_NOVA,
        element: <NovaMovimentacaoPage />,
      },
      {
        path: ROUTES.MOVIMENTACOES_DETALHE,
        element: <MovimentacaoDetailPage />,
      },
      {
        path: ROUTES.CERTIFICACAO,
        element: <CertificacaoPage />,
      },
      {
        path: ROUTES.CADASTROS,
        element: <CadastrosListPage />,
      },
      {
        path: ROUTES.CADASTRO_DETALHE,
        element: <CadastroDetailPage />,
      },
      {
        path: ROUTES.CREDITO_PROPOSTAS,
        element: <PropostasFinanciamentoPage />,
      },
      {
        path: ROUTES.CREDITO_SOLICITACOES,
        element: <SolicitacoesCreditoPage />,
      },
      {
        path: ROUTES.CREDITO_SOLICITACAO_DETALHE,
        element: <SolicitacaoCreditoDetailPage />,
      },
    ],
  },
]);

