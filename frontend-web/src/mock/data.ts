import type { Usuario, Movimentacao, SeloVerde, AlteracaoSelo, SolicitacaoCredito, PropostaFinanciamento, Commodity } from '../types';

// Mock Users
export const mockUsers: Record<string, Usuario> = {
  produtor: {
    id: 'prod-1',
    nome: 'João Silva',
    email: 'joao@fazenda.com',
    documento: '12345678900',
    role: 'produtor',
    status: 'APROVADO',
    criadoEm: '2024-01-15T10:00:00Z',
  },
  analista: {
    id: 'anal-1',
    nome: 'Maria Santos',
    email: 'maria@ecoledger.com',
    documento: '98765432100',
    role: 'analista',
    status: 'APROVADO',
    criadoEm: '2024-01-10T10:00:00Z',
  },
  auditor: {
    id: 'aud-1',
    nome: 'Carlos Oliveira',
    email: 'carlos@ecoledger.com',
    documento: '11122233344',
    role: 'auditor',
    status: 'APROVADO',
    criadoEm: '2024-01-05T10:00:00Z',
  },
  produtorPendente: {
    id: 'prod-2',
    nome: 'Pedro Costa',
    email: 'pedro@fazenda.com',
    documento: '55566677788',
    role: 'produtor',
    status: 'PENDENTE',
    criadoEm: '2024-12-10T10:00:00Z',
  },
};

// Mock Tokens
export const mockTokens = {
  accessToken: 'mock-access-token-' + Date.now(),
  refreshToken: 'mock-refresh-token-' + Date.now(),
  expiresIn: 3600,
};

// Mock Commodities
export const mockCommodities: Commodity[] = [
  {
    id: 'comm-1',
    nome: 'Soja',
    tipo: 'Grão',
    producerId: 'prod-1',
    criadoEm: '2024-01-20T10:00:00Z',
  },
  {
    id: 'comm-2',
    nome: 'Milho',
    tipo: 'Grão',
    producerId: 'prod-1',
    criadoEm: '2024-01-21T10:00:00Z',
  },
  {
    id: 'comm-3',
    nome: 'Café',
    tipo: 'Grão',
    producerId: 'prod-1',
    criadoEm: '2024-01-22T10:00:00Z',
  },
];

// Mock Movimentações
export const mockMovimentacoes: Movimentacao[] = [
  {
    id: 'mov-1',
    producerId: 'prod-1',
    commodityId: 'comm-1',
    tipo: 'COLHEITA',
    quantidade: 5000,
    unidade: 'kg',
    timestamp: '2024-12-01T08:00:00Z',
    localizacao: { latitude: -23.5505, longitude: -46.6333 },
    anexos: [
      {
        objectKey: 'doc-1',
        url: 'https://example.com/doc1.pdf',
        tipo: 'application/pdf',
        hash: 'abc123',
        size: 102400,
      },
    ],
  },
  {
    id: 'mov-2',
    producerId: 'prod-1',
    commodityId: 'comm-2',
    tipo: 'PLANTIO',
    quantidade: 100,
    unidade: 'hectares',
    timestamp: '2024-11-15T07:00:00Z',
    localizacao: { latitude: -23.5505, longitude: -46.6333 },
  },
  {
    id: 'mov-3',
    producerId: 'prod-1',
    commodityId: 'comm-1',
    tipo: 'COLHEITA',
    quantidade: 3000,
    unidade: 'kg',
    timestamp: '2024-11-01T08:00:00Z',
    localizacao: { latitude: -23.5505, longitude: -46.6333 },
  },
];

// Mock Selo Verde
export const mockSeloVerde: SeloVerde = {
  producerId: 'prod-1',
  status: 'ATIVO',
  nivel: 'OURO',
  pontuacao: 95,
  ultimoCheck: '2024-12-01T10:00:00Z',
  motivos: [
    'Uso de práticas sustentáveis comprovadas',
    'Rastreabilidade completa da produção',
    'Certificações ambientais válidas',
  ],
  evidencias: [
    { tipo: 'AUDITORIA', detalhe: 'Auditoria completa realizada em 01/12/2024' },
    { tipo: 'DOCUMENTO', detalhe: 'Certificado ISO 14001' },
  ],
};

// Mock Histórico de Alterações do Selo
export const mockHistoricoSelo: AlteracaoSelo[] = [
  {
    id: 'alt-1',
    producerId: 'prod-1',
    deStatus: 'PENDENTE',
    paraStatus: 'ATIVO',
    motivo: 'Aprovado após auditoria inicial',
    timestamp: '2024-01-20T10:00:00Z',
  },
  {
    id: 'alt-2',
    producerId: 'prod-1',
    deStatus: 'ATIVO',
    paraStatus: 'ATIVO',
    motivo: 'Atualização de nível: BRONZE → PRATA',
    timestamp: '2024-06-15T10:00:00Z',
  },
  {
    id: 'alt-3',
    producerId: 'prod-1',
    deStatus: 'ATIVO',
    paraStatus: 'ATIVO',
    motivo: 'Atualização de nível: PRATA → OURO',
    timestamp: '2024-12-01T10:00:00Z',
  },
];

// Mock Propostas de Financiamento
export const mockPropostas: PropostaFinanciamento[] = [
  {
    id: 'prop-1',
    instituicaoId: 'inst-1',
    valorMaximo: 500000,
    taxa: 4.5,
    opcoesPrazo: [12, 24, 36, 48],
    condicoes: 'Financiamento para produtores com Selo Verde Ouro',
  },
  {
    id: 'prop-2',
    instituicaoId: 'inst-2',
    valorMaximo: 300000,
    taxa: 5.0,
    opcoesPrazo: [12, 24, 36],
    condicoes: 'Financiamento para expansão de produção sustentável',
  },
];

// Mock Solicitações de Crédito
export const mockSolicitacoes: SolicitacaoCredito[] = [
  {
    id: 'sol-1',
    producerId: 'prod-1',
    propostaId: 'prop-1',
    valor: 250000,
    status: 'APROVADO',
    criadoEm: '2024-11-15T10:00:00Z',
    historico: [
      {
        id: 'hist-1',
        status: 'PENDENTE',
        timestamp: '2024-11-15T10:00:00Z',
        observacoes: 'Solicitação criada',
      },
      {
        id: 'hist-2',
        status: 'EM_ANALISE',
        timestamp: '2024-11-16T10:00:00Z',
        observacoes: 'Documentação em análise',
      },
      {
        id: 'hist-3',
        status: 'APROVADO',
        timestamp: '2024-11-20T10:00:00Z',
        observacoes: 'Solicitação aprovada pelo analista',
      },
    ],
  },
  {
    id: 'sol-2',
    producerId: 'prod-1',
    propostaId: 'prop-2',
    valor: 150000,
    status: 'PENDENTE',
    criadoEm: '2024-12-10T10:00:00Z',
    historico: [
      {
        id: 'hist-4',
        status: 'PENDENTE',
        timestamp: '2024-12-10T10:00:00Z',
        observacoes: 'Aguardando análise',
      },
    ],
  },
];

// Mock Cadastros Pendentes
export const mockCadastros = [
  {
    cadastroId: 'cad-1',
    nome: 'Pedro Costa',
    email: 'pedro@fazenda.com',
    documento: '55566677788',
    role: 'produtor',
    status: 'PENDENTE',
    criadoEm: '2024-12-10T10:00:00Z',
    dadosFazenda: {
      nomeFazenda: 'Fazenda São Pedro',
      area: 150,
      localizacao: 'São Paulo, SP',
    },
  },
  {
    cadastroId: 'cad-2',
    nome: 'Ana Lima',
    email: 'ana@fazenda.com',
    documento: '99988877766',
    role: 'produtor',
    status: 'PENDENTE',
    criadoEm: '2024-12-11T10:00:00Z',
    dadosFazenda: {
      nomeFazenda: 'Fazenda Verde',
      area: 200,
      localizacao: 'Minas Gerais, MG',
    },
  },
];

// Helper function to get user by email
export const getUserByEmail = (email: string): Usuario | null => {
  return Object.values(mockUsers).find((user) => user.email === email) || null;
};

// Helper function to get user by id
export const getUserById = (id: string): Usuario | null => {
  return Object.values(mockUsers).find((user) => user.id === id) || null;
};

