# Script de Validação - Frontend v2 no Docker
# Execute este script após rodar docker-compose up

Write-Host "🔍 Validando Frontend v2 no Docker..." -ForegroundColor Cyan
Write-Host ""

# Função para verificar serviço
function Test-Service {
    param (
        [string]$Name,
        [string]$Container,
        [string]$Url
    )
    
    Write-Host "Verificando $Name..." -NoNewline
    $status = docker inspect -f '{{.State.Running}}' $Container 2>$null
    
    if ($status -eq "true") {
        Write-Host " ✅ Rodando" -ForegroundColor Green
        
        if ($Url) {
            Start-Sleep -Milliseconds 500
            try {
                $response = Invoke-WebRequest -Uri $Url -TimeoutSec 5 -ErrorAction SilentlyContinue
                if ($response.StatusCode -eq 200) {
                    Write-Host "  └─ HTTP Status: 200 OK" -ForegroundColor Gray
                }
            } catch {
                Write-Host "  └─ Aguardando resposta HTTP..." -ForegroundColor Yellow
            }
        }
        return $true
    } else {
        Write-Host " ❌ Não está rodando" -ForegroundColor Red
        return $false
    }
}

Write-Host "📦 Infraestrutura" -ForegroundColor Yellow
Write-Host "─────────────────" -ForegroundColor Gray
Test-Service "PostgreSQL" "ecoledger-postgres" ""
Test-Service "Kafka" "ecoledger-kafka" ""
Test-Service "MinIO" "ecoledger-minio" "http://localhost:9001"
Write-Host ""

Write-Host "🔧 Microserviços Backend" -ForegroundColor Yellow
Write-Host "────────────────────────" -ForegroundColor Gray
$usersOk = Test-Service "Users Service" "ecoledger-users-service" "http://localhost:8084/actuator/health"
$movOk = Test-Service "Movimentacao Service" "ecoledger-movimentacao-service" "http://localhost:8082/actuator/health"
$audOk = Test-Service "Auditoria Service" "ecoledger-auditoria-service" "http://localhost:8083/actuator/health"
$certOk = Test-Service "Certificacao Service" "ecoledger-certificacao-service" "http://localhost:8085/actuator/health"
Write-Host ""

Write-Host "🌐 Frontends" -ForegroundColor Yellow
Write-Host "─────────────" -ForegroundColor Gray
$frontV1Ok = Test-Service "Frontend v1" "ecoledger-frontend-web" "http://localhost:3000"
$frontV2Ok = Test-Service "Frontend v2" "ecoledger-frontend-v2" "http://localhost:3001"
Write-Host ""

# Resumo
Write-Host "📊 Resumo" -ForegroundColor Yellow
Write-Host "─────────" -ForegroundColor Gray

$allBackendsOk = $usersOk -and $movOk -and $audOk -and $certOk

if ($frontV2Ok) {
    Write-Host "✅ Frontend v2 está rodando!" -ForegroundColor Green
    Write-Host ""
    Write-Host "🌐 Acesse:" -ForegroundColor Cyan
    Write-Host "   Frontend v2: " -NoNewline
    Write-Host "http://localhost:3001" -ForegroundColor Blue
    Write-Host "   Frontend v1: " -NoNewline
    Write-Host "http://localhost:3000" -ForegroundColor Blue
} else {
    Write-Host "❌ Frontend v2 não está rodando" -ForegroundColor Red
    Write-Host ""
    Write-Host "💡 Dicas:" -ForegroundColor Yellow
    
    if (-not $allBackendsOk) {
        Write-Host "   • Nem todos os backends estão prontos. Aguarde alguns minutos." -ForegroundColor Gray
    }
    
    Write-Host "   • Execute: docker logs ecoledger-frontend-v2" -ForegroundColor Gray
    Write-Host "   • Verifique: docker-compose -f docker-compose-ecoledger.yml ps" -ForegroundColor Gray
}

Write-Host ""

# Verificar dependências
Write-Host "🔗 Dependências do Frontend v2" -ForegroundColor Yellow
Write-Host "───────────────────────────────" -ForegroundColor Gray

$dependencies = @{
    "users-service" = $usersOk
    "movimentacao-service" = $movOk
    "auditoria-service" = $audOk
    "certificacao-service" = $certOk
}

foreach ($dep in $dependencies.GetEnumerator()) {
    if ($dep.Value) {
        Write-Host "  ✅ $($dep.Key)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $($dep.Key) - Frontend v2 aguarda este serviço" -ForegroundColor Red
    }
}

Write-Host ""

# Comandos úteis
Write-Host "📝 Comandos Úteis" -ForegroundColor Yellow
Write-Host "─────────────────" -ForegroundColor Gray
Write-Host "  Ver logs:        docker logs -f ecoledger-frontend-v2" -ForegroundColor Gray
Write-Host "  Ver status:      docker-compose -f docker-compose-ecoledger.yml ps" -ForegroundColor Gray
Write-Host "  Reiniciar:       docker-compose -f docker-compose-ecoledger.yml restart frontend-v2" -ForegroundColor Gray
Write-Host "  Rebuild:         docker-compose -f docker-compose-ecoledger.yml up -d --build frontend-v2" -ForegroundColor Gray
Write-Host ""
