#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.prod.yml"
URL="http://localhost:8080"

command -v docker >/dev/null 2>&1 || {
  echo "ERROR: Docker no está instalado." >&2
  exit 1
}

if docker compose version >/dev/null 2>&1; then
  COMPOSE=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  echo "ERROR: Docker Compose no está disponible." >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  command -v sudo >/dev/null 2>&1 || {
    echo "ERROR: Docker requiere permisos y sudo no está instalado." >&2
    exit 1
  }
  sudo -v || exit 1
  COMPOSE=(sudo "${COMPOSE[@]}")
fi

cd "$ROOT_DIR"

if docker ps --filter "name=inventario" --filter "status=running" --format "{{.Names}}" | grep -q inventario; then
  echo "El contenedor ya está en ejecución."
else
  echo "Levantando la aplicación..."
  "${COMPOSE[@]}" -f "$COMPOSE_FILE" up -d
fi

echo "Esperando a que la API responda..."
for _ in $(seq 1 30); do
  if curl --silent --fail --max-time 2 "$URL/api/instalacion/estado" >/dev/null 2>&1; then
    if command -v xdg-open >/dev/null 2>&1; then
      xdg-open "$URL" >/dev/null 2>&1 &
    elif command -v sensible-browser >/dev/null 2>&1; then
      sensible-browser "$URL" >/dev/null 2>&1 &
    else
      echo "Abre manualmente: $URL"
    fi
    echo "Aplicación disponible en $URL"
    exit 0
  fi
  sleep 2
done

"${COMPOSE[@]}" -f "$COMPOSE_FILE" ps
echo "El contenedor está iniciado, pero la API aún no responde: $URL" >&2
exit 1