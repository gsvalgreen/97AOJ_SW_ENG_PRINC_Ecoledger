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
  People,
  Assignment,
  AccountBalance,
  TrendingUp,
} from '@mui/icons-material';
import { useAuthStore } from '../../../store/authStore';
import { usersApi } from '../../../api/usersApi';
import { creditoApi } from '../../../api/creditoApi';
import { ROUTES } from '../../../utils/constants';

const DashboardAnalistaPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [stats, setStats] = useState({
    cadastrosPendentes: 0,
    solicitacoesPendentes: 0,
    cadastrosAprovados: 0,
    solicitacoesAprovadas: 0,
  });

  useEffect(() => {
    if (!user || user.role !== 'analista') {
      navigate(ROUTES.DASHBOARD);
      return;
    }

    loadStats();
  }, [user, navigate]);

  const loadStats = async () => {
    setLoading(true);
    setError(null);

    try {
      const [cadastrosData, solicitacoesData] = await Promise.all([
        usersApi.listarCadastros({ status: 'PENDENTE', page: 1, size: 1 }).catch(() => ({ items: [], total: 0 })),
        creditoApi.listarSolicitacoes({ status: 'PENDENTE', page: 1, size: 1 }).catch(() => ({ items: [], total: 0 })),
      ]);

      const [cadastrosAprovadosData, solicitacoesAprovadasData] = await Promise.all([
        usersApi.listarCadastros({ status: 'APROVADO', page: 1, size: 1 }).catch(() => ({ items: [], total: 0 })),
        creditoApi.listarSolicitacoes({ status: 'APROVADO', page: 1, size: 1 }).catch(() => ({ items: [], total: 0 })),
      ]);

      setStats({
        cadastrosPendentes: cadastrosData.total,
        solicitacoesPendentes: solicitacoesData.total,
        cadastrosAprovados: cadastrosAprovadosData.total,
        solicitacoesAprovadas: solicitacoesAprovadasData.total,
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar estatísticas');
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return null;
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h4" component="h1" gutterBottom>
              Dashboard do Analista
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Gerencie cadastros e analise solicitações de crédito
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
            <Grid container spacing={3} sx={{ mb: 4 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <Box>
                        <Typography variant="h4" color="warning.main">
                          {stats.cadastrosPendentes}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Cadastros Pendentes
                        </Typography>
                      </Box>
                      <People color="warning" sx={{ fontSize: 48 }} />
                    </Box>
                    <Button
                      variant="outlined"
                      fullWidth
                      sx={{ mt: 2 }}
                      onClick={() => navigate(ROUTES.CADASTROS)}
                    >
                      Ver Cadastros
                    </Button>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <Box>
                        <Typography variant="h4" color="primary.main">
                          {stats.solicitacoesPendentes}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Solicitações Pendentes
                        </Typography>
                      </Box>
                      <AccountBalance color="primary" sx={{ fontSize: 48 }} />
                    </Box>
                    <Button
                      variant="outlined"
                      fullWidth
                      sx={{ mt: 2 }}
                      onClick={() => navigate(ROUTES.CREDITO_SOLICITACOES)}
                    >
                      Ver Solicitações
                    </Button>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <Box>
                        <Typography variant="h4" color="success.main">
                          {stats.cadastrosAprovados}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Cadastros Aprovados
                        </Typography>
                      </Box>
                      <Assignment color="success" sx={{ fontSize: 48 }} />
                    </Box>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <Box>
                        <Typography variant="h4" color="success.main">
                          {stats.solicitacoesAprovadas}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Créditos Aprovados
                        </Typography>
                      </Box>
                      <TrendingUp color="success" sx={{ fontSize: 48 }} />
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>

            <Box>
              <Typography variant="h6" gutterBottom>
                Ações Rápidas
              </Typography>
              <Grid container spacing={2} sx={{ mt: 1 }}>
                <Grid item xs={12} sm={6} md={4}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                        <People color="primary" sx={{ fontSize: 48 }} />
                        <Button
                          variant="contained"
                          fullWidth
                          onClick={() => navigate(ROUTES.CADASTROS)}
                        >
                          Gerenciar Cadastros
                        </Button>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={4}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                        <AccountBalance color="primary" sx={{ fontSize: 48 }} />
                        <Button
                          variant="contained"
                          fullWidth
                          onClick={() => navigate(ROUTES.CREDITO_SOLICITACOES)}
                        >
                          Solicitações de Crédito
                        </Button>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={4}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                        <Assignment color="primary" sx={{ fontSize: 48 }} />
                        <Button
                          variant="contained"
                          fullWidth
                          onClick={() => navigate(ROUTES.MOVIMENTACOES)}
                        >
                          Consultar Movimentações
                        </Button>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            </Box>
          </>
        )}
      </Box>
    </Container>
  );
};

export default DashboardAnalistaPage;

