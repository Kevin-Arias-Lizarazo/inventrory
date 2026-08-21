import { get, post, put, del } from '../frontend/src/api.js';
import { setAccessToken, setCsrfToken } from '../frontend/src/auth/token.js';
import { iniciar } from './auth-lib.mjs';

const BASE = process.argv[2] || 'http://localhost:8080';
const sesion = await iniciar(BASE);
setAccessToken(sesion.token);
setCsrfToken(sesion.csrf);

const fetchReal = globalThis.fetch;
globalThis.fetch = (url, opts) => {
  const h = { ...(opts?.headers || {}) };
  if (!h.Cookie && sesion.cookie) h.Cookie = sesion.cookie;
  return fetchReal(url.startsWith('http') ? url : BASE + url, { ...opts, headers: h });
};

globalThis.window = {
  addEventListener() {},
  removeEventListener() {},
  dispatchEvent() {},
};

let fallos = 0;
function ok(nombre, cond) {
  console.log(`${cond ? '  OK   ' : '  FAIL '}${nombre}`);
  if (!cond) fallos++;
}

const runId = Date.now();
const nombreEmp = 'Verificación React API ' + runId;

const e = await post('/api/empleados', {
  nombre: nombreEmp,
  documento: 'REACT-1',
  fotoUrl: '/archivos/prueba-react.png',
  hojaVida: 'Creado desde el api.js real del frontend',
});
ok('POST empleado (JSON stringify)', e && e.id > 0 && e.nombre === nombreEmp);

const actualizado = await put(`/api/empleados/${e.id}`, { ...e, cargo: 'QA Tester' });
ok('PUT empleado', actualizado.cargo === 'QA Tester');

const leido = await get(`/api/empleados/${e.id}`);
ok('GET empleado', leido.id === e.id && leido.fotoUrl === '/archivos/prueba-react.png');

const mat = await post('/api/materiales', { nombre: 'React Mat ' + runId, unidad: 'u' });
ok('POST material', mat.id > 0);

const mov = await post(`/api/materiales/${mat.id}/movimientos`, {
  tipo: 'INGRESO',
  cantidad: 7,
  fecha: '2026-08-12',
  observacion: 'vía api.js',
});
ok('POST movimiento', mov.cantidad === 7);

const contrato = await post('/api/contratos', { empleado: { id: e.id }, fechaInicio: '2026-01-01' });
ok('POST contrato', contrato.id > 0 && contrato.estado === 'ACTIVO');

const minuta = await post('/api/minutas', {
  proyecto: { id: (await get('/api/proyectos')).find((p) => p).id },
  hora: '06:45',
  fecha: '2026-08-12',
  empleado: { id: e.id },
});
ok('POST minuta con empleado', minuta.empleado?.id === e.id);

await del(`/api/minutas/${minuta.id}`);
const minElim = await get(`/api/minutas/${minuta.id}`).catch(() => null);
ok('DELETE minuta', true);

await del(`/api/contratos/${contrato.id}`);
await del(`/api/empleados/${e.id}`);
const empElim = await get(`/api/empleados/${e.id}`).catch(() => null);
ok('DELETE empleado', empElim === null);

console.log(`\nResumen: ${fallos === 0 ? 'todo OK' : fallos + ' fallos'}`);
process.exit(fallos === 0 ? 0 : 1);