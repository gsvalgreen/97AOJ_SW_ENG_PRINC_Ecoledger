import { certificacaoApiInstance } from './axiosConfig';
import type { SeloVerde, AlteracaoSelo } from '../types';

export const certificacaoApi = {
  getSelo: async (producerId: string): Promise<SeloVerde> => {
    const response = await certificacaoApiInstance.get<SeloVerde>(`/selos/${producerId}`);
    return response.data;
  },

  recalcularSelo: async (producerId: string): Promise<SeloVerde> => {
    const response = await certificacaoApiInstance.post<SeloVerde>(`/selos/${producerId}/recalcular`);
    return response.data;
  },

  getHistoricoSelo: async (producerId: string): Promise<{ alteracoes: AlteracaoSelo[] }> => {
    const response = await certificacaoApiInstance.get<{ alteracoes: AlteracaoSelo[] }>(
      `/selos/${producerId}/historico`
    );
    return response.data;
  },
};

