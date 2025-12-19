#!/usr/bin/env bash

set -euo pipefail

USERS_DB_URL=${USERS_DB_URL:-"postgresql://ecoledger_users:ecoledger_users@localhost:5432/users"}
USERS_API_BASE=${USERS_API_BASE:-"http://localhost:8084"}
JWT_SECRET=${JWT_SECRET:-"ecoledger-secret-key-minimum-256-bits-for-hs256-algorithm-security"}
JWT_SUBJECT=${JWT_SUBJECT:-"demo-admin"}
JWT_SCOPES=${JWT_SCOPES:-"admin:usuarios usuarios:read usuarios:write"}
APPROVAL_REASON=${APPROVAL_REASON:-"demo-auto-approval"}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "error: '$1' is required but not installed" >&2
    exit 1
  fi
}

require_cmd psql
require_cmd python3
require_cmd curl

generate_jwt() {
  python3 - "$JWT_SECRET" "$JWT_SUBJECT" "$JWT_SCOPES" <<'PY'
import sys, time, json, base64, hmac, hashlib
secret, subject, scopes = sys.argv[1:]
now = int(time.time())
header = {"alg": "HS256", "typ": "JWT"}
payload = {"sub": subject, "scopes": scopes, "iat": now, "exp": now + 3600}
def encode(obj):
    return base64.urlsafe_b64encode(json.dumps(obj, separators=(',', ':')).encode()).rstrip(b'=').decode()
signing = f"{encode(header)}.{encode(payload)}"
signature = hmac.new(secret.encode(), signing.encode(), hashlib.sha256).digest()
print(f"{signing}.{base64.urlsafe_b64encode(signature).rstrip(b'=').decode()}")
PY
}

JWT_TOKEN=$(generate_jwt)

USERS_TO_APPROVE=()
while IFS= read -r line; do
  if [[ -n "$line" ]]; then
    USERS_TO_APPROVE+=("$line")
  fi
done < <(psql "$USERS_DB_URL" -At -c "select id from usuarios where status <> 'APROVADO';")

if [[ ${#USERS_TO_APPROVE[@]} -eq 0 ]]; then
  echo "Nenhum usuário pendente para aprovação."
  exit 0
fi

echo "Encontrados ${#USERS_TO_APPROVE[@]} usuário(s) pendente(s). Iniciando aprovação..."

for user_id in "${USERS_TO_APPROVE[@]}"; do
  tmp=$(mktemp)
  http_code=$(curl -s -o "$tmp" -w "%{http_code}" \
    -X PATCH "${USERS_API_BASE}/usuarios/${user_id}/status" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{\"status\":\"APROVADO\",\"reason\":\"${APPROVAL_REASON}\"}")

  if [[ "$http_code" == "200" ]]; then
    echo "✔ Usuário ${user_id} aprovado."
  else
    echo "✖ Falha ao aprovar ${user_id} (HTTP ${http_code}). Detalhes:"
    cat "$tmp"
    echo
  fi
  rm -f "$tmp"
done
