import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Paper,
  Grid,
  Button,
  CircularProgress,
  Alert,
  Divider,
  Chip,
} from '@mui/material';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
} from '@mui/lab';
import { ArrowBack } from '@mui/icons-material';
import { useAuthStore } from '../../../store/authStore';
import { creditoApi } from '../../../api/creditoApi';
import { ROUTES } from '../../../utils/constants';
import type { SolicitacaoCredito } from '../../../types';

const SolicitacaoCreditoDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [solicitacao, setSolicitacao] = useState<SolicitacaoCredito | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      navigate(ROUTES.CREDITO_SOLICITACOES);
      return;
    }

    loadSolicitacao();
  }, [id, navigate]);

  const loadSolicitacao = async () => {
    if (!id) return;

    setLoading(true);
    setError(null);

    try {
      const data = await creditoApi.getSolicitacao(id);
      setSolicitacao(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar solicitação');
    } finally {
      setLoading(false);
    }
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
    return new Date(dateString).toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <Container maxWidth="lg">
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error || !solicitacao) {
    return (
      <Container maxWidth="lg">
        <Box sx={{ mt: 4 }}>
          <Alert severity="error">{error || 'Solicitação não encontrada'}</Alert>
          <Button sx={{ mt: 2 }} onClick={() => navigate(ROUTES.CREDITO_SOLICITACOES)}>
            Voltar para Lista
          </Button>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate(ROUTES.CREDITO_SOLICITACOES)} sx={{ mb: 2 }}>
          Voltar
        </Button>

        <Typography variant="h4" component="h1" gutterBottom>
          Detalhes da Solicitação de Crédito
        </Typography>

        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            <Paper elevation={2} sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h5">Informações da Solicitação</Typography>
                <Chip
                  label={solicitacao.status}
                  color={getStatusColor(solicitacao.status) as 'success' | 'warning' | 'error'}
                />
              </Box>

              <Divider sx={{ mb: 3 }} />

              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    ID da Solicitação
                  </Typography>
                  <Typography variant="body1">{solicitacao.id}</Typography>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Status
                  </Typography>
                  <Typography variant="body1">{solicitacao.status}</Typography>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    ID do Produtor
                  </Typography>
                  <Typography variant="body1">{solicitacao.producerId}</Typography>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    ID da Proposta
                  </Typography>
                  <Typography variant="body1">{solicitacao.propostaId}</Typography>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Valor Solicitado
                  </Typography>
                  <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                    {formatCurrency(solicitacao.valor)}
                  </Typography>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Data de Criação
                  </Typography>
                  <Typography variant="body1">{formatDate(solicitacao.criadoEm)}</Typography>
                </Grid>
              </Grid>
            </Paper>

            {solicitacao.historico && solicitacao.historico.length > 0 && (
              <Paper elevation={2} sx={{ p: 3, mt: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Histórico
                </Typography>
                <Divider sx={{ mb: 2 }} />
                <Timeline>
                  {solicitacao.historico.map((item, index) => (
                    <TimelineItem key={index}>
                      <TimelineSeparator>
                        <TimelineDot color={getStatusColor(item.status) as 'success' | 'warning' | 'error'} />
                        {index < solicitacao.historico!.length - 1 && <TimelineConnector />}
                      </TimelineSeparator>
                      <TimelineContent>
                        <Typography variant="subtitle2">{item.status}</Typography>
                        <Typography variant="body2" color="text.secondary">
                          {formatDate(item.timestamp)}
                        </Typography>
                        {item.observacoes && (
                          <Typography variant="body2" sx={{ mt: 1 }}>
                            {item.observacoes}
                          </Typography>
                        )}
                      </TimelineContent>
                    </TimelineItem>
                  ))}
                </Timeline>
              </Paper>
            )}
          </Grid>

          <Grid item xs={12} md={4}>
            <Paper elevation={2} sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                Informações Adicionais
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Esta solicitação está vinculada à proposta de financiamento selecionada pelo produtor.
                </Typography>
                {user?.role === 'analista' && (
                  <Button
                    variant="outlined"
                    fullWidth
                    sx={{ mt: 2 }}
                    onClick={() => navigate(`${ROUTES.MOVIMENTACOES}?producerId=${solicitacao.producerId}`)}
                  >
                    Ver Movimentações do Produtor
                  </Button>
                )}
              </Box>
            </Paper>
          </Grid>
        </Grid>
      </Box>
    </Container>
  );
};

export default SolicitacaoCreditoDetailPage;

