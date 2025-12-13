import { describe, it, expect } from 'vitest';
import { theme } from './theme';

describe('theme', () => {
  it('should be defined', () => {
    expect(theme).toBeDefined();
  });

  it('should have primary color defined', () => {
    expect(theme.palette.primary).toBeDefined();
    expect(theme.palette.primary.main).toBe('#2e7d32');
  });

  it('should have secondary color defined', () => {
    expect(theme.palette.secondary).toBeDefined();
    expect(theme.palette.secondary.main).toBe('#1976d2');
  });

  it('should have typography configured', () => {
    expect(theme.typography).toBeDefined();
    expect(theme.typography.fontFamily).toContain('Inter');
  });

  it('should have shape borderRadius configured', () => {
    expect(theme.shape).toBeDefined();
    expect(theme.shape.borderRadius).toBe(8);
  });

  it('should have component overrides', () => {
    expect(theme.components).toBeDefined();
    expect(theme.components?.MuiButton).toBeDefined();
    expect(theme.components?.MuiCard).toBeDefined();
  });
});

