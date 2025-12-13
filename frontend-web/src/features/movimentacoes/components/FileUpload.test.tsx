import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../../../test/testUtils';
import userEvent from '@testing-library/user-event';
import FileUpload from './FileUpload';
import { movimentacoesApi } from '../../../api/movimentacoesApi';

vi.mock('../../../api/movimentacoesApi');

describe('FileUpload', () => {
  const mockOnFilesChange = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  it('should render file upload component', () => {
    render(<FileUpload onFilesChange={mockOnFilesChange} />);

    expect(screen.getByText(/selecionar arquivos/i)).toBeInTheDocument();
    expect(screen.getByText(/arraste arquivos aqui/i)).toBeInTheDocument();
  });

  it('should show max files and size information', () => {
    render(<FileUpload onFilesChange={mockOnFilesChange} maxFiles={5} maxSizeMB={20} />);

    expect(screen.getByText(/máximo 5 arquivos, 20MB por arquivo/i)).toBeInTheDocument();
  });

  it('should display file upload interface', () => {
    render(<FileUpload onFilesChange={mockOnFilesChange} />);
    expect(screen.getByText(/selecionar arquivos/i)).toBeInTheDocument();
  });

  it('should accept maxFiles prop', () => {
    render(<FileUpload onFilesChange={mockOnFilesChange} maxFiles={5} />);
    expect(screen.getByText(/máximo 5 arquivos/i)).toBeInTheDocument();
  });

  it('should accept maxSizeMB prop', () => {
    render(<FileUpload onFilesChange={mockOnFilesChange} maxSizeMB={20} />);
    expect(screen.getByText(/20MB por arquivo/i)).toBeInTheDocument();
  });
});

