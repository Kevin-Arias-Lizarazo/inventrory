export async function peticion(path, opciones = {}) {
  const { cuerpo, encabezados, ...resto } = opciones;
  const headers = { ...(encabezados || {}) };
  const esFormData = cuerpo instanceof FormData;
  if (cuerpo !== undefined && !esFormData) {
    headers['Content-Type'] = 'application/json';
  }
  const body = cuerpo === undefined || esFormData ? cuerpo : JSON.stringify(cuerpo);
  const res = await fetch(path, {
    ...resto,
    headers,
    body,
  })
  if (!res.ok) {
    let mensaje = 'Error del servidor';
    try {
      const d = await res.json();
      if (d && d.mensaje) mensaje = d.mensaje;
    } catch {
      /* ignore */
    }
    throw new Error(mensaje);
  }
  if (res.status === 204) return null;
  const texto = await res.text();
  return texto ? JSON.parse(texto) : null;
}

export function get(path) {
  return peticion(path);
}

export function post(path, cuerpo) {
  return peticion(path, { method: 'POST', cuerpo });
}

export function put(path, cuerpo) {
  return peticion(path, { method: 'PUT', cuerpo });
}

export function del(path) {
  return peticion(path, { method: 'DELETE' });
}

export async function subirArchivo(archivo) {
  const fd = new FormData();
  fd.append('archivo', archivo);
  const res = await fetch('/api/archivos', { method: 'POST', body: fd });
  if (!res.ok) {
    let mensaje = 'No se pudo subir el archivo';
    try {
      const d = await res.json();
      if (d && d.mensaje) mensaje = d.mensaje;
    } catch {
      /* ignore */
    }
    throw new Error(mensaje);
  }
  const d = await res.json();
  return d.url;
}

export function hoy() {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${dia}`;
}

export function ahora() {
  const d = new Date();
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

export function firmaAArchivo(dataUrl) {
  return fetch(dataUrl)
    .then((r) => r.blob())
    .then((blob) => new File([blob], 'firma.png', { type: 'image/png' }));
}