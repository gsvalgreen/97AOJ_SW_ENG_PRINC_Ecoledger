export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const API_SERVICES = {
  USERS: import.meta.env.VITE_USERS_API_URL || 'http://localhost:8084',
  MOVIMENTACOES: import.meta.env.VITE_MOVIMENTACOES_API_URL || 'http://localhost:8082',
  CERTIFICACAO: import.meta.env.VITE_CERTIFICACAO_API_URL || 'http://localhost:8085',
  AUDITORIA: import.meta.env.VITE_AUDITORIA_API_URL || 'http://localhost:8083',
  CREDITO: import.meta.env.VITE_CREDITO_API_URL || 'http://localhost:8086',
  NOTIFICACOES: import.meta.env.VITE_NOTIFICACOES_API_URL || 'http://localhost:8087',
} as const;

export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',
  DASHBOARD_PRODUTOR: '/dashboard/produtor',
  DASHBOARD_ANALISTA: '/dashboard/analista',
  DASHBOARD_AUDITOR: '/dashboard/auditor',
  MOVIMENTACOES: '/movimentacoes',
  MOVIMENTACOES_NOVA: '/movimentacoes/nova',
  MOVIMENTACOES_DETALHE: '/movimentacoes/:id',
  CERTIFICACAO: '/certificacao',
  CREDITO: '/credito',
  CREDITO_PROPOSTAS: '/credito/propostas',
  CREDITO_SOLICITACOES: '/credito/solicitacoes',
  CREDITO_SOLICITACAO_DETALHE: '/credito/solicitacoes/:id',
  USUARIOS: '/usuarios',
  CADASTROS: '/cadastros',
  CADASTRO_DETALHE: '/cadastros/:id',
  AUDITORIAS: '/auditorias',
} as const;

export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'ecoledger_access_token',
  REFRESH_TOKEN: 'ecoledger_refresh_token',
  USER: 'ecoledger_user',
} as const;

