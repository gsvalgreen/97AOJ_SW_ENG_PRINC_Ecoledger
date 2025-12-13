import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useAuthStore } from './authStore';
import type { Usuario, TokenAuth } from '../types';
import { STORAGE_KEYS } from '../utils/constants';

describe('authStore', () => {
  beforeEach(() => {
    localStorage.clear();
    useAuthStore.getState().logout();
    vi.clearAllMocks();
  });

  const mockUser: Usuario = {
    id: '1',
    nome: 'Test User',
    email: 'test@example.com',
    role: 'produtor',
    documento: '12345678900',
    status: 'APROVADO',
    criadoEm: '2024-01-01T00:00:00Z',
  };

  const mockAuthData: TokenAuth = {
    accessToken: 'access-token',
    refreshToken: 'refresh-token',
    expiresIn: 3600,
  };

  it('should initialize with null values', () => {
    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
    expect(state.refreshToken).toBeNull();
    expect(state.isAuthenticated).toBe(false);
  });

  it('should set auth data and user', () => {
    const setAuth = useAuthStore.getState().setAuth;
    setAuth(mockAuthData, mockUser);

    const state = useAuthStore.getState();
    expect(state.user).toEqual(mockUser);
    expect(state.token).toBe(mockAuthData.accessToken);
    expect(state.refreshToken).toBe(mockAuthData.refreshToken);
    expect(state.isAuthenticated).toBe(true);
  });

  it('should save tokens to localStorage when setting auth', () => {
    const setAuth = useAuthStore.getState().setAuth;
    setAuth(mockAuthData, mockUser);

    expect(localStorage.setItem).toHaveBeenCalledWith(
      STORAGE_KEYS.ACCESS_TOKEN,
      mockAuthData.accessToken
    );
    expect(localStorage.setItem).toHaveBeenCalledWith(
      STORAGE_KEYS.REFRESH_TOKEN,
      mockAuthData.refreshToken
    );
    expect(localStorage.setItem).toHaveBeenCalledWith(
      STORAGE_KEYS.USER,
      JSON.stringify(mockUser)
    );
  });

  it('should logout and clear all data', () => {
    const setAuth = useAuthStore.getState().setAuth;
    const logout = useAuthStore.getState().logout;

    setAuth(mockAuthData, mockUser);
    logout();

    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
    expect(state.refreshToken).toBeNull();
    expect(state.isAuthenticated).toBe(false);
  });

  it('should remove items from localStorage on logout', () => {
    const setAuth = useAuthStore.getState().setAuth;
    const logout = useAuthStore.getState().logout;

    setAuth(mockAuthData, mockUser);
    logout();

    expect(localStorage.removeItem).toHaveBeenCalledWith(STORAGE_KEYS.ACCESS_TOKEN);
    expect(localStorage.removeItem).toHaveBeenCalledWith(STORAGE_KEYS.REFRESH_TOKEN);
    expect(localStorage.removeItem).toHaveBeenCalledWith(STORAGE_KEYS.USER);
  });

  it('should update user', () => {
    const setAuth = useAuthStore.getState().setAuth;
    const updateUser = useAuthStore.getState().updateUser;

    setAuth(mockAuthData, mockUser);

    const updatedUser: Usuario = { ...mockUser, nome: 'Updated Name' };
    updateUser(updatedUser);

    const state = useAuthStore.getState();
    expect(state.user).toEqual(updatedUser);
    expect(localStorage.setItem).toHaveBeenCalledWith(
      STORAGE_KEYS.USER,
      JSON.stringify(updatedUser)
    );
  });

  it('should initialize from localStorage', () => {
    const mockStoredUser = JSON.stringify(mockUser);
    (localStorage.getItem as ReturnType<typeof vi.fn>).mockImplementation((key: string) => {
      if (key === STORAGE_KEYS.ACCESS_TOKEN) return 'stored-token';
      if (key === STORAGE_KEYS.USER) return mockStoredUser;
      return null;
    });

    useAuthStore.getState().initialize();

    const state = useAuthStore.getState();
    expect(state.token).toBe('stored-token');
    expect(state.user).toEqual(mockUser);
    expect(state.isAuthenticated).toBe(true);
  });

  it('should handle invalid JSON in localStorage gracefully', () => {
    (localStorage.getItem as ReturnType<typeof vi.fn>).mockImplementation((key: string) => {
      if (key === STORAGE_KEYS.ACCESS_TOKEN) return 'token';
      if (key === STORAGE_KEYS.USER) return 'invalid-json';
      return null;
    });

    useAuthStore.getState().initialize();

    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.isAuthenticated).toBe(false);
  });
});

