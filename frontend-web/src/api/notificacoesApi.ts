import { notificacoesApiInstance } from './axiosConfig';

export interface PreferenciaNotificacao {
  userId: string;
  canais: {
    email: boolean;
    push: boolean;
    webhook: boolean;
  };
}

export const notificacoesApi = {
  enviarNotificacao: async (data: {
    paraUsuarioId?: string;
    canal: 'email' | 'push' | 'webhook';
    templateId: string;
    data: Record<string, unknown>;
  }): Promise<{ notificacaoId: string }> => {
    const response = await notificacoesApiInstance.post<{ notificacaoId: string }>('/notificacoes/enviar', data);
    return response.data;
  },

  getPreferencias: async (userId: string): Promise<PreferenciaNotificacao> => {
    const response = await notificacoesApiInstance.get<PreferenciaNotificacao>(`/preferencias/${userId}`);
    return response.data;
  },

  updatePreferencias: async (userId: string, preferencias: Partial<PreferenciaNotificacao>): Promise<PreferenciaNotificacao> => {
    const response = await notificacoesApiInstance.patch<PreferenciaNotificacao>(
      `/preferencias/${userId}`,
      preferencias
    );
    return response.data;
  },
};

