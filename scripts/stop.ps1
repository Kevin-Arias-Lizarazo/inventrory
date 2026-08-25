# Detiene el contenedor productivo. Los datos del volumen inventario-data se conservan.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

docker compose -f (Join-Path $root "docker-compose.prod.yml") down
Write-Host "Contenedor detenido. Los datos del volumen se conservaron." -ForegroundColor Green