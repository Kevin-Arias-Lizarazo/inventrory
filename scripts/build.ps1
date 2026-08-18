$ErrorActionPreference = 'Stop'

Write-Host 'Construyendo imágenes de despliegue...' -ForegroundColor Cyan
docker compose -f docker-compose.prod.yml build
if ($LASTEXITCODE -ne 0) {
    throw 'Falló la construcción de las imágenes de despliegue.'
}

Write-Host 'Build de despliegue completado.' -ForegroundColor Green
