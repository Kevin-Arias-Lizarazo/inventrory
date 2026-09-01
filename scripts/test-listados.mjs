// Pruebas de API del kit de listados paginados y filtrables (Fase A.5 - F1).
// Cubre R1 (defaults, cap 100, orden/dir invalidos -> 400), R2 (q free-text),
// R3 (AND de filtros) y el filtro especial `estado` de herramientas (D9).
const BASE = process.argv[2] || "http://localhost:8080";
import { iniciar, para } from "./auth-lib.mjs";

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

async function request(path, { method = "GET", body, headers = {} } = {}) {
  const opts = { method, headers: { ...headers } };
  para(opts.headers, path, method);
  if (body !== undefined) {
    opts.body = JSON.stringify(body);
    opts.headers["Content-Type"] = "application/json";
  }
  const res = await fetch(`${BASE}${path}`, opts);
  const text = await res.text();
  let data = null;
  try {
    data = JSON.parse(text);
  } catch {
    data = text;
  }
  return { status: res.status, data };
}

async function main() {
  console.log(`\nLISTADOS PAGINADOS: ${BASE}\n`);
  await iniciar(BASE);
  const runId = Date.now();
  const nombre = `Listado ${runId}`;

  // Fijar datos para filtros: un consumible, un material, un epp y una herramienta
  const cons = await request("/api/consumibles", {
    method: "POST",
    body: { nombre, marca: "MarcaX", unidad: "unidad", stock: 25 },
  });
  const mat = await request("/api/materiales", {
    method: "POST",
    body: { nombre: nombre + " M", marca: "MarcaY", unidad: "bulto", stock: 10 },
  });
  const epp = await request("/api/epp", {
    method: "POST",
    body: { nombre: nombre + " E", marca: "MarcaX", stock: 5 },
  });
  const herr = await request("/api/herramientas", {
    method: "POST",
    body: { nombre: nombre + " H", marca: "Stanley", cantidadTotal: 10 },
  });
  ok("siembra datos catalogos", cons.status === 201 && mat.status === 201 && epp.status === 201 && herr.status === 201);

  // ===== R1: defaults, cap 100, orden/dir invalidos =====
  const def = await request("/api/consumibles/paginado");
  ok("R1 default tamano=50", def.status === 200 && def.data.tamano === 50, `tamano=${def.data.tamano}`);
  ok("R1 default pagina=0", def.data.pagina === 0, `pagina=${def.data.pagina}`);
  ok("R1 default id asc", isSortedDesc(def.data.contenido.map((c) => c.id)) === false, "orden de ids");

  const cap = await request("/api/materiales/paginado?tamano=9999");
  ok("R1 cap tamano a 100", cap.status === 200 && cap.data.tamano === 100 && cap.data.contenido.length <= 100, `tamano=${cap.data.tamano}`);

  const ordInvalido = await request("/api/consumibles/paginado?orden=inexistente");
  ok("R1 orden invalido -> 400", ordInvalido.status === 400, `status=${ordInvalido.status}`);
  const dirInvalida = await request("/api/consumibles/paginado?orden=nombre&dir=up");
  ok("R1 dir invalida -> 400", dirInvalida.status === 400, `status=${dirInvalida.status}`);

  const filtroInvalido = await request("/api/consumibles/paginado?campoFantasma=x");
  ok("R1 filtro desconocido -> 400", filtroInvalido.status === 400, `status=${filtroInvalido.status}`);

  // ===== R2: q free-text =====
  const qCons = await request(`/api/consumibles/paginado?q=${encodeURIComponent(nombre)}`);
  ok("R2 q trae consumible", qCons.status === 200 && qCons.data.contenido.some((c) => c.id === cons.data.id), `status=${qCons.status}`);
  const qMarca = await request(`/api/consumibles/paginado?q=MarcaX`);
  ok("R2 q por marca", qMarca.status === 200 && qMarca.data.contenido.some((c) => c.id === cons.data.id));

  // ===== R3: AND de filtros =====
  const andOk = await request(`/api/consumibles/paginado?marca=MarcaX&unidad=unidad`);
  ok("R3 AND marca+unidad", andOk.status === 200 && andOk.data.contenido.some((c) => c.id === cons.data.id), `status=${andOk.status}`);
  const andFail = await request(`/api/consumibles/paginado?marca=MarcaX&unidad=bulto`);
  ok("R3 AND excluye con marca distinta", andFail.status === 200 && !andFail.data.contenido.some((c) => c.id === cons.data.id));

  // filtro numerico + booleano
  const eppFiltro = await request("/api/epp/paginado?marca=MarcaX");
  ok("R3 epp por marca", eppFiltro.status === 200 && eppFiltro.data.contenido.some((e) => e.id === epp.data.id), `status=${eppFiltro.status}`);

  const matOrden = await request(`/api/materiales/paginado?marca=MarcaY&unidad=bulto&orden=nombre&dir=desc`);
  ok("R3 material con orden+dir", matOrden.status === 200 && matOrden.data.contenido.some((m) => m.id === mat.data.id), `status=${matOrden.status}`);

  // ===== Filtro estado herramientas (D9) =====
  // dar de baja una asignacion activa para poder filtrar por asignadas
  const emp = await request("/api/empleados", { method: "POST", body: { nombre: "Emp Estado " + runId } });
  await request("/api/contratos", { method: "POST", body: { empleado: { id: emp.data.id }, fechaInicio: "2026-01-01" } });
  const asig = await request("/api/asignaciones-herramientas", {
    method: "POST",
    body: { herramienta: { id: herr.data.id }, lugar: "Obra", fecha: "2026-08-12", devuelta: false, empleado: { id: emp.data.id } },
  });
  ok("asignacion activa creada", asig.status === 201, `status=${asig.status}`);

  const estadoAsignadas = await request("/api/herramientas/paginado?estado=asignadas");
  ok("D9 estado=asignadas", estadoAsignadas.status === 200 && estadoAsignadas.data.contenido.some((h) => h.id === herr.data.id), `status=${estadoAsignadas.status}`);
  const estadoDisponibles = await request("/api/herramientas/paginado?estado=disponibles");
  ok("D9 estado=disponibles", estadoDisponibles.status === 200 && !estadoDisponibles.data.contenido.some((h) => h.id === herr.data.id), "herramienta asignada no disponible");
  const estadoInvalido = await request("/api/herramientas/paginado?estado=marciano");
  ok("D9 estado invalido -> 400", estadoInvalido.status === 400, `status=${estadoInvalido.status}`);
  const estadoPerdidas = await request("/api/herramientas/paginado?estado=perdidas");
  ok("D9 estado=perdidas no incluye asignadas", estadoPerdidas.status === 200 && !estadoPerdidas.data.contenido.some((h) => h.id === herr.data.id));

  // ===== determinismo / tie-break id =====
  const p1 = await request("/api/herramientas/paginado?orden=nombre&dir=asc&pagina=0&tamano=10");
  const p2 = await request("/api/herramientas/paginado?orden=nombre&dir=asc&pagina=1&tamano=10");
  const p1Ids = p1.data.contenido.map((x) => x.id).join(",");
  const p2Ids = p2.data.contenido.map((x) => x.id).join(",");
  ok("tie-break id: paginas disjuntas", p1.status === 200 && p2.status === 200 && p1Ids !== p2Ids);

  // limpieza
  await request(`/api/asignaciones-herramientas/${asig.data.id}`, { method: "DELETE" });
  await request(`/api/herramientas/${herr.data.id}`, { method: "DELETE" });
  await request(`/api/consumibles/${cons.data.id}`, { method: "DELETE" });
  await request(`/api/materiales/${mat.data.id}`, { method: "DELETE" });
  await request(`/api/epp/${epp.data.id}`, { method: "DELETE" });

  console.log(`\n${pasos} pasos, ${fallos} fallos`);
  if (fallos > 0) process.exit(1);
}

function isSortedDesc(ids) {
  for (let i = 1; i < ids.length; i++) {
    if (ids[i - 1] > ids[i]) return true;
  }
  return false;
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
