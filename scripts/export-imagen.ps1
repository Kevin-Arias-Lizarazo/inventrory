param(
  [string]$Imagen = "inventario-inventario:latest",
  [string]$Destino = (Join-Path $env:USERPROFILE "Downloads")
)

# Exporta la imagen productiva a un archivo .tar para desplegar en una maquina
# de bajos recursos SIN compilar alli (docker load en el destino).
$ErrorActionPreference = "Stop"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
  throw "Docker no esta disponible en esta maquina."
}

New-Item -ItemType Directory -Force -Path $Destino | Out-Null
$fecha = Get-Date -Format "yyyyMMdd-HHmm"
$tar = Join-Path $Destino "inventario-imagen-$fecha.tar"

Write-Host "Exportando imagen '$Imagen' -> $tar" -ForegroundColor Cyan
docker save -o $tar $Imagen
if ($LASTEXITCODE -ne 0) { throw "Falló docker save." }

$tamanoMB = [math]::Round((Get-Item $tar).Length / 1MB, 1)
Write-Host "Imagen exportada: $tar ($tamanoMB MB)" -ForegroundColor Green
Write-Host ""
Write-Host "Siguiente paso en la maquina destino (Windows 11):" -ForegroundColor Yellow
Write-Host "  1. Copie este .tar al equipo destino."
Write-Host "  2. Ejecute:  .\scripts\load-y-levanta.ps1 -Tar <ruta-al-tar>" -ForegroundColor White