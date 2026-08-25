@echo off
setlocal
title Inventario - Detener
cd /d "%~dp0.."

REM ============================================================
REM  Inventario - Detener (doble clic)
REM  Detiene el contenedor de forma limpia ANTES de apagar la
REM  maquina. Los datos del volumen se conservan.
REM ============================================================

echo Deteniendo el contenedor de forma limpia...
docker compose -f docker-compose.prod.yml down
if errorlevel 1 (
  echo.
  echo No se pudo detener. Docker puede no estar corriendo.
  echo Si Docker no esta activo, puede apagar la maquina igualmente.
) else (
  echo.
  echo Contenedor detenido correctamente. Los datos se conservaron.
  echo Ya puede apagar la maquina sin riesgo.
)
timeout /t 4 /nobreak >nul
exit /b 0