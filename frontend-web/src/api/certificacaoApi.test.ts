import { describe, it, expect, beforeEach, vi } from 'vitest';
import { certificacaoApi } from './certificacaoApi';
import type { SeloVerde, AlteracaoSelo } from '../types';

vi.mock('./axiosConfig', () => ({
  certificacaoApiInstance: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

import { certificacaoApiInstance } from './axiosConfig';

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

      (certificacaoApiInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await certificacaoApi.getSelo('prod-1');

      expect(certificacaoApiInstance.get).toHaveBeenCalledWith('/selos/prod-1');
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

      (certificacaoApiInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await certificacaoApi.recalcularSelo('prod-1');

      expect(certificacaoApiInstance.post).toHaveBeenCalledWith('/selos/prod-1/recalcular');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getHistoricoSelo', () => {
    it('should fetch selo history', async () => {
      const mockResponse = {
        alteracoes: [
          {
            deStatus: 'PENDENTE',
            paraStatus: 'ATIVO',
            motivo: 'Aprovado',
            criadoEm: '2024-01-01T00:00:00Z',
          },
        ],
      };

      (certificacaoApiInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await certificacaoApi.getHistoricoSelo('prod-1');

      expect(certificacaoApiInstance.get).toHaveBeenCalledWith('/selos/prod-1/historico');
      expect(result).toEqual(mockResponse);
    });
  });
});

