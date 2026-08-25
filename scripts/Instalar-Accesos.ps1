param(
  [switch]$AutoInicio
)

# Crea accesos directos de doble clic en el escritorio:
#   "Inventario - Iniciar"   -> levanta Docker + contenedor + abre navegador
#   "Inventario - Detener"   -> apaga el contenedor limpio (antes de apagar la maquina)
# Con -AutoInicio: ademas deja Docker Desktop en el arranque de Windows para
# que el contenedor (restart: unless-stopped) suba solo al encender.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

$shell = New-Object -ComObject WScript.Shell
$cmd = "$env:SystemRoot\System32\cmd.exe"

function New-Accesso {
  param([string]$Nombre, [string]$Bat, [int]$IconoIdx, [string]$Carpeta)
  $ruta = Join-Path $Carpeta "$Nombre.lnk"
  $lnk = $shell.CreateShortcut($ruta)
  $lnk.TargetPath = $cmd
  $lnk.Arguments = "/c `"$Bat`""
  $lnk.WorkingDirectory = $root
  $lnk.IconLocation = "$env:SystemRoot\System32\shell32.dll,$IconoIdx"
  $lnk.Description = "Inventario - $Nombre"
  $lnk.Save()
  Write-Host "Creado: $ruta"
}

$escritorio = [Environment]::GetFolderPath('Desktop')
New-Accesso "Inventario - Iniciar" (Join-Path $root "scripts\Iniciar-Inventario.bat") 1 $escritorio
New-Accesso "Inventario - Detener" (Join-Path $root "scripts\Detener-Inventario.bat") 27 $escritorio

if ($AutoInicio) {
  $inicio = [Environment]::GetFolderPath('Startup')
  $lnk = $shell.CreateShortcut((Join-Path $inicio "Inventario-Docker.lnk"))
  $lnk.TargetPath = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
  $lnk.WorkingDirectory = $root
  $lnk.IconLocation = "C:\Program Files\Docker\Docker\Docker Desktop.exe,0"
  $lnk.Description = "Docker Desktop (Inventario)"
  $lnk.Save()
  Write-Host "Auto-inicio configurado: Docker Desktop arranca con Windows y el contenedor sube solo." -ForegroundColor Green
} else {
  Write-Host ""
  Write-Host "Tip: para que la app arranque sola al encender, ejecute con -AutoInicio:" -ForegroundColor Yellow
  Write-Host "  .\scripts\Instalar-Accesos.ps1 -AutoInicio" -ForegroundColor White
}