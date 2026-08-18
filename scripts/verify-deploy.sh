#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE=(docker compose -p inventario -f "$ROOT_DIR/docker-compose.prod.yml")
BASE="http://localhost:8080"

cd "$ROOT_DIR"

echo "1/5 Validando configuración Compose..."
"${COMPOSE[@]}" config --quiet

echo "2/5 Construyendo imagen productiva (React + Java)..."
"${COMPOSE[@]}" build

echo "3/5 Levantando contenedor..."
"${COMPOSE[@]}" up -d --remove-orphans

echo "4/5 Esperando API y frontend..."
ready=false
for _ in $(seq 1 36); do
  if curl --silent --fail --max-time 3 "$BASE/api/empleados" >/dev/null && \
     curl --silent --fail --max-time 3 "$BASE/" >/dev/null; then
    ready=true
    break
  fi
  sleep 5
done

if [ "$ready" != true ]; then
  "${COMPOSE[@]}" logs --tail 100 inventario
  echo "La API o el frontend no respondieron." >&2
  exit 1
fi

echo "5/5 Ejecutando pruebas API dentro de Node 22..."
docker run --rm --network inventario_default -v "$ROOT_DIR:/workspace:ro" node:22-alpine node /workspace/scripts/test-api.mjs http://inventario:8080
docker run --rm --network inventario_default -v "$ROOT_DIR:/workspace:ro" node:22-alpine node /workspace/scripts/test-react-api.mjs http://inventario:8080
docker run --rm --network inventario_default -v "$ROOT_DIR:/workspace:ro" node:22-alpine node /workspace/scripts/test-sse.mjs http://inventario:8080

"${COMPOSE[@]}" ps
echo "Verificación completa: $BASE"
