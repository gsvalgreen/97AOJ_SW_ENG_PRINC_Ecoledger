import { describe, it, expect } from 'vitest';
import type {
  UserRole,
  UserStatus,
  Usuario,
  CadastroCriacao,
  TokenAuth,
  Movimentacao,
  SeloVerde,
  Commodity,
} from './index';

describe('types', () => {
  it('should have correct UserRole type values', () => {
    const roles: UserRole[] = ['produtor', 'analista', 'auditor'];
    expect(roles).toHaveLength(3);
  });

  it('should have correct UserStatus type values', () => {
    const statuses: UserStatus[] = ['PENDENTE', 'APROVADO', 'REJEITADO'];
    expect(statuses).toHaveLength(3);
  });

  it('should create valid Usuario object', () => {
    const usuario: Usuario = {
      id: '1',
      nome: 'Test',
      email: 'test@example.com',
      role: 'produtor',
      documento: '12345678900',
      status: 'APROVADO',
      criadoEm: '2024-01-01T00:00:00Z',
    };

    expect(usuario.id).toBe('1');
    expect(usuario.role).toBe('produtor');
    expect(usuario.status).toBe('APROVADO');
  });

  it('should create valid CadastroCriacao object', () => {
    const cadastro: CadastroCriacao = {
      nome: 'Test',
      email: 'test@example.com',
      documento: '12345678900',
      role: 'produtor',
    };

    expect(cadastro.nome).toBe('Test');
    expect(cadastro.role).toBe('produtor');
  });

  it('should create valid TokenAuth object', () => {
    const tokenAuth: TokenAuth = {
      accessToken: 'token',
      refreshToken: 'refresh',
      expiresIn: 3600,
    };

    expect(tokenAuth.accessToken).toBe('token');
    expect(tokenAuth.expiresIn).toBe(3600);
  });

  it('should create valid Movimentacao object', () => {
    const movimentacao: Movimentacao = {
      id: '1',
      producerId: 'prod-1',
      commodityId: 'comm-1',
      tipo: 'COLHEITA',
      quantidade: 100,
      unidade: 'kg',
      timestamp: '2024-01-01T00:00:00Z',
    };

    expect(movimentacao.id).toBe('1');
    expect(movimentacao.quantidade).toBe(100);
  });

  it('should create valid SeloVerde object', () => {
    const selo: SeloVerde = {
      producerId: 'prod-1',
      status: 'ATIVO',
      nivel: 'OURO',
      pontuacao: 95,
      ultimoCheck: '2024-01-01T00:00:00Z',
    };

    expect(selo.status).toBe('ATIVO');
    expect(selo.nivel).toBe('OURO');
  });

  it('should create valid Commodity object', () => {
    const commodity: Commodity = {
      id: '1',
      nome: 'Soja',
      tipo: 'Grão',
      producerId: 'prod-1',
      criadoEm: '2024-01-01T00:00:00Z',
    };

    expect(commodity.nome).toBe('Soja');
    expect(commodity.tipo).toBe('Grão');
  });
});

