import { describe, it, expect, beforeEach, vi } from 'vitest';
import { creditoApi } from './creditoApi';
import type { PropostaFinanciamento, SolicitacaoCredito } from '../types';

vi.mock('./axiosConfig', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    patch: vi.fn(),
  },
}));

import axiosInstance from './axiosConfig';

describe('creditoApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getPropostas', () => {
    it('should fetch propostas for producer', async () => {
      const mockResponse: PropostaFinanciamento[] = [
        {
          id: '1',
          instituicaoId: 'inst-1',
          valorMaximo: 100000,
          taxa: 5.5,
          opcoesPrazo: [12, 24, 36],
          condicoes: 'Condições especiais',
        },
      ];

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await creditoApi.getPropostas('prod-1');

      expect(axiosInstance.get).toHaveBeenCalledWith('/credito/propostas?producerId=prod-1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('criarSolicitacao', () => {
    it('should create solicitacao', async () => {
      const mockData = {
        producerId: 'prod-1',
        propostaId: 'prop-1',
        valorSolicitado: 50000,
        prazoMeses: 24,
      };

      const mockResponse = { solicitacaoId: '1' };
      (axiosInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await creditoApi.criarSolicitacao(mockData);

      expect(axiosInstance.post).toHaveBeenCalledWith('/credito/solicitacoes-credito', mockData);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getSolicitacao', () => {
    it('should fetch solicitacao by id', async () => {
      const mockResponse: SolicitacaoCredito = {
        id: '1',
        producerId: 'prod-1',
        propostaId: 'prop-1',
        valor: 50000,
        status: 'PENDENTE',
        criadoEm: '2024-01-01T00:00:00Z',
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await creditoApi.getSolicitacao('1');

      expect(axiosInstance.get).toHaveBeenCalledWith('/credito/solicitacoes-credito/1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('updateSolicitacaoStatus', () => {
    it('should update solicitacao status', async () => {
      const mockResponse: SolicitacaoCredito = {
        id: '1',
        producerId: 'prod-1',
        propostaId: 'prop-1',
        valor: 50000,
        status: 'APROVADO',
        criadoEm: '2024-01-01T00:00:00Z',
      };

      (axiosInstance.patch as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await creditoApi.updateSolicitacaoStatus('1', 'APROVADO');

      expect(axiosInstance.patch).toHaveBeenCalledWith('/credito/solicitacoes-credito/1/status', {
        status: 'APROVADO',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('listarSolicitacoes', () => {
    it('should list solicitacoes with filters', async () => {
      const mockResponse = {
        items: [],
        total: 0,
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await creditoApi.listarSolicitacoes({
        status: 'PENDENTE',
        producerId: 'prod-1',
        page: 1,
        size: 20,
      });

      expect(axiosInstance.get).toHaveBeenCalledWith('/credito/solicitacoes-credito', {
        params: {
          status: 'PENDENTE',
          producerId: 'prod-1',
          page: 1,
          size: 20,
        },
      });
      expect(result).toEqual(mockResponse);
    });

    it('should list solicitacoes without filters', async () => {
      const mockResponse = {
        items: [],
        total: 0,
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await creditoApi.listarSolicitacoes();

      expect(axiosInstance.get).toHaveBeenCalledWith('/credito/solicitacoes-credito', {
        params: undefined,
      });
      expect(result).toEqual(mockResponse);
    });
  });
});

