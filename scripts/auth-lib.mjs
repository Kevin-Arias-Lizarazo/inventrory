const EXENTAS_CSRF = new Set([
  '/api/auth/login',
  '/api/auth/cambiar-contrasena-usuario',
  '/api/auth/csrf',
  '/api/instalacion/completar',
]);

const jar = {};
let token = '';
let csrfHeader = '';

function parseSetCookie(res) {
  const set = typeof res.headers.getSetCookie === 'function' ? res.headers.getSetCookie() : [];
  for (const c of set) {
    const [pair] = c.split(';');
    const idx = pair.indexOf('=');
    if (idx <= 0) continue;
    const nombre = pair.slice(0, idx);
    const valor = pair.slice(idx + 1);
    if (valor === '') delete jar[nombre];
    else jar[nombre] = valor;
  }
}

function cookieHeader() {
  return Object.entries(jar).map(([k, v]) => `${k}=${v}`).join('; ');
}

export async function iniciar(BASE, rootPassword = 'AdminTest2026') {
  token = '';
  csrfHeader = '';
  for (const k of Object.keys(jar)) delete jar[k];

  async function raw(path, { method = 'GET', headers = {}, body } = {}) {
    const res = await fetch(`${BASE}${path}`, { method, headers, body });
    parseSetCookie(res);
    const text = await res.text();
    let data = null;
    try { data = JSON.parse(text); } catch { data = text; }
    return { status: res.status, data };
  }

  const estado = await raw('/api/instalacion/estado');
  if (estado.data && estado.data.pendiente) {
    const fd = new FormData();
    fd.append('rootPassword', rootPassword);
    await raw('/api/instalacion/completar', { method: 'POST', body: fd });
  }

  const csrfRes = await raw('/api/auth/csrf');
  csrfHeader = csrfRes.data?.token || '';

  const login = await raw('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: rootPassword }),
  });
  if (login.status !== 200 || !login.data?.accessToken) {
    throw new Error('No se pudo iniciar sesión de prueba: ' + JSON.stringify(login.data));
  }
  token = login.data.accessToken;
  return { token, csrf: csrfHeader, cookie: cookieHeader() };
}

export function para(headers, path, method) {
  if (token) headers['Authorization'] = `Bearer ${token}`;
  const cookie = cookieHeader();
  if (cookie) headers['Cookie'] = cookie;
  const m = (method || 'GET').toUpperCase();
  if (m !== 'GET' && m !== 'HEAD' && m !== 'OPTIONS' && !EXENTAS_CSRF.has(path)) {
    headers['X-XSRF-TOKEN'] = csrfHeader;
  }
  return headers;
}