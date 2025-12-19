import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface User {
  id: string;
  email: string;
  role: string;
  nome: string;
  status: string;
}

interface AuthState {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
  updateUser: (user: User) => void;
  refreshUser: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      isAuthenticated: false,
      login: (token, user) => {
        // Limpar cache do Zustand persist antes de salvar novos dados
        localStorage.removeItem('auth-storage');
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
        set({ token, user, isAuthenticated: true });
      },
      logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        set({ token: null, user: null, isAuthenticated: false });
      },
      updateUser: (user) => {
        localStorage.setItem('user', JSON.stringify(user));
        set({ user });
      },
      refreshUser: async () => {
        const state = useAuthStore.getState();
        if (!state.user?.id) return;
        
        try {
          const { authService } = await import('../services/authService');
          const updatedUser = await authService.getProfile(state.user.id);
          const newUser = {
            id: updatedUser.id,
            email: updatedUser.email,
            role: updatedUser.role.toUpperCase(),
            nome: updatedUser.nome,
            status: updatedUser.status.toUpperCase(),
          };
          localStorage.setItem('user', JSON.stringify(newUser));
          // Forçar atualização do estado (criar novo objeto para garantir re-render)
          set({ user: { ...newUser } });
        } catch (error) {
          console.error('Erro ao atualizar perfil:', error);
          throw error;
        }
      },
    }),
    {
      name: 'auth-storage',
    }
  )
);
