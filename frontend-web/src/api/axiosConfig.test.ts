import { describe, it, expect, beforeEach, vi } from 'vitest';
import axiosInstance from './axiosConfig';

describe('axiosConfig', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should be defined', () => {
    expect(axiosInstance).toBeDefined();
    expect(axiosInstance).toHaveProperty('get');
    expect(axiosInstance).toHaveProperty('post');
    expect(axiosInstance).toHaveProperty('patch');
  });

  it('should have interceptors configured', () => {
    expect(axiosInstance.interceptors).toBeDefined();
    expect(axiosInstance.interceptors.request).toBeDefined();
    expect(axiosInstance.interceptors.response).toBeDefined();
  });
});

