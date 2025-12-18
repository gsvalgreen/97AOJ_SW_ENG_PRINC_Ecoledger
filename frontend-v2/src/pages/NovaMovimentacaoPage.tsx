import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/use-toast';
import { MovimentacaoRequest, movimentacaoService } from '@/services/movimentacaoService';
import { useAuthStore } from '@/store/authStore';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function NovaMovimentacaoPage() {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<Partial<MovimentacaoRequest>>({
    producerId: user?.id || '',
    tipo: 'ENTRADA',
    unidade: 'KG',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.commodityId || !formData.quantidade || !formData.localizacao || !formData.dataMovimentacao) {
      toast({
        variant: 'destructive',
        title: 'Campos obrigatórios',
        description: 'Preencha todos os campos obrigatórios',
      });
      return;
    }

    setLoading(true);

    try {
      const response = await movimentacaoService.criar(formData as MovimentacaoRequest);
      
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
                  onChange={(e) => setFormData({ ...formData, tipo: e.target.value as any })}
                  required
                >
                  <option value="ENTRADA">Entrada</option>
                  <option value="SAIDA">Saída</option>
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
                <Label htmlFor="localizacao">Localização *</Label>
                <Input
                  id="localizacao"
                  value={formData.localizacao || ''}
                  onChange={(e) => setFormData({ ...formData, localizacao: e.target.value })}
                  placeholder="Ex: Armazém A, Setor 3"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="dataMovimentacao">Data da Movimentação *</Label>
                <Input
                  id="dataMovimentacao"
                  type="datetime-local"
                  value={formData.dataMovimentacao || ''}
                  onChange={(e) => setFormData({ ...formData, dataMovimentacao: e.target.value })}
                  required
                />
              </div>

              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="observacoes">Observações</Label>
                <textarea
                  id="observacoes"
                  className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={formData.observacoes || ''}
                  onChange={(e) => setFormData({ ...formData, observacoes: e.target.value })}
                  placeholder="Informações adicionais sobre a movimentação..."
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
