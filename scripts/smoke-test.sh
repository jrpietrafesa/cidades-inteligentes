#!/usr/bin/env bash
# Smoke test pos-deploy: valida health, ambiente correto e fluxo de negocio.
# Uso: ./scripts/smoke-test.sh <url-base> <ambiente-esperado>
set -euo pipefail

BASE="${1:?URL base, ex: http://localhost:8081}"
ESPERADO="${2:?ambiente esperado, ex: staging}"

echo ">> Health check"
curl -fsS "$BASE/actuator/health" | tee /dev/stderr | grep -q '"status":"UP"'

echo; echo ">> Ambiente"
curl -fsS "$BASE/api/info" | tee /dev/stderr | grep -q "\"ambiente\":\"${ESPERADO}\""

echo; echo ">> Cadastro de edificio + medicao acima da meta"
ID=$(curl -fsS -X POST "$BASE/api/edificios" -H 'Content-Type: application/json' \
  -d '{"nome":"Smoke Test - Escola Municipal","cidade":"Sao Paulo","tipo":"ESCOLA","limiteDiarioKwh":100}' \
  | sed -E 's/.*"id":([0-9]+).*/\1/')
curl -fsS -X POST "$BASE/api/edificios/$ID/medicoes" -H 'Content-Type: application/json' \
  -d '{"kwh":150}' | tee /dev/stderr | grep -q '"alertaGerado":true'

echo; echo ">> Indicadores ESG"
curl -fsS "$BASE/api/indicadores" | tee /dev/stderr
echo; echo ">> SMOKE TEST OK (${ESPERADO})"
