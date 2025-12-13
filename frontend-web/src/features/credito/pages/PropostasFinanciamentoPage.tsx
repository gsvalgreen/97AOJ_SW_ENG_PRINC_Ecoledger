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
  CardActions,
  Button,
  CircularProgress,
  Alert,
  Chip,
  Divider,
  List,
  ListItem,
  ListItemText,
} from '@mui/material';
import { ArrowBack, AttachMoney, TrendingUp, CheckCircle } from '@mui/icons-material';
import { useAuthStore } from '../../../store/authStore';
import { creditoApi } from '../../../api/creditoApi';
import { ROUTES } from '../../../utils/constants';
import type { PropostaFinanciamento } from '../../../types';

const PropostasFinanciamentoPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [propostas, setPropostas] = useState<PropostaFinanciamento[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [creating, setCreating] = useState<string | null>(null);

  useEffect(() => {
    if (!user || user.role !== 'produtor') {
      navigate(ROUTES.DASHBOARD);
      return;
    }

    loadPropostas();
  }, [user, navigate]);

  const loadPropostas = async () => {
    if (!user) return;

    setLoading(true);
    setError(null);

    try {
      const data = await creditoApi.getPropostas(user.id);
      setPropostas(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar propostas');
    } finally {
      setLoading(false);
    }
  };

  const handleCriarSolicitacao = async (propostaId: string) => {
    if (!user) return;

    setCreating(propostaId);
    setError(null);

    try {
      // Aqui você poderia abrir um modal para o usuário preencher valor e prazo
      // Por enquanto, vamos usar valores padrão
      const proposta = propostas.find((p) => p.id === propostaId);
      if (!proposta) return;

      const valorSolicitado = Math.min(proposta.valorMaximo, proposta.valorMaximo * 0.5);
      const prazoMeses = proposta.opcoesPrazo[0] || 12;

      const response = await creditoApi.criarSolicitacao({
        producerId: user.id,
        propostaId,
        valorSolicitado,
        prazoMeses,
      });

      // Redirecionar para a página de detalhes da solicitação
      navigate(`${ROUTES.CREDITO_SOLICITACOES}/${response.solicitacaoId}`);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Erro ao criar solicitação');
      setCreating(null);
    }
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  if (loading) {
    return (
      <Container maxWidth="lg">
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <Button startIcon={<ArrowBack />} onClick={() => navigate(ROUTES.DASHBOARD_PRODUTOR)} sx={{ mr: 2 }}>
            Voltar
          </Button>
          <Typography variant="h4" component="h1">
            Propostas de Financiamento
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {propostas.length === 0 ? (
          <Paper sx={{ p: 4, textAlign: 'center' }}>
            <Typography variant="h6" color="text.secondary">
              Nenhuma proposta de financiamento disponível no momento
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
              As propostas são geradas com base no seu Selo Verde e histórico de produção.
            </Typography>
          </Paper>
        ) : (
          <Grid container spacing={3}>
            {propostas.map((proposta) => (
              <Grid item xs={12} md={6} key={proposta.id}>
                <Card elevation={3}>
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', mb: 2 }}>
                      <Box>
                        <Typography variant="h6" component="h2" gutterBottom>
                          <AttachMoney sx={{ verticalAlign: 'middle', mr: 1 }} />
                          Financiamento Disponível
                        </Typography>
                        <Chip
                          label={`Taxa: ${proposta.taxa}% a.a.`}
                          color="primary"
                          size="small"
                          sx={{ mt: 1 }}
                        />
                      </Box>
                      <CheckCircle color="success" />
                    </Box>

                    <Divider sx={{ my: 2 }} />

                    <List dense>
                      <ListItem disablePadding>
                        <ListItemText
                          primary="Valor Máximo"
                          secondary={
                            <Typography variant="h6" color="primary">
                              {formatCurrency(proposta.valorMaximo)}
                            </Typography>
                          }
                        />
                      </ListItem>
                      <ListItem disablePadding>
                        <ListItemText
                          primary="Taxa de Juros"
                          secondary={
                            <Typography variant="body1" fontWeight="bold">
                              {proposta.taxa}% ao ano
                            </Typography>
                          }
                        />
                      </ListItem>
                      <ListItem disablePadding>
                        <ListItemText
                          primary="Prazos Disponíveis"
                          secondary={proposta.opcoesPrazo.map((prazo) => `${prazo} meses`).join(', ')}
                        />
                      </ListItem>
                    </List>

                    {proposta.condicoes && (
                      <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                        <Typography variant="subtitle2" gutterBottom>
                          Condições Especiais:
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {proposta.condicoes}
                        </Typography>
                      </Box>
                    )}
                  </CardContent>
                  <CardActions sx={{ p: 2, pt: 0 }}>
                    <Button
                      variant="contained"
                      fullWidth
                      startIcon={<TrendingUp />}
                      onClick={() => handleCriarSolicitacao(proposta.id)}
                      disabled={creating === proposta.id}
                    >
                      {creating === proposta.id ? (
                        <>
                          <CircularProgress size={20} sx={{ mr: 1 }} />
                          Criando...
                        </>
                      ) : (
                        'Solicitar Financiamento'
                      )}
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}
      </Box>
    </Container>
  );
};

export default PropostasFinanciamentoPage;

