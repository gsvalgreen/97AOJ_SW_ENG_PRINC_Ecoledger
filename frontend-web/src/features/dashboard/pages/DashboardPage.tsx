import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Typography, Box, Paper, Button } from '@mui/material';
import { useAuthStore } from '../../../store/authStore';
import { ROUTES } from '../../../utils/constants';

const DashboardPage = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) {
      navigate(ROUTES.LOGIN);
      return;
    }

    if (user.role === 'produtor') {
      navigate(ROUTES.DASHBOARD_PRODUTOR);
    } else if (user.role === 'analista') {
      navigate(ROUTES.DASHBOARD_ANALISTA);
    }
  }, [user, navigate]);

  if (!user) {
    return null;
  }

  const getDashboardContent = () => {
    switch (user.role) {
      case 'produtor':
        return {
          title: 'Dashboard do Produtor',
          description: 'Gerencie suas movimentações e acompanhe seu Selo Verde',
          actions: [
            { label: 'Nova Movimentação', path: ROUTES.MOVIMENTACOES_NOVA },
            { label: 'Minhas Movimentações', path: ROUTES.MOVIMENTACOES },
            { label: 'Meu Selo Verde', path: ROUTES.CERTIFICACAO },
            { label: 'Financiamentos', path: ROUTES.CREDITO_PROPOSTAS },
          ],
        };
      case 'analista':
        return {
          title: 'Dashboard do Analista',
          description: 'Gerencie cadastros e analise solicitações de crédito',
          actions: [
            { label: 'Cadastros Pendentes', path: ROUTES.CADASTROS },
            { label: 'Movimentações', path: ROUTES.MOVIMENTACOES },
            { label: 'Solicitações de Crédito', path: ROUTES.CREDITO_SOLICITACOES },
          ],
        };
      case 'auditor':
        return {
          title: 'Dashboard do Auditor',
          description: 'Revise e audite movimentações para certificação',
          actions: [
            { label: 'Auditorias Pendentes', path: ROUTES.AUDITORIAS },
            { label: 'Histórico de Auditorias', path: ROUTES.AUDITORIAS },
          ],
        };
      default:
        return {
          title: 'Dashboard',
          description: 'Bem-vindo ao ECO LEDGER',
          actions: [],
        };
    }
  };

  const content = getDashboardContent();

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h4" component="h1" gutterBottom>
              {content.title}
            </Typography>
            <Typography variant="body1" color="text.secondary">
              {content.description}
            </Typography>
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mr: 2, display: 'inline' }}>
              {user.nome} ({user.email})
            </Typography>
            <Button variant="outlined" onClick={logout}>
              Sair
            </Button>
          </Box>
        </Box>

        <Paper elevation={2} sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Status do Cadastro
          </Typography>
          <Typography variant="body1" color={user.status === 'APROVADO' ? 'success.main' : 'warning.main'}>
            {user.status}
          </Typography>

          {user.status !== 'APROVADO' && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Seu cadastro está aguardando aprovação. Você receberá uma notificação quando for aprovado.
            </Typography>
          )}
        </Paper>

        {content.actions.length > 0 && (
          <Box sx={{ mt: 4 }}>
            <Typography variant="h6" gutterBottom>
              Ações Rápidas
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, mt: 2 }}>
              {content.actions.map((action) => (
                <Button
                  key={action.path}
                  variant="contained"
                  onClick={() => navigate(action.path)}
                  disabled={user.status !== 'APROVADO'}
                >
                  {action.label}
                </Button>
              ))}
            </Box>
          </Box>
        )}
      </Box>
    </Container>
  );
};

export default DashboardPage;

