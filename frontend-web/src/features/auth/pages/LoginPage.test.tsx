import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '../../../test/testUtils';
import LoginPage from './LoginPage';
import { useAuthStore } from '../../../store/authStore';
import { usersApi } from '../../../api/usersApi';

vi.mock('../../../store/authStore');
vi.mock('../../../api/usersApi');
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  };
});

describe('LoginPage', () => {
  const mockNavigate = vi.fn();
  const mockSetAuth = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (useAuthStore as unknown as ReturnType<typeof vi.fn>).mockReturnValue({
      setAuth: mockSetAuth,
    });

    vi.doMock('react-router-dom', () => ({
      useNavigate: () => mockNavigate,
      Link: ({ children, to }: { children: React.ReactNode; to: string }) => (
        <a href={to}>{children}</a>
      ),
    }));
  });

  it('should render login form', () => {
    render(<LoginPage />);

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/senha/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /entrar/i })).toBeInTheDocument();
  });

  it('should show validation errors for empty fields', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);

    const submitButton = screen.getByRole('button', { name: /entrar/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/email inválido/i)).toBeInTheDocument();
    });
  });

  it('should show error for invalid email', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);

    const emailInput = screen.getByLabelText(/email/i);
    await user.type(emailInput, 'invalid-email');

    const submitButton = screen.getByRole('button', { name: /entrar/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/email inválido/i)).toBeInTheDocument();
    });
  });

  it('should show error for short password', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/senha/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, '123');

    const submitButton = screen.getByRole('button', { name: /entrar/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/senha deve ter no mínimo 6 caracteres/i)).toBeInTheDocument();
    });
  });

  it('should call login API on valid form submission', async () => {
    const user = userEvent.setup();
    const mockTokenAuth = {
      accessToken: 'token',
      refreshToken: 'refresh',
      expiresIn: 3600,
    };
    const mockUser = {
      id: '1',
      nome: 'Test User',
      email: 'test@example.com',
      role: 'produtor' as const,
      documento: '12345678900',
      status: 'APROVADO' as const,
      criadoEm: '2024-01-01T00:00:00Z',
    };

    (usersApi.login as ReturnType<typeof vi.fn>).mockResolvedValue(mockTokenAuth);
    (usersApi.getUsuario as ReturnType<typeof vi.fn>).mockResolvedValue(mockUser);

    render(<LoginPage />);

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/senha/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');

    const submitButton = screen.getByRole('button', { name: /entrar/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(usersApi.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
      });
    });
  });

  it('should show error message on login failure', async () => {
    const user = userEvent.setup();
    (usersApi.login as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('Credenciais inválidas'));

    render(<LoginPage />);

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/senha/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'wrongpassword');

    const submitButton = screen.getByRole('button', { name: /entrar/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/credenciais inválidas/i)).toBeInTheDocument();
    });
  });

  it('should show loading state during login', async () => {
    const user = userEvent.setup();
    const mockTokenAuth = {
      accessToken: 'token',
      refreshToken: 'refresh',
      expiresIn: 3600,
    };
    const mockUser = {
      id: '1',
      nome: 'Test User',
      email: 'test@example.com',
      role: 'produtor' as const,
      documento: '12345678900',
      status: 'APROVADO' as const,
      criadoEm: '2024-01-01T00:00:00Z',
    };

    (usersApi.login as ReturnType<typeof vi.fn>).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockTokenAuth), 100))
    );
    (usersApi.getUsuario as ReturnType<typeof vi.fn>).mockResolvedValue(mockUser);

    render(<LoginPage />);

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/senha/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');

    const submitButton = screen.getByRole('button', { name: /entrar/i });
    await user.click(submitButton);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });
});

