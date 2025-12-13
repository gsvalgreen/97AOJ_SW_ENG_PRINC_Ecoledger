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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Chip,
} from '@mui/material';
import { ArrowBack, CheckCircle, Cancel } from '@mui/icons-material';
import { useAuthStore } from '../../../store/authStore';
import { usersApi } from '../../../api/usersApi';
import { ROUTES } from '../../../utils/constants';
import type { RespostaCadastro, Usuario } from '../../../types';

const CadastroDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [cadastro, setCadastro] = useState<RespostaCadastro | null>(null);
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [approveDialogOpen, setApproveDialogOpen] = useState(false);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [reason, setReason] = useState('');
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    if (!user || user.role !== 'analista') {
      navigate(ROUTES.DASHBOARD);
      return;
    }

    if (!id) {
      navigate(ROUTES.CADASTROS);
      return;
    }

    loadCadastro();
  }, [id, user, navigate]);

  const loadCadastro = async () => {
    if (!id) return;

    setLoading(true);
    setError(null);

    try {
      const cadastroData = await usersApi.getCadastro(id);
      setCadastro(cadastroData);

      if ((cadastroData as any).candidatoUsuario?.id) {
        const usuarioData = await usersApi.getUsuario((cadastroData as any).candidatoUsuario.id);
        setUsuario(usuarioData);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar cadastro');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async () => {
    if (!id || !usuario) return;

    setProcessing(true);
    try {
      await usersApi.updateUsuarioStatus(id, 'APROVADO');
      setApproveDialogOpen(false);
      await loadCadastro();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao aprovar cadastro');
    } finally {
      setProcessing(false);
    }
  };

  const handleReject = async () => {
    if (!id || !usuario) return;

    if (!reason.trim()) {
      setError('Motivo da rejeição é obrigatório');
      return;
    }

    setProcessing(true);
    try {
      await usersApi.updateUsuarioStatus(id, 'REJEITADO', reason);
      setRejectDialogOpen(false);
      setReason('');
      await loadCadastro();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao rejeitar cadastro');
    } finally {
      setProcessing(false);
    }
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

  if (loading) {
    return (
      <Container maxWidth="lg">
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error && !cadastro) {
    return (
      <Container maxWidth="lg">
        <Box sx={{ mt: 4 }}>
          <Alert severity="error">{error}</Alert>
          <Button sx={{ mt: 2 }} onClick={() => navigate(ROUTES.CADASTROS)}>
            Voltar para Lista
          </Button>
        </Box>
      </Container>
    );
  }

  const candidatoUsuario = (cadastro as any)?.candidatoUsuario || usuario;

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate(ROUTES.CADASTROS)} sx={{ mb: 2 }}>
          Voltar
        </Button>

        <Typography variant="h4" component="h1" gutterBottom>
          Detalhes do Cadastro
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        {cadastro && (
          <Grid container spacing={3}>
            <Grid item xs={12} md={8}>
              <Paper elevation={2} sx={{ p: 3 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                  <Typography variant="h5">Informações do Cadastro</Typography>
                  <Chip
                    label={cadastro.status}
                    color={getStatusColor(cadastro.status) as 'success' | 'warning' | 'error'}
                  />
                </Box>

                <Divider sx={{ mb: 3 }} />

                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="subtitle2" color="text.secondary">
                      ID do Cadastro
                    </Typography>
                    <Typography variant="body1">{cadastro.cadastroId}</Typography>
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <Typography variant="subtitle2" color="text.secondary">
                      Status
                    </Typography>
                    <Typography variant="body1">{cadastro.status}</Typography>
                  </Grid>

                  {candidatoUsuario && (
                    <>
                      <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                          Nome
                        </Typography>
                        <Typography variant="body1">{candidatoUsuario.nome}</Typography>
                      </Grid>

                      <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                          Email
                        </Typography>
                        <Typography variant="body1">{candidatoUsuario.email}</Typography>
                      </Grid>

                      <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                          Documento
                        </Typography>
                        <Typography variant="body1">{candidatoUsuario.documento}</Typography>
                      </Grid>

                      <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2" color="text.secondary">
                          Tipo de Usuário
                        </Typography>
                        <Typography variant="body1">{candidatoUsuario.role}</Typography>
                      </Grid>
                    </>
                  )}

                  {(cadastro as any).submetidoEm && (
                    <Grid item xs={12}>
                      <Typography variant="subtitle2" color="text.secondary">
                        Data de Submissão
                      </Typography>
                      <Typography variant="body1">
                        {new Date((cadastro as any).submetidoEm).toLocaleString('pt-BR')}
                      </Typography>
                    </Grid>
                  )}

                  {(cadastro as any).dadosFazenda && (
                    <Grid item xs={12}>
                      <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                        Dados da Fazenda
                      </Typography>
                      <Paper variant="outlined" sx={{ p: 2 }}>
                        <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                          {JSON.stringify((cadastro as any).dadosFazenda, null, 2)}
                        </pre>
                      </Paper>
                    </Grid>
                  )}
                </Grid>
              </Paper>
            </Grid>

            <Grid item xs={12} md={4}>
              <Paper elevation={2} sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Ações
                </Typography>
                <Divider sx={{ mb: 2 }} />

                {cadastro.status === 'PENDENTE' && (
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Button
                      variant="contained"
                      color="success"
                      startIcon={<CheckCircle />}
                      onClick={() => setApproveDialogOpen(true)}
                      fullWidth
                    >
                      Aprovar Cadastro
                    </Button>
                    <Button
                      variant="contained"
                      color="error"
                      startIcon={<Cancel />}
                      onClick={() => setRejectDialogOpen(true)}
                      fullWidth
                    >
                      Rejeitar Cadastro
                    </Button>
                  </Box>
                )}

                {cadastro.status !== 'PENDENTE' && (
                  <Typography variant="body2" color="text.secondary">
                    Este cadastro já foi processado.
                  </Typography>
                )}
              </Paper>
            </Grid>
          </Grid>
        )}

        <Dialog open={approveDialogOpen} onClose={() => setApproveDialogOpen(false)}>
          <DialogTitle>Aprovar Cadastro</DialogTitle>
          <DialogContent>
            <Typography>Deseja aprovar este cadastro?</Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setApproveDialogOpen(false)}>Cancelar</Button>
            <Button onClick={handleApprove} variant="contained" color="success" disabled={processing}>
              {processing ? <CircularProgress size={24} /> : 'Aprovar'}
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog open={rejectDialogOpen} onClose={() => setRejectDialogOpen(false)} fullWidth maxWidth="sm">
          <DialogTitle>Rejeitar Cadastro</DialogTitle>
          <DialogContent>
            <TextField
              fullWidth
              multiline
              rows={4}
              label="Motivo da Rejeição"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              required
              sx={{ mt: 2 }}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setRejectDialogOpen(false)}>Cancelar</Button>
            <Button onClick={handleReject} variant="contained" color="error" disabled={processing || !reason.trim()}>
              {processing ? <CircularProgress size={24} /> : 'Rejeitar'}
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </Container>
  );
};

export default CadastroDetailPage;

