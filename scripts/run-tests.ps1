param(
  [string]$BaseUrl = "http://localhost:8080",
  [switch]$External
)

# Runner autonomo de la puerta de regresion (Windows / PowerShell 5.1).
# Modo por defecto (gradle): levanta el backend con BD temporal, espera readiness,
# corre los 4 scripts de test y DETIENE el backend al final (pase o falle).
# Con -External: asume un backend ya corriendo en $BaseUrl y solo corre los tests.

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$tmp = Join-Path $env:TEMP "opencode\inv-test"
$db = Join-Path $tmp "inventario.db"

function Detener-Backend {
  $conn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
  if ($conn) {
    $conn | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object {
      Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue
    }
    Write-Host "Backend detenido."
  } else {
    Write-Host "No habia backend en el puerto 8080."
  }
}

if (-not $External) {
  New-Item -ItemType Directory -Force -Path "$tmp\sesiones", "$tmp\logs", "$tmp\uploads" | Out-Null
  Remove-Item -Force $db -ErrorAction SilentlyContinue

  $launch = Join-Path $tmp "launch-backend.ps1"
  @"
`$env:INVENTARIO_DB = "$db"
`$env:APP_SESIONES_DIR = "$tmp\sesiones"
`$env:APP_LOGS_DIR = "$tmp\logs"
`$env:APP_UPLOADS_DIR = "$tmp\uploads"
Set-Location "$root\backend"
.\gradlew.bat bootRun --no-daemon
"@ | Set-Content -Path $launch -Encoding UTF8

  $log = Join-Path $tmp "backend.log"
  Start-Process powershell.exe -ArgumentList "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $launch `
    -RedirectStandardOutput $log -RedirectStandardError "$log.err" -WindowStyle Hidden

  $ready = $false
  for ($i = 0; $i -lt 24; $i++) {
    Start-Sleep -Seconds 5
    try {
      $r = Invoke-WebRequest -Uri "$BaseUrl/api/instalacion/estado" -TimeoutSec 3 -UseBasicParsing
      if ($r.StatusCode -eq 200) { $ready = $true; break }
    } catch { }
  }
  if (-not $ready) {
    Detener-Backend
    Write-Host "`nBACKEND NO RESPONDIO en 120s. Ultimas lineas del log:" -ForegroundColor Red
    Get-Content $log -Tail 20 -ErrorAction SilentlyContinue
    exit 1
  }
  Write-Host "Backend listo ($BaseUrl)."
}

try {
  # Orden importante: test-auth exige instalacion PENDIENTE (BD fresca) y deja admin/AdminTest2026.
  $tests = @("test-auth.mjs", "test-api.mjs", "test-react-api.mjs", "test-sse.mjs")
  $fallos = 0
  foreach ($t in $tests) {
    Write-Host "`n=== $t ==="
    fnm env --use-on-cd | Out-String | Invoke-Expression
    & node (Join-Path $root "scripts\$t") $BaseUrl
    if ($LASTEXITCODE -ne 0) { $fallos++ }
  }
  if ($fallos -gt 0) {
    Write-Host "`n$fallos script(s) con fallos." -ForegroundColor Red
    exit 1
  }
  Write-Host "`nTODOS LOS TESTS PASARON." -ForegroundColor Green
  exit 0
} finally {
  if (-not $External) { Detener-Backend }
}