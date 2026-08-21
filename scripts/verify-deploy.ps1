$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$compose = @('-p', 'inventario', '-f', 'docker-compose.prod.yml')
$base = 'http://localhost:8080'

function Invoke-DockerCompose {
    & docker compose @compose @args
    if ($LASTEXITCODE -ne 0) { throw "Falló docker compose $args" }
}

Write-Host '1/5 Validando configuración Compose...' -ForegroundColor Cyan
Invoke-DockerCompose config --quiet

Write-Host '2/5 Construyendo imagen productiva (React + Java)...' -ForegroundColor Cyan
Invoke-DockerCompose build

Write-Host '3/5 Levantando contenedor...' -ForegroundColor Cyan
Invoke-DockerCompose up -d --remove-orphans

Write-Host '4/5 Esperando API y frontend...' -ForegroundColor Cyan
$ready = $false
for ($i = 0; $i -lt 36; $i++) {
    try {
        $api = Invoke-WebRequest -UseBasicParsing "$base/api/instalacion/estado" -TimeoutSec 3
        $web = Invoke-WebRequest -UseBasicParsing "$base/" -TimeoutSec 3
        if ($api.StatusCode -eq 200 -and $web.StatusCode -eq 200) {
            $ready = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 5
    }
}
if (-not $ready) {
    Invoke-DockerCompose logs --tail 100 inventario
    throw 'La API o el frontend no respondieron.'
}

Write-Host '5/5 Ejecutando pruebas API dentro de Node 22...' -ForegroundColor Cyan
$workspace = (Resolve-Path '.').Path
docker run --rm --network inventario_default -v "${workspace}:/workspace:ro" node:22-alpine node /workspace/scripts/test-api.mjs http://inventario:8080
if ($LASTEXITCODE -ne 0) { throw 'Falló test-api.mjs.' }
docker run --rm --network inventario_default -v "${workspace}:/workspace:ro" node:22-alpine node /workspace/scripts/test-react-api.mjs http://inventario:8080
if ($LASTEXITCODE -ne 0) { throw 'Falló test-react-api.mjs.' }
docker run --rm --network inventario_default -v "${workspace}:/workspace:ro" node:22-alpine node /workspace/scripts/test-sse.mjs http://inventario:8080
if ($LASTEXITCODE -ne 0) { throw 'Falló test-sse.mjs.' }

Invoke-DockerCompose ps
Write-Host "Verificación completa: $base" -ForegroundColor Green
