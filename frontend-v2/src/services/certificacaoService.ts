import api from './api';

export interface SeloResponse {
  producerId: string;
  nivel: 'BRONZE' | 'PRATA' | 'OURO' | 'DIAMANTE' | 'NENHUM';
  status: 'ATIVO' | 'SUSPENSO' | 'REVOGADO';
  score: number;
  dataUltimaAtualizacao: string;
  validadeAte?: string;
}

export interface RecalcularRequest {
  motivo?: string;
}

export interface RecalcularResponse {
  producerId: string;
  nivelAnterior: string;
  nivelAtual: string;
  score: number;
  dataCalculo: string;
}

export interface AlteracaoSeloResponse {
  id: string;
  producerId: string;
  nivelAnterior: string;
  nivelAtual: string;
  motivo: string;
  criadaEm: string;
}

export interface HistoricoSeloResponse {
  alteracoes: AlteracaoSeloResponse[];
}

export const certificacaoService = {
  obterSelo: async (producerId: string): Promise<SeloResponse> => {
    const response = await api.get<SeloResponse>(`/selos/${producerId}`);
    return response.data;
  },

  recalcular: async (producerId: string, motivo?: string): Promise<RecalcularResponse> => {
    const response = await api.post<RecalcularResponse>(
      `/selos/${producerId}/recalcular`,
      motivo ? { motivo } : {}
    );
    return response.data;
  },

  historico: async (producerId: string): Promise<HistoricoSeloResponse> => {
    const response = await api.get<HistoricoSeloResponse>(`/selos/${producerId}/historico`);
    return response.data;
  },
};
