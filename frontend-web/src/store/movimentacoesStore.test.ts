import { describe, it, expect, beforeEach } from 'vitest';
import { useMovimentacoesStore } from './movimentacoesStore';
import type { Movimentacao } from '../types';

describe('movimentacoesStore', () => {
  beforeEach(() => {
    useMovimentacoesStore.getState().setItems([]);
    useMovimentacoesStore.getState().setFilters({ page: 1, size: 20 });
  });

  const mockMovimentacao: Movimentacao = {
    id: '1',
    producerId: 'prod-1',
    commodityId: 'comm-1',
    tipo: 'COLHEITA',
    quantidade: 100,
    unidade: 'kg',
    timestamp: '2024-01-01T00:00:00Z',
  };

  it('should initialize with empty items', () => {
    const state = useMovimentacoesStore.getState();
    expect(state.items).toEqual([]);
    expect(state.loading).toBe(false);
    expect(state.filters.page).toBe(1);
    expect(state.filters.size).toBe(20);
  });

  it('should set items', () => {
    const setItems = useMovimentacoesStore.getState().setItems;
    setItems([mockMovimentacao]);

    const state = useMovimentacoesStore.getState();
    expect(state.items).toEqual([mockMovimentacao]);
    expect(state.items.length).toBe(1);
  });

  it('should set loading state', () => {
    const setLoading = useMovimentacoesStore.getState().setLoading;
    setLoading(true);

    expect(useMovimentacoesStore.getState().loading).toBe(true);

    setLoading(false);
    expect(useMovimentacoesStore.getState().loading).toBe(false);
  });

  it('should set filters', () => {
    const setFilters = useMovimentacoesStore.getState().setFilters;
    setFilters({ page: 2, size: 10 });

    const state = useMovimentacoesStore.getState();
    expect(state.filters.page).toBe(2);
    expect(state.filters.size).toBe(10);
  });

  it('should merge filters with existing ones', () => {
    const setFilters = useMovimentacoesStore.getState().setFilters;
    setFilters({ page: 1, size: 20 });
    setFilters({ page: 2 });

    const state = useMovimentacoesStore.getState();
    expect(state.filters.page).toBe(2);
    expect(state.filters.size).toBe(20);
  });

  it('should add item to list', () => {
    const addItem = useMovimentacoesStore.getState().addItem;
    addItem(mockMovimentacao);

    const state = useMovimentacoesStore.getState();
    expect(state.items).toContainEqual(mockMovimentacao);
    expect(state.items.length).toBe(1);
  });

  it('should add item at the beginning of list', () => {
    const setItems = useMovimentacoesStore.getState().setItems;
    const addItem = useMovimentacoesStore.getState().addItem;

    const existingMov: Movimentacao = { ...mockMovimentacao, id: '2' };
    setItems([existingMov]);
    addItem(mockMovimentacao);

    const state = useMovimentacoesStore.getState();
    expect(state.items[0]).toEqual(mockMovimentacao);
    expect(state.items[1]).toEqual(existingMov);
  });

  it('should update item by id', () => {
    const setItems = useMovimentacoesStore.getState().setItems;
    const updateItem = useMovimentacoesStore.getState().updateItem;

    setItems([mockMovimentacao]);
    updateItem('1', { quantidade: 200 });

    const state = useMovimentacoesStore.getState();
    expect(state.items[0].quantidade).toBe(200);
    expect(state.items[0].id).toBe('1');
  });

  it('should not update non-existent item', () => {
    const setItems = useMovimentacoesStore.getState().setItems;
    const updateItem = useMovimentacoesStore.getState().updateItem;

    setItems([mockMovimentacao]);
    updateItem('999', { quantidade: 200 });

    const state = useMovimentacoesStore.getState();
    expect(state.items[0].quantidade).toBe(100);
  });

  it('should handle filters with date ranges', () => {
    const setFilters = useMovimentacoesStore.getState().setFilters;
    setFilters({
      fromDate: '2024-01-01',
      toDate: '2024-12-31',
      commodityId: 'comm-1',
    });

    const state = useMovimentacoesStore.getState();
    expect(state.filters.fromDate).toBe('2024-01-01');
    expect(state.filters.toDate).toBe('2024-12-31');
    expect(state.filters.commodityId).toBe('comm-1');
  });
});

