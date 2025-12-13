import { describe, it, expect, beforeEach } from 'vitest';
import { useCertificacaoStore } from './certificacaoStore';
import type { SeloVerde, AlteracaoSelo } from '../types';

describe('certificacaoStore', () => {
  beforeEach(() => {
    useCertificacaoStore.getState().setSelo(null);
    useCertificacaoStore.getState().setHistorico([]);
    useCertificacaoStore.getState().setLoading(false);
  });

  const mockSelo: SeloVerde = {
    producerId: 'prod-1',
    status: 'ATIVO',
    nivel: 'OURO',
    pontuacao: 95,
    ultimoCheck: '2024-01-01T00:00:00Z',
    motivos: ['Sustentabilidade comprovada'],
    evidencias: [{ tipo: 'AUDITORIA', detalhe: 'Auditoria completa' }],
  };

  const mockAlteracao: AlteracaoSelo = {
    id: '1',
    producerId: 'prod-1',
    deStatus: 'PENDENTE',
    paraStatus: 'ATIVO',
    motivo: 'Aprovado após auditoria',
    timestamp: '2024-01-01T00:00:00Z',
  };

  it('should initialize with null selo', () => {
    const state = useCertificacaoStore.getState();
    expect(state.selo).toBeNull();
    expect(state.historico).toEqual([]);
    expect(state.loading).toBe(false);
  });

  it('should set selo', () => {
    const setSelo = useCertificacaoStore.getState().setSelo;
    setSelo(mockSelo);

    const state = useCertificacaoStore.getState();
    expect(state.selo).toEqual(mockSelo);
  });

  it('should set historico', () => {
    const setHistorico = useCertificacaoStore.getState().setHistorico;
    setHistorico([mockAlteracao]);

    const state = useCertificacaoStore.getState();
    expect(state.historico).toEqual([mockAlteracao]);
    expect(state.historico.length).toBe(1);
  });

  it('should set loading state', () => {
    const setLoading = useCertificacaoStore.getState().setLoading;
    setLoading(true);

    expect(useCertificacaoStore.getState().loading).toBe(true);

    setLoading(false);
    expect(useCertificacaoStore.getState().loading).toBe(false);
  });

  it('should handle multiple historico entries', () => {
    const setHistorico = useCertificacaoStore.getState().setHistorico;
    const alteracao2: AlteracaoSelo = {
      ...mockAlteracao,
      id: '2',
      deStatus: 'ATIVO',
      paraStatus: 'INATIVO',
    };

    setHistorico([mockAlteracao, alteracao2]);

    const state = useCertificacaoStore.getState();
    expect(state.historico.length).toBe(2);
    expect(state.historico[0]).toEqual(mockAlteracao);
    expect(state.historico[1]).toEqual(alteracao2);
  });

  it('should replace existing selo when setting new one', () => {
    const setSelo = useCertificacaoStore.getState().setSelo;
    setSelo(mockSelo);

    const newSelo: SeloVerde = {
      ...mockSelo,
      status: 'INATIVO',
      nivel: 'BRONZE',
    };

    setSelo(newSelo);

    const state = useCertificacaoStore.getState();
    expect(state.selo).toEqual(newSelo);
    expect(state.selo?.status).toBe('INATIVO');
  });
});

