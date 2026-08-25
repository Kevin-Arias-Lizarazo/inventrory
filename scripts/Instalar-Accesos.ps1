param(
  [ValidateSet("Docker", "JAR")]
  [string]$Modo = "Docker",
  [switch]$AutoInicio
)

# Crea accesos directos de doble clic en el escritorio:
#   "Inventario - Iniciar"   -> levanta la app y abre el navegador
#   "Inventario - Detener"   -> apaga limpio (antes de apagar la maquina)
#
# -Modo Docker (default): usa contenedor Docker (requiere Docker Desktop/WSL2).
# -Modo JAR: usa el JAR directamente (requiere JRE 17, sin Docker).
# Ambos compilan sus ejecutables con csc.exe (viene con Windows, sin instalar nada).
# Con -AutoInicio: la app queda en el arranque de Windows (Docker Desktop en modo
# Docker; el exe Iniciar en modo JAR).
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

$csc = "$env:windir\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if (-not (Test-Path $csc)) {
  $csc = Get-ChildItem "$env:windir\Microsoft.NET\Framework*\v4*\csc.exe" -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
}
if (-not $csc) { throw "No se encontro csc.exe (.NET Framework). No se puede compilar el ejecutable." }

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

function Compilar-Exe {
  param([string]$Nombre, [string]$Codigo, [string]$Refs)
  $exe = Join-Path $root $Nombre
  $cs = Join-Path $env:TEMP "$Nombre.cs"
  Set-Content -Path $cs -Value $Codigo -Encoding ASCII
  Write-Host "Compilando $Nombre ..." -ForegroundColor Cyan
  $argsList = @("/nologo", "/out:$exe")
  if ($Refs) { $argsList += $Refs }
  $argsList += $cs
  & $csc @argsList | Out-Null
  if ($LASTEXITCODE -ne 0) { Remove-Item $cs -ErrorAction SilentlyContinue; throw "Fallo la compilacion de $Nombre." }
  Remove-Item $cs -ErrorAction SilentlyContinue
  Write-Host "Generado: $exe" -ForegroundColor Green
  return $exe
}

if ($Modo -eq "Docker") {
  # ===================== MODO DOCKER =====================
  $exeDetener = Compilar-Exe "Detener-Inventario.exe" @'
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

  $escritorio = [Environment]::GetFolderPath('Desktop')
  New-Accesso "Inventario - Iniciar" $cmd "/c `"$root\scripts\Iniciar-Inventario.bat`"" "$env:SystemRoot\System32\shell32.dll,1" $escritorio
  New-Accesso "Inventario - Detener" $exeDetener "" "$env:SystemRoot\System32\shell32.dll,27" $escritorio

  if ($AutoInicio) {
    $inicio = [Environment]::GetFolderPath('Startup')
    $lnk = $shell.CreateShortcut((Join-Path $inicio "Inventario-Docker.lnk"))
    $lnk.TargetPath = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    $lnk.WorkingDirectory = $root
    $lnk.IconLocation = "C:\Program Files\Docker\Docker\Docker Desktop.exe,0"
    $lnk.Description = "Docker Desktop (Inventario)"
    $lnk.Save()
    Write-Host "Auto-inicio configurado: Docker Desktop arranca con Windows y el contenedor sube solo." -ForegroundColor Green
  }

} else {
  # ===================== MODO JAR =====================
  # Prepara app.jar en la raiz si el jar productivo existe.
  $jarOrigen = Join-Path $root "backend\build\libs\inventario-0.0.1-SNAPSHOT.jar"
  $jarDestino = Join-Path $root "app.jar"
  if (Test-Path $jarOrigen) {
    Copy-Item $jarOrigen $jarDestino -Force
    Write-Host "app.jar preparado ($([math]::Round((Get-Item $jarDestino).Length/1MB,1)) MB)" -ForegroundColor Green
  } elseif (-not (Test-Path $jarDestino)) {
    Write-Host "AVISO: no se encontro el JAR. Copie backend/build/libs/inventario-0.0.1-SNAPSHOT.jar como app.jar en la raiz." -ForegroundColor Yellow
  }

  $exeIniciar = Compilar-Exe "Iniciar-JAR.exe" @'
using System;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Threading;

class IniciarJAR {
  static int Main() {
    string raiz = AppDomain.CurrentDomain.BaseDirectory;
    string jar = Path.Combine(raiz, "app.jar");
    string data = Path.Combine(raiz, "data");
    if (!File.Exists(jar)) {
      Console.WriteLine("No se encontro app.jar junto a este ejecutable.");
      Console.WriteLine("Copie backend/build/libs/inventario-0.0.1-SNAPSHOT.jar como app.jar en la raiz.");
      Console.WriteLine();
      Console.WriteLine("Presione una tecla para cerrar...");
      try { Console.ReadKey(); } catch { }
      return 1;
    }
    Directory.CreateDirectory(Path.Combine(data, "uploads"));
    Directory.CreateDirectory(Path.Combine(data, "sesiones"));
    Directory.CreateDirectory(Path.Combine(data, "logs"));
    Environment.SetEnvironmentVariable("INVENTARIO_DB", Path.Combine(data, "inventario.db"));
    Environment.SetEnvironmentVariable("APP_UPLOADS_DIR", Path.Combine(data, "uploads"));
    Environment.SetEnvironmentVariable("APP_SESIONES_DIR", Path.Combine(data, "sesiones"));
    Environment.SetEnvironmentVariable("APP_LOGS_DIR", Path.Combine(data, "logs"));
    Environment.SetEnvironmentVariable("AUTH_SECRET_FILE", Path.Combine(data, ".env.auth"));
    Console.WriteLine("Iniciando Inventario (JAR)...");
    try {
      var psi = new ProcessStartInfo("java", "-jar \"" + jar + "\"") {
        WorkingDirectory = raiz,
        UseShellExecute = false,
        CreateNoWindow = true
      };
      using (var p = Process.Start(psi)) {
        bool ready = false;
        for (int i = 0; i < 120; i++) {
          Thread.Sleep(2000);
          if (p.HasExited) break;
          try {
            var req = (HttpWebRequest)WebRequest.Create("http://localhost:8080/api/instalacion/estado");
            req.Timeout = 2000;
            using (var resp = (HttpWebResponse)req.GetResponse()) {
              if ((int)resp.StatusCode == 200) { ready = true; break; }
            }
          } catch { }
        }
        if (ready) {
          Process.Start(new ProcessStartInfo("http://localhost:8080") { UseShellExecute = true });
          Console.WriteLine("Inventario disponible en http://localhost:8080");
        } else {
          Console.WriteLine("La aplicacion no respondio o se detuvo. Revise data/logs.");
        }
      }
    } catch (Exception ex) {
      Console.WriteLine("No se pudo iniciar: " + ex.Message);
      Console.WriteLine("Verifique que Java 17 (JRE) este instalado.");
    }
    Console.WriteLine();
    Console.WriteLine("Presione una tecla para cerrar...");
    try { Console.ReadKey(); } catch { }
    return 0;
  }
}
'@

  $exeDetener = Compilar-Exe "Detener-JAR.exe" @'
using System;
using System.Management;
using System.Diagnostics;

class DetenerJAR {
  static int Main() {
    Console.WriteLine("Deteniendo Inventario...");
    int muertos = 0;
    try {
      using (var searcher = new ManagementObjectSearcher(
        "SELECT ProcessId FROM Win32_Process WHERE Name='java.exe' AND CommandLine LIKE '%app.jar%'")) {
        foreach (var obj in searcher.Get()) {
          int pid = Convert.ToInt32(obj["ProcessId"]);
          try { Process.GetProcessById(pid).Kill(); muertos++; } catch { }
        }
      }
    } catch (Exception ex) {
      Console.WriteLine("No se pudo detener: " + ex.Message);
    }
    Console.WriteLine(muertos > 0
      ? "Inventario detenido correctamente. Ya puede apagar la maquina."
      : "Inventario no estaba en ejecucion.");
    Console.WriteLine();
    Console.WriteLine("Presione una tecla para cerrar...");
    try { Console.ReadKey(); } catch { }
    return 0;
  }
}
'@ -Refs "/r:System.Management.dll"

  $escritorio = [Environment]::GetFolderPath('Desktop')
  New-Accesso "Inventario - Iniciar" $exeIniciar "" "$env:SystemRoot\System32\shell32.dll,1" $escritorio
  New-Accesso "Inventario - Detener" $exeDetener "" "$env:SystemRoot\System32\shell32.dll,27" $escritorio

  if ($AutoInicio) {
    $inicio = [Environment]::GetFolderPath('Startup')
    $lnk = $shell.CreateShortcut((Join-Path $inicio "Inventario-Iniciar.lnk"))
    $lnk.TargetPath = $exeIniciar
    $lnk.WorkingDirectory = $root
    $lnk.IconLocation = "$env:SystemRoot\System32\shell32.dll,1"
    $lnk.Description = "Inventario - Iniciar (JAR)"
    $lnk.Save()
    Write-Host "Auto-inicio configurado: la app arranca sola con Windows (modo JAR)." -ForegroundColor Green
  }

  Write-Host ""
  Write-Host "Requisito: JRE 17 instalado en el equipo (https://adoptium.net)." -ForegroundColor Yellow
}