# Roadmap / Tareas pendientes

Documento de trabajo: tareas aprobadas y pendientes para implementar en futuras sesiones.

---

## Fase A — Prestaciones y tipos de contrato (APROBADO, luz verde)

> **Decisión (2026-08-25):** los endpoints NUEVOS de la Fase A (contratos, prestaciones, tipos de contrato, parámetros) nacen con el **contrato estándar de listados** (paginación + búsqueda `q` + filtros por campo + ordenamiento `orden`/`dir` desde la API, nunca filtros/orden en el frontend). Ver Fase A.5 para el estándar completo.

Objetivo: normalizar tipos de contrato y calcular prestaciones/seguridad social según la ley colombiana, con desglose persistido como snapshot (cambiar un parámetro no afecta valores ya calculados).

### A.1 Catálogos configurables en BD (editables por admin, no rompen cálculos previos)
- **`tipos_contrato`**: `id, codigo, nombre, categoria` (LABORAL / PRESTACION_SERVICIOS / APRENDIZAJE).
  - Seed: `LABORAL_FIJO`, `LABORAL_INDEFINIDO`, `LABORAL_OBRA`, `LABORAL_OCASIONAL`, `PRESTACION_SERVICIOS`, `APRENDIZAJE`.
- **`prestaciones`**: `id, codigo, nombre, tipo` (PRESTACION / SEGURIDAD_SOCIAL / PARAFISCAL / OPCIONAL), `quienPaga` (EMPLEADOR / TRABAJADOR / CONTRATISTA), `porcentaje`, `valorBase`, `obligatoria`.
  - Seed: Prima 8.33%, Cesantías 8.33%, Intereses cesantías 1%, Vacaciones 4.17%, Dotación, Auxilio transporte, Salud 8.5%, Pensión 12%, ARL, Caja 4%, ICBF 3%, SENA 2%.
- **`tipo_contrato_prestacion`**: `tipo_contrato_id, prestacion_id, aplica, obligatoria, porcentaje(override)` — matriz de normalización por tipo.
- **`parametros_legales`**: `id, anio, smlmv, auxilioTransporte, arl, umbralAuxilioTransporteSmmlv, exencionParafiscalesTrabajadores`. Seed 2026.

### A.2 Contrato (base de remuneración)
- `contratos` + columnas: `tipo_contrato_id`, `remuneracionMensual`, `faseAprendizaje` (LECTIVA / PRACTICA).

### A.3 Resultado calculado (SNAPSHOT — requisito clave)
- **`contrato_prestacion_calculada`**: `id, contrato_id, concepto, tipo, quienPaga, base, porcentaje, valorMensual, valorAnual, obligatoria, fechaCalculo`.
  - Persiste los valores **al momento del cálculo**; cambiar un parámetro/catálogo después **no altera** estas filas.

### A.4 Extras por contrato
- **`contrato_prestacion_extra`**: `id, contrato_id, concepto, tipo` (RECURRENTE / EVENTUAL), `valor`, `fecha` (si EVENTUAL), `vigenciaDesde/vigenciaHasta` (si RECURRENTE), `observacion`.
  - Ej.: viáticos de un viaje (EVENTUAL, con fecha y valor), prima extralegal (RECURRENTE).

### A.5 Cálculo
- **Recalcular** (`POST /api/contratos/{id}/calcular-prestaciones`): regenera el snapshot con parámetros/catálogo/matriz actuales + remuneración + extras y lo persiste.
- Al crear/editar contrato (tipo o remuneración) se recalcula y persiste automáticamente.
- Cambiar un parámetro **no** toca snapshots existentes; solo afecta recálculos futuros.
- Lógica por categoría:
  - **LABORAL** → prestaciones sociales + seguridad social + parafiscales.
  - **PRESTACION_SERVICIOS** → solo seguridad social (100% a cargo del contratista, base mínima 40% del valor mensual). Sin prestaciones sociales ni parafiscales.
  - **APRENDIZAJE** → Salud (100% empresa) y ARL (solo fase práctica). Sin prima/cesantías/pensión. Base = auxilio de sostenimiento.

### A.6 API (hexagonal, endpoints nuevos)
- `GET /api/tipos-contrato`
- `GET /api/prestaciones`
- `GET /api/prestaciones/tipo/{id}` (matriz por tipo)
- `GET /api/parametros-legales` + `PUT` (admin)
- Contrato CRUD ampliado (tipo + remuneración + fase)
- `POST /api/contratos/{id}/prestaciones` (crear extra) / `DELETE /api/contratos/{id}/prestaciones/{extraId}` (quitar extra)
- `GET /api/contratos/{id}/prestaciones` → desglose (snapshot + extras) y total empleador
- `POST /api/contratos/{id}/calcular-prestaciones`

### A.7 Frontend (`Contratos.jsx`)
- Formulario: tipo de contrato (select catálogo), remuneración mensual, fase (si aprendizaje), añadir/editar prestaciones extra (viáticos con fecha y valor; primas recurrentes).
- Panel "Prestaciones": desglose calculado (snapshot) + extras + total.

### A.8 Migración / seguridad
- `ddl-auto:update` crea tablas nuevas; runner siembra catálogos y parámetros si faltan (sin romper instalaciones).
- Admin edita catálogos/parámetros; LECTOR solo ve; USUARIO/SUPERVISOR gestionan contratos.

### A.9 Verificación
- compileJava + bootJar, lint/build, suites auth/api/react/sse, Docker.
- Probar: cambiar un parámetro no altera snapshot previo; recálculo sí.

---

## Fase A.5 — Listados estándar de API: filtros + búsqueda + paginación + orden (antes de Fase B)

Objetivo: un contrato uniforme para TODAS las vistas de tabla (incluidos historiales: movimientos, asignaciones, minutas, entregas, auditoría), donde la API entrega la página ya filtrada/ordenada y el frontend no trae todo para filtrar en memoria.

### Contrato estándar
- `GET /api/{recurso}/paginado?pagina=0&tamano=50&q=...&{campo}={valor}&orden={campo}&dir=asc|desc`
- `q` = búsqueda libre por texto (nombre, documento, código, id…); filtros por campo (ej. `empleadoId`, `fechaDesde`/`fechaHasta`, `estado`, `devuelta`); `orden`/`dir` validados contra una lista blanca de campos ordenables.
- Respuesta: `{contenido,pagina,tamano,total,totalPaginas}` (ya existente). Los params son **opcionales y aditivos**: no rompen clientes que no los usan.
- Filtros y orden se aplican en SQL (Spring Data JPA con `PageRequest` + `Sort` + predicados), nunca en memoria.

### Recursos a cubrir (todos los que tienen tabla, incluido historial)
- Catálogos: consumibles, materiales, epp, herramientas, proveedores, códigos.
- RRHH/operación: empleados, contratos, minutas (asistencia — poder filtrar por empleado y rango de fechas), entregas EPP, entregas ropa, asignaciones herramientas, asignaciones consumibles.
- Historiales: movimientos de stock, asignaciones con devolución, auditoría (`/api/auditoria`).
- Nuevos recursos de Fase A: contratos, prestaciones, tipos de contrato (ya nacen con el estándar).

### Frontend
- Ampliar `useListaPaginada` para aceptar filtros + orden y re-consultar la API con debounce.
- Reemplazar anti-patrones: `DetalleHerramienta.jsx` (hoy trae TODAS las asignaciones y filtra en el navegador) → endpoint dedicado `GET /api/herramientas/{id}/asignaciones` paginado; listas completas usadas solo para `<select>` quedan como están si no son tabla.
- Caso de uso explícito del dueño: poder buscar "10 días de inasistencia" filtrando minutas por empleado y rango de fechas, o buscar por nombre/id a quién corresponde.

---

## Fase B — Archivo base de siembra de la BD (después de Fase A y A.5)

Objetivo: crear un **archivo de datos base** (SQL/JSON/seed script) que inicialice toda la información base en una sola operación, eliminando el paso lento de configuración inicial.

- Decidir formato (SQL, JSON, script Node, o migración) y qué incluye:
  - Catálogos legales (tipos de contrato, prestaciones, matriz, parámetros 2026).
  - Datos de ejemplo opcionales.
  - Mecanismo para que se aplique sin duplicar (idempotente) y sin romper instalaciones existentes.

---

## Fase C — Detalle de proyecto con costo (reservado, pendiente de definir)

- Ruta `GET /proyectos/{id}` (ya reservada en el router) → detalle con:
  - Empleados asignados con tiempos (minutas).
  - Consumibles utilizados (`AsignacionConsumible`).
  - **Materiales utilizados** → requiere definir el **vínculo materiales→proyecto** (hoy no existe).
  - Tiempo transcurrido / estado (concluido o en curso).
  - **Costo del proyecto** (suma de mano de obra por prestaciones + consumibles + materiales).
- Nota: la mano de obra dependerá del módulo de prestaciones (Fase A).

---

## Fase D — Conversión de modales a páginas (opcional)

- Los formularios de **crear/editar** siguen en modal; si se quiere, convertirlos a páginas con ruta (sin tocar backend).

---

## Notas / pendientes menores
- Los archivos `.atl/` (registro de skills: `.atl/skill-registry.cache.json`, `.atl/skill-registry.md`) quedan staged sin commitear. Decidir: ignorarlos (`.gitignore`) o commitearlos aparte.
- Confirmar despliegue del rediseño del frontend (router + Home + menú sectorizado) con `docker compose -f docker-compose.prod.yml up --build -d`.