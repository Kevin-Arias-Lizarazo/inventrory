import { clearAccessToken, csrfToken, getAccessToken, setAccessToken } from './auth/token.js';

const RUTAS_SIN_CSRF = new Set(['/api/auth/login', '/api/auth/cambiar-contrasena-usuario', '/api/instalacion/completar']);

let renovando = null;

async function renovarSesion() {
  if (renovando) return renovando;
  renovando = (async () => {
    const res = await fetch('/api/auth/renovar', {
      method: 'POST',
      credentials: 'include',
      headers: { 'X-XSRF-TOKEN': csrfToken() || '' },
    });
    if (!res.ok) throw new Error('no-sesion');
    const d = await res.json();
    setAccessToken(d.accessToken);
    return d.accessToken;
  })().finally(() => {
    renovando = null;
  });
  return renovando;
}

export async function peticion(path, opciones = {}) {
  const { cuerpo, encabezados, ...resto } = opciones;
  const headers = { ...(encabezados || {}) };
  const esFormData = cuerpo instanceof FormData;
  if (cuerpo !== undefined && !esFormData) {
    headers['Content-Type'] = 'application/json';
  }
  const metodo = resto.method || 'GET';
  const requiereCsrf = metodo !== 'GET' && !RUTAS_SIN_CSRF.has(path);
  if (requiereCsrf) {
    headers['X-XSRF-TOKEN'] = csrfToken() || '';
  }
  const token = getAccessToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const body = cuerpo === undefined || esFormData ? cuerpo : JSON.stringify(cuerpo);

  const ejecutar = async () => {
    const res = await fetch(path, { ...resto, method: metodo, headers, body, credentials: 'include' });
    return res;
  };

  let res = await ejecutar();
  if (res.status === 401 && !path.startsWith('/api/auth/login') && !path.startsWith('/api/auth/recuperar-admin')) {
    try {
      await renovarSesion();
      const tokenNuevo = getAccessToken();
      if (tokenNuevo) {
        headers['Authorization'] = `Bearer ${tokenNuevo}`;
        res = await ejecutar();
      }
    } catch {
      clearAccessToken();
      window.dispatchEvent(new CustomEvent('auth:cerrada'));
    }
  }

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

export function patch(path, cuerpo) {
  return peticion(path, { method: 'PATCH', cuerpo });
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
  const res = await peticion('/api/archivos', { method: 'POST', cuerpo: fd });
  return res.url;
}

export async function descargar(path, { metodo = 'GET', nombreArchivo } = {}) {
  let token = getAccessToken();
  const ejecutar = () =>
    fetch(path, {
      method: metodo,
      credentials: 'include',
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(metodo !== 'GET' ? { 'X-XSRF-TOKEN': csrfToken() || '' } : {}),
      },
    });
  let res = await ejecutar();
  if (res.status === 401) {
    try {
      await renovarSesion();
      token = getAccessToken();
      res = await ejecutar();
    } catch {
      clearAccessToken();
      window.dispatchEvent(new CustomEvent('auth:cerrada'));
      throw new Error('Sesión cerrada');
    }
  }
  if (!res.ok) {
    throw new Error('No se pudo descargar el archivo');
  }
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = nombreDeDescarga(res, path, nombreArchivo);
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

function nombreDeDescarga(res, path, nombreArchivo) {
  if (nombreArchivo) return nombreArchivo;
  const disposicion = res.headers.get('Content-Disposition');
  if (disposicion) {
    const conEstrella = /filename\*=(?:UTF-8'')?([^;]+)/i.exec(disposicion);
    if (conEstrella) {
      try {
        return decodeURIComponent(conEstrella[1]);
      } catch {
        /* fall through to the plain filename */
      }
    }
    const plano = /filename="?([^";]+)"?/i.exec(disposicion);
    if (plano) return plano[1];
  }
  return path.split('/').pop().split('?')[0] || 'descarga';
}

export function hoy() {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${dia}`;
}

export function primerDiaMes() {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  return `${d.getFullYear()}-${m}-01`;
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