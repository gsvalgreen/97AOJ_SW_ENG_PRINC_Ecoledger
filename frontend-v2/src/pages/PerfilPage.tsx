import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/use-toast';
import { authService, UsuarioAtualizacaoDto, UsuarioDto } from '@/services/authService';
import { useAuthStore } from '@/store/authStore';
import { Loader2, Mail, MapPin, Phone, Save, Shield, User } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function PerfilPage() {
  const { user, updateUser } = useAuthStore();
  const { toast } = useToast();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [profile, setProfile] = useState<UsuarioDto | null>(null);
  const [formData, setFormData] = useState<UsuarioAtualizacaoDto>({
    nomeCompleto: '',
    telefone: '',
    localizacao: '',
  });

  useEffect(() => {
    loadProfile();
  }, [user]);

  const loadProfile = async () => {
    if (!user) return;

    try {
      setLoading(true);
      const data = await authService.getProfile(user.id);
      setProfile(data);
      setFormData({
        nomeCompleto: data.nomeCompleto,
        telefone: data.telefone || '',
        localizacao: data.localizacao || '',
      });
    } catch (error: any) {
      toast({
        variant: 'destructive',
        title: 'Erro ao carregar perfil',
        description: error.response?.data?.message || 'Erro desconhecido',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    setSaving(true);
    try {
      const updated = await authService.updateProfile(user.id, formData);
      setProfile(updated);
      
      updateUser({
        ...user,
        nomeCompleto: updated.nomeCompleto,
      });

      toast({
        title: 'Perfil atualizado!',
        description: 'Suas informações foram salvas com sucesso.',
      });
    } catch (error: any) {
      toast({
        variant: 'destructive',
        title: 'Erro ao atualizar perfil',
        description: error.response?.data?.message || 'Erro desconhecido',
      });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold">Meu Perfil</h1>
        <p className="text-muted-foreground">Gerencie suas informações pessoais</p>
      </div>

      {/* Info Card */}
      <Card>
        <CardHeader>
          <CardTitle>Informações da Conta</CardTitle>
          <CardDescription>Dados básicos do seu perfil</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
              <Mail className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">E-mail</p>
                <p className="font-medium">{profile?.email}</p>
              </div>
            </div>
            <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
              <Shield className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Tipo de Usuário</p>
                <p className="font-medium">{profile?.role}</p>
              </div>
            </div>
            {profile?.cpf && (
              <div className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
                <User className="w-5 h-5 text-muted-foreground" />
                <div>
                  <p className="text-sm text-muted-foreground">CPF</p>
                  <p className="font-medium">{profile.cpf}</p>
                </div>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Editable Info */}
      <Card>
        <CardHeader>
          <CardTitle>Informações Editáveis</CardTitle>
          <CardDescription>Atualize seus dados pessoais</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSave} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="nomeCompleto">Nome Completo</Label>
              <div className="relative">
                <User className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="nomeCompleto"
                  className="pl-10"
                  value={formData.nomeCompleto}
                  onChange={(e) => setFormData({ ...formData, nomeCompleto: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="telefone">Telefone</Label>
              <div className="relative">
                <Phone className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="telefone"
                  className="pl-10"
                  value={formData.telefone}
                  onChange={(e) => setFormData({ ...formData, telefone: e.target.value })}
                  placeholder="(00) 00000-0000"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="localizacao">Localização</Label>
              <div className="relative">
                <MapPin className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="localizacao"
                  className="pl-10"
                  value={formData.localizacao}
                  onChange={(e) => setFormData({ ...formData, localizacao: e.target.value })}
                  placeholder="Cidade, Estado"
                />
              </div>
            </div>

            <div className="flex justify-end">
              <Button type="submit" disabled={saving}>
                {saving ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Salvando...
                  </>
                ) : (
                  <>
                    <Save className="mr-2 h-4 w-4" />
                    Salvar Alterações
                  </>
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Status Card */}
      <Card>
        <CardHeader>
          <CardTitle>Status da Conta</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between p-4 border rounded-lg">
            <div>
              <p className="font-medium">Status Atual</p>
              <p className="text-sm text-muted-foreground">
                Cadastrado em {profile?.criadoEm ? new Date(profile.criadoEm).toLocaleDateString('pt-BR') : '-'}
              </p>
            </div>
            <div className={`px-4 py-2 rounded-full ${
              profile?.status === 'ATIVO' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
            }`}>
              {profile?.status || 'PENDENTE'}
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
