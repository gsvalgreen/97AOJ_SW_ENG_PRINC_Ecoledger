# Script PowerShell para testar login

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Teste de Login - ECO LEDGER" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Usuário de teste
$email = "jane.doe@exemple.com"
$password = "123456"

Write-Host "1. Testando login..." -ForegroundColor Yellow
Write-Host "   Email: $email"
Write-Host "   Password: $password"
Write-Host ""

# Fazer requisição
try {
    $body = @{
        email = $email
        password = $password
    } | ConvertTo-Json

    Write-Host "2. Enviando requisição para http://localhost:8084/usuarios/auth/login" -ForegroundColor Yellow
    
    $response = Invoke-WebRequest `
        -Uri "http://localhost:8084/usuarios/auth/login" `
        -Method POST `
        -Body $body `
        -ContentType "application/json" `
        -ErrorAction Stop

    $data = $response.Content | ConvertFrom-Json
    
    Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host ""
    Write-Host "3. Response completo:" -ForegroundColor Yellow
    Write-Host ($data | ConvertTo-Json -Depth 10)
    Write-Host ""
    
    # Verificar accessToken
    if ($data.accessToken) {
        $token = $data.accessToken
        Write-Host "4. Access Token recebido (primeiros 80 caracteres):" -ForegroundColor Yellow
        Write-Host $token.Substring(0, [Math]::Min(80, $token.Length))...
        Write-Host ""
        
        # Dividir token
        $tokenParts = $token.Split('.')
        Write-Host "5. Número de partes do token: $($tokenParts.Length)" -ForegroundColor Yellow
        
        if ($tokenParts.Length -eq 3) {
            Write-Host "✅ Token JWT válido (3 partes)" -ForegroundColor Green
            Write-Host ""
            
            # Decodificar payload
            Write-Host "6. Decodificando payload..." -ForegroundColor Yellow
            try {
                $payloadBase64 = $tokenParts[1]
                
                # Adicionar padding se necessário
                $padding = 4 - ($payloadBase64.Length % 4)
                if ($padding -lt 4) {
                    $payloadBase64 += "=" * $padding
                }
                
                $payloadBytes = [System.Convert]::FromBase64String($payloadBase64)
                $payloadJson = [System.Text.Encoding]::UTF8.GetString($payloadBytes)
                $payload = $payloadJson | ConvertFrom-Json
                
                Write-Host "✅ Payload decodificado:" -ForegroundColor Green
                Write-Host ($payload | ConvertTo-Json -Depth 10)
                Write-Host ""
                
                # Extrair userId
                $userId = if ($payload.userId) { $payload.userId } else { $payload.sub }
                Write-Host "7. User ID: $userId" -ForegroundColor Yellow
                
                if ($userId) {
                    Write-Host "✅ userId encontrado no token" -ForegroundColor Green
                } else {
                    Write-Host "❌ userId não encontrado no token" -ForegroundColor Red
                }
                
            } catch {
                Write-Host "❌ Erro ao decodificar payload: $_" -ForegroundColor Red
                Write-Host "   Payload base64: $($tokenParts[1])" -ForegroundColor Yellow
            }
        } else {
            Write-Host "❌ Token inválido! Esperado 3 partes, recebido $($tokenParts.Length)" -ForegroundColor Red
            Write-Host "   Token completo: $token" -ForegroundColor Yellow
        }
    } else {
        Write-Host "❌ accessToken não encontrado na resposta" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host "TESTE CONCLUÍDO" -ForegroundColor Cyan
    Write-Host "=========================================" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Erro na requisição:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response body:" -ForegroundColor Yellow
        Write-Host $responseBody
    }
}
