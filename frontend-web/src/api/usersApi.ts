import axiosInstance from './axiosConfig';
import type { Usuario, CadastroCriacao, RespostaCadastro, TokenAuth, LoginRequest } from '../types';

export const usersApi = {
  login: async (data: LoginRequest): Promise<TokenAuth> => {
    const response = await axiosInstance.post<TokenAuth>('/usuarios/auth/login', data);
    return response.data;
  },

  register: async (data: CadastroCriacao, idempotencyKey?: string): Promise<RespostaCadastro> => {
    const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {};
    const response = await axiosInstance.post<RespostaCadastro>('/usuarios/cadastros', data, { headers });
    return response.data;
  },

  getCadastro: async (id: string): Promise<RespostaCadastro> => {
    const response = await axiosInstance.get<RespostaCadastro>(`/usuarios/cadastros/${id}`);
    return response.data;
  },

  getUsuario: async (id: string): Promise<Usuario> => {
    const response = await axiosInstance.get<Usuario>(`/usuarios/usuarios/${id}`);
    return response.data;
  },

  updateUsuario: async (id: string, data: Partial<Usuario>): Promise<Usuario> => {
    const response = await axiosInstance.patch<Usuario>(`/usuarios/usuarios/${id}`, data);
    return response.data;
  },

  updateUsuarioStatus: async (id: string, status: string, reason?: string): Promise<Usuario> => {
    const response = await axiosInstance.patch<Usuario>(`/usuarios/usuarios/${id}/status`, {
      status,
      reason,
    });
    return response.data;
  },

  listarCadastros: async (filters?: { status?: string; page?: number; size?: number }): Promise<{
    items: RespostaCadastro[];
    total: number;
  }> => {
    const response = await axiosInstance.get<{ items: RespostaCadastro[]; total: number }>('/usuarios/cadastros', {
      params: filters,
    });
    return response.data;
  },
};

