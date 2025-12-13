import { describe, it, expect, beforeEach, vi } from 'vitest';
import { auditoriaApi } from './auditoriaApi';
import type { RegistroAuditoria } from './auditoriaApi';

vi.mock('./axiosConfig', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

import axiosInstance from './axiosConfig';

describe('auditoriaApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getAuditoria', () => {
    it('should fetch auditoria by id', async () => {
      const mockResponse: RegistroAuditoria = {
        id: '1',
        movimentacaoId: 'mov-1',
        producerId: 'prod-1',
        versaoRegra: '1.0',
        resultado: 'APROVADO',
        evidencias: [],
        processadoEm: '2024-01-01T00:00:00Z',
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await auditoriaApi.getAuditoria('1');

      expect(axiosInstance.get).toHaveBeenCalledWith('/auditoria/auditorias/1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getHistoricoProdutor', () => {
    it('should fetch historico for producer', async () => {
      const mockResponse = {
        auditorias: [],
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await auditoriaApi.getHistoricoProdutor('prod-1');

      expect(axiosInstance.get).toHaveBeenCalledWith('/auditoria/produtores/prod-1/historico-auditorias');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('revisarAuditoria', () => {
    it('should submit revisao', async () => {
      const mockResponse: RegistroAuditoria = {
        id: '1',
        movimentacaoId: 'mov-1',
        producerId: 'prod-1',
        versaoRegra: '1.0',
        resultado: 'APROVADO',
        evidencias: [],
        processadoEm: '2024-01-01T00:00:00Z',
      };

      (axiosInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await auditoriaApi.revisarAuditoria('1', {
        auditorId: 'aud-1',
        resultado: 'APROVADO',
        observacoes: 'Aprovado após revisão',
      });

      expect(axiosInstance.post).toHaveBeenCalledWith('/auditoria/auditorias/1/revisao', {
        auditorId: 'aud-1',
        resultado: 'APROVADO',
        observacoes: 'Aprovado após revisão',
      });
      expect(result).toEqual(mockResponse);
    });
  });
});

