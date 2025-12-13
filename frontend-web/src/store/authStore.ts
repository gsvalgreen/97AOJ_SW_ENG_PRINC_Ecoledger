import { create } from 'zustand';
import type { Usuario, TokenAuth } from '../types';
import { STORAGE_KEYS } from '../utils/constants';

interface AuthState {
  user: Usuario | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  setAuth: (authData: TokenAuth, user: Usuario) => void;
  logout: () => void;
  updateUser: (user: Usuario) => void;
  initialize: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: null,
  refreshToken: null,
  isAuthenticated: false,
  setAuth: (authData, user) => {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, authData.accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, authData.refreshToken);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    set({
      token: authData.accessToken,
      refreshToken: authData.refreshToken,
      user,
      isAuthenticated: true,
    });
  },
  logout: () => {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    set({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
    });
  },
  updateUser: (user) => {
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    set({ user });
  },
  initialize: () => {
    const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    const userStr = localStorage.getItem(STORAGE_KEYS.USER);
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr) as Usuario;
        set({
          token,
          user,
          isAuthenticated: true,
        });
      } catch {
        localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER);
      }
    }
  },
}));

