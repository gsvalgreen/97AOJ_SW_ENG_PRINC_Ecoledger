import { create } from 'zustand';
import type { SeloVerde, AlteracaoSelo } from '../types';

interface CertificacaoState {
  selo: SeloVerde | null;
  historico: AlteracaoSelo[];
  loading: boolean;
  setSelo: (selo: SeloVerde) => void;
  setHistorico: (historico: AlteracaoSelo[]) => void;
  setLoading: (loading: boolean) => void;
}

export const useCertificacaoStore = create<CertificacaoState>((set) => ({
  selo: null,
  historico: [],
  loading: false,
  setSelo: (selo) => set({ selo }),
  setHistorico: (historico) => set({ historico }),
  setLoading: (loading) => set({ loading }),
}));

