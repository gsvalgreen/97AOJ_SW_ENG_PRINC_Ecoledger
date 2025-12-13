import axiosInstance from './axiosConfig';
import type { SeloVerde, AlteracaoSelo } from '../types';

export const certificacaoApi = {
  getSelo: async (producerId: string): Promise<SeloVerde> => {
    const response = await axiosInstance.get<SeloVerde>(`/certificacao/selos/${producerId}`);
    return response.data;
  },

  recalcularSelo: async (producerId: string): Promise<SeloVerde> => {
    const response = await axiosInstance.post<SeloVerde>(`/certificacao/selos/${producerId}/recalcular`);
    return response.data;
  },

  getHistoricoSelo: async (producerId: string): Promise<{ alteracoes: AlteracaoSelo[] }> => {
    const response = await axiosInstance.get<{ alteracoes: AlteracaoSelo[] }>(
      `/certificacao/selos/${producerId}/historico`
    );
    return response.data;
  },
};

