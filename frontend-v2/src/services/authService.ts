import api from './api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenAuthDto {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface CadastroCriacaoDto {
  nome: string;
  email: string;
  documento: string;
  senha: string;
  role: string;
  dadosFazenda: Record<string, any>;
  anexos: Array<Record<string, any>>;
}

export interface RespostaCadastroDto {
  cadastroId: string;
  status: string;
  criadoEm: string;
}

export interface UsuarioDto {
  id: string;
  nomeCompleto: string;
  email: string;
  role: string;
  status: string;
  cpf?: string;
  telefone?: string;
  localizacao?: string;
  criadoEm: string;
  atualizadoEm: string;
}

export interface UsuarioAtualizacaoDto {
  nomeCompleto?: string;
  telefone?: string;
  localizacao?: string;
}

export const authService = {
  login: async (credentials: LoginRequest): Promise<TokenAuthDto> => {
    const response = await api.post<TokenAuthDto>('/usuarios/auth/login', credentials);
    return response.data;
  },

  cadastrar: async (dados: CadastroCriacaoDto): Promise<RespostaCadastroDto> => {
    const response = await api.post<RespostaCadastroDto>('/usuarios/cadastros', dados);
    return response.data;
  },

  getProfile: async (userId: string): Promise<UsuarioDto> => {
    const response = await api.get<UsuarioDto>(`/usuarios/${userId}`);
    return response.data;
  },

  updateProfile: async (userId: string, dados: UsuarioAtualizacaoDto): Promise<UsuarioDto> => {
    const response = await api.patch<UsuarioDto>(`/usuarios/${userId}`, dados);
    return response.data;
  },

  updateStatus: async (userId: string, status: string): Promise<UsuarioDto> => {
    const response = await api.patch<UsuarioDto>(`/usuarios/${userId}/status`, { status });
    return response.data;
  },
};

// Helper para decodificar JWT e extrair userId
export const decodeToken = (token: string): { sub: string; role: string } => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    throw new Error('Token inválido');
  }
};
