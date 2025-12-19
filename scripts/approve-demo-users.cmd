@echo off
setlocal enabledelayedexpansion

if not defined USERS_DB_URL set "USERS_DB_URL=postgresql://ecoledger_users:ecoledger_users@localhost:5432/users"
if not defined USERS_API_BASE set "USERS_API_BASE=http://localhost:8084"
if not defined JWT_SECRET set "JWT_SECRET=ecoledger-secret-key-minimum-256-bits-for-hs256-algorithm-security"
if not defined JWT_SUBJECT set "JWT_SUBJECT=demo-admin"
if not defined JWT_SCOPES set "JWT_SCOPES=admin:usuarios usuarios:read usuarios:write"
if not defined APPROVAL_REASON set "APPROVAL_REASON=demo-auto-approval"

call :require_cmd psql || exit /b 1
call :require_cmd curl || exit /b 1

set "PYTHON=python"
where python >nul 2>&1 || (
    where python3 >nul 2>&1 && set "PYTHON=python3"
)
where %PYTHON% >nul 2>&1 || (
    echo Erro: python nao encontrado. Instale Python e tente novamente.
    exit /b 1
)

for /f "usebackq delims=" %%T in (`%PYTHON% -c "import sys,time,json,base64,hmac,hashlib;secret,sub,scopes=sys.argv[1:];now=int(time.time());hdr={'alg':'HS256','typ':'JWT'};pl={'sub':sub,'scopes':scopes,'iat':now,'exp':now+3600};enc=lambda o:base64.urlsafe_b64encode(json.dumps(o,separators=(',',':')).encode()).rstrip(b'=').decode();sign=f\"{enc(hdr)}.{enc(pl)}\";sig=hmac.new(secret.encode(),sign.encode(),hashlib.sha256).digest();print(f\"{sign}.{base64.urlsafe_b64encode(sig).rstrip(b'=').decode()}\")" "%JWT_SECRET%" "%JWT_SUBJECT%" "%JWT_SCOPES%"`) do set "JWT_TOKEN=%%T"

if not defined JWT_TOKEN (
    echo Falha ao gerar JWT.
    exit /b 1
)

set COUNT=0
for /f "usebackq delims=" %%U in (`psql "%USERS_DB_URL%" -At -c "select id from usuarios where status <> 'APROVADO';" 2^>nul`) do (
    if not "%%U"=="" (
        set "USER_!COUNT!=%%U"
        set /a COUNT+=1
    )
)

if "!COUNT!"=="0" (
    echo Nenhum usuario pendente para aprovacao.
    exit /b 0
)

echo Encontrados !COUNT! usuario(s) pendente(s). Iniciando aprovacao...

for /l %%I in (0,1,!COUNT!-1) do (
    set "USER_ID=!USER_%%I!"
    set "TMP=%TEMP%\approve-%%I.out"
    for /f %%H in ('curl -s -o "!TMP!" -w "%%{http_code}" -X PATCH "!USERS_API_BASE!/usuarios/!USER_ID!/status" -H "Authorization: Bearer !JWT_TOKEN!" -H "Content-Type: application/json" -d "{\"status\":\"APROVADO\",\"reason\":\"!APPROVAL_REASON!\"}"') do set "HTTP_CODE=%%H"
    if "!HTTP_CODE!"=="200" (
        echo [OK] Usuario !USER_ID! aprovado.
    ) else (
        echo [ERRO] Falha ao aprovar !USER_ID! (HTTP !HTTP_CODE!). Detalhes:
        type "!TMP!"
        echo.
    )
    del /q "!TMP!" >nul 2>&1
)

exit /b 0

:require_cmd
where %1 >nul 2>&1
if errorlevel 1 (
    echo Erro: comando '%1' nao encontrado. Instale-o e tente novamente.
    exit /b 1
)
exit /b 0
