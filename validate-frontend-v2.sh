#!/bin/bash

# Script de Validação - Frontend v2 no Docker
# Execute este script após rodar docker-compose up

echo -e "\n\033[1;36m🔍 Validando Frontend v2 no Docker...\033[0m\n"

# Função para verificar serviço
check_service() {
    local name=$1
    local container=$2
    local url=$3
    
    echo -n "Verificando $name..."
    
    if docker inspect -f '{{.State.Running}}' $container 2>/dev/null | grep -q "true"; then
        echo -e " \033[1;32m✅ Rodando\033[0m"
        
        if [ ! -z "$url" ]; then
            sleep 0.5
            if curl -s -o /dev/null -w "%{http_code}" $url 2>/dev/null | grep -q "200"; then
                echo -e "  └─ HTTP Status: 200 OK"
            else
                echo -e "  └─ \033[1;33mAguardando resposta HTTP...\033[0m"
            fi
        fi
        return 0
    else
        echo -e " \033[1;31m❌ Não está rodando\033[0m"
        return 1
    fi
}

echo -e "\033[1;33m📦 Infraestrutura\033[0m"
echo "─────────────────"
check_service "PostgreSQL" "ecoledger-postgres" ""
check_service "Kafka" "ecoledger-kafka" ""
check_service "MinIO" "ecoledger-minio" "http://localhost:9001"
echo ""

echo -e "\033[1;33m🔧 Microserviços Backend\033[0m"
echo "────────────────────────"
check_service "Users Service" "ecoledger-users-service" "http://localhost:8084/actuator/health"
users_ok=$?
check_service "Movimentacao Service" "ecoledger-movimentacao-service" "http://localhost:8082/actuator/health"
mov_ok=$?
check_service "Auditoria Service" "ecoledger-auditoria-service" "http://localhost:8083/actuator/health"
aud_ok=$?
check_service "Certificacao Service" "ecoledger-certificacao-service" "http://localhost:8085/actuator/health"
cert_ok=$?
echo ""

echo -e "\033[1;33m🌐 Frontends\033[0m"
echo "─────────────"
check_service "Frontend v1" "ecoledger-frontend-web" "http://localhost:3000"
check_service "Frontend v2" "ecoledger-frontend-v2" "http://localhost:3001"
front_v2_ok=$?
echo ""

# Resumo
echo -e "\033[1;33m📊 Resumo\033[0m"
echo "─────────"

if [ $front_v2_ok -eq 0 ]; then
    echo -e "\033[1;32m✅ Frontend v2 está rodando!\033[0m"
    echo ""
    echo -e "\033[1;36m🌐 Acesse:\033[0m"
    echo -e "   Frontend v2: \033[1;34mhttp://localhost:3001\033[0m"
    echo -e "   Frontend v1: \033[1;34mhttp://localhost:3000\033[0m"
else
    echo -e "\033[1;31m❌ Frontend v2 não está rodando\033[0m"
    echo ""
    echo -e "\033[1;33m💡 Dicas:\033[0m"
    
    if [ $users_ok -ne 0 ] || [ $mov_ok -ne 0 ] || [ $aud_ok -ne 0 ] || [ $cert_ok -ne 0 ]; then
        echo "   • Nem todos os backends estão prontos. Aguarde alguns minutos."
    fi
    
    echo "   • Execute: docker logs ecoledger-frontend-v2"
    echo "   • Verifique: docker-compose -f docker-compose-ecoledger.yml ps"
fi

echo ""

# Verificar dependências
echo -e "\033[1;33m🔗 Dependências do Frontend v2\033[0m"
echo "───────────────────────────────"

[ $users_ok -eq 0 ] && echo -e "  \033[1;32m✅ users-service\033[0m" || echo -e "  \033[1;31m❌ users-service - Frontend v2 aguarda este serviço\033[0m"
[ $mov_ok -eq 0 ] && echo -e "  \033[1;32m✅ movimentacao-service\033[0m" || echo -e "  \033[1;31m❌ movimentacao-service - Frontend v2 aguarda este serviço\033[0m"
[ $aud_ok -eq 0 ] && echo -e "  \033[1;32m✅ auditoria-service\033[0m" || echo -e "  \033[1;31m❌ auditoria-service - Frontend v2 aguarda este serviço\033[0m"
[ $cert_ok -eq 0 ] && echo -e "  \033[1;32m✅ certificacao-service\033[0m" || echo -e "  \033[1;31m❌ certificacao-service - Frontend v2 aguarda este serviço\033[0m"

echo ""

# Comandos úteis
echo -e "\033[1;33m📝 Comandos Úteis\033[0m"
echo "─────────────────"
echo "  Ver logs:        docker logs -f ecoledger-frontend-v2"
echo "  Ver status:      docker-compose -f docker-compose-ecoledger.yml ps"
echo "  Reiniciar:       docker-compose -f docker-compose-ecoledger.yml restart frontend-v2"
echo "  Rebuild:         docker-compose -f docker-compose-ecoledger.yml up -d --build frontend-v2"
echo ""
