#!/usr/bin/env bash
# Popula dados de exemplo. Uso: ./scripts/seed.sh [url-base]
set -euo pipefail
BASE="${1:-http://localhost:8080}"
post() { curl -fsS -X POST "$BASE$1" -H 'Content-Type: application/json' -d "$2"; echo; }

post /api/edificios '{"nome":"EMEF Jardim Verde","cidade":"Sao Paulo","tipo":"ESCOLA","limiteDiarioKwh":180}'
post /api/edificios '{"nome":"Hospital Municipal Norte","cidade":"Sao Paulo","tipo":"HOSPITAL","limiteDiarioKwh":900}'
post /api/edificios '{"nome":"Paco Municipal","cidade":"Campinas","tipo":"ADMINISTRATIVO","limiteDiarioKwh":350}'

post /api/edificios/1/medicoes '{"kwh":95.5}'
post /api/edificios/1/medicoes '{"kwh":110.0}'
post /api/edificios/2/medicoes '{"kwh":420.0}'
post /api/edificios/3/medicoes '{"kwh":210.7}'
curl -fsS "$BASE/api/indicadores"; echo
