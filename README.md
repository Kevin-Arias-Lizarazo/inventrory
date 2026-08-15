# Inventario

Sistema de gestión de inventario con **Spring Boot** (backend) + **React/Vite** (frontend).

## Levantar la aplicación usando solo Docker

No necesitas instalar Java, Gradle ni Node localmente: todo se compila y ejecuta dentro de contenedores.

### Requisitos

- Docker con **Docker Compose** (v2). En Windows/Mac viene incluido con Docker Desktop.

### Primer arranque

```bash
docker compose up --build
```

Esto hace dos cosas:

1. **Compila dentro del contenedor** el jar del backend (Gradle + JDK 17 en un stage intermedio) y levanta la API.
2. Levanta el **servidor de desarrollo de React** (Node 22 + Vite) que sirve la app con hot-reload.

Cuando termine, abre:

| Servicio | URL |
| --- | --- |
| Frontend (React dev server) | http://localhost:5173 |
| API / backend | http://localhost:8080 |

> El frontend (5173) hace proxy de `/api` y `/archivos` hacia el contenedor `inventario` (8080), así que en el navegador solo usas `http://localhost:5173`.

### Vuelta a levantar / parar

```bash
docker compose up          # levanta sin reconstruir (usa la última imagen)
docker compose down        # detiene los contenedores
docker compose down -v     # detiene y BORRA la base de datos y archivos subidos
```

### Datos y persistencia

- La base de datos (SQLite) y los archivos subidos (fotos/firmas) viven en el **volumen** `inventario-data` (`/data` dentro del contenedor `inventario`).
- Son persistentes entre reinicios: `docker compose down` **no** los borra. Solo `docker compose down -v` los elimina.

### Recompilar después de cambios en el código

```bash
docker compose up --build
```

Esto reconstruye las imágenes (recompila el jar y reinstala dependencias del frontend). El frontend, además, recarga en caliente los cambios de `frontend/src` gracias al dev server de Vite; para verlos en el navegador solo recarga la página.

### Ver logs

```bash
docker compose logs -f backend      # logs de la API
docker compose logs -f frontend     # logs del dev server de React
```

> Nota: los servicios se llaman `inventario` y `frontend` internamente; si prefieres usar esos nombres en `docker compose logs`, sirve cualquiera de los dos (`docker compose ps` los muestra).

### Estructura de contenedores

```text
┌───────────────┐    /api, /archivos (proxy Vite)    ┌───────────────┐
│   frontend    │ ─────────────────────────────────▶ │  inventario   │
│ Node 22 + Vite│        http://inventario:8080      │ Spring Boot   │
│  :5173        │                                    │  :8080        │
└───────────────┘                                    └───────┬───────┘
                                                            │ SQLite + uploads
                                                     ┌──────┴───────┐
                                                     │  volumen     │
                                                     │ inventario-data │
                                                     └──────────────┘
```

### Detalles de la configuración

- **`backend/Dockerfile`**: build de 2 etapas — en la primera compila el jar con la imagen `gradle:jdk17`; en la segunda copia el jar a `eclipse-temurin:17-jre-alpine` y lo ejecuta.
- **`frontend/Dockerfile`**: imagen `node:22-alpine`, instala dependencias con `npm ci` y arranca `vite` en modo desarrollo con `--host 0.0.0.0`.
- **`docker-compose.yml`**: define los servicios `inventario` y `frontend`, la red interna, el proxy del backend (`VITE_BACKEND_HOST=http://inventario:8080`) y el volumen de datos.
- **`frontend/vite.config.js`**: el proxy usa `VITE_BACKEND_HOST` si está definida; si no, cae a `http://localhost:8080` (modo desarrollo local sin Docker).

### Uso en producción (opcional)

Este setup levanta el **dev server** de React (ideal para desarrollo, con hot-reload). Para producción puedes eliminar el servicio `frontend`, compilar el frontend (`npm run build`, que genera el estático en `backend/src/main/resources/static`) y dejar que Spring sirva la SPA desde `http://localhost:8080`.