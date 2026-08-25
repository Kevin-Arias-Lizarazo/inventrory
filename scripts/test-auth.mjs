const BASE = process.argv[2] || "http://localhost:8080";
const ROOT_PWD = "AdminTest2026";

let fallos = 0;
let pasos = 0;

function ok(name, cond, detalle = "") {
  pasos++;
  if (cond) {
    console.log(`  OK   ${name}`);
  } else {
    fallos++;
    console.log(`  FAIL ${name} ${detalle}`);
  }
}

const jar = {};

function guardarCookies(res) {
  const set = res.headers.getSetCookie ? res.headers.getSetCookie() : [];
  for (const c of set) {
    const [par] = c.split(';');
    const idx = par.indexOf('=');
    if (idx > 0) jar[par.slice(0, idx)] = par.slice(idx + 1);
  }
}

function cookieHeader() {
  return Object.entries(jar).map(([k, v]) => `${k}=${v}`).join('; ');
}

async function peticion(path, { method = "GET", body, token, csrf, cookie = true, formData } = {}) {
  const headers = {};
  let payload;
  if (formData) {
    const fd = new FormData();
    for (const [k, v] of Object.entries(body || {})) fd.append(k, v);
    payload = fd;
  } else {
    if (body !== undefined) {
      headers["Content-Type"] = "application/json";
      payload = JSON.stringify(body);
    }
  }
  if (token) headers["Authorization"] = `Bearer ${token}`;
  if (csrf) headers["X-XSRF-TOKEN"] = csrf;
  const cks = cookieHeader();
  if (cookie && cks) headers["Cookie"] = cks;
  const res = await fetch(`${BASE}${path}`, { method, headers, body: payload });
  guardarCookies(res);
  const text = await res.text();
  let data = null;
  try { data = JSON.parse(text); } catch { data = text; }
  return { status: res.status, data };
}

async function main() {
  console.log(`\nSEGURIDAD: ${BASE}\n`);

  let r = await peticion("/api/instalacion/estado", { cookie: false });
  ok("instalacion pendiente al inicio", r.status === 200 && r.data.pendiente === true, JSON.stringify(r.data));

  r = await peticion("/api/instalacion/completar", { method: "POST", body: { rootPassword: ROOT_PWD, adminPassword: ROOT_PWD }, cookie: false, formData: true });
  ok("completar instalacion", r.status === 200 && r.data.usuario?.username === "root", `status=${r.status}`);
  const secretoRoot = r.data.secretoRecuperacion;
  const rootId = r.data.usuario?.id;
  ok("root id obtenido", !!rootId, `id=${rootId}`);

  r = await peticion("/api/instalacion/completar", { method: "POST", body: { rootPassword: "OtraClave2026" }, cookie: false, formData: true });
  ok("re-instalacion bloqueada (400)", r.status === 400, `status=${r.status}`);

  // Root no puede iniciar sesión
  r = await peticion("/api/auth/login", { method: "POST", body: { username: "root", password: ROOT_PWD }, cookie: false });
  ok("login de ROOT bloqueado (400)", r.status === 400, `status=${r.status}`);

  // Admin inicia sesión
  r = await peticion("/api/auth/login", { method: "POST", body: { username: "admin", password: ROOT_PWD } });
  ok("login admin correcto", r.status === 200 && !!r.data.accessToken && r.data.usuario?.nivel === "ADMIN", `status=${r.status}`);
  const tokenAdmin = r.data.accessToken;
  const adminId = r.data.usuario?.id;

  r = await peticion("/api/auth/csrf", { cookie: false });
  const csrf = r.data.token;
  ok("obtener CSRF", !!csrf);

  // Crear usuario y lector
  r = await peticion("/api/usuarios", { method: "POST", body: { username: "op1", contrasena: "Operador123", nivel: "USUARIO" }, token: tokenAdmin, csrf });
  ok("crear USUARIO", r.status === 201 && r.data.nivel === "USUARIO", `status=${r.status}`);
  const opId = r.data.id;
  r = await peticion("/api/usuarios", { method: "POST", body: { username: "lec1", contrasena: "Lector12345", nivel: "LECTOR" }, token: tokenAdmin, csrf });
  ok("crear LECTOR", r.status === 201, `status=${r.status}`);
  r = await peticion("/api/usuarios", { method: "POST", body: { username: "admin2", contrasena: "Admin12345", nivel: "ADMIN" }, token: tokenAdmin, csrf });
  ok("no permite crear ADMIN (400)", r.status === 400, `status=${r.status}`);

  // Admin no puede bloquear root
  r = await peticion(`/api/usuarios/${rootId}/bloquear`, { method: "POST", token: tokenAdmin, csrf });
  ok("admin no puede bloquear root (409)", r.status === 409, `status=${r.status} ${JSON.stringify(r.data)}`);

  // Admin no puede cambiar clave de root por el endpoint de terceros
  r = await peticion("/api/auth/cambiar-contrasena-usuario", { method: "POST", body: { usuarioId: rootId, contrasena: "NuevaRoot2026" }, token: tokenAdmin, csrf });
  ok("admin no puede cambiar clave de root (400)", r.status === 400, `status=${r.status} ${JSON.stringify(r.data)}`);

  // Admin no puede cambiar su propia clave por el endpoint de terceros
  r = await peticion("/api/auth/cambiar-contrasena-usuario", { method: "POST", body: { usuarioId: adminId, contrasena: "NuevaAdmin123" }, token: tokenAdmin, csrf });
  ok("admin no cambia su propia clave por endpoint terceros (400)", r.status === 400, `status=${r.status}`);

  // Admin cambia clave de un USUARIO (tercero)
  r = await peticion("/api/auth/cambiar-contrasena-usuario", { method: "POST", body: { usuarioId: opId, contrasena: "OperadorNueva1" }, token: tokenAdmin, csrf });
  ok("admin cambia clave de tercero (204)", r.status === 204, `status=${r.status}`);

  // ROOT (secreto) solo puede cambiar al admin
  r = await peticion("/api/auth/cambiar-contrasena-usuario", { method: "POST", body: { usuarioId: opId, contrasena: "Nueva12345", secretoRoot }, csrf });
  ok("ROOT no puede cambiar clave de no-admin (400)", r.status === 400, `status=${r.status} ${JSON.stringify(r.data)}`);

  r = await peticion("/api/auth/cambiar-contrasena-usuario", { method: "POST", body: { contrasena: "AdminTemp12345", secretoRoot }, csrf });
  ok("ROOT restablece clave del admin (204)", r.status === 204, `status=${r.status}`);

  // login admin con nueva clave (tras ROOT) -> al cerrar sesion previa se invalidó
  r = await peticion("/api/empleados", { token: tokenAdmin });
  ok("sesion del admin invalidada tras restablecer clave (401)", r.status === 401, `status=${r.status}`);

  r = await peticion("/api/auth/login", { method: "POST", body: { username: "admin", password: "AdminTemp12345" } });
  ok("login admin con clave restablecida", r.status === 200 && !!r.data.accessToken, `status=${r.status}`);
  const tokenAdmin2 = r.data.accessToken;

  // soft-delete: bloquear no elimina el registro (sigue listado)
  r = await peticion("/api/usuarios", { token: tokenAdmin2 });
  ok("listar usuarios excluye root", Array.isArray(r.data) && !r.data.some((u) => u.id === rootId), `datos=${JSON.stringify(r.data)}`);

  // CSRF exigido en escritura protegida
  r = await peticion("/api/usuarios", { method: "POST", body: { username: "sincsrf", contrasena: "xxxx1234", nivel: "LECTOR" }, token: tokenAdmin2 });
  ok("POST sin CSRF (403)", r.status === 403, `status=${r.status}`);

  // Devolver la clave del admin al valor compartido para que otras suites entren
  r = await peticion("/api/auth/cambiar-contrasena-usuario", { method: "POST", body: { contrasena: ROOT_PWD, secretoRoot }, csrf });
  ok("ROOT restaura clave compartida del admin (204)", r.status === 204, `status=${r.status}`);

  console.log(`\nResultado: ${pasos} pasos, ${fallos} fallos`);
  process.exit(fallos === 0 ? 0 : 1);
}

main().catch((e) => { console.error(e); process.exit(1); });