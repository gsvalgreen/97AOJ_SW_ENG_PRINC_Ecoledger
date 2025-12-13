import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Paper,
  Grid,
  Card,
  CardContent,
  Button,
  CircularProgress,
  Alert,
  Chip,
} from '@mui/material';
import {
  AddCircleOutline,
  ListAlt,
  VerifiedUser,
  AccountBalance,
} from '@mui/icons-material';
import { useAuthStore } from '../../../store/authStore';
import { useCertificacaoStore } from '../../../store/certificacaoStore';
import { useMovimentacoesStore } from '../../../store/movimentacoesStore';
import { certificacaoApi } from '../../../api/certificacaoApi';
import { movimentacoesApi } from '../../../api/movimentacoesApi';
import { ROUTES } from '../../../utils/constants';

const DashboardProdutorPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { selo, setSelo, setLoading: setCertificacaoLoading } = useCertificacaoStore();
  const { items: movimentacoes, setItems, setLoading: setMovimentacoesLoading } = useMovimentacoesStore();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user || user.role !== 'produtor') {
      navigate(ROUTES.DASHBOARD);
      return;
    }

    const loadData = async () => {
      setLoading(true);
      setError(null);

      try {
        setCertificacaoLoading(true);
        setMovimentacoesLoading(true);

        const [seloData, movimentacoesData] = await Promise.all([
          certificacaoApi.getSelo(user.id).catch(() => null),
          movimentacoesApi.listarPorProdutor(user.id, { page: 1, size: 5 }).catch(() => ({ items: [], total: 0 })),
        ]);

        if (seloData) {
          setSelo(seloData);
        }
        setItems(movimentacoesData.items);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erro ao carregar dados');
      } finally {
        setLoading(false);
        setCertificacaoLoading(false);
        setMovimentacoesLoading(false);
      }
    };

    loadData();
  }, [user, navigate, setSelo, setItems, setCertificacaoLoading, setMovimentacoesLoading]);

  if (!user) {
    return null;
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ATIVO':
        return 'success';
      case 'PENDENTE':
        return 'warning';
      case 'INATIVO':
        return 'error';
      default:
        return 'default';
    }
  };

  const getNivelColor = (nivel?: string) => {
    switch (nivel) {
      case 'OURO':
        return 'warning';
      case 'PRATA':
        return 'default';
      case 'BRONZE':
        return 'secondary';
      default:
        return 'default';
    }
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h4" component="h1" gutterBottom>
              Dashboard do Produtor
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Gerencie suas movimentações e acompanhe seu Selo Verde
            </Typography>
          </Box>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <Grid container spacing={3}>
              <Grid item xs={12} md={4}>
                <Paper elevation={2} sx={{ p: 3, height: '100%' }}>
                  <Typography variant="h6" gutterBottom>
                    Status do Selo Verde
                  </Typography>
                  {selo ? (
                    <Box>
                      <Chip
                        label={selo.status}
                        color={getStatusColor(selo.status) as 'success' | 'warning' | 'error'}
                        sx={{ mb: 1 }}
                      />
                      {selo.nivel && (
                        <Box sx={{ mt: 1 }}>
                          <Chip
                            label={`Nível: ${selo.nivel}`}
                            color={getNivelColor(selo.nivel) as 'warning' | 'default' | 'secondary'}
                            size="small"
                          />
                        </Box>
                      )}
                      {selo.pontuacao !== undefined && (
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                          Pontuação: {selo.pontuacao}
                        </Typography>
                      )}
                      <Button
                        variant="outlined"
                        fullWidth
                        sx={{ mt: 2 }}
                        onClick={() => navigate(ROUTES.CERTIFICACAO)}
                      >
                        Ver Detalhes
                      </Button>
                    </Box>
                  ) : (
                    <Typography variant="body2" color="text.secondary">
                      Selo ainda não disponível
                    </Typography>
                  )}
                </Paper>
              </Grid>

              <Grid item xs={12} md={4}>
                <Paper elevation={2} sx={{ p: 3, height: '100%' }}>
                  <Typography variant="h6" gutterBottom>
                    Movimentações Recentes
                  </Typography>
                  <Typography variant="h4" color="primary">
                    {movimentacoes.length}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Últimas 5 movimentações
                  </Typography>
                  <Button
                    variant="outlined"
                    fullWidth
                    onClick={() => navigate(ROUTES.MOVIMENTACOES)}
                  >
                    Ver Todas
                  </Button>
                </Paper>
              </Grid>

              <Grid item xs={12} md={4}>
                <Paper elevation={2} sx={{ p: 3, height: '100%' }}>
                  <Typography variant="h6" gutterBottom>
                    Status do Cadastro
                  </Typography>
                  <Chip
                    label={user.status}
                    color={user.status === 'APROVADO' ? 'success' : 'warning'}
                    sx={{ mb: 2 }}
                  />
                  {user.status !== 'APROVADO' && (
                    <Typography variant="body2" color="text.secondary">
                      Seu cadastro está aguardando aprovação
                    </Typography>
                  )}
                </Paper>
              </Grid>
            </Grid>

            <Box sx={{ mt: 4 }}>
              <Typography variant="h6" gutterBottom>
                Ações Rápidas
              </Typography>
              <Grid container spacing={2} sx={{ mt: 1 }}>
                <Grid item xs={12} sm={6} md={3}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                        <AddCircleOutline color="primary" sx={{ fontSize: 48 }} />
                        <Button
                          variant="contained"
                          fullWidth
                          onClick={() => navigate(ROUTES.MOVIMENTACOES_NOVA)}
                          disabled={user.status !== 'APROVADO'}
                        >
                          Nova Movimentação
                        </Button>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                        <ListAlt color="primary" sx={{ fontSize: 48 }} />
                        <Button
                          variant="contained"
                          fullWidth
                          onClick={() => navigate(ROUTES.MOVIMENTACOES)}
                        >
                          Minhas Movimentações
                        </Button>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                        <VerifiedUser color="primary" sx={{ fontSize: 48 }} />
                        <Button
                          variant="contained"
                          fullWidth
                          onClick={() => navigate(ROUTES.CERTIFICACAO)}
                        >
                          Meu Selo Verde
                        </Button>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                        <AccountBalance color="primary" sx={{ fontSize: 48 }} />
                        <Button
                          variant="contained"
                          fullWidth
                          onClick={() => navigate(ROUTES.CREDITO_PROPOSTAS)}
                          disabled={user.status !== 'APROVADO' || selo?.status !== 'ATIVO'}
                        >
                          Financiamentos
                        </Button>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            </Box>

            {movimentacoes.length > 0 && (
              <Box sx={{ mt: 4 }}>
                <Typography variant="h6" gutterBottom>
                  Últimas Movimentações
                </Typography>
                <Paper elevation={2} sx={{ p: 2 }}>
                  {movimentacoes.slice(0, 5).map((mov) => (
                    <Box
                      key={mov.id}
                      sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        py: 1,
                        borderBottom: '1px solid',
                        borderColor: 'divider',
                        '&:last-child': { borderBottom: 'none' },
                      }}
                    >
                      <Box>
                        <Typography variant="body1">{mov.tipo}</Typography>
                        <Typography variant="body2" color="text.secondary">
                          {new Date(mov.timestamp).toLocaleDateString('pt-BR')}
                        </Typography>
                      </Box>
                      <Box sx={{ textAlign: 'right' }}>
                        <Typography variant="body1">
                          {mov.quantidade} {mov.unidade}
                        </Typography>
                        <Button
                          size="small"
                          onClick={() => navigate(`${ROUTES.MOVIMENTACOES}/${mov.id}`)}
                        >
                          Ver Detalhes
                        </Button>
                      </Box>
                    </Box>
                  ))}
                </Paper>
              </Box>
            )}
          </>
        )}
      </Box>
    </Container>
  );
};

export default DashboardProdutorPage;

