#!/bin/bash

# Script de teste do fluxo de login

echo "========================================="
echo "Teste de Login - ECO LEDGER"
echo "========================================="
echo ""

# Dados do usuário de teste
EMAIL="jane.doe@exemple.com"
PASSWORD="123456"

echo "1. Testando login com usuário existente..."
echo "   Email: $EMAIL"
echo "   Password: $PASSWORD"
echo ""

# Fazer login
LOGIN_RESPONSE=$(docker exec ecoledger-users-service curl -s -X POST \
  http://localhost:8080/usuarios/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

echo "Response do login:"
echo "$LOGIN_RESPONSE" | jq .

# Extrair accessToken
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')

if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
  echo ""
  echo "❌ ERRO: Token não retornado!"
  exit 1
fi

echo ""
echo "2. Token recebido (primeiros 50 caracteres):"
echo "${ACCESS_TOKEN:0:50}..."

# Contar partes do token
TOKEN_PARTS=$(echo "$ACCESS_TOKEN" | tr '.' '\n' | wc -l)
echo ""
echo "3. Número de partes do token: $TOKEN_PARTS"

if [ "$TOKEN_PARTS" -ne 3 ]; then
  echo "❌ ERRO: Token JWT deve ter 3 partes (header.payload.signature)"
  echo "   Token completo: $ACCESS_TOKEN"
  exit 1
fi

# Decodificar payload (segunda parte)
PAYLOAD_BASE64=$(echo "$ACCESS_TOKEN" | cut -d'.' -f2)
echo ""
echo "4. Decodificando payload do token..."

# Adicionar padding se necessário (base64 requer múltiplo de 4)
PAYLOAD_BASE64_PADDED="$PAYLOAD_BASE64"
while [ $((${#PAYLOAD_BASE64_PADDED} % 4)) -ne 0 ]; do
  PAYLOAD_BASE64_PADDED="${PAYLOAD_BASE64_PADDED}="
done

PAYLOAD_JSON=$(echo "$PAYLOAD_BASE64_PADDED" | base64 -d 2>/dev/null)

if [ $? -eq 0 ]; then
  echo "✅ Payload decodificado com sucesso:"
  echo "$PAYLOAD_JSON" | jq .
  
  # Extrair userId
  USER_ID=$(echo "$PAYLOAD_JSON" | jq -r '.userId // .sub')
  echo ""
  echo "5. User ID extraído: $USER_ID"
  
  if [ "$USER_ID" == "null" ] || [ -z "$USER_ID" ]; then
    echo "❌ ERRO: userId não encontrado no payload"
    exit 1
  fi
  
  # Buscar dados do usuário
  echo ""
  echo "6. Buscando dados do usuário..."
  USER_DATA=$(docker exec ecoledger-users-service curl -s \
    http://localhost:8080/usuarios/$USER_ID \
    -H "Authorization: Bearer $ACCESS_TOKEN")
  
  echo "Dados do usuário:"
  echo "$USER_DATA" | jq .
  
  echo ""
  echo "========================================="
  echo "✅ TESTE CONCLUÍDO COM SUCESSO!"
  echo "========================================="
else
  echo "❌ ERRO: Falha ao decodificar payload (não é base64 válido)"
  echo "   Payload recebido: $PAYLOAD_BASE64"
  exit 1
fi
