import { describe, it, expect } from 'vitest';
import { ROUTES, STORAGE_KEYS } from './constants';

describe('constants', () => {
  describe('ROUTES', () => {
    it('should have all required routes defined', () => {
      expect(ROUTES.HOME).toBe('/');
      expect(ROUTES.LOGIN).toBe('/login');
      expect(ROUTES.REGISTER).toBe('/register');
      expect(ROUTES.DASHBOARD).toBe('/dashboard');
      expect(ROUTES.DASHBOARD_PRODUTOR).toBe('/dashboard/produtor');
      expect(ROUTES.DASHBOARD_ANALISTA).toBe('/dashboard/analista');
      expect(ROUTES.DASHBOARD_AUDITOR).toBe('/dashboard/auditor');
      expect(ROUTES.MOVIMENTACOES).toBe('/movimentacoes');
      expect(ROUTES.MOVIMENTACOES_NOVA).toBe('/movimentacoes/nova');
      expect(ROUTES.MOVIMENTACOES_DETALHE).toBe('/movimentacoes/:id');
      expect(ROUTES.CERTIFICACAO).toBe('/certificacao');
      expect(ROUTES.CREDITO_PROPOSTAS).toBe('/credito/propostas');
      expect(ROUTES.CREDITO_SOLICITACOES).toBe('/credito/solicitacoes');
      expect(ROUTES.CADASTROS).toBe('/cadastros');
    });

    it('should have routes as const', () => {
      expect(typeof ROUTES).toBe('object');
      expect(ROUTES).toBeDefined();
    });
  });

  describe('STORAGE_KEYS', () => {
    it('should have all required storage keys defined', () => {
      expect(STORAGE_KEYS.ACCESS_TOKEN).toBe('ecoledger_access_token');
      expect(STORAGE_KEYS.REFRESH_TOKEN).toBe('ecoledger_refresh_token');
      expect(STORAGE_KEYS.USER).toBe('ecoledger_user');
    });

    it('should have storage keys as const', () => {
      expect(typeof STORAGE_KEYS).toBe('object');
      expect(STORAGE_KEYS).toBeDefined();
    });
  });
});

