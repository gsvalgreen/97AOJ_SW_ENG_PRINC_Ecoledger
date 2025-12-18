import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/use-toast';
import { authService, CadastroCriacaoDto } from '@/services/authService';
import { Leaf, Loader2 } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function CadastroPage() {
  const [formData, setFormData] = useState<CadastroCriacaoDto>({
    nome: '',
    email: '',
    documento: '',
    senha: '',
    role: 'produtor',
    dadosFazenda: {},
    anexos: [],
  });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { toast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Adiciona telefone e localização aos dadosFazenda se preenchidos
      const dadosFazenda: Record<string, any> = {};
      if (formData.dadosFazenda.telefone) dadosFazenda.telefone = formData.dadosFazenda.telefone;
      if (formData.dadosFazenda.localizacao) dadosFazenda.localizacao = formData.dadosFazenda.localizacao;
      
      await authService.cadastrar({
        ...formData,
        dadosFazenda,
      });
      
      toast({
        title: 'Cadastro realizado com sucesso!',
        description: 'Aguarde aprovação para acessar o sistema.',
      });

      navigate('/login');
    } catch (error: any) {
      toast({
        variant: 'destructive',
        title: 'Erro ao cadastrar',
        description: error.response?.data?.message || 'Erro ao processar cadastro',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 to-green-100 p-4">
      <Card className="w-full max-w-2xl">
        <CardHeader className="space-y-1 flex flex-col items-center">
          <div className="w-16 h-16 bg-primary rounded-full flex items-center justify-center mb-2">
            <Leaf className="w-10 h-10 text-white" />
          </div>
          <CardTitle className="text-3xl font-bold text-center">Novo Cadastro</CardTitle>
          <CardDescription className="text-center">
            Preencha seus dados para começar
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="nome">Nome Completo *</Label>
                <Input
                  id="nome"
                  value={formData.nome}
                  onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="email">E-mail *</Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="senha">Senha *</Label>
                <Input
                  id="senha"
                  type="password"
                  value={formData.senha}
                  onChange={(e) => setFormData({ ...formData, senha: e.target.value })}
                  required
                  minLength={6}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="role">Tipo de Usuário *</Label>
                <select
                  id="role"
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                  required
                >
                  <option value="produtor">Produtor</option>
                  <option value="analista">Analista</option>
                  <option value="auditor">Auditor</option>
                </select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="documento">CPF/CNPJ *</Label>
                <Input
                  id="documento"
                  value={formData.documento}
                  onChange={(e) => setFormData({ ...formData, documento: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="telefone">Telefone</Label>
                <Input
                  id="telefone"
                  value={formData.dadosFazenda.telefone || ''}
                  onChange={(e) => setFormData({ ...formData, dadosFazenda: { ...formData.dadosFazenda, telefone: e.target.value } })}
                />
              </div>
              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="localizacao">Localização</Label>
                <Input
                  id="localizacao"
                  value={formData.dadosFazenda.localizacao || ''}
                  onChange={(e) => setFormData({ ...formData, dadosFazenda: { ...formData.dadosFazenda, localizacao: e.target.value } })}
                  placeholder="Cidade, Estado"
                />
              </div>
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Cadastrando...
                </>
              ) : (
                'Cadastrar'
              )}
            </Button>
            <div className="text-sm text-center text-muted-foreground">
              Já tem uma conta?{' '}
              <Link to="/login" className="text-primary hover:underline">
                Faça login
              </Link>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
