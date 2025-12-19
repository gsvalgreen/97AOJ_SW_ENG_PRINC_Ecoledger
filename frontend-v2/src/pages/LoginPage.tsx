import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/use-toast';
import { authService, decodeToken } from '@/services/authService';
import { useAuthStore } from '@/store/authStore';
import { Leaf, Loader2 } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { toast } = useToast();
  const login = useAuthStore((state) => state.login);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await authService.login({ email, password: senha });
      
      // Decodificar o token para obter o userId
      const decoded = decodeToken(response.accessToken);
      
      // IMPORTANTE: Salvar token ANTES de buscar o perfil
      // O interceptor do axios precisa do token no localStorage
      localStorage.setItem('token', response.accessToken);
      
      // Buscar perfil do usuário (agora com token no header)
      const profile = await authService.getProfile(decoded.sub);
      
      console.log('Profile recebido do backend:', profile);
      
      login(response.accessToken, {
        id: profile.id,
        email: profile.email,
        role: profile.role,
        nome: profile.nome,
        status: profile.status,
      });

      console.log('User após login:', useAuthStore.getState().user);

      toast({
        title: 'Login realizado com sucesso!',
        description: `Bem-vindo, ${profile.nome}`,
      });

      navigate('/dashboard');
    } catch (error: any) {
      toast({
        variant: 'destructive',
        title: 'Erro ao fazer login',
        description: error.response?.data?.message || 'Credenciais inválidas',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 to-green-100 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1 flex flex-col items-center">
          <div className="w-16 h-16 bg-primary rounded-full flex items-center justify-center mb-2">
            <Leaf className="w-10 h-10 text-white" />
          </div>
          <CardTitle className="text-3xl font-bold text-center">ECO LEDGER</CardTitle>
          <CardDescription className="text-center">
            Sistema de Gestão Sustentável e Certificação Verde
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleLogin}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">E-mail</Label>
              <Input
                id="email"
                type="email"
                placeholder="seu@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="senha">Senha</Label>
              <Input
                id="senha"
                type="password"
                placeholder="••••••••"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                required
              />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Entrando...
                </>
              ) : (
                'Entrar'
              )}
            </Button>
            <div className="text-sm text-center text-muted-foreground">
              Não tem uma conta?{' '}
              <Link to="/cadastro" className="text-primary hover:underline">
                Cadastre-se
              </Link>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
