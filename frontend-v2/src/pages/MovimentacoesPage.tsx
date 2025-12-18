import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/components/ui/use-toast';
import { formatDateTime } from '@/lib/utils';
import { MovimentacaoListItem, movimentacaoService } from '@/services/movimentacaoService';
import { useAuthStore } from '@/store/authStore';
import { Calendar, Package, Plus, TrendingDown, TrendingUp } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function MovimentacoesPage() {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [movimentacoes, setMovimentacoes] = useState<MovimentacaoListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);

  useEffect(() => {
    if (user?.role === 'PRODUTOR') {
      loadMovimentacoes();
    }
  }, [user]);

  const loadMovimentacoes = async () => {
    if (!user) return;

    try {
      setLoading(true);
      const response = await movimentacaoService.listarPorProdutor(user.id, 1, 50);
      setMovimentacoes(response.movimentacoes || []);
      setTotal(response.total || 0);
    } catch (error: any) {
      toast({
        variant: 'destructive',
        title: 'Erro ao carregar movimentações',
        description: error.response?.data?.message || 'Erro desconhecido',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Movimentações</h1>
          <p className="text-muted-foreground">Gerencie suas movimentações de commodities</p>
        </div>
        {user?.role === 'PRODUTOR' && (
          <Button onClick={() => navigate('/movimentacoes/nova')}>
            <Plus className="w-4 h-4 mr-2" />
            Nova Movimentação
          </Button>
        )}
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{total}</div>
            <p className="text-xs text-muted-foreground">Movimentações registradas</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Entradas</CardTitle>
            <TrendingUp className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {movimentacoes.filter(m => m.tipo === 'ENTRADA').length}
            </div>
            <p className="text-xs text-muted-foreground">Commodities recebidas</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Saídas</CardTitle>
            <TrendingDown className="h-4 w-4 text-red-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600">
              {movimentacoes.filter(m => m.tipo === 'SAIDA').length}
            </div>
            <p className="text-xs text-muted-foreground">Commodities expedidas</p>
          </CardContent>
        </Card>
      </div>

      {/* Lista de Movimentações */}
      <Card>
        <CardHeader>
          <CardTitle>Histórico de Movimentações</CardTitle>
          <CardDescription>Todas as suas movimentações registradas</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="text-center py-8 text-muted-foreground">Carregando...</div>
          ) : movimentacoes.length === 0 ? (
            <div className="text-center py-8">
              <Package className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground">Nenhuma movimentação registrada</p>
              <Button onClick={() => navigate('/movimentacoes/nova')} className="mt-4">
                Registrar primeira movimentação
              </Button>
            </div>
          ) : (
            <div className="space-y-4">
              {movimentacoes.map((mov) => (
                <div
                  key={mov.id}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50 cursor-pointer"
                  onClick={() => navigate(`/movimentacoes/${mov.id}`)}
                >
                  <div className="flex items-center space-x-4">
                    <div className={`p-2 rounded-full ${mov.tipo === 'ENTRADA' ? 'bg-green-100' : 'bg-red-100'}`}>
                      {mov.tipo === 'ENTRADA' ? (
                        <TrendingUp className="w-5 h-5 text-green-600" />
                      ) : (
                        <TrendingDown className="w-5 h-5 text-red-600" />
                      )}
                    </div>
                    <div>
                      <p className="font-medium">{mov.commodityId}</p>
                      <p className="text-sm text-muted-foreground">
                        {mov.quantidade} {mov.unidade}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium">{mov.tipo}</p>
                    <p className="text-xs text-muted-foreground flex items-center">
                      <Calendar className="w-3 h-3 mr-1" />
                      {formatDateTime(mov.dataMovimentacao)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
