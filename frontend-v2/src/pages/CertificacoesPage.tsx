import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/components/ui/use-toast';
import { formatDateTime } from '@/lib/utils';
import { AlteracaoSeloResponse, certificacaoService, SeloResponse } from '@/services/certificacaoService';
import { useAuthStore } from '@/store/authStore';
import { Award, Calendar, Loader2, RefreshCw, TrendingUp } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function CertificacoesPage() {
  const { user } = useAuthStore();
  const { toast } = useToast();
  const [selo, setSelo] = useState<SeloResponse | null>(null);
  const [historico, setHistorico] = useState<AlteracaoSeloResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [recalculando, setRecalculando] = useState(false);

  useEffect(() => {
    if (user?.role === 'PRODUTOR') {
      loadCertificacao();
    }
  }, [user]);

  const loadCertificacao = async () => {
    if (!user) return;

    try {
      setLoading(true);
      const [seloData, historicoData] = await Promise.all([
        certificacaoService.obterSelo(user.id).catch(() => null),
        certificacaoService.historico(user.id).catch(() => ({ alteracoes: [] })),
      ]);

      setSelo(seloData);
      setHistorico(historicoData.alteracoes || []);
    } catch (error: any) {
      toast({
        variant: 'destructive',
        title: 'Erro ao carregar certificação',
        description: error.response?.data?.message || 'Erro desconhecido',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleRecalcular = async () => {
    if (!user) return;

    setRecalculando(true);
    try {
      const response = await certificacaoService.recalcular(user.id, 'recalculo-manual');
      
      toast({
        title: 'Recálculo realizado!',
        description: `Nível: ${response.nivelAnterior} → ${response.nivelAtual}`,
      });

      await loadCertificacao();
    } catch (error: any) {
      toast({
        variant: 'destructive',
        title: 'Erro ao recalcular',
        description: error.response?.data?.message || 'Erro desconhecido',
      });
    } finally {
      setRecalculando(false);
    }
  };

  const getSeloColor = (nivel: string) => {
    const colors: Record<string, string> = {
      DIAMANTE: 'from-blue-400 to-blue-600',
      OURO: 'from-yellow-400 to-yellow-600',
      PRATA: 'from-gray-300 to-gray-500',
      BRONZE: 'from-orange-400 to-orange-600',
      NENHUM: 'from-gray-200 to-gray-400',
    };
    return colors[nivel] || colors.NENHUM;
  };

  const getSeloEmoji = (nivel: string) => {
    const emojis: Record<string, string> = {
      DIAMANTE: '💎',
      OURO: '🥇',
      PRATA: '🥈',
      BRONZE: '🥉',
      NENHUM: '⭕',
    };
    return emojis[nivel] || emojis.NENHUM;
  };

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      ATIVO: 'bg-green-100 text-green-800',
      SUSPENSO: 'bg-yellow-100 text-yellow-800',
      REVOGADO: 'bg-red-100 text-red-800',
    };
    return colors[status] || colors.ATIVO;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Certificação Verde</h1>
          <p className="text-muted-foreground">Acompanhe seu selo de sustentabilidade</p>
        </div>
        <Button onClick={handleRecalcular} disabled={recalculando}>
          {recalculando ? (
            <>
              <Loader2 className="w-4 h-4 mr-2 animate-spin" />
              Recalculando...
            </>
          ) : (
            <>
              <RefreshCw className="w-4 h-4 mr-2" />
              Recalcular
            </>
          )}
        </Button>
      </div>

      {/* Selo Atual */}
      {selo ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle>Selo Atual</CardTitle>
              <CardDescription>Status da sua certificação verde</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex items-center space-x-6">
                <div className={`w-32 h-32 bg-gradient-to-br ${getSeloColor(selo.nivel)} rounded-full flex items-center justify-center text-6xl`}>
                  {getSeloEmoji(selo.nivel)}
                </div>
                <div className="flex-1">
                  <h2 className="text-4xl font-bold mb-2">{selo.nivel}</h2>
                  <div className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(selo.status)} mb-3`}>
                    {selo.status}
                  </div>
                  <div className="space-y-2 text-sm">
                    <div className="flex items-center space-x-2">
                      <TrendingUp className="w-4 h-4 text-muted-foreground" />
                      <span className="text-muted-foreground">Score:</span>
                      <span className="font-bold text-lg">{selo.score}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Calendar className="w-4 h-4 text-muted-foreground" />
                      <span className="text-muted-foreground">Última atualização:</span>
                      <span>{formatDateTime(selo.dataUltimaAtualizacao)}</span>
                    </div>
                    {selo.validadeAte && (
                      <div className="flex items-center space-x-2">
                        <Calendar className="w-4 h-4 text-muted-foreground" />
                        <span className="text-muted-foreground">Válido até:</span>
                        <span>{formatDateTime(selo.validadeAte)}</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Níveis de Certificação</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {['DIAMANTE', 'OURO', 'PRATA', 'BRONZE'].map((nivel) => (
                  <div
                    key={nivel}
                    className={`flex items-center space-x-3 p-2 rounded-lg ${selo.nivel === nivel ? 'bg-primary/10 border-2 border-primary' : ''}`}
                  >
                    <span className="text-2xl">{getSeloEmoji(nivel)}</span>
                    <span className="font-medium">{nivel}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      ) : (
        <Card>
          <CardContent className="py-12 text-center">
            <Award className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-xl font-semibold mb-2">Sem Certificação</h3>
            <p className="text-muted-foreground">
              Continue registrando movimentações sustentáveis para obter sua certificação
            </p>
          </CardContent>
        </Card>
      )}

      {/* Histórico de Alterações */}
      <Card>
        <CardHeader>
          <CardTitle>Histórico de Alterações</CardTitle>
          <CardDescription>Registro de mudanças no seu selo verde</CardDescription>
        </CardHeader>
        <CardContent>
          {historico.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              Nenhuma alteração registrada ainda
            </div>
          ) : (
            <div className="space-y-4">
              {historico.map((alteracao) => (
                <div
                  key={alteracao.id}
                  className="flex items-center justify-between p-4 border rounded-lg"
                >
                  <div className="flex items-center space-x-4">
                    <div className="flex items-center space-x-2">
                      <span className="text-2xl">{getSeloEmoji(alteracao.nivelAnterior)}</span>
                      <span className="text-muted-foreground">→</span>
                      <span className="text-2xl">{getSeloEmoji(alteracao.nivelAtual)}</span>
                    </div>
                    <div>
                      <p className="font-medium">
                        {alteracao.nivelAnterior} → {alteracao.nivelAtual}
                      </p>
                      <p className="text-sm text-muted-foreground">{alteracao.motivo}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-sm text-muted-foreground">
                      {formatDateTime(alteracao.criadaEm)}
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
