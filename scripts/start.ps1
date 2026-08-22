$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$url = 'http://localhost:8080'
$compose = 'docker-compose.prod.yml'

$corriendo = docker ps --filter "name=inventario" --filter "status=running" --format "{{.Names}}"
if ($corriendo -match 'inventario') {
    Write-Host 'El contenedor ya está en ejecución.' -ForegroundColor Cyan
} else {
    Write-Host 'Levantando la aplicación...' -ForegroundColor Cyan
    docker compose -f $compose up -d
    if ($LASTEXITCODE -ne 0) {
        throw 'No se pudieron iniciar los contenedores. Verifica Docker Desktop.'
    }
}

Write-Host 'Esperando a que responda la API...' -ForegroundColor Cyan
for ($i = 0; $i -lt 30; $i++) {
    try {
        $response = Invoke-WebRequest -UseBasicParsing "$url/api/instalacion/estado" -TimeoutSec 2
        if ($response.StatusCode -eq 200) {
            Start-Process $url
            Write-Host "Aplicación disponible en $url" -ForegroundColor Green
            exit 0
        }
    } catch {
        Start-Sleep -Seconds 2
    }
}

docker compose -f $compose ps
throw "El contenedor inició, pero la API no responde en $url"