import axiosInstance from './axiosConfig';
import type { Movimentacao, MovimentacaoCriacao, MovimentacaoLista } from '../types';

export interface MovimentacaoFilters {
  page?: number;
  size?: number;
  fromDate?: string;
  toDate?: string;
  commodityId?: string;
}

export const movimentacoesApi = {
  criar: async (data: MovimentacaoCriacao, idempotencyKey?: string): Promise<{ movimentacaoId: string }> => {
    const headers = idempotencyKey ? { 'X-Idempotency-Key': idempotencyKey } : {};
    const response = await axiosInstance.post<{ movimentacaoId: string }>('/movimentacoes/movimentacoes', data, {
      headers,
    });
    return response.data;
  },

  obter: async (id: string): Promise<Movimentacao> => {
    const response = await axiosInstance.get<Movimentacao>(`/movimentacoes/movimentacoes/${id}`);
    return response.data;
  },

  listarPorProdutor: async (producerId: string, filters?: MovimentacaoFilters): Promise<MovimentacaoLista> => {
    const response = await axiosInstance.get<MovimentacaoLista>(
      `/movimentacoes/produtores/${producerId}/movimentacoes`,
      { params: filters }
    );
    return response.data;
  },

  historicoCommodity: async (commodityId: string): Promise<{ movimentacoes: Movimentacao[] }> => {
    const response = await axiosInstance.get<{ movimentacoes: Movimentacao[] }>(
      `/movimentacoes/commodities/${commodityId}/historico`
    );
    return response.data;
  },

  gerarUploadUrl: async (contentType: string): Promise<{ objectKey: string; uploadUrl: string }> => {
    const response = await axiosInstance.post<{ objectKey: string; uploadUrl: string }>(
      '/movimentacoes/anexos/upload-url',
      { contentType }
    );
    return response.data;
  },

  confirmarUpload: async (objectKey: string): Promise<{
    objectKey: string;
    url: string;
    tipo: string;
    hash: string;
    size: number;
  }> => {
    const response = await axiosInstance.post('/movimentacoes/anexos/confirm', { objectKey });
    return response.data;
  },
};

