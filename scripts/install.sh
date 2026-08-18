#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.prod.yml"

die() {
  echo "ERROR: $*" >&2
  exit 1
}

command -v docker >/dev/null 2>&1 || die "Docker no está instalado. Instala Docker Engine y Docker Compose v2."

if docker compose version >/dev/null 2>&1; then
  COMPOSE=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  die "Docker Compose no está disponible. Instala Docker Compose v2."
fi

if ! docker info >/dev/null 2>&1; then
  command -v sudo >/dev/null 2>&1 || die "Docker requiere permisos y sudo no está instalado. Agrega el usuario al grupo docker o ejecuta como root."
  sudo -v || die "No se pudieron obtener permisos para Docker."
  COMPOSE=(sudo "${COMPOSE[@]}")
fi

echo "Construyendo la aplicación productiva..."
cd "$ROOT_DIR"
"${COMPOSE[@]}" -f "$COMPOSE_FILE" up --build -d
"${COMPOSE[@]}" -f "$COMPOSE_FILE" ps

echo
echo "Instalación completada. Aplicación: http://localhost:8080"
