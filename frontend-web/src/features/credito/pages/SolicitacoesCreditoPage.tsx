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
import { Visibility } from '@mui/icons-material';
import { useAuthStore } from '../../../store/authStore';
import { creditoApi } from '../../../api/creditoApi';
import { ROUTES } from '../../../utils/constants';
import type { SolicitacaoCredito } from '../../../types';

const SolicitacoesCreditoPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [solicitacoes, setSolicitacoes] = useState<SolicitacaoCredito[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(1);
  const [size] = useState(20);
  const [statusFilter, setStatusFilter] = useState<string>('PENDENTE');
  const [producerIdFilter, setProducerIdFilter] = useState('');

  useEffect(() => {
    if (!user || (user.role !== 'analista' && user.role !== 'produtor')) {
      navigate(ROUTES.DASHBOARD);
      return;
    }

    loadSolicitacoes();
  }, [user, navigate, page, statusFilter, producerIdFilter]);

  const loadSolicitacoes = async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await creditoApi.listarSolicitacoes({
        status: statusFilter || undefined,
        producerId: producerIdFilter || undefined,
        page,
        size,
      });
      setSolicitacoes(data.items);
      setTotal(data.total);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar solicitações');
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
    switch (status.toUpperCase()) {
      case 'APROVADO':
      case 'APROVADA':
        return 'success';
      case 'REJEITADO':
      case 'REJEITADA':
        return 'error';
      case 'PENDENTE':
        return 'warning';
      default:
        return 'default';
    }
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Solicitações de Crédito
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
                label="ID do Produtor"
                value={producerIdFilter}
                onChange={(e) => setProducerIdFilter(e.target.value)}
                placeholder="Filtrar por produtor"
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
              <Button variant="contained" fullWidth onClick={loadSolicitacoes}>
                Atualizar
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : solicitacoes.length === 0 ? (
          <Paper elevation={2} sx={{ p: 4, textAlign: 'center' }}>
            <Typography variant="h6" color="text.secondary">
              Nenhuma solicitação encontrada
            </Typography>
          </Paper>
        ) : (
          <>
            <TableContainer component={Paper} elevation={2}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Produtor ID</TableCell>
                    <TableCell>Proposta ID</TableCell>
                    <TableCell>Valor</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Data</TableCell>
                    <TableCell align="right">Ações</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {solicitacoes.map((solicitacao) => (
                    <TableRow key={solicitacao.id} hover>
                      <TableCell>{solicitacao.id}</TableCell>
                      <TableCell>{solicitacao.producerId}</TableCell>
                      <TableCell>{solicitacao.propostaId}</TableCell>
                      <TableCell>{formatCurrency(solicitacao.valor)}</TableCell>
                      <TableCell>
                        <Chip
                          label={solicitacao.status}
                          color={getStatusColor(solicitacao.status) as 'success' | 'warning' | 'error'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{formatDate(solicitacao.criadoEm)}</TableCell>
                      <TableCell align="right">
                        <IconButton
                          size="small"
                          onClick={() => navigate(`${ROUTES.CREDITO_SOLICITACOES}/${solicitacao.id}`)}
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

export default SolicitacoesCreditoPage;

