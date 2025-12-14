import { auditoriaApiInstance } from './axiosConfig';

export interface RegistroAuditoria {
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

export interface RevisaoAuditoria {
  auditorId: string;
  resultado: 'APROVADO' | 'REPROVADO';
  observacoes?: string;
}

export const auditoriaApi = {
  getAuditoria: async (id: string): Promise<RegistroAuditoria> => {
    const response = await auditoriaApiInstance.get<RegistroAuditoria>(`/auditorias/${id}`);
    return response.data;
  },

  getHistoricoProdutor: async (producerId: string): Promise<{ items: RegistroAuditoria[]; total: number }> => {
    const response = await auditoriaApiInstance.get<{ items: RegistroAuditoria[]; total: number }>(
      `/produtores/${producerId}/historico-auditorias`
    );
    return response.data;
  },

  revisarAuditoria: async (id: string, data: RevisaoAuditoria): Promise<RegistroAuditoria> => {
    const response = await auditoriaApiInstance.post<RegistroAuditoria>(`/auditorias/${id}/revisao`, data);
    return response.data;
  },
};

