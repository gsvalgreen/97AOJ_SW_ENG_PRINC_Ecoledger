import axiosInstance from './axiosConfig';

export interface PreferenciaNotificacao {
  userId: string;
  canais: {
    email: boolean;
    push: boolean;
    webhook: boolean;
  };
}

export const notificacoesApi = {
  getPreferencias: async (userId: string): Promise<PreferenciaNotificacao> => {
    const response = await axiosInstance.get<PreferenciaNotificacao>(`/notificacoes/preferencias/${userId}`);
    return response.data;
  },

  updatePreferencias: async (userId: string, preferencias: Partial<PreferenciaNotificacao>): Promise<PreferenciaNotificacao> => {
    const response = await axiosInstance.patch<PreferenciaNotificacao>(
      `/notificacoes/preferencias/${userId}`,
      preferencias
    );
    return response.data;
  },
};

