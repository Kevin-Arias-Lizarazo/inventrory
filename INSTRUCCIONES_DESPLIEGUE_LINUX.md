# Despliegue en Linux y Windows

Este proyecto se despliega con Docker. Linux no utiliza archivos `.exe`; los comandos se ejecutan mediante scripts `.sh`.

## Requisitos

- Docker Engine.
- Docker Compose v2.
- `curl`.
- `xdg-open` o `sensible-browser` para abrir el navegador automáticamente.

Verificar la instalación:

```bash
docker version
docker compose version
```

`docker version` debe mostrar las secciones `Client` y `Server`.

## Instalación Inicial

Desde la raíz del proyecto:

```bash
chmod +x scripts/install.sh scripts/start.sh scripts/stop.sh
./scripts/install.sh
```

El instalador:

1. Verifica Docker y Compose.
2. Comprueba si el usuario puede acceder al daemon Docker.
3. Solicita permisos con `sudo` si son necesarios.
4. Construye React con Node 22.
5. Incorpora React al JAR de Spring Boot.
6. Compila Java con Gradle y JDK 17.
7. Construye la imagen productiva.
8. Crea y levanta el contenedor.

La aplicación queda en:

```text
http://localhost:8080
```

Solo es accesible desde el mismo equipo (el puerto está atado a `127.0.0.1`).

## Primera ejecución (asistente de instalación)

En el primer arranque no existe la cuenta `root`, así que la app abre un **asistente de instalación** en el navegador:

1. Define la **contraseña de root** (mínimo 8 caracteres) y su confirmación.
2. Opcionalmente importa una **base SQLite** (`.db`) y/o una **carpeta de uploads** (`.zip`).
3. Si importa datos, debe escribir `SOBRESCRIBIR` para confirmar el reemplazo.
4. Se crea `root` y, si no existe, el `admin` con la misma contraseña.
5. Se genera y **muestra una sola vez** el **secreto de recuperación** (guardado en `/data/.env.auth`). Guárdelo: solo sirve para restablecer la contraseña del `admin` desde la pantalla de inicio de sesión.

Datos creados en el volumen `/data`:

```text
/data/inventario.db        BD operativa
/data/uploads/             archivos subidos
/data/sesiones/sesiones.db sesiones y tokens (temporal)
/data/logs/                logs JSONL por día (log_dd_MM_yyyy.jsonl y log_get_...)
/data/.env.auth            secreto raíz de recuperación
```

## Inicio Normal

Para iniciar sin reconstruir imágenes:

```bash
./scripts/start.sh
```

El script espera a que la API responda y abre el navegador. Si ya está activo, no recompila.

## Detener

```bash
./scripts/stop.sh
```

Este comando detiene el contenedor y conserva la base de datos y los archivos subidos.

No ejecutar:

```bash
docker compose down -v
```

porque elimina el volumen `inventario-data`.

## Comandos Manuales Equivalentes

Si los scripts no existen o fallan, ejecutar directamente:

```bash
docker compose -f docker-compose.prod.yml build
docker compose -f docker-compose.prod.yml up -d
docker compose -f docker-compose.prod.yml ps
```

Abrir manualmente:

```text
http://localhost:8080
```

Para detener:

```bash
docker compose -f docker-compose.prod.yml down
```

## Recrear Los Scripts

Si los archivos se borraron o quedaron dañados, crearlos desde la raíz:

### `scripts/install.sh`

```bash
mkdir -p scripts
nano scripts/install.sh
```

Contenido mínimo:

```bash
#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! docker info >/dev/null 2>&1; then
  sudo -v
  DOCKER=(sudo docker compose)
else
  DOCKER=(docker compose)
fi

"${DOCKER[@]}" -f docker-compose.prod.yml up --build -d
"${DOCKER[@]}" -f docker-compose.prod.yml ps
```

### `scripts/start.sh`

```bash
nano scripts/start.sh
```

Contenido mínimo:

```bash
#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! docker info >/dev/null 2>&1; then
  sudo -v
  DOCKER=(sudo docker compose)
else
  DOCKER=(docker compose)
fi

"${DOCKER[@]}" -f docker-compose.prod.yml up -d

for _ in $(seq 1 30); do
  if curl --silent --fail --max-time 2 http://localhost:8080/api/empleados >/dev/null; then
    command -v xdg-open >/dev/null 2>&1 && xdg-open http://localhost:8080 >/dev/null 2>&1 &
    echo "Aplicación disponible en http://localhost:8080"
    exit 0
  fi
  sleep 2
done

echo "El contenedor inició, pero la API aún no responde."
exit 1
```

### `scripts/stop.sh`

```bash
nano scripts/stop.sh
```

Contenido mínimo:

```bash
#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"
docker compose -f docker-compose.prod.yml down
```

Después de crear o corregirlos:

```bash
chmod +x scripts/install.sh scripts/start.sh scripts/stop.sh
bash -n scripts/install.sh
bash -n scripts/start.sh
bash -n scripts/stop.sh
```

## Permisos Docker

La opción recomendada es agregar el usuario al grupo `docker`:

```bash
sudo usermod -aG docker "$USER"
```

Cerrar sesión y volver a entrar después. Mientras tanto, usar `sudo docker ...`.

Comprobar acceso:

```bash
docker info
```

## Diagnóstico

Ver estado de contenedores:

```bash
docker compose -f docker-compose.prod.yml ps
```

Ver logs del backend:

```bash
docker compose -f docker-compose.prod.yml logs -f inventario
```

Ver imágenes:

```bash
docker images
```

Comprobar API:

```bash
curl http://localhost:8080/api/empleados
```

Si Docker devuelve `HTTP 500`, `_ping` o `engine is not running`, el problema está en Docker y no en el proyecto. Reiniciar Docker Engine/Desktop y volver a comprobar:

```bash
docker version
docker info
```

No reconstruir ni borrar volúmenes hasta que `docker info` funcione correctamente.

## Entorno De Desarrollo

Para desarrollo con Vite y hot reload:

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

Abrir:

```text
http://localhost:5173
```

Para volver a producción:

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml down
./scripts/install.sh
```

## Despliegue En Windows

En Windows los scripts de automatización son `.ps1` y Docker Desktop debe estar iniciado.

Desde PowerShell, ubicado en la raíz del proyecto:

### Instalación Inicial

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\install.ps1
```

Este comando construye React, compila Java dentro de Docker, crea la imagen productiva y levanta el contenedor.

### Inicio Normal

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\start.ps1
```

El script ejecuta `docker compose up -d`, espera el endpoint:

```text
http://localhost:8080/api/empleados
```

Cuando la API responde, abre automáticamente:

```text
http://localhost:8080
```

### Detener

```powershell
.\scripts\stop.ps1
```

### Comandos Manuales

Si los scripts `.ps1` no existen o fallan:

```powershell
docker compose -f docker-compose.prod.yml up --build -d
docker compose -f docker-compose.prod.yml ps
Start-Process http://localhost:8080
```

Para detener sin borrar datos:

```powershell
docker compose -f docker-compose.prod.yml down
```

## Verificación Completa

Para comprobar que React, Java, Docker, API, frontend, SSE y persistencia quedaron montados:

En Linux:

```bash
chmod +x scripts/verify-deploy.sh
./scripts/verify-deploy.sh
```

En Windows PowerShell:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\verify-deploy.ps1
```

La verificación:

1. Valida `docker-compose.prod.yml`.
2. Construye React dentro de Node 22.
3. Compila Java y genera el JAR dentro de Gradle/JDK 17.
4. Levanta el contenedor productivo.
5. Comprueba `/` y `/api/empleados`.
6. Ejecuta `test-api.mjs`, `test-react-api.mjs` y `test-sse.mjs` dentro de Node 22 en la red Docker.
7. Muestra el estado final de los contenedores.

El script no ejecuta `down -v` y conserva `inventario-data`.

### Recrear Los Scripts De Windows

Crear la carpeta y los archivos si fueron eliminados:

```powershell
Test-Path .\scripts
New-Item -ItemType File .\scripts\install.ps1
New-Item -ItemType File .\scripts\start.ps1
New-Item -ItemType File .\scripts\stop.ps1
notepad .\scripts\install.ps1
```

Contenido mínimo de `install.ps1`:

```powershell
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
docker compose -f docker-compose.prod.yml up --build -d
if ($LASTEXITCODE -ne 0) { throw 'Falló Docker.' }
docker compose -f docker-compose.prod.yml ps
```

Contenido mínimo de `start.ps1`:

```powershell
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$url = 'http://localhost:8080'
docker compose -f docker-compose.prod.yml up -d
if ($LASTEXITCODE -ne 0) { throw 'No se pudieron iniciar los contenedores.' }
for ($i = 0; $i -lt 30; $i++) {
  try {
    $r = Invoke-WebRequest -UseBasicParsing "$url/api/empleados" -TimeoutSec 2
    if ($r.StatusCode -eq 200) { Start-Process $url; exit 0 }
  } catch { Start-Sleep -Seconds 2 }
}
throw "La API no responde en $url"
```

Contenido mínimo de `stop.ps1`:

```powershell
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
docker compose -f docker-compose.prod.yml down
```

Si PowerShell bloquea los scripts, usar solo para la sesión actual:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
```

No usar `docker compose down -v`, porque elimina `inventario-data`.
