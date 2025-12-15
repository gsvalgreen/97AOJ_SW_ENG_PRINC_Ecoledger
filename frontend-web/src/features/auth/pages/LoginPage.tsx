import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Container,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { usersApi } from '../../../api/usersApi';
import { useAuthStore } from '../../../store/authStore';
import { ROUTES } from '../../../utils/constants';

const loginSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'Senha deve ter no mínimo 6 caracteres'),
});

type LoginFormData = z.infer<typeof loginSchema>;

const LoginPage = () => {
  const navigate = useNavigate();
  const { setAuth } = useAuthStore();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setError(null);
    setLoading(true);

    try {
      console.log('[LOGIN] Iniciando login com:', { email: data.email });
      const authData = await usersApi.login(data);
      console.log('[LOGIN] Token recebido:', authData);
      
      // Decodificar token JWT
      const tokenParts = authData.accessToken.split('.');
      console.log('[LOGIN] Token parts:', tokenParts.length);
      
      if (tokenParts.length !== 3) {
        throw new Error(`Token inválido: esperado 3 partes (header.payload.signature), recebido ${tokenParts.length}`);
      }
      
      const tokenPayload = JSON.parse(atob(tokenParts[1]));
      console.log('[LOGIN] Token payload:', tokenPayload);
      
      const userId = tokenPayload.sub || tokenPayload.userId;
      console.log('[LOGIN] User ID extraído:', userId);
      
      if (!userId) {
        throw new Error('Token inválido: ID do usuário não encontrado no payload');
      }

      console.log('[LOGIN] Buscando dados do usuário:', userId);
      const user = await usersApi.getUsuario(userId);
      console.log('[LOGIN] Dados do usuário:', user);
      
      setAuth(authData, user);
      console.log('[LOGIN] Auth state atualizado');

      const role = user.role;
      console.log('[LOGIN] Redirecionando para dashboard:', role);
      
      if (role === 'produtor') {
        navigate(ROUTES.DASHBOARD_PRODUTOR);
      } else if (role === 'analista') {
        navigate(ROUTES.DASHBOARD_ANALISTA);
      } else if (role === 'auditor') {
        navigate(ROUTES.DASHBOARD_AUDITOR);
      } else {
        navigate(ROUTES.DASHBOARD);
      }
    } catch (err: unknown) {
      console.error('[LOGIN] Erro:', err);
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Erro ao fazer login. Verifique suas credenciais.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ padding: 4, width: '100%' }}>
          <Typography component="h1" variant="h4" align="center" gutterBottom>
            ECO LEDGER
          </Typography>
          <Typography variant="body2" align="center" color="text.secondary" gutterBottom>
            A green Hub
          </Typography>
          <Typography component="h2" variant="h5" align="center" sx={{ mt: 2, mb: 3 }}>
            Login
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email"
              autoComplete="email"
              autoFocus
              {...register('email')}
              error={!!errors.email}
              helperText={errors.email?.message}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              label="Senha"
              type="password"
              id="password"
              autoComplete="current-password"
              {...register('password')}
              error={!!errors.password}
              helperText={errors.password?.message}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
            >
              {loading ? <CircularProgress size={24} /> : 'Entrar'}
            </Button>
            <Box textAlign="center">
              <Link to={ROUTES.REGISTER} style={{ textDecoration: 'none', color: 'inherit' }}>
                <Typography variant="body2" color="primary">
                  Não tem uma conta? Cadastre-se
                </Typography>
              </Link>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default LoginPage;

