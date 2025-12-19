import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/components/ui/use-toast';
import { formatDateTime } from '@/lib/utils';
import { auditoriaService, RegistroAuditoriaResponse } from '@/services/auditoriaService';
import { useAuthStore } from '@/store/authStore';
import { AlertCircle, CheckCircle, ClipboardCheck, Clock } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function AuditoriasPage() {
  const { user } = useAuthStore();
  const { toast } = useToast();
  const [auditorias, setAuditorias] = useState<RegistroAuditoriaResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.role === 'PRODUTOR') {
      loadAuditorias();
    }
  }, [user]);

  const loadAuditorias = async () => {
    if (!user) return;

    try {
      setLoading(true);
      const response = await auditoriaService.historicoPorProdutor(user.id);
      setAuditorias(response.auditorias || []);
    } catch (error: any) {
      toast({
        variant: 'destructive',
        title: 'Erro ao carregar auditorias',
        description: error.response?.data?.message || 'Erro desconhecido',
      });
    } finally {
      setLoading(false);
    }
  };

  const getResultadoBadge = (resultado: string) => {
    const badges: Record<string, { color: string; icon: any; text: string }> = {
      APROVADA: { color: 'bg-green-100 text-green-800', icon: CheckCircle, text: 'Aprovada' },
      REPROVADA: { color: 'bg-red-100 text-red-800', icon: AlertCircle, text: 'Reprovada' },
      PENDENTE_REVISAO: { color: 'bg-yellow-100 text-yellow-800', icon: Clock, text: 'Pendente' },
    };
    return badges[resultado] || badges.PENDENTE_REVISAO;
  };

  const stats = {
    total: auditorias.length,
    aprovadas: auditorias.filter(a => a.resultado === 'APROVADO').length,
    reprovadas: auditorias.filter(a => a.resultado === 'REPROVADO').length,
    pendentes: auditorias.filter(a => a.resultado === 'REQUER_REVISAO').length,
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold">Auditorias</h1>
        <p className="text-muted-foreground">Histórico de auditorias das suas movimentações</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total</CardTitle>
            <ClipboardCheck className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.total}</div>
            <p className="text-xs text-muted-foreground">Auditorias realizadas</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Aprovadas</CardTitle>
            <CheckCircle className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">{stats.aprovadas}</div>
            <p className="text-xs text-muted-foreground">Conformidade aprovada</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Reprovadas</CardTitle>
            <AlertCircle className="h-4 w-4 text-red-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600">{stats.reprovadas}</div>
            <p className="text-xs text-muted-foreground">Não conformidade</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Pendentes</CardTitle>
            <Clock className="h-4 w-4 text-yellow-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-yellow-600">{stats.pendentes}</div>
            <p className="text-xs text-muted-foreground">Aguardando revisão</p>
          </CardContent>
        </Card>
      </div>

      {/* Lista de Auditorias */}
      <Card>
        <CardHeader>
          <CardTitle>Histórico de Auditorias</CardTitle>
          <CardDescription>Todas as auditorias realizadas nas suas movimentações</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="text-center py-8 text-muted-foreground">Carregando...</div>
          ) : auditorias.length === 0 ? (
            <div className="text-center py-8">
              <ClipboardCheck className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground">Nenhuma auditoria realizada ainda</p>
            </div>
          ) : (
            <div className="space-y-4">
              {auditorias.map((auditoria) => {
                const badge = getResultadoBadge(auditoria.resultado);
                const Icon = badge.icon;
                return (
                  <div
                    key={auditoria.id}
                    className="border rounded-lg p-4 hover:bg-gray-50 transition-colors"
                  >
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex items-center space-x-3">
                        <div className={`p-2 rounded-full ${badge.color}`}>
                          <Icon className="w-5 h-5" />
                        </div>
                        <div>
                          <p className="font-medium">Auditoria #{auditoria.id.slice(0, 8)}</p>
                          <p className="text-sm text-muted-foreground">
                            Movimentação: {auditoria.movimentacaoId.slice(0, 8)}
                          </p>
                        </div>
                      </div>
                      <div className={`px-3 py-1 rounded-full text-sm font-medium ${badge.color}`}>
                        {badge.text}
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                      <div>
                        <span className="text-muted-foreground">Data:</span>
                        <span className="ml-2">{formatDateTime(auditoria.processadoEm)}</span>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Versão da Regra:</span>
                        <span className="ml-2">{auditoria.versaoRegra}</span>
                      </div>
                    </div>

                    {auditoria.evidencias && auditoria.evidencias.length > 0 && (
                      <div className="mt-3 p-3 bg-yellow-50 rounded-md">
                        <p className="text-sm font-medium text-yellow-900 mb-1">Evidências:</p>
                        <ul className="text-sm text-yellow-700 space-y-1">
                          {auditoria.evidencias.map((evidencia, idx) => (
                            <li key={idx}>
                              <span className="font-medium">{evidencia.tipo}:</span> {evidencia.detalhe}
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}

                    {auditoria.observacoes && (
                      <div className="mt-3 p-3 bg-blue-50 rounded-md">
                        <p className="text-sm font-medium text-blue-900 mb-1">Observações da Revisão:</p>
                        <p className="text-sm text-blue-700">{auditoria.observacoes}</p>
                        {auditoria.auditorId && (
                          <p className="text-xs text-blue-600 mt-1">Revisado por: {auditoria.auditorId}</p>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
