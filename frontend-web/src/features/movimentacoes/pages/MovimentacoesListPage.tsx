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
} from '@mui/material';
import { Add, Visibility, FilterList } from '@mui/icons-material';
import { useAuthStore } from '../../../store/authStore';
import { useMovimentacoesStore } from '../../../store/movimentacoesStore';
import { movimentacoesApi } from '../../../api/movimentacoesApi';
import { ROUTES } from '../../../utils/constants';
import type { Movimentacao } from '../../../types';

const MovimentacoesListPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { items, filters, setItems, setFilters, setLoading } = useMovimentacoesStore();
  const [total, setTotal] = useState(0);
  const [loading, setLoadingState] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [commodityId, setCommodityId] = useState('');

  useEffect(() => {
    if (!user) {
      navigate(ROUTES.LOGIN);
      return;
    }

    loadMovimentacoes();
  }, [user, filters.page, filters.size]);

  const loadMovimentacoes = async () => {
    if (!user) return;

    setLoadingState(true);
    setError(null);

    try {
      setLoading(true);
      const data = await movimentacoesApi.listarPorProdutor(user.id, {
        ...filters,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
        commodityId: commodityId || undefined,
      });
      setItems(data.items);
      setTotal(data.total);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar movimentações');
    } finally {
      setLoadingState(false);
      setLoading(false);
    }
  };

  const handleFilter = () => {
    setFilters({ page: 1 });
    loadMovimentacoes();
  };

  const handlePageChange = (_event: unknown, page: number) => {
    setFilters({ page });
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getTipoColor = (tipo: string) => {
    const colors: Record<string, 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error'> = {
      COLHEITA: 'success',
      PROCESSAMENTO: 'primary',
      TRANSPORTE: 'warning',
    };
    return colors[tipo] || 'default';
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4" component="h1">
            Minhas Movimentações
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => navigate(ROUTES.MOVIMENTACOES_NOVA)}
            disabled={user?.status !== 'APROVADO'}
          >
            Nova Movimentação
          </Button>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
            <FilterList sx={{ mr: 1 }} />
            <Typography variant="h6">Filtros</Typography>
          </Box>
          <Grid container spacing={2}>
            {/* @ts-expect-error - MUI v7 Grid API change */}
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="Data Inicial"
                type="date"
                value={fromDate}
                onChange={(e) => setFromDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            {/* @ts-expect-error - MUI v7 Grid API change */}
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="Data Final"
                type="date"
                value={toDate}
                onChange={(e) => setToDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            {/* @ts-expect-error - MUI v7 Grid API change */}
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="ID da Commodity"
                value={commodityId}
                onChange={(e) => setCommodityId(e.target.value)}
              />
            </Grid>
            {/* @ts-expect-error - MUI v7 Grid API change */}
            <Grid item xs={12}>
              <Button variant="contained" onClick={handleFilter}>
                Aplicar Filtros
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : items.length === 0 ? (
          <Paper elevation={2} sx={{ p: 4, textAlign: 'center' }}>
            <Typography variant="h6" color="text.secondary">
              Nenhuma movimentação encontrada
            </Typography>
            <Button
              variant="contained"
              sx={{ mt: 2 }}
              onClick={() => navigate(ROUTES.MOVIMENTACOES_NOVA)}
              disabled={user?.status !== 'APROVADO'}
            >
              Criar Primeira Movimentação
            </Button>
          </Paper>
        ) : (
          <>
            <TableContainer component={Paper} elevation={2}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Commodity ID</TableCell>
                    <TableCell>Quantidade</TableCell>
                    <TableCell>Data/Hora</TableCell>
                    <TableCell>Anexos</TableCell>
                    <TableCell align="right">Ações</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {items.map((mov) => (
                    <TableRow key={mov.id} hover>
                      <TableCell>
                        <Chip label={mov.tipo} color={getTipoColor(mov.tipo)} size="small" />
                      </TableCell>
                      <TableCell>{mov.commodityId}</TableCell>
                      <TableCell>
                        {mov.quantidade} {mov.unidade}
                      </TableCell>
                      <TableCell>{formatDate(mov.timestamp)}</TableCell>
                      <TableCell>{mov.anexos?.length || 0}</TableCell>
                      <TableCell align="right">
                        <IconButton
                          size="small"
                          onClick={() => navigate(`${ROUTES.MOVIMENTACOES}/${mov.id}`)}
                        >
                          <Visibility />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>

            {total > filters.size && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                <Pagination
                  count={Math.ceil(total / filters.size)}
                  page={filters.page}
                  onChange={handlePageChange}
                  color="primary"
                />
              </Box>
            )}
          </>
        )}
      </Box>
    </Container>
  );
};

export default MovimentacoesListPage;

