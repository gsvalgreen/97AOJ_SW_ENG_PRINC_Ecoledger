import { describe, it, expect, beforeEach, vi } from 'vitest';
import axios from 'axios';
import { usersApi } from './usersApi';
import type { Usuario, CadastroCriacao, TokenAuth } from '../types';

vi.mock('./axiosConfig', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    patch: vi.fn(),
  },
}));

import axiosInstance from './axiosConfig';

describe('usersApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('login', () => {
    it('should call login endpoint with correct data', async () => {
      const mockResponse: TokenAuth = {
        accessToken: 'token',
        refreshToken: 'refresh',
        expiresIn: 3600,
      };

      (axiosInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await usersApi.login({ email: 'test@example.com', password: 'password' });

      expect(axiosInstance.post).toHaveBeenCalledWith('/usuarios/auth/login', {
        email: 'test@example.com',
        password: 'password',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('register', () => {
    it('should call register endpoint with data', async () => {
      const mockData: CadastroCriacao = {
        nome: 'Test User',
        email: 'test@example.com',
        documento: '12345678900',
        role: 'produtor',
      };

      const mockResponse = { cadastroId: '1', status: 'PENDENTE' };
      (axiosInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await usersApi.register(mockData);

      expect(axiosInstance.post).toHaveBeenCalledWith('/usuarios/cadastros', mockData, { headers: {} });
      expect(result).toEqual(mockResponse);
    });

    it('should include idempotency key when provided', async () => {
      const mockData: CadastroCriacao = {
        nome: 'Test User',
        email: 'test@example.com',
        documento: '12345678900',
        role: 'produtor',
      };

      const mockResponse = { cadastroId: '1', status: 'PENDENTE' };
      (axiosInstance.post as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      await usersApi.register(mockData, 'idempotency-key');

      expect(axiosInstance.post).toHaveBeenCalledWith(
        '/usuarios/cadastros',
        mockData,
        { headers: { 'Idempotency-Key': 'idempotency-key' } }
      );
    });
  });

  describe('getCadastro', () => {
    it('should fetch cadastro by id', async () => {
      const mockResponse = { cadastroId: '1', status: 'PENDENTE' };
      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await usersApi.getCadastro('1');

      expect(axiosInstance.get).toHaveBeenCalledWith('/usuarios/cadastros/1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getUsuario', () => {
    it('should fetch usuario by id', async () => {
      const mockResponse: Usuario = {
        id: '1',
        nome: 'Test User',
        email: 'test@example.com',
        role: 'produtor',
        documento: '12345678900',
        status: 'APROVADO',
        criadoEm: '2024-01-01T00:00:00Z',
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await usersApi.getUsuario('1');

      expect(axiosInstance.get).toHaveBeenCalledWith('/usuarios/usuarios/1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('updateUsuario', () => {
    it('should update usuario with partial data', async () => {
      const mockResponse: Usuario = {
        id: '1',
        nome: 'Updated Name',
        email: 'test@example.com',
        role: 'produtor',
        documento: '12345678900',
        status: 'APROVADO',
        criadoEm: '2024-01-01T00:00:00Z',
      };

      (axiosInstance.patch as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await usersApi.updateUsuario('1', { nome: 'Updated Name' });

      expect(axiosInstance.patch).toHaveBeenCalledWith('/usuarios/usuarios/1', { nome: 'Updated Name' });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('updateUsuarioStatus', () => {
    it('should update usuario status', async () => {
      const mockResponse: Usuario = {
        id: '1',
        nome: 'Test User',
        email: 'test@example.com',
        role: 'produtor',
        documento: '12345678900',
        status: 'APROVADO',
        criadoEm: '2024-01-01T00:00:00Z',
      };

      (axiosInstance.patch as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await usersApi.updateUsuarioStatus('1', 'APROVADO');

      expect(axiosInstance.patch).toHaveBeenCalledWith('/usuarios/usuarios/1/status', {
        status: 'APROVADO',
        reason: undefined,
      });
      expect(result).toEqual(mockResponse);
    });

    it('should include reason when provided', async () => {
      const mockResponse: Usuario = {
        id: '1',
        nome: 'Test User',
        email: 'test@example.com',
        role: 'produtor',
        documento: '12345678900',
        status: 'REJEITADO',
        criadoEm: '2024-01-01T00:00:00Z',
      };

      (axiosInstance.patch as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      await usersApi.updateUsuarioStatus('1', 'REJEITADO', 'Documentação incompleta');

      expect(axiosInstance.patch).toHaveBeenCalledWith('/usuarios/usuarios/1/status', {
        status: 'REJEITADO',
        reason: 'Documentação incompleta',
      });
    });
  });

  describe('listarCadastros', () => {
    it('should list cadastros with filters', async () => {
      const mockResponse = {
        items: [{ cadastroId: '1', status: 'PENDENTE' }],
        total: 1,
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await usersApi.listarCadastros({ status: 'PENDENTE', page: 1, size: 20 });

      expect(axiosInstance.get).toHaveBeenCalledWith('/usuarios/cadastros', {
        params: { status: 'PENDENTE', page: 1, size: 20 },
      });
      expect(result).toEqual(mockResponse);
    });

    it('should list cadastros without filters', async () => {
      const mockResponse = {
        items: [],
        total: 0,
      };

      (axiosInstance.get as ReturnType<typeof vi.fn>).mockResolvedValue({ data: mockResponse });

      const result = await usersApi.listarCadastros();

      expect(axiosInstance.get).toHaveBeenCalledWith('/usuarios/cadastros', {
        params: undefined,
      });
      expect(result).toEqual(mockResponse);
    });
  });
});

