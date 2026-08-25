# Despliegue productivo en Windows 11 (maquina de bajos recursos)

Guía para instalar el sistema en un equipo Windows 11 usando **solo Docker**.
En el equipo destino NO se instala Java, Node.js, npm ni un motor SQLite:
todo vive dentro de un único contenedor. SQLite va embebido en el JAR.

## Estrategia

El build (React + Java) se hace UNA vez en la máquina de desarrollo y se exporta
como imagen. El equipo destino solo **carga la imagen** y levanta el contenedor
con `--no-build`: no compila nada, no necesita recursos de build.

```
[Maquina dev]  scripts/export-imagen.ps1  ->  inventario-imagen-<fecha>.tar
        |
        |  copiar el .tar (USB / red)
        v
[Windows 11]   scripts/load-y-levanta.ps1 -Tar inventario-imagen-<fecha>.tar
```

## Requisitos del equipo destino

- Windows 11 con virtualización habilitada en BIOS (VT-x/AMD-V).
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) con backend
  WSL2 (requiere `wsl --install` la primera vez y reiniciar).
- ~2 GB de RAM disponibles para Docker (la imagen runtime es JRE 17 alpine,
  liviana; el contenedor usa ~300-400 MB en ejecución).

### Limitar memoria en equipos chicos (recomendado)

Crear `C:\Users\<usuario>\.wslconfig`:

```
[wsl2]
memory=2GB
processors=2
```

Y reiniciar WSL (`wsl --shutdown` en PowerShell) para aplicar.

## Pasos en el equipo destino

1. Instalar Docker Desktop (WSL2) y reiniciar.
2. Copiar el repositorio (o al menos `docker-compose.prod.yml` y `scripts/`) y el
   archivo `inventario-imagen-<fecha>.tar` al equipo.
3. Cargar la imagen y levantar:

   ```powershell
   .\scripts\load-y-levanta.ps1 -Tar .\inventario-imagen-<fecha>.tar
   ```

4. Abrir http://localhost:8080 y completar el asistente de instalación inicial
   (define root, crea admin y el secreto de recuperación).

## Operación diaria

| Acción | Comando |
|---|---|
| Ver estado | `docker compose -f docker-compose.prod.yml ps` |
| Detener (conserva datos) | `.\scripts\stop.ps1` |
| Levantar de nuevo | `.\scripts\load-y-levanta.ps1 -Tar <tar>` (o `up -d` si la imagen ya está cargada) |
| Logs | `docker compose -f docker-compose.prod.yml logs -f inventario` |

Los datos viven en el volumen `inventario-data` (`/data` dentro del contenedor):
`inventario.db`, `uploads/`, `sesiones/`, `logs/`, `.env.auth`. Mientras el
volumen exista, los datos sobreviven a detener/recrear el contenedor.
**No ejecutar `docker compose down -v`** (borra el volumen con los datos).

## Backup / migración (desde la UI, rol ADMIN)

Administración → Mantenimiento:
- **Descargar backup (.db)** — la base de datos.
- **Descargar uploads (.zip)** — imágenes y archivos subidos.
- **Restaurar backup (.db)** — requiere reiniciar el servicio tras subirlo.
- **Restaurar uploads (.zip)** — imágenes, sin reiniciar.

Para migrar a otra máquina: exportar `.db` + `.zip` de uploads, desplegar la
imagen en el destino y restaurar ambos desde Mantenimiento.

## Actualizar a una versión nueva

En la máquina de desarrollo:

```powershell
.\scripts\deploy.ps1          # build + up local
.\scripts\export-imagen.ps1   # genera el nuevo .tar
```

En el destino: copiar el nuevo `.tar`, ejecutar `load-y-levanta.ps1` con él.
El contenedor se recrea con la imagen nueva; el volumen conserva los datos.

## Uso para el usuario final (sin terminal, doble clic)

El usuario final no debe abrir Docker ni usar PowerShell. Tras el despliegue,
ejecutar UNA vez (en la maquina destino):

```powershell
.\scripts\Instalar-Accesos.ps1 -AutoInicio
```

Esto crea en el **escritorio** dos iconos:

| Icono | Que hace |
|---|---|
| **Inventario - Iniciar** | Arranca Docker Desktop si hace falta, levanta el contenedor y abre http://localhost:8080 en el navegador |
| **Inventario - Detener** | Detiene el contenedor de forma limpia (los datos se conservan) — para usarlo **antes de apagar la maquina** |

Con `-AutoInicio`, además, Docker Desktop queda en el arranque de Windows y el
contenedor (`restart: unless-stopped`) sube **solo al encender la maquina**: el
usuario final solo abre el navegador en http://localhost:8080, sin doble clic.

**Rutina de apagado recomendada**: doble clic en "Inventario - Detener" y esperar
el mensaje "Contenedor detenido correctamente" antes de apagar Windows. Esto evita
cortes bruscos sobre la base SQLite. Si el usuario apaga sin detener, el contenedor
se detiene con Windows y SQLite se recupera solo (journal/WAL), pero es preferible
el apagado limpio.

> Nota: no existe boton "apagar Docker" dentro de la UI porque el contenedor no
> tiene acceso al motor de Docker (montar el socket seria un riesgo de seguridad).
> Los iconos del escritorio cubren ese flujo de forma segura.