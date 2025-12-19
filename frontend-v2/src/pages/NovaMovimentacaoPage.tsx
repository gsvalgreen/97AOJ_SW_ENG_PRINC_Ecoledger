import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/use-toast';
import { MovimentacaoRequest, movimentacaoService } from '@/services/movimentacaoService';
import { useAuthStore } from '@/store/authStore';
import { ArrowLeft, Loader2, MapPin } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function NovaMovimentacaoPage() {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [loadingLocation, setLoadingLocation] = useState(false);

  // Verificar se o usuário está aprovado
  useEffect(() => {
    if (user && user.status !== 'APROVADO') {
      toast({
        variant: 'destructive',
        title: 'Acesso não autorizado',
        description: 'Seu cadastro precisa estar APROVADO para registrar movimentações',
      });
    }
  }, [user, toast]);

  // Função para obter data/hora atual no formato datetime-local
  const getCurrentDateTimeLocal = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  const [formData, setFormData] = useState({
    producerId: user?.id || '',
    commodityId: '',
    tipo: 'PRODUCAO',
    quantidade: 0,
    unidade: 'KG',
    timestamp: getCurrentDateTimeLocal(),
    lat: undefined as number | undefined,
    lon: undefined as number | undefined,
  });

  const handleGetLocation = () => {
    if (!navigator.geolocation) {
      toast({
        variant: 'destructive',
        title: 'Geolocalização não suportada',
        description: 'Seu navegador não suporta geolocalização',
      });
      return;
    }

    setLoadingLocation(true);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setFormData({
          ...formData,
          lat: position.coords.latitude,
          lon: position.coords.longitude,
        });
        toast({
          title: 'Localização capturada!',
          description: `Lat: ${position.coords.latitude.toFixed(6)}, Lon: ${position.coords.longitude.toFixed(6)}`,
        });
        setLoadingLocation(false);
      },
      (error) => {
        toast({
          variant: 'destructive',
          title: 'Erro ao obter localização',
          description: error.message || 'Permita o acesso à localização',
        });
        setLoadingLocation(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Verifica se o usuário está aprovado
    if (user?.status !== 'APROVADO') {
      toast({
        variant: 'destructive',
        title: 'Operação não permitida',
        description: 'Apenas usuários com cadastro APROVADO podem registrar movimentações',
      });
      return;
    }

    if (!formData.commodityId || !formData.quantidade || !formData.timestamp) {
      toast({
        variant: 'destructive',
        title: 'Campos obrigatórios',
        description: 'Preencha todos os campos obrigatórios',
      });
      return;
    }

    setLoading(true);

    try {
      // Converter datetime-local para ISO-8601
      const timestampISO = new Date(formData.timestamp).toISOString();
      
      const payload: MovimentacaoRequest = {
        producerId: formData.producerId,
        commodityId: formData.commodityId,
        tipo: formData.tipo,
        quantidade: formData.quantidade,
        unidade: formData.unidade,
        timestamp: timestampISO,
        localizacao: (formData.lat !== undefined && formData.lon !== undefined) 
          ? { lat: formData.lat, lon: formData.lon }
          : undefined,
        anexos: [],
      };

      const response = await movimentacaoService.criar(payload);
      
      toast({
        title: 'Movimentação criada com sucesso!',
        description: `ID: ${response.movimentacaoId}`,
      });

      navigate('/movimentacoes');
    } catch (error: any) {
      toast({
        variant: 'destructive',
        title: 'Erro ao criar movimentação',
        description: error.response?.data?.message || 'Erro desconhecido',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/movimentacoes')}>
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold">Nova Movimentação</h1>
          <p className="text-muted-foreground">Registre uma nova movimentação de commodity</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Dados da Movimentação</CardTitle>
          <CardDescription>Preencha os detalhes da movimentação</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="commodityId">ID da Commodity *</Label>
                <Input
                  id="commodityId"
                  value={formData.commodityId || ''}
                  onChange={(e) => setFormData({ ...formData, commodityId: e.target.value })}
                  placeholder="Ex: CAFE-001"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="tipo">Tipo *</Label>
                <select
                  id="tipo"
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={formData.tipo}
                  onChange={(e) => setFormData({ ...formData, tipo: e.target.value })}
                  required
                >
                  <option value="PRODUCAO">Produção</option>
                  <option value="PROCESSAMENTO">Processamento</option>
                  <option value="TRANSPORTE">Transporte</option>
                  <option value="ARMAZENAMENTO">Armazenamento</option>
                </select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="quantidade">Quantidade *</Label>
                <Input
                  id="quantidade"
                  type="number"
                  step="0.01"
                  value={formData.quantidade || ''}
                  onChange={(e) => setFormData({ ...formData, quantidade: parseFloat(e.target.value) })}
                  placeholder="0.00"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="unidade">Unidade *</Label>
                <select
                  id="unidade"
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={formData.unidade}
                  onChange={(e) => setFormData({ ...formData, unidade: e.target.value })}
                  required
                >
                  <option value="KG">Quilograma (KG)</option>
                  <option value="TON">Tonelada (TON)</option>
                  <option value="LT">Litro (LT)</option>
                  <option value="UN">Unidade (UN)</option>
                </select>
              </div>

              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label>Localização (opcional)</Label>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={handleGetLocation}
                    disabled={loadingLocation}
                  >
                    {loadingLocation ? (
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    ) : (
                      <MapPin className="w-4 h-4 mr-2" />
                    )}
                    Usar minha localização
                  </Button>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Input
                      id="lat"
                      type="number"
                      step="0.000001"
                      value={formData.lat || ''}
                      onChange={(e) => setFormData({ ...formData, lat: e.target.value ? parseFloat(e.target.value) : undefined })}
                      placeholder="Latitude"
                    />
                  </div>
                  <div>
                    <Input
                      id="lon"
                      type="number"
                      step="0.000001"
                      value={formData.lon || ''}
                      onChange={(e) => setFormData({ ...formData, lon: e.target.value ? parseFloat(e.target.value) : undefined })}
                      placeholder="Longitude"
                    />
                  </div>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="timestamp">Data da Movimentação *</Label>
                <Input
                  id="timestamp"
                  type="datetime-local"
                  value={formData.timestamp || ''}
                  onChange={(e) => setFormData({ ...formData, timestamp: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="flex justify-end space-x-4">
              <Button type="button" variant="outline" onClick={() => navigate('/movimentacoes')}>
                Cancelar
              </Button>
              <Button type="submit" disabled={loading}>
                {loading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Salvando...
                  </>
                ) : (
                  'Salvar Movimentação'
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
