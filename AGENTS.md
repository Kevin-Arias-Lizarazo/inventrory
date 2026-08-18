# Instrucciones del proyecto

Inventario: SQLite + Spring Boot (backend) + React/Vite (frontend).

## Levantar desde Docker (vía preferida)

La app corre en contenedores definidos en `docker-compose.yml`:

- `inventario` → backend Spring Boot, puerto **8080** (`java -jar app.jar`).
- `frontend` → Vite dev (Node 22), puerto **5173** con proxy hacia `VITE_BACKEND_HOST` (por defecto `http://localhost:8080`).

Para construir las imágenes con el código actual y levantarlas:

```powershell
docker compose up --build -d
```

- App web (Vite): http://localhost:5173
- API backend: http://localhost:8080 (ej. `GET /api/empleados`)
- El proxy del frontend permite llamar `/api/...` desde el navegador.

Tras cambios en backend o frontend, volver a reconstruir:

```powershell
docker compose up --build -d
```

Detener:

```powershell
docker compose down
```

### Modo desarrollo (hot reload desde Docker)

Para iterar en código sin reconstruir imágenes, usar el override `docker-compose.dev.yml`:

```powershell
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

- **Frontend**: monta `./frontend` en el contenedor y corre Vite dev → HMR instantáneo en http://localhost:5173 al guardar.
- **Backend**: usa la imagen `gradle:jdk17` con `gradle bootRun --no-daemon` montando `./backend`. El comando anula el `org.gradle.java.home` local (ruta Windows) vía `-Dorg.gradle.java.home=/opt/java/openjdk`.
- Tras cambios de Java, reiniciar el contenedor del backend (recompila y arranca en ~45 s, sin rebuild de imagen):

```powershell
docker compose -f docker-compose.yml -f docker-compose.dev.yml restart inventario
```

- El caché de Gradle persiste en el volumen `gradle-home`; la BD en `inventario-data` (independiente de `backend/inventario.db`).

Para volver al modo "build empaquetado" (jar):

```powershell
docker compose down
docker compose up --build -d
```

## Despliegue productivo

El despliegue productivo compila React dentro de una etapa Node 22, copia el estático al JAR de Spring Boot y ejecuta un único contenedor en `8080`:

```powershell
.\scripts\build.ps1
.\scripts\deploy.ps1
```

Equivalente directo:

```powershell
docker compose -f docker-compose.prod.yml up --build -d
```

En Linux usar los scripts ejecutables:

```bash
chmod +x scripts/install.sh scripts/start.sh scripts/stop.sh
./scripts/install.sh
./scripts/start.sh
```

`install.sh` construye la imagen productiva y crea el contenedor; `start.sh` lo levanta sin reconstruir y abre el navegador cuando la API responde; `stop.sh` detiene el servicio sin borrar el volumen.

Verificación completa del despliegue:

```powershell
.\scripts\verify-deploy.ps1
```

En Linux:

```bash
chmod +x scripts/verify-deploy.sh
./scripts/verify-deploy.sh
```

La verificación construye React + Java, levanta Docker, comprueba `/` y `/api/empleados`, y ejecuta las pruebas API/React/SSE dentro de Node 22 en la red Docker.

No ejecutar `docker compose down -v` si se deben conservar la base de datos y los archivos subidos.

### Persistencia en Docker

- La base de datos del contenedor vive en el volumen del compose y es **independiente** de `backend/inventario.db` local (el volumen se crea vacío).
- Los archivos subidos van a `backend/uploads` (montado como volumen).
- `application.yaml` usa `ddl-auto: update`: las tablas nuevas se crean automáticamente al arrancar.

## Verificación / build local

Backend (requiere JDK 17):

```powershell
cd backend
.\gradlew.bat compileJava
.\gradlew.bat bootJar
```

Frontend (requiere Node via fnm):

```powershell
cd frontend
fnm env --use-on-cd | Out-String | Invoke-Expression
npm run lint
npm run build   # copia el dist a backend/src/main/resources/static (ignorado por git)
```

Pruebas de la API (con el backend corriendo en 8080):

```powershell
fnm env --use-on-cd | Out-String | Invoke-Expression
node scripts\test-api.mjs
node scripts\test-react-api.mjs
node scripts\test-sse.mjs
```

> Ojo: para correr el backend local hay que liberar el puerto 8080 primero (p. ej. `docker compose down`).

## Arquitectura del backend

Hexagonal: `dominio` (POJO) → `puerto/entrada` (CasoDeUso) → `aplicacion` (@Service) → `puerto/salida` (interfaz Persistencia) → `persistencia/` (`entidad` @Entity, `consulta` JpaRepository, `adaptador` @Repository, `Mapeador`) → `controlador` → `CambiosNotificador.publicar(recurso)` (SSE).

Reglas:
- NO modificar endpoints existentes; funcionalidad nueva en endpoints nuevos.
- Paginación: `{contenido,pagina,tamano,total,totalPaginas}`, `tamano` máx 100.
- Los movimientos de stock de un producto viven en su propia persistencia (patrón `Material`).
- Compra = entrada a stock sin precio (movimientos `"Compra #<id>"`); Factura fija `ultimoCosto` solo al facturar y lo recalcula al editar/eliminar.
- Pagos de factura: `POST /api/facturas/{id}/pagos`, `DELETE /api/pagos-factura/{id}`; saldo = total − pagos; un pago no puede exceder el saldo. Factura expone `totalPagado`, `saldo`, `estadoPago` (PENDIENTE/PARCIAL/PAGADA).
- Órdenes de compra simples: `CRUD /api/ordenes-compra` con líneas (`tipo`, `productoId` o `descripcion` para ropa, `cantidad`, `costoUnitario`), total calculado; **no** mueven stock.
- Ajustes de inventario: las líneas nuevas pueden enviar `cantidadDisponible` como objetivo; el backend calcula el ingreso/egreso necesario sin alterar unidades asignadas, dañadas o perdidas de herramientas. El formato anterior (`tipoMovimiento` + `cantidad`) sigue siendo válido.
- Herramientas: `POST /api/herramientas/{id}/reparar` devuelve una unidad dañada a disponible; `POST /api/herramientas/{id}/desechar-danada` la pasa de dañada a perdida y no vuelve a estar disponible; `POST /api/herramientas/{id}/perdida` marca una unidad disponible como perdida.
- Otros: `GET /api/dashboard?desde&hasta`, `GET /api/empleados/{id}/equipamiento`, `GET /api/backup` + `POST /api/backup/restaurar` (multipart, copia el SQLite en `backups/`), `POST /api/importar/{recurso}` (CSV: proveedores, materiales, consumibles, epp), reportes PDF `GET /api/reportes/{inventario,facturas,valor-inventario,alertas-reposicion}.pdf` (OpenPDF).
- El frontend del backup/restauración e importación vive en `frontend/src/pages/Mantenimiento.jsx`.
