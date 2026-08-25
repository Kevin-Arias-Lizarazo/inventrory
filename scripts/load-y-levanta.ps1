param(
  [Parameter(Mandatory = $true)]
  [string]$Tar,
  [string]$BaseUrl = "http://localhost:8080"
)

# Despliegue en maquina destino (Windows 11, bajos recursos): carga la imagen
# pre-construida y levanta el contenedor SIN compilar (docker load + up -d).
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
  throw "Docker no esta instalado. Instale Docker Desktop (WSL2) primero. Ver DEPLOY-WINDOWS.md"
}
if (-not (Test-Path -LiteralPath $Tar)) {
  throw "No se encontro el archivo de imagen: $Tar"
}

Write-Host "1/4 Cargando imagen desde $Tar ..." -ForegroundColor Cyan
docker load -i $Tar
if ($LASTEXITCODE -ne 0) { throw "Falló docker load." }

Write-Host "2/4 Validando configuracion Compose..." -ForegroundColor Cyan
docker compose -f (Join-Path $root "docker-compose.prod.yml") config --quiet
if ($LASTEXITCODE -ne 0) { throw "Falló la configuracion Compose." }

Write-Host "3/4 Levantando contenedor (sin build)..." -ForegroundColor Cyan
docker compose -f (Join-Path $root "docker-compose.prod.yml") up -d --no-build
if ($LASTEXITCODE -ne 0) { throw "Falló docker compose up." }

Write-Host "4/4 Esperando a que la API responda..." -ForegroundColor Cyan
$ready = $false
for ($i = 0; $i -lt 36; $i++) {
  try {
    $r = Invoke-WebRequest -UseBasicParsing "$BaseUrl/api/instalacion/estado" -TimeoutSec 3
    if ($r.StatusCode -eq 200) { $ready = $true; break }
  } catch { Start-Sleep -Seconds 5 }
}
if (-not $ready) {
  docker compose -f (Join-Path $root "docker-compose.prod.yml") logs --tail 100 inventario
  throw "La API no respondio en 180s."
}

Write-Host "Aplicacion disponible en $BaseUrl" -ForegroundColor Green
Write-Host "Datos persistidos en el volumen inventario-data (/data)." -ForegroundColor Yellow
Write-Host "Detener:  .\scripts\stop.ps1   (los datos se conservan)" -ForegroundColor White