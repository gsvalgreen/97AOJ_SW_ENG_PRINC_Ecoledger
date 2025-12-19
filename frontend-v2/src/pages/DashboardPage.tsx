import { StatusBadge } from '@/components/StatusBadge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/components/ui/use-toast';
import { auditoriaService } from '@/services/auditoriaService';
import { certificacaoService, SeloResponse } from '@/services/certificacaoService';
import { MovimentacaoDetailResponse, movimentacaoService } from '@/services/movimentacaoService';
import { useAuthStore } from '@/store/authStore';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { Award, ClipboardCheck, Factory, Leaf, Package, RefreshCw, TrendingUp, Truck, Warehouse } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function DashboardPage() {
  const { user, refreshUser } = useAuthStore();
  const { toast } = useToast();
  const [selo, setSelo] = useState<SeloResponse | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [stats, setStats] = useState({
    movimentacoes: 0,
    auditorias: 0,
    pontuacao: 0,
  });
  const [recentMovimentacoes, setRecentMovimentacoes] = useState<MovimentacaoDetailResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    console.log('DashboardPage - user changed:', user);
    loadDashboardData();
  }, [user]);

  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      console.log('Iniciando atualização completa...');
      
      // Limpar dados em cache (mas manter autenticação)
      setSelo(null);
      setStats({ movimentacoes: 0, auditorias: 0, pontuacao: 0 });
      setRecentMovimentacoes([]);
      
      // Força reload da página para limpar cache do browser (service workers, etc)
      // mas mantém o token e user no localStorage
      if ('caches' in window) {
        const cacheNames = await caches.keys();
        await Promise.all(cacheNames.map(name => caches.delete(name)));
        console.log('Cache do browser limpo');
      }
      
      // Recarregar dados do usuário
      await refreshUser();
      console.log('Usuário atualizado:', useAuthStore.getState().user);
      
      // Recarregar dados do dashboard com timestamp para evitar cache
      await loadDashboardData();
      
      toast({
        title: 'Atualizado com sucesso!',
        description: 'Todos os dados foram recarregados do servidor.',
      });
    } catch (error) {
      console.error('Erro ao atualizar:', error);
      toast({
        variant: 'destructive',
        title: 'Erro ao atualizar',
        description: 'Não foi possível recarregar os dados.',
      });
    } finally {
      setRefreshing(false);
    }
  };

  const loadDashboardData = async () => {
    if (!user) return;

    try {
      setLoading(true);

      if (user.role === 'PRODUTOR') {
        // Carregar dados do produtor
        console.log('Carregando dados do dashboard para produtor:', user.id);
        const [seloData, movimentacoesData, auditoriasData] = await Promise.all([
          certificacaoService.obterSelo(user.id).catch((error) => {
            console.error('Erro ao carregar selo:', error);
            return null;
          }),
          movimentacaoService.listarPorProdutor(user.id, 1, 100).catch((error) => {
            console.error('Erro ao carregar movimentações:', error);
            return { items: [], total: 0 };
          }),
          auditoriaService.historicoPorProdutor(user.id).catch((error) => {
            console.error('Erro ao carregar auditorias:', error);
            return { auditorias: [] };
          }),
        ]);

        console.log('Selo carregado:', seloData);
        console.log('Movimentações carregadas:', movimentacoesData);
        console.log('Auditorias carregadas:', auditoriasData);

        setSelo(seloData);
        setStats({
          movimentacoes: movimentacoesData.total || 0,
          auditorias: auditoriasData.auditorias?.length || 0,
          pontuacao: seloData?.pontuacao || 0,
        });
        
        // Pegar as 4 movimentações mais recentes
        const recent = (movimentacoesData.items || [])
          .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
          .slice(0, 4);
        setRecentMovimentacoes(recent);
      }
    } catch (error: any) {
      console.error('Erro ao carregar dashboard:', error);
      toast({
        variant: 'destructive',
        title: 'Erro ao carregar dados',
        description: 'Não foi possível carregar os dados do dashboard',
      });
    } finally {
      setLoading(false);
    }
  };

  const getSeloColor = (nivel: string) => {
    const colors: Record<string, string> = {
      DIAMANTE: 'text-blue-600 bg-blue-50',
      OURO: 'text-yellow-600 bg-yellow-50',
      PRATA: 'text-gray-600 bg-gray-50',
      BRONZE: 'text-orange-600 bg-orange-50',
      NENHUM: 'text-gray-400 bg-gray-50',
    };
    return colors[nivel] || colors.NENHUM;
  };

  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      {/* Welcome Section */}
      <div className="bg-gradient-to-r from-green-600 to-green-700 rounded-lg p-6 text-white">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center space-x-3">
            <Leaf className="w-8 h-8" />
            <div>
              <h1 className="text-3xl font-bold">Olá, {user?.nome}!</h1>
              <div className="mt-2">
                {user?.status && <StatusBadge status={user.status} />}
              </div>
            </div>
          </div>
          <Button
            onClick={handleRefresh}
            disabled={refreshing}
            variant="secondary"
            size="sm"
            className="bg-white/20 hover:bg-white/30 text-white border-white/30"
          >
            <RefreshCw className={`w-4 h-4 mr-2 ${refreshing ? 'animate-spin' : ''}`} />
            Atualizar
          </Button>
        </div>
        <p className="text-green-100 mt-3">
          {user?.role === 'PRODUTOR' && 'Gerencie suas movimentações e acompanhe sua certificação verde'}
          {user?.role === 'AUDITOR' && 'Revise e aprove movimentações em conformidade'}
          {user?.role === 'ANALISTA' && 'Monitore e analise dados de sustentabilidade'}
        </p>
      </div>

      {/* Stats Cards */}
      {user?.role === 'PRODUTOR' && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Certificação</CardTitle>
              <Award className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              {selo ? (
                <>
                  <div className={`text-2xl font-bold ${getSeloColor(selo.nivel).split(' ')[0]}`}>
                    {selo.nivel}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    Status: {selo.status}
                  </p>
                </>
              ) : (
                <div className="text-2xl font-bold text-gray-400">
                  Sem selo
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Pontuação</CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.pontuacao}</div>
              <p className="text-xs text-muted-foreground">
                Pontuação atual
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Movimentações</CardTitle>
              <Package className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.movimentacoes}</div>
              <p className="text-xs text-muted-foreground">
                Total registrado
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Auditorias</CardTitle>
              <ClipboardCheck className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.auditorias}</div>
              <p className="text-xs text-muted-foreground">
                Total realizadas
              </p>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Atividades Recentes</CardTitle>
            <CardDescription>Últimas movimentações e eventos</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {loading ? (
                <p className="text-sm text-muted-foreground">Carregando...</p>
              ) : recentMovimentacoes.length === 0 ? (
                <p className="text-sm text-muted-foreground">
                  Nenhuma movimentação registrada ainda
                </p>
              ) : (
                recentMovimentacoes.map((mov) => (
                  <div key={mov.id} className="flex items-center space-x-3 pb-3 border-b last:border-0">
                    <div className={`p-2 rounded-full ${
                      mov.tipo === 'PRODUCAO' ? 'bg-green-100' :
                      mov.tipo === 'ARMAZENAMENTO' ? 'bg-blue-100' :
                      mov.tipo === 'PROCESSAMENTO' ? 'bg-purple-100' :
                      'bg-orange-100'
                    }`}>
                      {mov.tipo === 'PRODUCAO' && <Warehouse className="w-4 h-4 text-green-600" />}
                      {mov.tipo === 'ARMAZENAMENTO' && <Package className="w-4 h-4 text-blue-600" />}
                      {mov.tipo === 'PROCESSAMENTO' && <Factory className="w-4 h-4 text-purple-600" />}
                      {mov.tipo === 'TRANSPORTE' && <Truck className="w-4 h-4 text-orange-600" />}
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium">{mov.commodityId}</p>
                      <p className="text-xs text-muted-foreground">
                        {mov.tipo} • {mov.quantidade} {mov.unidade}
                      </p>
                    </div>
                    <div className="text-xs text-muted-foreground text-right">
                      {format(new Date(mov.timestamp), "dd/MM/yy HH:mm", { locale: ptBR })}
                    </div>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Próximas Ações</CardTitle>
            <CardDescription>Tarefas e lembretes importantes</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {user?.role === 'PRODUTOR' && (
                <div className="flex items-start space-x-3">
                  <div className="w-2 h-2 bg-green-500 rounded-full mt-2"></div>
                  <div>
                    <p className="text-sm font-medium">Registrar movimentações</p>
                    <p className="text-xs text-muted-foreground">
                      Mantenha seus registros atualizados
                    </p>
                  </div>
                </div>
              )}
              {selo && (
                <div className="flex items-start space-x-3">
                  <div className="w-2 h-2 bg-yellow-500 rounded-full mt-2"></div>
                  <div>
                    <p className="text-sm font-medium">Última verificação</p>
                    <p className="text-xs text-muted-foreground">
                      {new Date(selo.ultimoCheck).toLocaleDateString('pt-BR')}
                    </p>
                  </div>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Quick Actions */}
      {user?.role === 'PRODUTOR' && (
        <Card>
          <CardHeader>
            <CardTitle>Ações Rápidas</CardTitle>
            <CardDescription>Acesso rápido às funcionalidades principais</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <a
                href="/movimentacoes/nova"
                className="flex flex-col items-center justify-center p-6 border rounded-lg hover:bg-gray-50 transition-colors"
              >
                <Package className="w-8 h-8 text-primary mb-2" />
                <span className="text-sm font-medium">Nova Movimentação</span>
              </a>
              <a
                href="/movimentacoes"
                className="flex flex-col items-center justify-center p-6 border rounded-lg hover:bg-gray-50 transition-colors"
              >
                <ClipboardCheck className="w-8 h-8 text-primary mb-2" />
                <span className="text-sm font-medium">Ver Histórico</span>
              </a>
              <a
                href="/certificacoes"
                className="flex flex-col items-center justify-center p-6 border rounded-lg hover:bg-gray-50 transition-colors"
              >
                <Award className="w-8 h-8 text-primary mb-2" />
                <span className="text-sm font-medium">Certificação</span>
              </a>
              <a
                href="/auditorias"
                className="flex flex-col items-center justify-center p-6 border rounded-lg hover:bg-gray-50 transition-colors"
              >
                <TrendingUp className="w-8 h-8 text-primary mb-2" />
                <span className="text-sm font-medium">Auditorias</span>
              </a>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
