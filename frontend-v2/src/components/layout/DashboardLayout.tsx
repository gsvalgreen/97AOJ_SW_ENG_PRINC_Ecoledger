import { Button } from '@/components/ui/button';
import { useAuthStore } from '@/store/authStore';
import {
    Award,
    ClipboardCheck,
    LayoutDashboard,
    Leaf,
    LogOut,
    Menu,
    Package,
    User,
    X
} from 'lucide-react';
import { useState } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';

export default function DashboardLayout() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const menuItems = [
    { path: '/dashboard', icon: LayoutDashboard, label: 'Dashboard', roles: ['PRODUTOR', 'ANALISTA', 'AUDITOR'] },
    { path: '/movimentacoes', icon: Package, label: 'Movimentações', roles: ['PRODUTOR', 'ANALISTA'] },
    { path: '/auditorias', icon: ClipboardCheck, label: 'Auditorias', roles: ['AUDITOR', 'ANALISTA', 'PRODUTOR'] },
    { path: '/certificacoes', icon: Award, label: 'Certificações', roles: ['PRODUTOR', 'ANALISTA'] },
    { path: '/perfil', icon: User, label: 'Perfil', roles: ['PRODUTOR', 'ANALISTA', 'AUDITOR'] },
  ];

  const filteredMenuItems = menuItems.filter(item => 
    item.roles.includes(user?.role || '')
  );

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-primary rounded-full flex items-center justify-center">
                <Leaf className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-gray-900">ECO LEDGER</h1>
                <p className="text-xs text-gray-500">Gestão Sustentável</p>
              </div>
            </div>

            {/* Desktop Navigation */}
            <nav className="hidden md:flex space-x-1">
              {filteredMenuItems.map((item) => {
                const Icon = item.icon;
                const isActive = location.pathname === item.path;
                return (
                  <Link key={item.path} to={item.path}>
                    <Button
                      variant={isActive ? 'default' : 'ghost'}
                      size="sm"
                      className="flex items-center space-x-2"
                    >
                      <Icon className="w-4 h-4" />
                      <span>{item.label}</span>
                    </Button>
                  </Link>
                );
              })}
            </nav>

            <div className="flex items-center space-x-4">
              <div className="hidden md:flex flex-col items-end">
                <span className="text-sm font-medium">{user?.nome}</span>
                <span className="text-xs text-gray-500">{user?.role}</span>
              </div>
              <Button variant="ghost" size="icon" onClick={handleLogout} className="hidden md:flex">
                <LogOut className="w-5 h-5" />
              </Button>
              <Button 
                variant="ghost" 
                size="icon" 
                className="md:hidden"
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              >
                {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
              </Button>
            </div>
          </div>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t bg-white">
            <div className="px-4 py-3 space-y-1">
              {filteredMenuItems.map((item) => {
                const Icon = item.icon;
                const isActive = location.pathname === item.path;
                return (
                  <Link 
                    key={item.path} 
                    to={item.path}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <Button
                      variant={isActive ? 'default' : 'ghost'}
                      size="sm"
                      className="w-full justify-start space-x-2"
                    >
                      <Icon className="w-4 h-4" />
                      <span>{item.label}</span>
                    </Button>
                  </Link>
                );
              })}
              <Button
                variant="ghost"
                size="sm"
                className="w-full justify-start space-x-2 text-red-600 hover:text-red-700 hover:bg-red-50"
                onClick={handleLogout}
              >
                <LogOut className="w-4 h-4" />
                <span>Sair</span>
              </Button>
            </div>
          </div>
        )}
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-white border-t mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <p className="text-center text-sm text-gray-500">
            © 2024 ECO LEDGER. Todos os direitos reservados.
          </p>
        </div>
      </footer>
    </div>
  );
}
