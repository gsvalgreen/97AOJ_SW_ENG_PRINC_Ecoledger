export type UserRole = 'produtor' | 'analista' | 'auditor';

export type UserStatus = 'PENDENTE' | 'APROVADO' | 'REJEITADO';

export interface Usuario {
  id: string;
  nome: string;
  email: string;
  role: UserRole;
  documento: string;
  status: UserStatus;
  criadoEm: string;
}

export interface CadastroCriacao {
  nome: string;
  email: string;
  documento: string;
  role: UserRole;
  dadosFazenda?: Record<string, unknown>;
  anexos?: Array<{
    tipo: string;
    url: string;
  }>;
}

export interface RespostaCadastro {
  cadastroId: string;
  status: string;
}

export interface TokenAuth {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface Movimentacao {
  id: string;
  producerId: string;
  commodityId: string;
  tipo: string;
  quantidade: number;
  unidade: string;
  timestamp: string;
  localizacao?: {
    lat: number;
    lon: number;
  };
  anexos?: Array<{
    tipo: string;
    url: string;
    hash?: string;
  }>;
}

export interface MovimentacaoCriacao {
  producerId: string;
  commodityId: string;
  tipo: string;
  quantidade: number;
  unidade: string;
  timestamp: string;
  localizacao?: {
    lat: number;
    lon: number;
  };
  anexos?: Array<{
    tipo: string;
    url: string;
    hash?: string;
  }>;
}

export interface MovimentacaoLista {
  items: Movimentacao[];
  total: number;
}

export interface SeloVerde {
  producerId: string;
  status: 'ATIVO' | 'PENDENTE' | 'INATIVO';
  nivel?: 'BRONZE' | 'PRATA' | 'OURO';
  pontuacao?: number;
  motivos?: string[];
  ultimoCheck: string;
  evidencias?: Array<{
    tipo: string;
    detalhe: string;
  }>;
}

export interface AlteracaoSelo {
  id: string;
  producerId: string;
  deStatus: string;
  paraStatus: string;
  motivo: string;
  timestamp: string;
  evidencia?: string;
}

export interface PropostaFinanciamento {
  id: string;
  instituicaoId: string;
  valorMaximo: number;
  taxa: number;
  opcoesPrazo: number[];
  condicoes: string;
}

export interface SolicitacaoCredito {
  id: string;
  producerId: string;
  propostaId: string;
  valor: number;
  status: string;
  criadoEm: string;
  historico?: Array<{
    status: string;
    timestamp: string;
    observacoes?: string;
  }>;
}

export interface Commodity {
  id: string;
  nome: string;
  tipo: string;
  descricao?: string;
  producerId: string;
  criadoEm: string;
}

export interface CommodityCriacao {
  nome: string;
  tipo: string;
  descricao?: string;
}

export interface ApiError {
  codigo: string;
  mensagem: string;
}

