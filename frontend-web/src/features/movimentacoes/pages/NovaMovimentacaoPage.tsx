import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Paper,
  TextField,
  Button,
  Grid,
  MenuItem,
  Alert,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuthStore } from '../../../store/authStore';
import { movimentacoesApi } from '../../../api/movimentacoesApi';
import { useMovimentacoesStore } from '../../../store/movimentacoesStore';
import FileUpload, { type UploadedFile } from '../components/FileUpload';
import { ROUTES } from '../../../utils/constants';
import type { MovimentacaoCriacao } from '../../../types';

const movimentacaoSchema = z.object({
  commodityId: z.string().min(1, 'Commodity é obrigatória'),
  tipo: z.string().min(1, 'Tipo é obrigatório'),
  quantidade: z.number().min(0.01, 'Quantidade deve ser maior que zero'),
  unidade: z.string().min(1, 'Unidade é obrigatória'),
  timestamp: z.string().min(1, 'Data e hora são obrigatórias'),
  localizacaoLat: z.number().optional(),
  localizacaoLon: z.number().optional(),
});

type MovimentacaoFormData = z.infer<typeof movimentacaoSchema>;

const NovaMovimentacaoPage = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { addItem } = useMovimentacoesStore();
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
  } = useForm<MovimentacaoFormData>({
    resolver: zodResolver(movimentacaoSchema),
    defaultValues: {
      timestamp: new Date().toISOString().slice(0, 16),
    },
  });

  const onSubmit = async (data: MovimentacaoFormData) => {
    if (!user) {
      setError('Usuário não autenticado');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      const movimentacaoData: MovimentacaoCriacao = {
        producerId: user.id,
        commodityId: data.commodityId,
        tipo: data.tipo,
        quantidade: data.quantidade,
        unidade: data.unidade,
        timestamp: new Date(data.timestamp).toISOString(),
        localizacao:
          data.localizacaoLat && data.localizacaoLon
            ? {
                lat: data.localizacaoLat,
                lon: data.localizacaoLon,
              }
            : undefined,
        anexos: uploadedFiles.map((file) => ({
          tipo: file.tipo,
          url: file.url,
          hash: file.hash,
        })),
      };

      const idempotencyKey = `${user.id}-${Date.now()}`;
      const response = await movimentacoesApi.criar(movimentacaoData, idempotencyKey);

      const newMovimentacao = await movimentacoesApi.obter(response.movimentacaoId);
      addItem(newMovimentacao);

      navigate(`${ROUTES.MOVIMENTACOES}/${response.movimentacaoId}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao criar movimentação');
    } finally {
      setLoading(false);
    }
  };

  const handleFilesChange = (files: UploadedFile[]) => {
    setUploadedFiles(files);
  };

  const handleGetLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude;
          const lon = position.coords.longitude;
          const form = document.querySelector('form');
          if (form) {
            const latInput = form.querySelector('[name="localizacaoLat"]') as HTMLInputElement;
            const lonInput = form.querySelector('[name="localizacaoLon"]') as HTMLInputElement;
            if (latInput) latInput.value = lat.toString();
            if (lonInput) lonInput.value = lon.toString();
          }
        },
        () => {
          setError('Não foi possível obter a localização');
        }
      );
    } else {
      setError('Geolocalização não suportada pelo navegador');
    }
  };

  if (!user || user.status !== 'APROVADO') {
    return (
      <Container maxWidth="md">
        <Box sx={{ mt: 4 }}>
          <Alert severity="warning">
            Seu cadastro precisa estar aprovado para registrar movimentações.
          </Alert>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Nova Movimentação
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        <Paper elevation={2} sx={{ p: 4 }}>
          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <Grid container spacing={3}>
              {/* @ts-expect-error - MUI v7 Grid API change */}
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  required
                  label="ID da Commodity"
                  {...register('commodityId')}
                  error={!!errors.commodityId}
                  helperText={errors.commodityId?.message}
                />
              </Grid>

              {/* @ts-expect-error - MUI v7 Grid API change */}
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth required error={!!errors.tipo}>
                  <InputLabel>Tipo</InputLabel>
                  <Select {...register('tipo')} label="Tipo">
                    <MenuItem value="COLHEITA">Colheita</MenuItem>
                    <MenuItem value="PROCESSAMENTO">Processamento</MenuItem>
                    <MenuItem value="TRANSPORTE">Transporte</MenuItem>
                  </Select>
                </FormControl>
                {errors.tipo && (
                  <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.75 }}>
                    {errors.tipo.message}
                  </Typography>
                )}
              </Grid>

              {/* @ts-expect-error - MUI v7 Grid API change */}
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  required
                  label="Unidade"
                  {...register('unidade')}
                  error={!!errors.unidade}
                  helperText={errors.unidade?.message}
                  placeholder="ex: kg, ton, litros"
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  required
                  type="number"
                  label="Quantidade"
                  inputProps={{ step: '0.01', min: 0 }}
                  {...register('quantidade', { valueAsNumber: true })}
                  error={!!errors.quantidade}
                  helperText={errors.quantidade?.message}
                />
              </Grid>

              {/* @ts-expect-error - MUI v7 Grid API change */}
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  required
                  type="datetime-local"
                  label="Data e Hora"
                  InputLabelProps={{ shrink: true }}
                  {...register('timestamp')}
                  error={!!errors.timestamp}
                  helperText={errors.timestamp?.message}
                />
              </Grid>

              {/* @ts-expect-error - MUI v7 Grid API change */}
              <Grid item xs={12}>
                <Typography variant="subtitle2" gutterBottom>
                  Localização (opcional)
                </Typography>
                <Button variant="outlined" size="small" onClick={handleGetLocation} sx={{ mb: 2 }}>
                  Usar Localização Atual
                </Button>
                <Grid container spacing={2}>
                  {/* @ts-expect-error - MUI v7 Grid API change */}
                  <Grid item xs={6}>
                    <TextField
                      fullWidth
                      type="number"
                      label="Latitude"
                      inputProps={{ step: '0.000001' }}
                      {...register('localizacaoLat', { valueAsNumber: true })}
                    />
                  </Grid>
                  {/* @ts-expect-error - MUI v7 Grid API change */}
                  <Grid item xs={6}>
                    <TextField
                      fullWidth
                      type="number"
                      label="Longitude"
                      inputProps={{ step: '0.000001' }}
                      {...register('localizacaoLon', { valueAsNumber: true })}
                    />
                  </Grid>
                </Grid>
              </Grid>

              {/* @ts-expect-error - MUI v7 Grid API change */}
              <Grid item xs={12}>
                <Typography variant="subtitle2" gutterBottom>
                  Anexos (opcional)
                </Typography>
                <FileUpload onFilesChange={handleFilesChange} />
              </Grid>

              {/* @ts-expect-error - MUI v7 Grid API change */}
              <Grid item xs={12}>
                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                  <Button variant="outlined" onClick={() => navigate(ROUTES.MOVIMENTACOES)}>
                    Cancelar
                  </Button>
                  <Button type="submit" variant="contained" disabled={loading}>
                    {loading ? <CircularProgress size={24} /> : 'Salvar Movimentação'}
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default NovaMovimentacaoPage;

