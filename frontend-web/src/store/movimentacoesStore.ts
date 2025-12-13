import { create } from 'zustand';
import type { Movimentacao } from '../types';

interface MovimentacoesState {
  items: Movimentacao[];
  loading: boolean;
  filters: {
    page: number;
    size: number;
    fromDate?: string;
    toDate?: string;
    commodityId?: string;
  };
  setItems: (items: Movimentacao[]) => void;
  setLoading: (loading: boolean) => void;
  setFilters: (filters: Partial<MovimentacoesState['filters']>) => void;
  addItem: (item: Movimentacao) => void;
  updateItem: (id: string, item: Partial<Movimentacao>) => void;
}

export const useMovimentacoesStore = create<MovimentacoesState>((set) => ({
  items: [],
  loading: false,
  filters: {
    page: 1,
    size: 20,
  },
  setItems: (items) => set({ items }),
  setLoading: (loading) => set({ loading }),
  setFilters: (filters) =>
    set((state) => ({
      filters: { ...state.filters, ...filters },
    })),
  addItem: (item) =>
    set((state) => ({
      items: [item, ...state.items],
    })),
  updateItem: (id, updates) =>
    set((state) => ({
      items: state.items.map((item) => (item.id === id ? { ...item, ...updates } : item)),
    })),
}));

