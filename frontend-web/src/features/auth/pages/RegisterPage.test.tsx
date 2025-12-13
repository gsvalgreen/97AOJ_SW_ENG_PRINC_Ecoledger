import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '../../../test/testUtils';
import RegisterPage from './RegisterPage';
import { usersApi } from '../../../api/usersApi';

vi.mock('../../../api/usersApi');
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  };
});

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render registration form', () => {
    render(<RegisterPage />);

    expect(screen.getByLabelText(/nome completo/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/cpf\/cnpj/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/tipo de usuário/i)).toBeInTheDocument();
  });

  it('should have required fields in the form', () => {
    render(<RegisterPage />);

    const nomeInput = screen.getByLabelText(/nome completo/i);
    const emailInput = screen.getByLabelText(/email/i);
    const documentoInput = screen.getByLabelText(/cpf\/cnpj/i);

    expect(nomeInput).toBeRequired();
    expect(emailInput).toBeRequired();
    expect(documentoInput).toBeRequired();
  });

  it('should navigate to next step when clicking next', async () => {
    const user = userEvent.setup();
    render(<RegisterPage />);

    const nomeInput = screen.getByLabelText(/nome completo/i);
    const emailInput = screen.getByLabelText(/email/i);
    const documentoInput = screen.getByLabelText(/cpf\/cnpj/i);

    await user.type(nomeInput, 'Test User');
    await user.type(emailInput, 'test@example.com');
    await user.type(documentoInput, '12345678900');

    const nextButton = screen.getByRole('button', { name: /próximo/i });
    await user.click(nextButton);

    await waitFor(() => {
      expect(screen.getByText(/dados pessoais/i)).toBeInTheDocument();
    });
  });

  it('should show fazenda fields for produtor role', async () => {
    const user = userEvent.setup();
    render(<RegisterPage />);

    const nomeInput = screen.getByLabelText(/nome completo/i);
    const emailInput = screen.getByLabelText(/email/i);
    const documentoInput = screen.getByLabelText(/cpf\/cnpj/i);

    await user.type(nomeInput, 'Test User');
    await user.type(emailInput, 'test@example.com');
    await user.type(documentoInput, '12345678900');

    const nextButton = screen.getByRole('button', { name: /próximo/i });
    await user.click(nextButton);

    await waitFor(() => {
      expect(screen.getByLabelText(/nome da fazenda/i)).toBeInTheDocument();
    });
  });

  it('should navigate to next step with valid data', async () => {
    const user = userEvent.setup();
    render(<RegisterPage />);

    const nomeInput = screen.getByLabelText(/nome completo/i);
    const emailInput = screen.getByLabelText(/email/i);
    const documentoInput = screen.getByLabelText(/cpf\/cnpj/i);

    await user.type(nomeInput, 'Test User');
    await user.type(emailInput, 'test@example.com');
    await user.type(documentoInput, '12345678900');

    const nextButton = screen.getByRole('button', { name: /próximo/i });
    await user.click(nextButton);

    await waitFor(() => {
      expect(screen.getByText(/informações adicionais/i)).toBeInTheDocument();
    });
  });
});

