import { describe, it, expect } from 'vitest';
import { PublicRoute } from './PublicRoute';

describe('PublicRoute', () => {
  it('should be defined', () => {
    expect(PublicRoute).toBeDefined();
  });

  it('should be a function component', () => {
    expect(typeof PublicRoute).toBe('function');
  });
});

