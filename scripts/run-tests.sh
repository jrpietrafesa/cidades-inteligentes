#!/usr/bin/env bash
# Executa os testes automatizados.
#   bash scripts/run-tests.sh          -> local, com banco H2 em memoria (requer JDK 21 e Maven)
#   bash scripts/run-tests.sh docker   -> Docker Compose, com API e PostgreSQL reais (requer Docker)
set -euo pipefail
cd "$(dirname "$0")/.."

case "${1:-local}" in
  local)
    mvn -B verify
    echo "Relatorio: target/cucumber-reports/cucumber.html"
    ;;
  docker)
    mkdir -p relatorios-testes
    status=0
    docker compose -f docker-compose.test.yml up --build \
      --abort-on-container-exit --exit-code-from testes || status=$?
    docker compose -f docker-compose.test.yml down -v
    echo "Relatorio: relatorios-testes/cucumber.html"
    exit "$status"
    ;;
  *)
    echo "Uso: $0 [local|docker]" >&2
    exit 2
    ;;
esac
