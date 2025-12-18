import api from './api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenAuthDto {
  accessToken: string;
  refreshToken: string;
  userId: string;
  role: string;
}

export interface CadastroCriacaoDto {
  nomeCompleto: string;
  email: string;
  senha: string;
  role: 'PRODUTOR' | 'ANALISTA' | 'AUDITOR';
  cpf?: string;
  telefone?: string;
  localizacao?: string;
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
