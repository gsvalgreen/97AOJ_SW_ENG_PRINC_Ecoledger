import { describe, it, expect, beforeEach, vi } from 'vitest';
import { certificacaoApi } from './certificacaoApi';
import type { SeloVerde, AlteracaoSelo } from '../types';

vi.mock('./axiosConfig', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

import axiosInstance from './axiosConfig';

describe('certificacaoApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getSelo', () => {
    it('should fetch selo for producer', async () => {
      const mockResponse: SeloVerde = {
        producerId: 'prod-1',
        status: 'ATIVO',
        nivel: 'OURO',
        pontuacao: 95,
        ultimoCheck: '2024-01-01T00:00:00Z',
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await certificacaoApi.getSelo('prod-1');

      expect(axiosInstance.get).toHaveBeenCalledWith('/certificacao/selos/prod-1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('recalcularSelo', () => {
    it('should recalculate selo', async () => {
      const mockResponse: SeloVerde = {
        producerId: 'prod-1',
        status: 'ATIVO',
        nivel: 'PRATA',
        pontuacao: 85,
        ultimoCheck: '2024-01-01T00:00:00Z',
      };

      (axiosInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await certificacaoApi.recalcularSelo('prod-1');

      expect(axiosInstance.post).toHaveBeenCalledWith('/certificacao/selos/prod-1/recalcular');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getHistoricoSelo', () => {
    it('should fetch selo history', async () => {
      const mockResponse = {
        alteracoes: [
          {
            id: '1',
            producerId: 'prod-1',
            deStatus: 'PENDENTE',
            paraStatus: 'ATIVO',
            motivo: 'Aprovado',
            timestamp: '2024-01-01T00:00:00Z',
          },
        ],
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await certificacaoApi.getHistoricoSelo('prod-1');

      expect(axiosInstance.get).toHaveBeenCalledWith('/certificacao/selos/prod-1/historico');
      expect(result).toEqual(mockResponse);
    });
  });
});

