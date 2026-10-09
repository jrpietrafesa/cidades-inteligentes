#!/usr/bin/env bash
# Deploy de um ambiente com Docker Compose.
# Uso: ./scripts/deploy.sh <staging|production> [imagem:tag]
#   - Com imagem: faz pull do registry (modo pipeline)
#   - Sem imagem: builda localmente
# Requer DB_PASSWORD exportada no shell (no pipeline vem dos secrets).
set -euo pipefail

AMBIENTE="${1:?Informe o ambiente: staging ou production}"
IMAGEM="${2:-}"
ENV_FILE="deploy/.env.${AMBIENTE}"
PROJETO="esg-${AMBIENTE}"

[[ -f "$ENV_FILE" ]] || { echo "Arquivo $ENV_FILE nao encontrado"; exit 1; }
: "${DB_PASSWORD:?Exporte DB_PASSWORD antes do deploy}"

export APP_VERSION="${APP_VERSION:-$(git rev-parse --short HEAD 2>/dev/null || echo manual)}"

echo ">> Deploy do ambiente '${AMBIENTE}' (projeto ${PROJETO}, versao ${APP_VERSION})"

if [[ -n "$IMAGEM" ]]; then
  export APP_IMAGE="$IMAGEM"
  docker compose -p "$PROJETO" --env-file "$ENV_FILE" pull app
  docker compose -p "$PROJETO" --env-file "$ENV_FILE" up -d --no-build --wait --wait-timeout 180
else
  export APP_IMAGE="cidades-esg:${AMBIENTE}"
  docker compose -p "$PROJETO" --env-file "$ENV_FILE" up -d --build --wait --wait-timeout 180
fi

docker compose -p "$PROJETO" --env-file "$ENV_FILE" ps
echo ">> Ambiente ${AMBIENTE} no ar."
