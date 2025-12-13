import { describe, it, expect, beforeEach, vi } from 'vitest';
import { notificacoesApi } from './notificacoesApi';
import type { PreferenciaNotificacao } from './notificacoesApi';

vi.mock('./axiosConfig', () => ({
  default: {
    get: vi.fn(),
    patch: vi.fn(),
  },
}));

import axiosInstance from './axiosConfig';

describe('notificacoesApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getPreferencias', () => {
    it('should fetch preferencias for user', async () => {
      const mockResponse: PreferenciaNotificacao = {
        userId: 'user-1',
        canais: {
          email: true,
          push: false,
          webhook: true,
        },
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await notificacoesApi.getPreferencias('user-1');

      expect(axiosInstance.get).toHaveBeenCalledWith('/notificacoes/preferencias/user-1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('updatePreferencias', () => {
    it('should update preferencias', async () => {
      const mockResponse: PreferenciaNotificacao = {
        userId: 'user-1',
        canais: {
          email: false,
          push: true,
          webhook: false,
        },
      };

      (axiosInstance.patch as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await notificacoesApi.updatePreferencias('user-1', {
        canais: {
          email: false,
          push: true,
          webhook: false,
        },
      });

      expect(axiosInstance.patch).toHaveBeenCalledWith('/notificacoes/preferencias/user-1', {
        canais: {
          email: false,
          push: true,
          webhook: false,
        },
      });
      expect(result).toEqual(mockResponse);
    });
  });
});

