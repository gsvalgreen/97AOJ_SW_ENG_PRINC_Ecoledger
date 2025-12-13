import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Paper,
  Grid,
  Chip,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Divider,
} from '@mui/material';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
} from '@mui/lab';
import { VerifiedUser, CheckCircle, Cancel, Pending } from '@mui/icons-material';
import { useAuthStore } from '../../../store/authStore';
import { useCertificacaoStore } from '../../../store/certificacaoStore';
import { certificacaoApi } from '../../../api/certificacaoApi';
import { ROUTES } from '../../../utils/constants';

const CertificacaoPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { selo, historico, setSelo, setHistorico, setLoading } = useCertificacaoStore();
  const [loading, setLoadingState] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user) {
      navigate(ROUTES.LOGIN);
      return;
    }

    loadData();
  }, [user, navigate]);

  const loadData = async () => {
    if (!user) return;

    setLoadingState(true);
    setError(null);

    try {
      setLoading(true);
      const [seloData, historicoData] = await Promise.all([
        certificacaoApi.getSelo(user.id).catch(() => null),
        certificacaoApi.getHistoricoSelo(user.id).catch(() => ({ alteracoes: [] })),
      ]);

      if (seloData) {
        setSelo(seloData);
      }
      setHistorico(historicoData.alteracoes);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar dados do selo');
    } finally {
      setLoadingState(false);
      setLoading(false);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ATIVO':
        return <CheckCircle color="success" />;
      case 'PENDENTE':
        return <Pending color="warning" />;
      case 'INATIVO':
        return <Cancel color="error" />;
      default:
        return <VerifiedUser />;
    }
  };

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

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (!user) {
    return null;
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Meu Selo Verde
        </Typography>

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
          <Grid container spacing={3}>
            {/* @ts-expect-error - MUI v7 Grid API change */}
            <Grid item xs={12} md={6}>
              <Paper elevation={2} sx={{ p: 4, textAlign: 'center' }}>
                <Box sx={{ mb: 3 }}>
                  {selo ? getStatusIcon(selo.status) : <VerifiedUser />}
                </Box>
                <Typography variant="h5" gutterBottom>
                  Status do Selo Verde
                </Typography>
                {selo ? (
                  <>
                    <Chip
                      label={selo.status}
                      color={getStatusColor(selo.status) as 'success' | 'warning' | 'error'}
                      sx={{ mb: 2, fontSize: '1rem', py: 2 }}
                    />
                    {selo.nivel && (
                      <Box sx={{ mb: 2 }}>
                        <Chip
                          label={`Nível ${selo.nivel}`}
                          color={getNivelColor(selo.nivel) as 'warning' | 'default' | 'secondary'}
                          sx={{ fontSize: '1rem', py: 1 }}
                        />
                      </Box>
                    )}
                    {selo.pontuacao !== undefined && (
                      <Typography variant="h6" color="primary" sx={{ mb: 2 }}>
                        Pontuação: {selo.pontuacao}
                      </Typography>
                    )}
                    <Typography variant="body2" color="text.secondary">
                      Última verificação: {formatDate(selo.ultimoCheck)}
                    </Typography>
                  </>
                ) : (
                  <Typography variant="body1" color="text.secondary">
                    Selo ainda não disponível. Continue registrando movimentações para obter seu selo verde.
                  </Typography>
                )}
              </Paper>
            </Grid>

            {/* @ts-expect-error - MUI v7 Grid API change */}
            <Grid item xs={12} md={6}>
              <Paper elevation={2} sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Evidências
                </Typography>
                <Divider sx={{ mb: 2 }} />
                {selo?.evidencias && selo.evidencias.length > 0 ? (
                  <Box>
                    {selo.evidencias.map((evidencia, index) => (
                      <Card key={index} sx={{ mb: 2 }}>
                        <CardContent>
                          <Typography variant="subtitle2" gutterBottom>
                            {evidencia.tipo}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {evidencia.detalhe}
                          </Typography>
                        </CardContent>
                      </Card>
                    ))}
                  </Box>
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    Nenhuma evidência disponível
                  </Typography>
                )}
              </Paper>
            </Grid>

            {selo?.motivos && selo.motivos.length > 0 && (
              <Grid item xs={12}>
                <Paper elevation={2} sx={{ p: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Motivos
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Box>
                    {selo.motivos.map((motivo, index) => (
                      <Typography key={index} variant="body2" sx={{ mb: 1 }}>
                        • {motivo}
                      </Typography>
                    ))}
                  </Box>
                </Paper>
              </Grid>
            )}

            {historico.length > 0 && (
              <Grid item xs={12}>
                <Paper elevation={2} sx={{ p: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Histórico de Alterações
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Timeline>
                    {historico.map((alteracao, index) => (
                      <TimelineItem key={alteracao.id}>
                        <TimelineSeparator>
                          <TimelineDot color={getStatusColor(alteracao.paraStatus) as 'success' | 'warning' | 'error'}>
                            {getStatusIcon(alteracao.paraStatus)}
                          </TimelineDot>
                          {index < historico.length - 1 && <TimelineConnector />}
                        </TimelineSeparator>
                        <TimelineContent>
                          <Typography variant="subtitle2">
                            {alteracao.deStatus} → {alteracao.paraStatus}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {formatDate(alteracao.timestamp)}
                          </Typography>
                          <Typography variant="body2" sx={{ mt: 1 }}>
                            {alteracao.motivo}
                          </Typography>
                          {alteracao.evidencia && (
                            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                              Evidência: {alteracao.evidencia}
                            </Typography>
                          )}
                        </TimelineContent>
                      </TimelineItem>
                    ))}
                  </Timeline>
                </Paper>
              </Grid>
            )}
          </Grid>
        )}
      </Box>
    </Container>
  );
};

export default CertificacaoPage;

