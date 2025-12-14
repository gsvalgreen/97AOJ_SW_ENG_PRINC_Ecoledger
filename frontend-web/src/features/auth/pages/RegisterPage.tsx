import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Container,
  Paper,
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  MenuItem,
  Stepper,
  Step,
  StepLabel,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { usersApi } from '../../../api/usersApi';
import { ROUTES } from '../../../utils/constants';

const steps = ['Dados Pessoais', 'Informações Adicionais'];

const registerSchema = z.object({
  nome: z.string().min(3, 'Nome deve ter no mínimo 3 caracteres'),
  email: z.string().email('Email inválido'),
  documento: z.string().min(11, 'Documento inválido'),
  role: z.enum(['produtor', 'analista', 'auditor']),
  dadosFazenda: z
    .object({
      nomeFazenda: z.string().optional(),
      area: z.number().optional(),
      localizacao: z.string().optional(),
    })
    .optional(),
});

type RegisterFormData = z.infer<typeof registerSchema>;

const RegisterPage = () => {
  const navigate = useNavigate();
  const [activeStep, setActiveStep] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      role: 'produtor',
    },
  });

  const role = watch('role');

  const onSubmit = async (data: RegisterFormData) => {
    setError(null);
    setLoading(true);

    try {
      const idempotencyKey = `${data.email}-${Date.now()}`;
      
      const payload = {
        nome: data.nome,
        email: data.email,
        documento: data.documento,
        role: data.role,
        dadosFazenda: data.dadosFazenda || {},
        anexos: [],
      };
      
      await usersApi.register(payload, idempotencyKey);
      setSuccess(true);
      setTimeout(() => {
        navigate(ROUTES.LOGIN);
      }, 2000);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Erro ao realizar cadastro. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  return (
    <Container component="main" maxWidth="md">
      <Box
        sx={{
          marginTop: 4,
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
            Cadastro
          </Typography>

          <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
            {steps.map((label) => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          {success && (
            <Alert severity="success" sx={{ mb: 2 }}>
              Cadastro enviado com sucesso! Redirecionando para login...
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            {activeStep === 0 && (
              <>
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="nome"
                  label="Nome Completo"
                  autoFocus
                  {...register('nome')}
                  error={!!errors.nome}
                  helperText={errors.nome?.message}
                />
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="email"
                  label="Email"
                  type="email"
                  autoComplete="email"
                  {...register('email')}
                  error={!!errors.email}
                  helperText={errors.email?.message}
                />
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="documento"
                  label="CPF/CNPJ"
                  {...register('documento')}
                  error={!!errors.documento}
                  helperText={errors.documento?.message}
                />
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  select
                  id="role"
                  label="Tipo de Usuário"
                  {...register('role')}
                  error={!!errors.role}
                  helperText={errors.role?.message}
                >
                  <MenuItem value="produtor">Produtor Rural</MenuItem>
                  <MenuItem value="analista">Analista de Crédito</MenuItem>
                  <MenuItem value="auditor">Auditor</MenuItem>
                </TextField>
              </>
            )}

            {activeStep === 1 && role === 'produtor' && (
              <>
                <TextField
                  margin="normal"
                  fullWidth
                  id="nomeFazenda"
                  label="Nome da Fazenda"
                  {...register('dadosFazenda.nomeFazenda')}
                />
                <TextField
                  margin="normal"
                  fullWidth
                  id="area"
                  label="Área (hectares)"
                  type="number"
                  {...register('dadosFazenda.area', { valueAsNumber: true })}
                />
                <TextField
                  margin="normal"
                  fullWidth
                  id="localizacao"
                  label="Localização"
                  {...register('dadosFazenda.localizacao')}
                />
              </>
            )}

            {activeStep === 1 && role !== 'produtor' && (
              <Typography variant="body1" color="text.secondary" align="center" sx={{ py: 4 }}>
                Revise seus dados e clique em "Enviar Cadastro"
              </Typography>
            )}

            <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
              <Button disabled={activeStep === 0} onClick={handleBack}>
                Voltar
              </Button>
              {activeStep < steps.length - 1 ? (
                <Button variant="contained" onClick={handleNext}>
                  Próximo
                </Button>
              ) : (
                <Button type="submit" variant="contained" disabled={loading}>
                  {loading ? <CircularProgress size={24} /> : 'Enviar Cadastro'}
                </Button>
              )}
            </Box>

            <Box textAlign="center" sx={{ mt: 2 }}>
              <Link to={ROUTES.LOGIN} style={{ textDecoration: 'none', color: 'inherit' }}>
                <Typography variant="body2" color="primary">
                  Já tem uma conta? Faça login
                </Typography>
              </Link>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default RegisterPage;

