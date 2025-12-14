import { describe, it, expect, beforeEach, vi } from 'vitest';
import { movimentacoesApi } from './movimentacoesApi';
import type { Movimentacao, MovimentacaoCriacao } from '../types';

vi.mock('./axiosConfig', () => ({
  movimentacoesApiInstance: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

import { movimentacoesApiInstance } from './axiosConfig';

describe('movimentacoesApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('criar', () => {
    it('should create movimentacao', async () => {
      const mockData: MovimentacaoCriacao = {
        producerId: 'prod-1',
        commodityId: 'comm-1',
        tipo: 'COLHEITA',
        quantidade: 100,
        unidade: 'kg',
        timestamp: '2024-01-01T00:00:00Z',
      };

      const mockResponse = { movimentacaoId: '1' };
      (movimentacoesApiInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await movimentacoesApi.criar(mockData);

      expect(movimentacoesApiInstance.post).toHaveBeenCalledWith('/movimentacoes/movimentacoes', mockData, {
        headers: {},
      });
      expect(result).toEqual(mockResponse);
    });

    it('should include idempotency key when provided', async () => {
      const mockData: MovimentacaoCriacao = {
        producerId: 'prod-1',
        commodityId: 'comm-1',
        tipo: 'COLHEITA',
        quantidade: 100,
        unidade: 'kg',
        timestamp: '2024-01-01T00:00:00Z',
      };

      const mockResponse = { movimentacaoId: '1' };
      (movimentacoesApiInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      await movimentacoesApi.criar(mockData, 'idempotency-key');

      expect(movimentacoesApiInstance.post).toHaveBeenCalledWith('/movimentacoes/movimentacoes', mockData, {
        headers: { 'X-Idempotency-Key': 'idempotency-key' },
      });
    });
  });

  describe('obter', () => {
    it('should fetch movimentacao by id', async () => {
      const mockResponse: Movimentacao = {
        id: '1',
        producerId: 'prod-1',
        commodityId: 'comm-1',
        tipo: 'COLHEITA',
        quantidade: 100,
        unidade: 'kg',
        timestamp: '2024-01-01T00:00:00Z',
      };

      (movimentacoesApiInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await movimentacoesApi.obter('1');

      expect(movimentacoesApiInstance.get).toHaveBeenCalledWith('/movimentacoes/1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('listarPorProdutor', () => {
    it('should list movimentacoes with filters', async () => {
      const mockResponse = {
        items: [],
        total: 0,
      };

      (movimentacoesApiInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await movimentacoesApi.listarPorProdutor('prod-1', {
        page: 1,
        size: 20,
        fromDate: '2024-01-01',
        toDate: '2024-12-31',
      });

      expect(movimentacoesApiInstance.get).toHaveBeenCalledWith('/produtores/prod-1/movimentacoes', {
        params: {
          page: 1,
          size: 20,
          fromDate: '2024-01-01',
          toDate: '2024-12-31',
        },
      });
      expect(result).toEqual(mockResponse);
    });

    it('should list movimentacoes without filters', async () => {
      const mockResponse = {
        items: [],
        total: 0,
      };

      (movimentacoesApiInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await movimentacoesApi.listarPorProdutor('prod-1');

      expect(movimentacoesApiInstance.get).toHaveBeenCalledWith('/produtores/prod-1/movimentacoes', {
        params: undefined,
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('historicoCommodity', () => {
    it('should fetch commodity history', async () => {
      const mockResponse = {
        movimentacoes: [],
      };

      (movimentacoesApiInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await movimentacoesApi.historicoCommodity('comm-1');

      expect(movimentacoesApiInstance.get).toHaveBeenCalledWith('/commodities/comm-1/historico');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('gerarUploadUrl', () => {
    it('should generate upload URL', async () => {
      const mockResponse = {
        objectKey: 'key-1',
        uploadUrl: 'https://example.com/upload',
      };

      (movimentacoesApiInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await movimentacoesApi.gerarUploadUrl('image/png');

      expect(movimentacoesApiInstance.post).toHaveBeenCalledWith('/anexos/upload-url', {
        contentType: 'image/png',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('confirmarUpload', () => {
    it('should confirm upload', async () => {
      const mockResponse = {
        objectKey: 'key-1',
        url: 'https://example.com/file',
        tipo: 'image/png',
        hash: 'abc123',
        size: 1024,
      };

      (movimentacoesApiInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await movimentacoesApi.confirmarUpload('key-1');

      expect(movimentacoesApiInstance.post).toHaveBeenCalledWith('/anexos/confirm', {
        objectKey: 'key-1',
      });
      expect(result).toEqual(mockResponse);
    });
  });
});

