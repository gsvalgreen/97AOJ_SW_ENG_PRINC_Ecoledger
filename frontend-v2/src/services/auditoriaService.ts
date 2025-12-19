import api from './api';

export interface RegistroAuditoriaResponse {
  id: string;
  movimentacaoId: string;
  producerId: string;
  versaoRegra: string;
  resultado: 'APROVADO' | 'REPROVADO' | 'REQUER_REVISAO';
  evidencias: Array<{
    tipo: string;
    detalhe: string;
  }>;
  processadoEm: string;
  auditorId?: string;
  observacoes?: string;
  revisadoEm?: string;
}

export interface HistoricoAuditoriasResponse {
  auditorias: RegistroAuditoriaResponse[];
}

export interface RevisaoRequest {
  auditorId: string;
  aprovado: boolean;
  observacoes?: string;
}

export const auditoriaService = {
  buscarPorId: async (id: string): Promise<RegistroAuditoriaResponse> => {
    const response = await api.get<RegistroAuditoriaResponse>(`/auditorias/${id}`);
    return response.data;
  },

  historicoPorProdutor: async (producerId: string): Promise<HistoricoAuditoriasResponse> => {
    const response = await api.get<HistoricoAuditoriasResponse>(`/produtores/${producerId}/historico-auditorias`);
    return response.data;
  },

  aplicarRevisao: async (auditoriaId: string, revisao: RevisaoRequest): Promise<RegistroAuditoriaResponse> => {
    const response = await api.post<RegistroAuditoriaResponse>(`/auditorias/${auditoriaId}/revisao`, revisao);
    return response.data;
  },
};
