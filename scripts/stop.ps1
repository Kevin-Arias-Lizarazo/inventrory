$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
docker compose -f docker-compose.prod.yml down
Write-Host 'Contenedor detenido. El volumen de datos se conservó.' -ForegroundColor Green
