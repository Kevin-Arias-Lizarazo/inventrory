@echo off
setlocal
title Inventario - Iniciar
cd /d "%~dp0.."

REM ============================================================
REM  Inventario - Iniciar (doble clic)
REM  Inicia Docker Desktop si hace falta, levanta el contenedor
REM  y abre la aplicacion en el navegador.
REM ============================================================

docker info >nul 2>&1
if not errorlevel 1 goto docker_ok

echo Iniciando Docker Desktop...
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"

set /a intentos=0
:wait_docker
timeout /t 3 /nobreak >nul
docker info >nul 2>&1
if not errorlevel 1 goto docker_ok
set /a intentos+=1
if %intentos% lss 40 goto wait_docker

echo.
echo Docker no respondio. Abra Docker Desktop manualmente e intente de nuevo.
pause
exit /b 1

:docker_ok
echo Docker listo.
echo Levantando el contenedor...
docker compose -f docker-compose.prod.yml up -d --no-build
if errorlevel 1 (
  echo.
  echo Fallo al levantar el contenedor.
  echo Si es la primera instalacion, ejecute:  scripts\deploy.ps1  (compila la imagen).
  pause
  exit /b 1
)

echo Esperando a que la aplicacion responda...
set /a intentos=0
:wait_app
timeout /t 3 /nobreak >nul
powershell -NoProfile -Command "try { $r = Invoke-WebRequest -UseBasicParsing 'http://localhost:8080/api/instalacion/estado' -TimeoutSec 2; if ($r.StatusCode -eq 200) { exit 0 } } catch { exit 1 }" >nul 2>&1
if not errorlevel 1 goto app_ok
set /a intentos+=1
if %intentos% lss 40 goto wait_app

echo La aplicacion no respondio. Revise:  docker compose -f docker-compose.prod.yml logs inventario
pause
exit /b 1

:app_ok
start "" "http://localhost:8080"
echo Inventario disponible en http://localhost:8080
timeout /t 2 /nobreak >nul
exit /b 0