import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  TextField,
  Grid,
  Chip,
  IconButton,
  CircularProgress,
  Alert,
  Pagination,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
} from '@mui/material';
import { Visibility, CheckCircle, Cancel } from '@mui/icons-material';
import { useAuthStore } from '../../../store/authStore';
import { usersApi } from '../../../api/usersApi';
import { ROUTES } from '../../../utils/constants';
import type { RespostaCadastro } from '../../../types';

const CadastrosListPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [cadastros, setCadastros] = useState<RespostaCadastro[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(1);
  const [size] = useState(20);
  const [statusFilter, setStatusFilter] = useState<string>('PENDENTE');
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    if (!user || user.role !== 'analista') {
      navigate(ROUTES.DASHBOARD);
      return;
    }

    loadCadastros();
  }, [user, navigate, page, statusFilter]);

  const loadCadastros = async () => {
    if (!user) return;

    setLoading(true);
    setError(null);

    try {
      const data = await usersApi.listarCadastros({
        status: statusFilter || undefined,
        page,
        size,
      });
      setCadastros(data.items);
      setTotal(data.total);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar cadastros');
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (_event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleStatusFilterChange = (newStatus: string) => {
    setStatusFilter(newStatus);
    setPage(1);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'APROVADO':
        return 'success';
      case 'REJEITADO':
        return 'error';
      case 'PENDENTE':
        return 'warning';
      default:
        return 'default';
    }
  };

  const filteredCadastros = cadastros.filter((cadastro) => {
    if (!searchTerm) return true;
    const searchLower = searchTerm.toLowerCase();
    return (
      cadastro.cadastroId.toLowerCase().includes(searchLower) ||
      (cadastro as any).candidatoUsuario?.nome?.toLowerCase().includes(searchLower) ||
      (cadastro as any).candidatoUsuario?.email?.toLowerCase().includes(searchLower)
    );
  });

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Gestão de Cadastros
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="Buscar"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="ID, nome ou email"
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              <FormControl fullWidth>
                <InputLabel>Status</InputLabel>
                <Select value={statusFilter} onChange={(e) => handleStatusFilterChange(e.target.value)} label="Status">
                  <MenuItem value="PENDENTE">Pendente</MenuItem>
                  <MenuItem value="APROVADO">Aprovado</MenuItem>
                  <MenuItem value="REJEITADO">Rejeitado</MenuItem>
                  <MenuItem value="">Todos</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Button variant="contained" fullWidth onClick={loadCadastros}>
                Atualizar
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : filteredCadastros.length === 0 ? (
          <Paper elevation={2} sx={{ p: 4, textAlign: 'center' }}>
            <Typography variant="h6" color="text.secondary">
              Nenhum cadastro encontrado
            </Typography>
          </Paper>
        ) : (
          <>
            <TableContainer component={Paper} elevation={2}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Nome</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Data de Submissão</TableCell>
                    <TableCell align="right">Ações</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredCadastros.map((cadastro) => (
                    <TableRow key={cadastro.cadastroId} hover>
                      <TableCell>{cadastro.cadastroId}</TableCell>
                      <TableCell>{(cadastro as any).candidatoUsuario?.nome || 'N/A'}</TableCell>
                      <TableCell>{(cadastro as any).candidatoUsuario?.email || 'N/A'}</TableCell>
                      <TableCell>
                        <Chip
                          label={cadastro.status}
                          color={getStatusColor(cadastro.status) as 'success' | 'warning' | 'error'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        {(cadastro as any).submetidoEm
                          ? new Date((cadastro as any).submetidoEm).toLocaleDateString('pt-BR')
                          : 'N/A'}
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          size="small"
                          onClick={() => navigate(`${ROUTES.CADASTROS}/${cadastro.cadastroId}`)}
                        >
                          <Visibility />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>

            {total > size && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                <Pagination count={Math.ceil(total / size)} page={page} onChange={handlePageChange} color="primary" />
              </Box>
            )}
          </>
        )}
      </Box>
    </Container>
  );
};

export default CadastrosListPage;

