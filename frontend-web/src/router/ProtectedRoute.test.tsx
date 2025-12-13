import { describe, it, expect } from 'vitest';
import { ProtectedRoute } from './ProtectedRoute';

describe('ProtectedRoute', () => {
  it('should be defined', () => {
    expect(ProtectedRoute).toBeDefined();
  });

  it('should be a function component', () => {
    expect(typeof ProtectedRoute).toBe('function');
  });
});

