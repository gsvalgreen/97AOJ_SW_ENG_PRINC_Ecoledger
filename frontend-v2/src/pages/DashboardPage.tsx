import { StatusBadge } from '@/components/StatusBadge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/components/ui/use-toast';
import { auditoriaService } from '@/services/auditoriaService';
import { certificacaoService, SeloResponse } from '@/services/certificacaoService';
import { movimentacaoService } from '@/services/movimentacaoService';
import { useAuthStore } from '@/store/authStore';
import { Award, ClipboardCheck, Leaf, Package, RefreshCw, TrendingUp } from 'lucide-react';
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
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    console.log('DashboardPage - user changed:', user);
    loadDashboardData();
  }, [user]);

  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      console.log('Atualizando dados do usuário...');
      await refreshUser();
      console.log('Usuário atualizado:', useAuthStore.getState().user);
      await loadDashboardData();
      toast({
        title: 'Atualizado!',
        description: 'Seus dados foram recarregados do servidor.',
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
        const [seloData, movimentacoesData, auditoriasData] = await Promise.all([
          certificacaoService.obterSelo(user.id).catch(() => null),
          movimentacaoService.listarPorProdutor(user.id, 1, 100).catch(() => ({ total: 0 })),
          auditoriaService.historicoPorProdutor(user.id).catch(() => ({ auditorias: [] })),
        ]);

        setSelo(seloData);
        setStats({
          movimentacoes: movimentacoesData.total || 0,
          auditorias: auditoriasData.auditorias?.length || 0,
          pontuacao: seloData?.pontuacao || 0,
        });
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
              <h1 className="text-3xl font-bold">Bem-vindo, {user?.nome}!</h1>
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
              ) : (
                <p className="text-sm text-muted-foreground">
                  Nenhuma atividade recente
                </p>
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
