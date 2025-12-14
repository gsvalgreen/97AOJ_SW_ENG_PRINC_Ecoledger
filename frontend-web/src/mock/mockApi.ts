import axios, { AxiosError } from 'axios';
import type { AxiosRequestConfig, AxiosResponse } from 'axios';
import {
  mockUsers,
  mockTokens,
  mockMovimentacoes,
  mockSeloVerde,
  mockHistoricoSelo,
  mockPropostas,
  mockSolicitacoes,
  mockCadastros,
  getUserByEmail,
  getUserById,
} from './data';
import type { TokenAuth, Usuario, Movimentacao, SeloVerde, SolicitacaoCredito } from '../types';

const MOCK_DELAY = 500; // Simulate network delay

// Simulate delay
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

// Mock API responses
export const mockApi = {
  // Auth
  async login(email: string, password: string): Promise<TokenAuth & { userId?: string }> {
    await delay(MOCK_DELAY);
    
    // Accept any password for mock users
    const user = getUserByEmail(email);
    if (!user) {
      throw new Error('Credenciais inválidas');
    }

    // Create a mock JWT token with user ID
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({ sub: user.id, userId: user.id, email: user.email, exp: Math.floor(Date.now() / 1000) + 3600 }));
    const mockJWT = `${header}.${payload}.mock-signature`;

    return {
      accessToken: mockJWT,
      refreshToken: mockTokens.refreshToken,
      expiresIn: mockTokens.expiresIn,
      userId: user.id,
    };
  },

  async getUsuario(id: string): Promise<Usuario> {
    await delay(MOCK_DELAY);
    const user = getUserById(id);
    if (!user) {
      throw new Error('Usuário não encontrado');
    }
    return user;
  },

  async register(data: any): Promise<{ cadastroId: string; status: string }> {
    await delay(MOCK_DELAY);
    return {
      cadastroId: `cad-${Date.now()}`,
      status: 'PENDENTE',
    };
  },

  async listarCadastros(filters?: any): Promise<{ items: any[]; total: number }> {
    await delay(MOCK_DELAY);
    let items = [...mockCadastros];
    
    if (filters?.status) {
      items = items.filter((c) => c.status === filters.status);
    }
    
    return {
      items,
      total: items.length,
    };
  },

  async getCadastro(id: string): Promise<any> {
    await delay(MOCK_DELAY);
    const cadastro = mockCadastros.find((c) => c.cadastroId === id);
    if (!cadastro) {
      throw new Error('Cadastro não encontrado');
    }
    return cadastro;
  },

  async updateUsuarioStatus(id: string, status: string, reason?: string): Promise<Usuario> {
    await delay(MOCK_DELAY);
    const user = getUserById(id);
    if (!user) {
      throw new Error('Usuário não encontrado');
    }
    return { ...user, status: status as any };
  },

  // Movimentações
  async criarMovimentacao(data: any): Promise<{ movimentacaoId: string }> {
    await delay(MOCK_DELAY);
    return {
      movimentacaoId: `mov-${Date.now()}`,
    };
  },

  async obterMovimentacao(id: string): Promise<Movimentacao> {
    await delay(MOCK_DELAY);
    const mov = mockMovimentacoes.find((m) => m.id === id);
    if (!mov) {
      throw new Error('Movimentação não encontrada');
    }
    return mov;
  },

  async listarMovimentacoes(producerId: string, filters?: any): Promise<{ items: Movimentacao[]; total: number }> {
    await delay(MOCK_DELAY);
    let items = mockMovimentacoes.filter((m) => m.producerId === producerId);
    
    if (filters?.fromDate) {
      items = items.filter((m) => m.timestamp >= filters.fromDate);
    }
    if (filters?.toDate) {
      items = items.filter((m) => m.timestamp <= filters.toDate);
    }
    
    return {
      items,
      total: items.length,
    };
  },

  async historicoCommodity(commodityId: string): Promise<{ movimentacoes: Movimentacao[] }> {
    await delay(MOCK_DELAY);
    const movimentacoes = mockMovimentacoes.filter((m) => m.commodityId === commodityId);
    return { movimentacoes };
  },

  async gerarUploadUrl(contentType: string): Promise<{ objectKey: string; uploadUrl: string }> {
    await delay(MOCK_DELAY);
    return {
      objectKey: `doc-${Date.now()}`,
      uploadUrl: 'https://example.com/upload',
    };
  },

  async confirmarUpload(objectKey: string): Promise<any> {
    await delay(MOCK_DELAY);
    return {
      objectKey,
      url: `https://example.com/files/${objectKey}`,
      tipo: 'application/pdf',
      hash: 'abc123',
      size: 102400,
    };
  },

  // Certificação
  async getSelo(producerId: string): Promise<SeloVerde> {
    await delay(MOCK_DELAY);
    return mockSeloVerde;
  },

  async getHistoricoSelo(producerId: string): Promise<{ alteracoes: any[] }> {
    await delay(MOCK_DELAY);
    return {
      alteracoes: mockHistoricoSelo,
    };
  },

  // Crédito
  async getPropostas(producerId: string): Promise<any[]> {
    await delay(MOCK_DELAY);
    return mockPropostas;
  },

  async criarSolicitacao(data: any): Promise<{ solicitacaoId: string }> {
    await delay(MOCK_DELAY);
    return {
      solicitacaoId: `sol-${Date.now()}`,
    };
  },

  async getSolicitacao(id: string): Promise<SolicitacaoCredito> {
    await delay(MOCK_DELAY);
    const sol = mockSolicitacoes.find((s) => s.id === id);
    if (!sol) {
      throw new Error('Solicitação não encontrada');
    }
    return sol;
  },

  async listarSolicitacoes(filters?: any): Promise<{ items: SolicitacaoCredito[]; total: number }> {
    await delay(MOCK_DELAY);
    let items = [...mockSolicitacoes];
    
    if (filters?.status) {
      items = items.filter((s) => s.status === filters.status);
    }
    if (filters?.producerId) {
      items = items.filter((s) => s.producerId === filters.producerId);
    }
    
    return {
      items,
      total: items.length,
    };
  },

  async updateSolicitacaoStatus(id: string, status: string): Promise<SolicitacaoCredito> {
    await delay(MOCK_DELAY);
    const sol = mockSolicitacoes.find((s) => s.id === id);
    if (!sol) {
      throw new Error('Solicitação não encontrada');
    }
    return { ...sol, status: status as any };
  },
};

// Intercept axios requests and return mock data
// This function should be called with the axios instance to intercept
export const setupMockInterceptor = (instance: any) => {
  if (import.meta.env.VITE_MOCK_API !== 'true') {
    return;
  }

  // Intercept requests before they're sent
  instance.interceptors.request.use(async (config: any) => {
    const url = config.url || '';
    const method = (config.method || 'get').toLowerCase();

    try {
      let response: any;

      // Auth endpoints
      if (url.includes('/usuarios/auth/login') && method === 'post') {
        const { email, password } = config.data || {};
        response = await mockApi.login(email, password);
      } else if (url.match(/\/usuarios\/[^/]+$/) && method === 'get' && !url.includes('/status') && !url.includes('/cadastros') && !url.includes('/auth')) {
        const id = url.split('/').pop()?.split('?')[0];
        response = await mockApi.getUsuario(id!);
      } else if (url.includes('/usuarios/cadastros') && method === 'post') {
        response = await mockApi.register(config.data);
      } else if (url.includes('/usuarios/cadastros') && method === 'get' && !url.match(/\/usuarios\/cadastros\/[^/]+$/)) {
        response = await mockApi.listarCadastros(config.params);
      } else if (url.match(/\/usuarios\/cadastros\/[^/]+$/) && method === 'get') {
        const id = url.split('/').pop()?.split('?')[0];
        response = await mockApi.getCadastro(id!);
      } else if (url.match(/\/usuarios\/[^/]+\/status$/) && method === 'patch') {
        const parts = url.split('/');
        const id = parts[parts.indexOf('usuarios') + 1];
        const { status, reason } = config.data || {};
        response = await mockApi.updateUsuarioStatus(id, status, reason);
      }
      // Movimentações endpoints
      else if (url.includes('/movimentacoes') && method === 'post' && !url.includes('/anexos')) {
        response = await mockApi.criarMovimentacao(config.data);
      } else if (url.match(/\/movimentacoes\/[^/]+$/) && method === 'get') {
        const id = url.split('/').pop()?.split('?')[0];
        response = await mockApi.obterMovimentacao(id!);
      } else if (url.includes('/produtores/') && url.includes('/movimentacoes') && method === 'get') {
        const parts = url.split('/');
        const producerId = parts[parts.indexOf('produtores') + 1];
        response = await mockApi.listarMovimentacoes(producerId, config.params);
      } else if (url.includes('/commodities/') && url.includes('/historico') && method === 'get') {
        const parts = url.split('/');
        const commodityId = parts[parts.indexOf('commodities') + 1];
        response = await mockApi.historicoCommodity(commodityId);
      } else if (url.includes('/anexos/upload-url') && method === 'post') {
        const { contentType } = config.data || {};
        response = await mockApi.gerarUploadUrl(contentType);
      } else if (url.includes('/anexos/confirm') && method === 'post') {
        const { objectKey } = config.data || {};
        response = await mockApi.confirmarUpload(objectKey);
      }
      // Certificação endpoints
      else if (url.match(/\/selos\/[^/]+$/) && !url.includes('/historico') && !url.includes('/recalcular') && method === 'get') {
        const id = url.split('/').pop()?.split('?')[0];
        response = await mockApi.getSelo(id!);
      } else if (url.includes('/selos/') && url.includes('/historico') && method === 'get') {
        const parts = url.split('/');
        const producerId = parts[parts.indexOf('selos') + 1];
        response = await mockApi.getHistoricoSelo(producerId);
      }
      // Crédito endpoints
      else if (url.includes('/propostas') && method === 'get') {
        // Extract producerId from query params or URL
        const producerId = config.params?.producerId || new URLSearchParams(url.split('?')[1] || '').get('producerId');
        response = await mockApi.getPropostas(producerId || 'prod-1');
      } else if (url.includes('/solicitacoes-credito') && method === 'post') {
        response = await mockApi.criarSolicitacao(config.data);
      } else if (url.match(/\/solicitacoes-credito\/[^/]+$/) && !url.includes('/status') && method === 'get') {
        const id = url.split('/').pop()?.split('?')[0];
        response = await mockApi.getSolicitacao(id!);
      } else if (url.includes('/solicitacoes-credito') && method === 'get' && !url.match(/\/solicitacoes-credito\/[^/]+$/)) {
        response = await mockApi.listarSolicitacoes(config.params);
      } else if (url.includes('/solicitacoes-credito/') && url.includes('/status') && method === 'patch') {
        const parts = url.split('/');
        const id = parts[parts.indexOf('solicitacoes-credito') + 1];
        const { status } = config.data || {};
        response = await mockApi.updateSolicitacaoStatus(id, status);
      } else {
        // Not a mock endpoint, let it proceed normally
        return config;
      }

      // Return mock response by throwing a special error that will be caught
      throw { __isMockResponse: true, data: response, config };
    } catch (error: any) {
      if (error.__isMockResponse) {
        // Create a custom adapter that returns the mock response
        config.adapter = async () => {
          return {
            data: error.data,
            status: 200,
            statusText: 'OK',
            headers: {},
            config: error.config,
          } as AxiosResponse;
        };
        return config;
      }
      // Re-throw if it's a real error
      throw error;
    }
  });
};

