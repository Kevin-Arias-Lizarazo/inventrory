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
function ok(nombre, cond, detalle) {
  console.log(`${cond ? '  OK   ' : '  FAIL '}${nombre}${detalle ? ' — ' + detalle : ''}`);
  if (!cond) fallos++;
}

const runId = Date.now();
const nombreEmp = 'Verificación Prestaciones ' + runId;

// 1. Localizar un tipo de contrato laboral (TERMINO_FIJO idealmente).
const tipos = await get('/api/tipos-contrato');
const laboral =
  tipos.find((t) => t.nombre === 'TERMINO_FIJO') ||
  tipos.find((t) => ['TERMINO_INDEFINIDO', 'OBRA_LABOR'].includes(t.nombre));
ok(
  'tipos-contrato: existe tipo laboral',
  !!laboral,
  laboral ? `usando ${laboral.nombre}` : 'ningún LABORAL sembrado'
);

if (!laboral) {
  console.log(`\nResumen: ${fallos} fallos (faltan tipos laborales)`);
  process.exit(fallos === 0 ? 0 : 1);
}

// 2. Empleado de soporte.
const e = await post('/api/empleados', {
  nombre: nombreEmp,
  documento: 'PRES-' + runId,
  hojaVida: 'Creado desde test-prestaciones.mjs',
});
ok('POST empleado', e && e.id > 0);

// 3. Contrato con tipo y remuneración mensual.
const c = await post('/api/contratos', {
  empleado: { id: e.id },
  fechaInicio: '2026-01-01',
  tipoContrato: { id: laboral.id },
  remuneracionMensual: 1000000,
});
ok('POST contrato con tipo y remuneración', c && c.id > 0);
ok('contrato devuelve tipoContrato', c.tipoContrato?.id === laboral.id);
ok('contrato devuelve remuneracionMensual', Number(c.remuneracionMensual) === 1000000);

// 4. Auto-cálculo: la creación genera instantáneas.
let pre = await get(`/api/contratos/${c.id}/prestaciones`);
ok(
  'auto-recalculo: desglose no vacío (laboral)',
  Array.isArray(pre.calculadas) && pre.calculadas.length > 0,
  `${pre.calculadas?.length ?? 0} líneas`
);
ok('respuesta expone totalEmpleador', typeof pre.totalEmpleador !== 'undefined');

// 5. Recalcular explícito mantiene desglose calculado.
pre = await post(`/api/contratos/${c.id}/calcular-prestaciones`);
ok('POST calcular-prestaciones', Array.isArray(pre.calculadas) && pre.calculadas.length > 0);

// 6. Prestación extra eventual (viático).
const extra = await post(`/api/contratos/${c.id}/prestaciones`, {
  concepto: 'Viático soporte ' + runId,
  tipo: 'EVENTUAL',
  valor: 50000,
  fecha: '2026-08-12',
  observacion: 'vía test-prestaciones.mjs',
});
ok('POST prestación extra', extra && extra.id > 0 && extra.tipo === 'EVENTUAL');

pre = await get(`/api/contratos/${c.id}/prestaciones`);
ok(
  'extra aparece en el desglose',
  Array.isArray(pre.extras) && pre.extras.some((x) => x.id === extra.id)
);

// 7. Extra recurrente (prima).
const extraRec = await post(`/api/contratos/${c.id}/prestaciones`, {
  concepto: 'Prima especial ' + runId,
  tipo: 'RECURRENTE',
  valor: 120000,
  vigenciaDesde: '2026-01-01',
  vigenciaHasta: '2026-12-31',
});
ok('POST extra recurrente', extraRec && extraRec.id > 0 && extraRec.tipo === 'RECURRENTE');

// 8. Eliminar extra.
await del(`/api/contratos/${c.id}/prestaciones/${extra.id}`);
pre = await get(`/api/contratos/${c.id}/prestaciones`);
ok('DELETE extra', !pre.extras.some((x) => x.id === extra.id));
ok('extra recurrente aún presente', pre.extras.some((x) => x.id === extraRec.id));

// 9. Limpieza.
await del(`/api/contratos/${c.id}/prestaciones/${extraRec.id}`);
await del(`/api/contratos/${c.id}`);
await del(`/api/empleados/${e.id}`);

console.log(`\nResumen: ${fallos === 0 ? 'todo OK' : fallos + ' fallos'}`);
process.exit(fallos === 0 ? 0 : 1);
