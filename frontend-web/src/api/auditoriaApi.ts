import axiosInstance from './axiosConfig';

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
}

export interface RevisaoAuditoria {
  auditorId: string;
  resultado: 'APROVADO' | 'REPROVADO';
  observacoes?: string;
}

export const auditoriaApi = {
  getAuditoria: async (id: string): Promise<RegistroAuditoria> => {
    const response = await axiosInstance.get<RegistroAuditoria>(`/auditoria/auditorias/${id}`);
    return response.data;
  },

  getHistoricoProdutor: async (producerId: string): Promise<{ auditorias: RegistroAuditoria[] }> => {
    const response = await axiosInstance.get<{ auditorias: RegistroAuditoria[] }>(
      `/auditoria/produtores/${producerId}/historico-auditorias`
    );
    return response.data;
  },

  revisarAuditoria: async (id: string, data: RevisaoAuditoria): Promise<RegistroAuditoria> => {
    const response = await axiosInstance.post<RegistroAuditoria>(`/auditoria/auditorias/${id}/revisao`, data);
    return response.data;
  },
};

