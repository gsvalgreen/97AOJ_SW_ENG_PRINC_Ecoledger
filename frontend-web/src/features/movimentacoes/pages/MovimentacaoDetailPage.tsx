import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Paper,
  Grid,
  Chip,
  Button,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Divider,
  Link,
} from '@mui/material';
import { ArrowBack, LocationOn, AttachFile } from '@mui/icons-material';
import { movimentacoesApi } from '../../../api/movimentacoesApi';
import { ROUTES } from '../../../utils/constants';
import type { Movimentacao } from '../../../types';

const MovimentacaoDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [movimentacao, setMovimentacao] = useState<Movimentacao | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      navigate(ROUTES.MOVIMENTACOES);
      return;
    }

    loadMovimentacao();
  }, [id, navigate]);

  const loadMovimentacao = async () => {
    if (!id) return;

    setLoading(true);
    setError(null);

    try {
      const data = await movimentacoesApi.obter(id);
      setMovimentacao(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar movimentação');
    } finally {
      setLoading(false);
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

  const getTipoColor = (tipo: string) => {
    const colors: Record<string, 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error'> = {
      COLHEITA: 'success',
      PROCESSAMENTO: 'primary',
      TRANSPORTE: 'warning',
    };
    return colors[tipo] || 'default';
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

  if (error || !movimentacao) {
    return (
      <Container maxWidth="lg">
        <Box sx={{ mt: 4 }}>
          <Alert severity="error">{error || 'Movimentação não encontrada'}</Alert>
          <Button sx={{ mt: 2 }} onClick={() => navigate(ROUTES.MOVIMENTACOES)}>
            Voltar para Lista
          </Button>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate(ROUTES.MOVIMENTACOES)} sx={{ mb: 2 }}>
          Voltar
        </Button>

        <Typography variant="h4" component="h1" gutterBottom>
          Detalhes da Movimentação
        </Typography>

        <Grid container spacing={3}>
          {/* @ts-expect-error - MUI v7 Grid API change */}
          <Grid item xs={12} md={8}>
            <Paper elevation={2} sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h5">Informações Gerais</Typography>
                <Chip label={movimentacao.tipo} color={getTipoColor(movimentacao.tipo)} />
              </Box>

              <Divider sx={{ mb: 3 }} />

              <Grid container spacing={2}>
                {/* @ts-expect-error - MUI v7 Grid API change */}
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    ID
                  </Typography>
                  <Typography variant="body1">{movimentacao.id}</Typography>
                </Grid>

                {/* @ts-expect-error - MUI v7 Grid API change */}
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Commodity ID
                  </Typography>
                  <Typography variant="body1">{movimentacao.commodityId}</Typography>
                </Grid>

                {/* @ts-expect-error - MUI v7 Grid API change */}
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Quantidade
                  </Typography>
                  <Typography variant="body1">
                    {movimentacao.quantidade} {movimentacao.unidade}
                  </Typography>
                </Grid>

                {/* @ts-expect-error - MUI v7 Grid API change */}
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Data e Hora
                  </Typography>
                  <Typography variant="body1">{formatDate(movimentacao.timestamp)}</Typography>
                </Grid>

                {movimentacao.localizacao && (
                  <Grid item xs={12}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <LocationOn color="action" />
                      <Typography variant="subtitle2" color="text.secondary">
                        Localização
                      </Typography>
                    </Box>
                    <Typography variant="body1">
                      {movimentacao.localizacao.lat.toFixed(6)}, {movimentacao.localizacao.lon.toFixed(6)}
                    </Typography>
                  </Grid>
                )}
              </Grid>
            </Paper>

            {movimentacao.anexos && movimentacao.anexos.length > 0 && (
              <Paper elevation={2} sx={{ p: 3, mt: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Anexos ({movimentacao.anexos.length})
                </Typography>
                <Grid container spacing={2}>
                  {movimentacao.anexos.map((anexo, index) => (
                    <Grid item xs={12} sm={6} md={4} key={index}>
                      <Card>
                        <CardContent>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                            <AttachFile color="action" />
                            <Typography variant="subtitle2">{anexo.tipo}</Typography>
                          </Box>
                          <Link href={anexo.url} target="_blank" rel="noopener noreferrer">
                            Ver Anexo
                          </Link>
                          {anexo.hash && (
                            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
                              Hash: {anexo.hash.substring(0, 16)}...
                            </Typography>
                          )}
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </Paper>
            )}
          </Grid>

          {/* @ts-expect-error - MUI v7 Grid API change */}
          <Grid item xs={12} md={4}>
            <Paper elevation={2} sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                Informações Adicionais
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle2" color="text.secondary">
                  Producer ID
                </Typography>
                <Typography variant="body2">{movimentacao.producerId}</Typography>
              </Box>
            </Paper>
          </Grid>
        </Grid>
      </Box>
    </Container>
  );
};

export default MovimentacaoDetailPage;

