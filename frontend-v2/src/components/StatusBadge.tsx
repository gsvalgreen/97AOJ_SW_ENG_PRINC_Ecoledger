import { Badge } from '@/components/ui/badge';
import { AlertCircle, CheckCircle, Clock, XCircle } from 'lucide-react';

interface StatusBadgeProps {
  status: string;
  className?: string;
}

const statusConfig = {
  ATIVO: {
    label: 'Ativo',
    variant: 'default' as const,
    icon: CheckCircle,
    className: 'bg-green-500 hover:bg-green-600',
  },
  APROVADO: {
    label: 'Aprovado',
    variant: 'default' as const,
    icon: CheckCircle,
    className: 'bg-green-500 hover:bg-green-600',
  },
  PENDENTE: {
    label: 'Pendente',
    variant: 'secondary' as const,
    icon: Clock,
    className: 'bg-yellow-500 hover:bg-yellow-600 text-white',
  },
  REJEITADO: {
    label: 'Rejeitado',
    variant: 'destructive' as const,
    icon: XCircle,
    className: 'bg-red-500 hover:bg-red-600',
  },
  INATIVO: {
    label: 'Inativo',
    variant: 'destructive' as const,
    icon: XCircle,
    className: 'bg-red-500 hover:bg-red-600',
  },
  SUSPENSO: {
    label: 'Suspenso',
    variant: 'outline' as const,
    icon: AlertCircle,
    className: 'bg-orange-500 hover:bg-orange-600 text-white',
  },
};

export function StatusBadge({ status, className = '' }: StatusBadgeProps) {
  console.log('StatusBadge recebeu status:', status);
  const config = statusConfig[status as keyof typeof statusConfig] || statusConfig.PENDENTE;
  const Icon = config.icon;

  return (
    <Badge 
      variant={config.variant}
      className={`${config.className} ${className} flex items-center gap-1.5 px-3 py-1.5`}
    >
      <Icon className="w-3.5 h-3.5" />
      <span className="font-medium">{config.label}</span>
    </Badge>
  );
}
