import { usersApiInstance } from './axiosConfig';
import type { Usuario, CadastroCriacao, RespostaCadastro, TokenAuth, LoginRequest } from '../types';

export const usersApi = {
  login: async (data: LoginRequest): Promise<TokenAuth> => {
    const response = await usersApiInstance.post<TokenAuth>('/usuarios/auth/login', data);
    return response.data;
  },

  register: async (data: CadastroCriacao, idempotencyKey?: string): Promise<RespostaCadastro> => {
    const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {};
    const payload = {
      ...data,
      dadosFazenda: data.dadosFazenda || {},
      anexos: data.anexos || [],
    };
    const response = await usersApiInstance.post<RespostaCadastro>('/usuarios/cadastros', payload, { headers });
    return response.data;
  },

  getCadastro: async (id: string): Promise<RespostaCadastro> => {
    const response = await usersApiInstance.get<RespostaCadastro>(`/usuarios/cadastros/${id}`);
    return response.data;
  },

  getUsuario: async (id: string): Promise<Usuario> => {
    const response = await usersApiInstance.get<Usuario>(`/usuarios/${id}`);
    return response.data;
  },

  updateUsuario: async (id: string, data: Partial<Usuario>): Promise<Usuario> => {
    const response = await usersApiInstance.patch<Usuario>(`/usuarios/${id}`, data);
    return response.data;
  },

  updateUsuarioStatus: async (id: string, status: string, reason?: string): Promise<Usuario> => {
    const response = await usersApiInstance.patch<Usuario>(`/usuarios/${id}/status`, {
      status,
      reason,
    });
    return response.data;
  },

  listarCadastros: async (filters?: { status?: string; page?: number; size?: number }): Promise<{
    items: RespostaCadastro[];
    total: number;
  }> => {
    const response = await usersApiInstance.get<{ items: RespostaCadastro[]; total: number }>('/usuarios/cadastros', {
      params: filters,
    });
    return response.data;
  },
};

