import axios, { AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';
import { API_SERVICES, STORAGE_KEYS } from '../utils/constants';

const createAxiosInstance = (baseURL: string): AxiosInstance => {
  const instance = axios.create({
    baseURL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  instance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  instance.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true;

        const refreshToken = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
        if (refreshToken) {
          try {
            const response = await axios.post(`${API_SERVICES.USERS}/auth/refresh`, {
              refreshToken,
            });
            const { accessToken } = response.data;
            localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, accessToken);
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${accessToken}`;
            }
            return instance(originalRequest);
          } catch (refreshError) {
            localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
            localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
            localStorage.removeItem(STORAGE_KEYS.USER);
            window.location.href = '/login';
            return Promise.reject(refreshError);
          }
        } else {
          localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.USER);
          window.location.href = '/login';
        }
      }

      return Promise.reject(error);
    }
  );

  return instance;
};

export const usersApiInstance = createAxiosInstance(API_SERVICES.USERS);
export const movimentacoesApiInstance = createAxiosInstance(API_SERVICES.MOVIMENTACOES);
export const certificacaoApiInstance = createAxiosInstance(API_SERVICES.CERTIFICACAO);
export const auditoriaApiInstance = createAxiosInstance(API_SERVICES.AUDITORIA);
export const creditoApiInstance = createAxiosInstance(API_SERVICES.CREDITO);
export const notificacoesApiInstance = createAxiosInstance(API_SERVICES.NOTIFICACOES);

const axiosInstance = usersApiInstance;

export default axiosInstance;

