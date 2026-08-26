# Cómo configurar Inventario en la máquina del cliente

Guía paso a paso para dejar el sistema funcionando. Si un paso dice "espere" o "verifique", haga exactamente eso y no continúe hasta que pase.

---

## 1. Qué es esto

Es un sistema de inventario (herramientas, EPP, materiales, consumibles, empleados, proyectos, compras). Corre como una sola aplicación. Hay dos formas de instalarlo: **con Docker** (opción A) o **sin Docker con Java** (opción B). Elija UNA.

---

## 2. Qué necesita la máquina

- Windows 11 (actualizado).
- Disco libre: al menos 2 GB.
- Memoria: al menos 2 GB libres (4 GB recomendados).
- Opción A: instalar **Docker Desktop**.
- Opción B: instalar **Java 17 (JRE)**.

---

## OPCIÓN A — Con Docker (recomendada)

### Paso A1. Instalar Docker Desktop

1. Descargue Docker Desktop desde: https://www.docker.com/products/docker-desktop/
2. Ejecute el instalador y acepte todo con los valores por defecto.
3. Reinicie la computadora cuando lo pida.
4. Abra Docker Desktop y acepte el contrato la primera vez.
5. Espere a que diga "Engine running" (el motor corriendo).
6. Verifique: abra PowerShell y escriba:

```powershell
docker version
```

Debe mostrar algo como "Client" y "Server". Si no aparece "Server", Docker no está listo: espere o reinicie.

### Paso A2. Copiar el programa

1. Copie la carpeta del proyecto (la que contiene este archivo) a la máquina del cliente, por ejemplo en `C:\inventario`.
2. Confirme que dentro de `C:\inventario` existe el archivo `docker-compose.prod.yml`.

### Paso A3. Levantar la aplicación

Abra PowerShell **en esa carpeta** y ejecute:

```powershell
cd C:\inventario
docker compose -f docker-compose.prod.yml up --build -d
```

Espere. La primera vez tarda varios minutos (está compilando). No cierre la ventana.

### Paso A4. Verificar que funciona

En PowerShell:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8080/api/instalacion/estado
```

Debe responder `200` y algo como `{"pendiente":true}`. Si responde, siga al paso A5.

### Paso A5. Instalación inicial (una sola vez)

1. Abra el navegador en: http://localhost:8080
2. Complete el asistente de instalación:
   - **Contraseña de root**: elija una segura y anótela.
   - **Contraseña de admin**: elija otra segura y anótela.
   - **Secreto de recuperación**: cópielo y guárdelo en un lugar seguro. Es la única forma de recuperar la cuenta admin si se olvida la contraseña.
3. Espere a que termine y entre al sistema.

### Paso A6. Iconos de doble clic (opcional pero recomendado)

En PowerShell (en `C:\inventario`):

```powershell
.\scripts\Instalar-Accesos.ps1 -AutoInicio
```

Esto crea en el escritorio dos iconos: **Inventario - Iniciar** y **Inventario - Detener**, y deja la aplicación para que arranque sola al encender la máquina.

---

## OPCIÓN B — Sin Docker (con Java)

### Paso B1. Instalar Java 17

1. Descargue Java 17 (JRE) desde: https://adoptium.net
2. Instale con los valores por defecto.
3. Verifique en PowerShell:

```powershell
java -version
```

Debe mostrar `17.0.x`. Si no, reinicie y pruebe otra vez.

### Paso B2. Copiar el programa

1. Copie la carpeta del proyecto a la máquina, por ejemplo `C:\inventario`.
2. Confirme que en `C:\inventario` existen: `app.jar` e `Iniciar-JAR.exe`.
3. Si `app.jar` no existe, debe generarlo en la máquina de desarrollo (con `.\backend\gradlew.bat bootJar`) y copiarlo.

### Paso B3. Levantar la aplicación

Haga **doble clic** en `Iniciar-JAR.exe` (o en PowerShell):

```powershell
cd C:\inventario
.\Iniciar-JAR.exe
```

Espere hasta que diga "Inventario disponible en http://localhost:8080".

### Paso B4. Instalación inicial

Igual que el paso A5: abra http://localhost:8080, complete root/admin y guarde el secreto.

### Paso B5. Iconos de doble clic

Igual que el paso A6:

```powershell
.\scripts\Instalar-Accesos.ps1 -AutoInicio
```

---

## 3. Usar el lector de códigos (lo importante)

1. Entre al sistema.
2. En la barra lateral izquierda, haga clic en **Inventario**.
3. Haga clic en **Lector de códigos**.
4. Con el lector (scanner), escanee en este orden:
   - **El destino**: el código del empleado (empieza con `E`) o del proyecto (empieza con `P`).
   - **Los ítems**: códigos de herramientas (empiezan con `H`) o consumibles (empiezan con `C`). Puede escanear varios.
   - **FIN**: para cerrar el lote.
5. Revise el lote en pantalla y presione **Confirmar**.

Qué pasa si:
- **El código no existe**: el lote queda "pendiente de acomodar" y NO se abre nada solo. Haga clic en el ítem pendiente y use el formulario para crear el ítem rápido, o verifique el código.
- **Falta stock**: el sistema avisa cuántas unidades hay; puede usar "Incrementar stock" (mini formulario) o corregir.
- **Es una devolución**: escanee primero `DV`, luego el empleado, luego los códigos de herramientas a devolver, y FIN.

---

## 4. Operación diaria

| Acción | Cómo |
|---|---|
| Iniciar la app | Doble clic en **Inventario - Iniciar** (o arranca sola si se configuró auto-inicio) |
| Detener antes de apagar la máquina | Doble clic en **Inventario - Detener** (conserva los datos) |
| Entrar al sistema | Navegador → http://localhost:8080 |
| Hacer backup | En el sistema: **Administración → Mantenimiento** → Descargar backup (.db) y Descargar uploads (.zip) |
| Restaurar | En Mantenimiento → Restaurar backup / Restaurar uploads |

**IMPORTANTE:** no ejecute `docker compose down -v` (con `-v`) — borraría todos los datos.

---

## 5. Si algo falla

| Problema | Qué hacer |
|---|---|
| "No se puede conectar a Docker" | Abra Docker Desktop, espere "Engine running", pruebe de nuevo |
| "Puerto 8080 en uso" | Cierre programas que usen el puerto, o reinicie la máquina y pruebe |
| La página no abre | Verifique que el paso "Verificar que funciona" respondió `200`; si no, mire los logs: `docker compose -f docker-compose.prod.yml logs inventario` |
| Se olvidó la contraseña de admin | Use el secreto de recuperación guardado en la instalación (Administración → Mantenimiento o la pantalla de login) |
| El lector no escanea | Verifique que el cursor esté en el campo de captura y que el lector esté configurado como teclado USB |

---

## 6. Checklist final

- [ ] La página http://localhost:8080 abre y pide login.
- [ ] Entró con admin.
- [ ] En Inventario aparece **Lector de códigos**.
- [ ] Al escanear un código existente, se agrega al lote.
- [ ] Al escanear FIN, el lote se cierra y se puede Confirmar.
- [ ] Al escanear un código que no existe, el lote queda pendiente y NO abre formularios solo.
- [ ] El icono **Inventario - Detener** existe en el escritorio.
- [ ] Se hizo un backup inicial desde Mantenimiento.

Si todo está marcado, la máquina del cliente quedó configurada.