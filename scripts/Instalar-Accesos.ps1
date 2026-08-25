param(
  [switch]$AutoInicio
)

# Crea accesos directos de doble clic en el escritorio:
#   "Inventario - Iniciar"   -> levanta Docker + contenedor + abre navegador
#   "Inventario - Detener"   -> apaga el contenedor limpio (antes de apagar la maquina)
# "Detener" es un .exe real compilado localmente con csc.exe (viene con Windows,
# sin instalar nada). Con -AutoInicio: Docker Desktop queda en el arranque de
# Windows para que el contenedor (restart: unless-stopped) suba solo al encender.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

$csc = "$env:windir\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if (-not (Test-Path $csc)) {
  $csc = Get-ChildItem "$env:windir\Microsoft.NET\Framework*\v4*\csc.exe" -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
}
if (-not $csc) { throw "No se encontro csc.exe (.NET Framework). No se puede compilar el ejecutable." }

# ---- Codigo fuente del ejecutable "Detener-Inventario.exe" ----
$codigoCs = @'
using System;
using System.Diagnostics;

class DetenerInventario {
  static int Main() {
    string repo = AppDomain.CurrentDomain.BaseDirectory;
    Console.WriteLine("Deteniendo el contenedor de forma limpia...");
    try {
      var psi = new ProcessStartInfo("docker", "compose -f docker-compose.prod.yml down") {
        WorkingDirectory = repo,
        UseShellExecute = false,
        RedirectStandardOutput = true,
        RedirectStandardError = true,
        CreateNoWindow = true
      };
      using (var p = Process.Start(psi)) {
        Console.WriteLine(p.StandardOutput.ReadToEnd());
        Console.WriteLine(p.StandardError.ReadToEnd());
        p.WaitForExit();
      }
      Console.WriteLine();
      Console.WriteLine("Contenedor detenido correctamente. Los datos se conservaron.");
      Console.WriteLine("Ya puede apagar la maquina sin riesgo.");
    } catch (Exception ex) {
      Console.WriteLine("No se pudo detener: " + ex.Message);
      Console.WriteLine("Si Docker no esta activo, puede apagar la maquina igualmente.");
    }
    Console.WriteLine();
    Console.WriteLine("Presione una tecla para cerrar...");
    try { Console.ReadKey(); } catch { }
    return 0;
  }
}
'@

$exeDestino = Join-Path $root "Detener-Inventario.exe"
$csTemp = Join-Path $env:TEMP "Detener-Inventario.cs"
Set-Content -Path $csTemp -Value $codigoCs -Encoding ASCII

Write-Host "Compilando Detener-Inventario.exe..." -ForegroundColor Cyan
& $csc /nologo /out:$exeDestino $csTemp | Out-Null
if ($LASTEXITCODE -ne 0) { Remove-Item $csTemp -ErrorAction SilentlyContinue; throw "Falló la compilación del ejecutable." }
Remove-Item $csTemp -ErrorAction SilentlyContinue
Write-Host "Ejecutable generado: $exeDestino" -ForegroundColor Green

$shell = New-Object -ComObject WScript.Shell
$cmd = "$env:SystemRoot\System32\cmd.exe"

function New-Accesso {
  param([string]$Nombre, [string]$Target, [string]$Args, [string]$Icono, [string]$Carpeta)
  $ruta = Join-Path $Carpeta "$Nombre.lnk"
  $lnk = $shell.CreateShortcut($ruta)
  $lnk.TargetPath = $Target
  if ($Args) { $lnk.Arguments = $Args }
  $lnk.WorkingDirectory = $root
  $lnk.IconLocation = $Icono
  $lnk.Description = "Inventario - $Nombre"
  $lnk.Save()
  Write-Host "Creado: $ruta"
}

$escritorio = [Environment]::GetFolderPath('Desktop')
New-Accesso "Inventario - Iniciar" $cmd "/c `"$root\scripts\Iniciar-Inventario.bat`"" "$env:SystemRoot\System32\shell32.dll,1" $escritorio
New-Accesso "Inventario - Detener" $exeDestino "" "$env:SystemRoot\System32\shell32.dll,27" $escritorio

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