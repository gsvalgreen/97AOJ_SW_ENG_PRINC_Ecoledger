import { creditoApiInstance } from './axiosConfig';
import type { PropostaFinanciamento, SolicitacaoCredito } from '../types';

export interface SolicitacaoCreditoCriacao {
  producerId: string;
  propostaId: string;
  valorSolicitado: number;
  prazoMeses: number;
  documentos?: Array<{
    tipo: string;
    url: string;
  }>;
}

export const creditoApi = {
  getPropostas: async (producerId: string): Promise<PropostaFinanciamento[]> => {
    const response = await creditoApiInstance.get<PropostaFinanciamento[]>(
      `/propostas?producerId=${producerId}`
    );
    return response.data;
  },

  criarSolicitacao: async (data: SolicitacaoCreditoCriacao): Promise<{ solicitacaoId: string }> => {
    const response = await creditoApiInstance.post<{ solicitacaoId: string }>('/solicitacoes-credito', data);
    return response.data;
  },

  getSolicitacao: async (id: string): Promise<SolicitacaoCredito> => {
    const response = await creditoApiInstance.get<SolicitacaoCredito>(`/solicitacoes-credito/${id}`);
    return response.data;
  },

  updateSolicitacaoStatus: async (id: string, status: string): Promise<SolicitacaoCredito> => {
    const response = await creditoApiInstance.patch<SolicitacaoCredito>(`/solicitacoes-credito/${id}/status`, {
      status,
    });
    return response.data;
  },

  listarSolicitacoes: async (filters?: { status?: string; producerId?: string; page?: number; size?: number }): Promise<{
    items: SolicitacaoCredito[];
    total: number;
  }> => {
    const response = await creditoApiInstance.get<{ items: SolicitacaoCredito[]; total: number }>(
      '/solicitacoes-credito',
      { params: filters }
    );
    return response.data;
  },
};

