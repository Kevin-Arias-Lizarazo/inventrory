# HANDOFF — Proyecto Inventario (2026-08-26)

> Este documento consolida TODO el contexto acumulado: decisiones de arquitectura,
> convenciones, estado del repo, funcionalidades implementadas, lo que falta por
> hacer, y lo que quedó sin commitear. Sirve como punto de partida para quien
> continúe el proyecto.

---

## 1. Estado del repo

- **Repo**: `github.com/Ktein-Arias-Lizarazo/inventrory.git`
- **Branch**: `main` (up to date with `origin/main`)
- **HEAD**: `c3e3c3f` (docs: guia paso a paso para configurar la maquina del cliente)
- **Últimos 20 commits** = cierre del SDD 'rediseno-ui' + escaneo-lotes + despliegue Windows

### 1.1 Cambios SIN commitear (al momento del handoff)

**Modificados (staged o sin stage):**
| Archivo | Qué cambió |
|---------|-----------|
| `AGENTS.md` | +2 líneas: referencia al ROADMAP.md para sesiones futuras |
| `docker-compose.prod.yml` | `restart: unless-stopped` → `restart: "no"` (deshabilitado auto-restart en dev) |
| `.atl/skill-registry.md` | Última actualización 2026-08-25, fuente `.copilot/skills` agregada |
| `.atl/.skill-registry.cache.json` | Fingerprint actualizado |

**Sin trackear:**
| Archivo | Qué es |
|---------|--------|
| `ROADMAP.md` | Roadmap completo de funcionalidades futuras (Fases A–D) |
| `idea_usuario.txt` | Transcript de la conversación original donde el dueño plantea el modelo de datos |

**Recomendación**: commitear TODO antes de archivar el proyecto.

---

## 2. Arquitectura

### 2.1 Stack
- **Backend**: Java 17 + Spring Boot + SQLite (`inventario.db`)
- **Frontend**: React 18 + Vite (dev en puerto 5173 con proxy a 8080)
- **Despliegue prod**: Docker multi-stage (node:22 → gradle:jdk17 → eclipse-temurin:17-jre-alpine), un solo contenedor en `127.0.0.1:8080`
- **Persistencia**: SQLite con `ddl-auto: update`; volumen Docker `/data` (db, uploads, sesiones, logs, `.env.auth`)
- **Memoria**: Engram usa project key `inventrory` (NO `inventario`)

### 2.2 Patrón hexagonal
```
dominio (POJO) → puerto/entrada (CasoDeUso) → aplicacion (@Service)
    → puerto/salida (interfaz Persistencia) → persistencia/ (entidad, consulta, adaptador, Mapeador)
        → controlador → CambiosNotificador.publicar(recurso) (SSE)
```

### 2.3 Convención API (decisión acordada con el dueño)
1. **GET** → query params (NUNCA body)
2. **Escritura** (POST/PUT/PATCH/DELETE) → body JSON (`application/json`)
3. **Excepciones técnicas legítimas** (no endpoints):
   - `multipart/form-data`: subida de archivos (`/api/archivos`, `/api/instalacion/completar`, `/api/backup/restaurar`, `/api/backup/restaurar-uploads`)
   - `application/octet-stream`: descargas (`.db`, `.zip`, PDFs)
   - `text/event-stream`: SSE (`/api/cambios/suscripcion`)
4. **Pendiente de decisión**: `POST /api/importar/{recurso}` envía CSV como String crudo; el estándar pide envolver en `{csv: "..."}` con DTO. Funciona actualmente pero es "rareza de diseño".

### 2.4 Autenticación y seguridad
- **Niveles**: ROOT, ADMIN, USUARIO, LECTOR (tabla `niveles_acceso`, sin enums)
- **ROOT**: no inicia sesión (login lo rechaza); su contraseña solo se define en instalación
- **Tokens**: opacos (32 chars, SHA-256 en `sesiones.db`), access 10 min, refresh cookie HttpOnly 12 h
- **CSRF**: token XOR de Spring Security 6 (header `X-XSRF-TOKEN` = token del body de `GET /api/auth/csrf`)
- **Bloqueos**: 10 intentos fallidos en 5 min → login bloqueado 5 min
- **2 endpoints de contraseña INTENCIONALES**:
  - `POST /api/auth/cambiar-contrasena` — self-service (con clave actual, principal autenticado)
  - `POST /api/auth/cambiar-contrasena-usuario` — dedicado a terceros (ADMIN cambia USUARIO/LECTOR; ROOT solo al admin)
  - Cambiar clave de ROOT → `400 "No es posible cambiar la contraseña de root"`
- **Backup**: regla genérica `SeguridadConfig` → `/api/backup/**` → `hasAnyRole("ADMIN","ROOT")`

---

## 3. Funcionalidades implementadas

### 3.1 SDD 'rediseno-ui' (9 commits, completado)
- Filtros server-side en `/api/{facturas,compras,ordenes-compra}/paginado` (filtros SQL con Spring Data JPA)
- Filtros compartidos: hook `useListaPaginada` + componente `FilterBar`
- Navegación compacta sin doble tab de códigos
- Backup exportar-completo ZIP (DB + uploads) y restaurar ZIP
- Fix de `estadoPago` en 3 capas (controlador + caso de uso + aplicación)

### 3.2 SDD 'escaneo-lotes' (4 commits, completado)
- Flujo entity-first de escaneo QR: asignaciones/devoluciones por lote sin teclear
- API de lotes con asignación/devolución FIFO
- Express = mini-formularios que completa el operador (NADA automático)
- UI rediseñada entity-first con buffer localStorage y mini-formularios
- Cantidad firmada en asignaciones y decimales en consumibles (BigDecimal)

### 3.3 Despliegue Windows 11
- Kit completo: imagen Docker pre-construida (`export-imagen.ps1` → `docker save`)
- `load-y-levanta.ps1`: `docker load` + `up --no-build` + verificación de readiness
- `scripts/stop.ps1`: `down` conservando volumen
- `scripts/install.sh` / `start.sh` / `stop.sh` para Linux
- `verify-deploy.ps1`: condicional (si pendiente=true → suite completa; si no → omite destructivos)
- `run-tests.ps1`: runner autónomo que levanta/detiene backend con BD temporal
- Detener-Inventario.exe: compilado localmente con `csc.exe` (C# 4.8, sin instalar nada), doble clic del usuario final
- Accesos directos: "Inventario - Iniciar" (.bat) + "Inventario - Detener" (.exe) + Docker Desktop en auto-inicio
- `DEPLOY-WINDOWS.md`: guía completa de configuración

### 3.4 Backup
- GET `/api/backup` → descarga `.db`
- POST `/api/backup/exportar-uploads` → ZIP de imágenes (sin prefijo)
- POST `/api/backup/restaurar` → restaura `.db`
- POST `/api/backup/restaurar-uploads` → extrae ZIP a uploads/, protege zip-slip
- Todos requieren ADMIN o ROOT

### 3.5 Otros
- Runner autónomo de regresión (`run-tests.ps1`): auth 21 OK, api 210 OK, react OK, sse OK
- `verify-deploy.ps1` corregido para re-despliegues (no asume instalación fresca)
- Frontend: secciones compactas, navegación por tabs, filtros con debounce
- Importación CSV de proveedores, materiales, consumibles, EPP
- Reportes PDF (OpenPDF): inventario, facturas, valor-inventario, alertas-reposicion
- Búsqueda global: `/api/buscar?q=...`
- Sistema de códigos QR para escaneo

---

## 4. Cosas que hay que saber

### 4.1 Gotchas y lecciones
- **Engram registra el proyecto como `inventrory`** (no `inventario`)
- **`GET /api/movimientos-*` NO son endpoints muertos**: el frontend los usa vía SSE (Alertas.jsx, Consumibles.jsx) y test-sse.mjs — NO eliminarlos
- **`test-auth.mjs` exige instalación pendiente**: corre siempre primero con BD fresca y crea admin/AdminTest2026
- **ROOT no inicia sesión** → el actor real de backup es ADMIN; la regla ROOT es defensiva
- **Facturas alineadas a `estadoPago`** (fix en 3 capas) — no reintroducir el param `estado`
- **Docker Desktop puede no arrancar** → el runner usa `gradle bootRun` local con BD temporal (no toca `backend/inventario.db` real)
- **Las entidades guardan `fotoUrl`/`firmaUrl`** como URL relativa `/archivos/<uuid>.<ext>`; el archivo físico vive en `uploads/`
- **Editores**: al agregar métodos a controladores Java, verificar estructura de llaves antes de compilar (lección del apply)

### 4.2 Convenciones del dueño
- **Código y comentarios en INGLÉS**; nombres de recursos/entidades en español
- **NO tocar endpoints de contraseña ni Fase A (prestaciones)** salvo luz verde explícita
- **Solo frontend y códigos** como regla de alcance del dueño (a partir de cierto punto)
- **Preferencia**: explicaciones breves, límites de tiempo visibles en comandos largos
- **Expres = mini-formularios**: nunca crear/incrementar automáticamente sin intervención del operador

---

## 5. Roadmap: funcionalidades pendientes

> **Estas funcionalidades NUNCA se implementaron.** Están diseñadas y aprobadas
> pero no hay código. El ROADMAP.md es la fuente definitiva.

### Fase A — Prestaciones y tipos de contrato (APROBADO, luz verde)
- Catálogos configurables: `tipos_contrato`, `prestaciones`, `tipo_contrato_prestacion` (matriz), `parametros_legales`
- Contrato ampliado: tipo + remuneración + fase aprendizaje
- **Snapshot persistido**: `contrato_prestacion_calculada` (valores al momento del cálculo; cambiar parámetros NO altera snapshots existentes)
- Extras por contrato: viáticos eventuales, primas recurrentes
- Cálculo: `POST /api/contratos/{id}/calcular-prestaciones`
- Lógica por categoría: LABORAL → todo; PRESTACION_SERVICIOS → solo seguridad social; APRENDIZAJE → salud + ARL
- Frontend: formulario en `Contratos.jsx` con panel de desglose

### Fase A.5 — Listados estándar (antes de Fase B)
- Endpoint `/paginado` para TODOS los recursos con tabla (incluidos historiales)
- Filtros SQL con Spring Data JPA, nunca en memoria
- Ampliar `useListaPaginada` para aceptar filtros + orden con debounce

### Fase B — Archivo base de siembra
- Seed script idempotente que inicialice toda la información base de una sola operación
- Catálogos legales, datos de ejemplo, sin duplicar

### Fase C — Detalle de proyecto con costo
- Ruta `GET /proyectos/{id}` con empleados asignados, consumibles, materiales, tiempo, estado
- **Requiere**: vínculo materiales→proyecto (HOY NO EXISTE)
- Costo = mano de obra (prestaciones) + consumibles + materiales

### Fase D — Conversión de modales a páginas (opcional)
- Convertir formularios crear/editar de modal a páginas con ruta

---

## 6. Estructura clave de archivos

### Backend (hexagonal)
```
backend/src/main/java/com/art/inventario/
├── dominio/          # POJOs
├── puerto/
│   ├── entrada/      # CasoDeUso (interfaces de uso)
│   └── salida/       # Persistencia (interfaces de persistencia)
├── aplicacion/       # @Service (implementa casos de uso)
├── persistencia/
│   ├── entidad/      # @Entity JPA
│   ├── consulta/     # JpaRepository
│   ├── adaptador/    # @Repository (implementa puertos)
│   └── Mapeador.java
├── controlador/      # REST endpoints
├── configuracion/    # SeguridadConfig, SesionDataConfig, WebConfig, SpaForwardFiltro
└── segundoplano/     # CambiosNotificador (SSE)
```

### Frontend
```
frontend/src/
├── auth/             # token.js, auth-contexto.js, AuthContext.jsx
├── components/       # Layout.jsx, SeccionTabs.jsx, ui.jsx (FilterBar)
├── pages/            # Empleados, Inventario, Proyectos, Compras, Administración, etc.
│   ├── Escaneo.jsx   # Sistema de escaneo QR entity-first
│   ├── Mantenimiento.jsx # Backup/restauración/importación
│   ├── Busqueda.jsx  # Búsqueda global
│   ├── Instalacion.jsx # Asistente de instalación
│   ├── Login.jsx     # Login + recuperar contraseña
│   └── Usuarios.jsx, Auditoria.jsx, MiCuenta.jsx
├── hooks.js          # useListaPaginada (filtros, debounce, paginación)
├── api.js            # Cliente HTTP (Bearer, CSRF, renovación en 401)
├── App.jsx           # Router (5 secciones + Home)
└── secciones.js      # Submenús por ruta
```

### Scripts
```
scripts/
├── run-tests.ps1     # Runner autónomo (levanta/detiene backend + 4 suites)
├── test-api.mjs      # Suite API (210 assertions)
├── test-react-api.mjs # Suite React
├── test-sse.mjs      # Suite SSE
├── test-auth.mjs     # Suite auth (requiere instalación fresca)
├── verify-deploy.ps1 # Verificación completa de despliegue
├── build.ps1 / deploy.ps1 # Build + deploy prod
├── install.sh / start.sh / stop.sh # Linux
├── export-imagen.ps1 # docker save de imagen productiva
├── load-y-levanta.ps1 # docker load + up en destino
├── stop.ps1          # docker down conservando volumen
├── Instalar-Accesos.ps1 # Crea accesos directos + .exe
├── auth-lib.mjs      # Helper de auth para tests
└── test-api.mjs      # Suite de regresión API completa
```

### Docker
```
docker-compose.yml        # Dev: inventario (8080) + frontend (5173)
docker-compose.dev.yml    # Override hot-reload (monta código fuente)
docker-compose.prod.yml   # Prod: 1 contenedor multi-stage (8080)
Dockerfile.prod           # node:22 → gradle:jdk17 → eclipse-temurin:17-jre-alpine
```

---

## 7. Contexto de la conversación original (idea_usuario.txt)

El dueño planteó inicialmente un modelo de datos relacional con:
- Empleados, contratos, tipos de contrato, prestaciones
- Proyectos con cliente/ubicación
- Consumibles, materiales, EPP, dotación
- Asistencia/minuta por empleado
- Períodos de contratación, costos por empleado, pagos, seguros
- **snap_dato**: snapshots por vista que se actualizan indirectamente (no directamente) al cambiar datos fuente

Este modelo conceptual guió todo el diseño del sistema.

---

## 8. Pendientes operativos

1. **Commitear los 4 archivos modificados + 2 sin trackear** (ver §1.1)
2. **Push a origin/main** (todo está local)
3. **No hay PRs abiertos** — todo quedó en main local
4. **Fases A–D del ROADMAP** quedan pendientes para quien continúe
5. **Decisión pendiente**: envolver `POST /api/importar/{recurso}` en JSON `{csv: "..."}`

---

*Documento generado el 2026-08-26 como parte del cierre del proyecto.*
