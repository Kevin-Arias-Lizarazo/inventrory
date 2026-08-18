$ErrorActionPreference = 'Stop'

Write-Host 'Construyendo y levantando el entorno de despliegue...' -ForegroundColor Cyan
docker compose -f docker-compose.prod.yml up --build -d
if ($LASTEXITCODE -ne 0) {
    throw 'Falló el despliegue.'
}

docker compose -f docker-compose.prod.yml ps
Write-Host 'Aplicación disponible en http://localhost:8080' -ForegroundColor Green
