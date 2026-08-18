$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host 'Construyendo y levantando el despliegue productivo...' -ForegroundColor Cyan
docker compose -f docker-compose.prod.yml up --build -d
if ($LASTEXITCODE -ne 0) {
    throw 'Falló Docker. Verifica docker version y que el daemon esté activo.'
}

docker compose -f docker-compose.prod.yml ps
Write-Host 'Instalación completada: http://localhost:8080' -ForegroundColor Green
