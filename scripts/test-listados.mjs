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

  // ============================================================
  // F2: migración a paginación SQL de los recursos listados por
  // la aplicación (Reglas de negocio cubren F1 + nuevos /paginado).
  // Cada recurso: 200 por defecto, filtro por id/estado devuelve la
  // fila sembrada, orden válido, y filtro desconocido -> 400.
  // ============================================================
  const rf = `F2 ${runId}`;

  // --- empleados + contrato (base para minutas/entregas) ---
  const empF2 = await request("/api/empleados", {
    method: "POST",
    body: { nombre: "F2 Emp " + runId, cargo: "Oficial", fechaIngreso: "2026-01-01" },
  });
  const contrF2 = await request("/api/contratos", {
    method: "POST",
    body: { empleado: { id: empF2.data.id }, fechaInicio: "2026-01-01" },
  });
  const proyF2 = await request("/api/proyectos", {
    method: "POST",
    body: { nombre: "F2 Proy " + runId, cliente: "F2 Cliente " + runId, estado: "EN_CURSO" },
  });

  // empleados /paginado
  const empPag = await request(`/api/empleados/paginado?q=${encodeURIComponent(rf)}`);
  ok("F2 empleados q", empPag.status === 200 && empPag.data.contenido.some((e) => e.id === empF2.data.id), `status=${empPag.status}`);
  const empCargo = await request(`/api/empleados/paginado?cargo=${encodeURIComponent("Oficial")}`);
  ok("F2 empleados filtro cargo", empCargo.status === 200 && empCargo.data.contenido.some((e) => e.id === empF2.data.id), `status=${empCargo.status}`);
  const empOrden = await request("/api/empleados/paginado?orden=nombre&dir=asc");
  ok("F2 empleados orden nombre", empOrden.status === 200 && Array.isArray(empOrden.data.contenido), `status=${empOrden.status}`);
  const empFiltroInvalido = await request("/api/empleados/paginado?campoFantasma=x");
  ok("F2 empleados filtro invalido -> 400", empFiltroInvalido.status === 400, `status=${empFiltroInvalido.status}`);

  // contratos /paginado
  const contrPag = await request(`/api/contratos/paginado?empleadoId=${empF2.data.id}`);
  ok("F2 contratos filtro empleadoId", contrPag.status === 200 && contrPag.data.contenido.some((c) => c.id === contrF2.data.id), `status=${contrPag.status}`);
  const contrQ = await request(`/api/contratos/paginado?q=${encodeURIComponent("F2 Emp ")}`);
  ok("F2 contratos q empleado.nombre", contrQ.status === 200 && contrQ.data.contenido.some((c) => c.id === contrF2.data.id), `status=${contrQ.status}`);

  // proyectos /paginado
  const proyPag = await request(`/api/proyectos/paginado?estado=${encodeURIComponent("EN_CURSO")}`);
  ok("F2 proyectos filtro estado", proyPag.status === 200 && proyPag.data.contenido.some((p) => p.id === proyF2.data.id), `status=${proyPag.status}`);
  const proyQ = await request(`/api/proyectos/paginado?q=${encodeURIComponent(rf)}`);
  ok("F2 proyectos q", proyQ.status === 200 && proyQ.data.contenido.some((p) => p.id === proyF2.data.id), `status=${proyQ.status}`);
  const proyCliente = await request(`/api/proyectos/paginado?cliente=${encodeURIComponent("F2 Cliente " + runId)}`);
  ok("F2 proyectos filtro cliente", proyCliente.status === 200 && proyCliente.data.contenido.some((p) => p.id === proyF2.data.id), `status=${proyCliente.status}`);

  // minutas /paginado (nuevo; /filtradas se conserva)
  const minF2 = await request("/api/minutas", {
    method: "POST",
    body: { proyecto: { id: proyF2.data.id }, hora: "07:30", fecha: "2026-08-12", empleado: { id: empF2.data.id } },
  });
  const minPag = await request(`/api/minutas/paginado?empleadoId=${empF2.data.id}`);
  ok("F2 minutas filtro empleadoId", minPag.status === 200 && minPag.data.contenido.some((m) => m.id === minF2.data.id), `status=${minPag.status}`);
  const minFiltradas = await request(`/api/minutas/filtradas?empleadoId=${empF2.data.id}&pagina=0&tamano=100`);
  ok("F2 minutas /filtradas conservado", minFiltradas.status === 200 && minFiltradas.data.contenido.some((m) => m.id === minF2.data.id), `status=${minFiltradas.status}`);

  // entregas-ropa /paginado
  const ropaF2 = await request("/api/entregas-ropa", {
    method: "POST",
    body: { fecha: "2026-08-12", observacion: rf, empleado: { id: empF2.data.id } },
  });
  const ropaPag = await request(`/api/entregas-ropa/paginado?q=${encodeURIComponent(rf)}`);
  ok("F2 entregas-ropa q observacion", ropaPag.status === 200 && ropaPag.data.contenido.some((r) => r.id === ropaF2.data.id), `status=${ropaPag.status}`);

  // entregas-epp /paginado (+ epp con stock)
  const eppF2 = await request("/api/epp", { method: "POST", body: { nombre: "F2 EPP " + runId, stock: 5 } });
  const entregaEppF2 = await request("/api/entregas-epp", {
    method: "POST",
    body: { fecha: "2026-08-12", epp: { id: eppF2.data.id }, observacion: rf, empleado: { id: empF2.data.id } },
  });
  const eppPag = await request(`/api/entregas-epp/paginado?q=${encodeURIComponent(rf)}`);
  ok("F2 entregas-epp q observacion", eppPag.status === 200 && eppPag.data.contenido.some((x) => x.id === entregaEppF2.data.id), `status=${eppPag.status}`);
  const eppFiltradas = await request(`/api/entregas-epp/filtradas?empleadoId=${empF2.data.id}&pagina=0&tamano=100`);
  ok("F2 entregas-epp /filtradas conservado", eppFiltradas.status === 200 && eppFiltradas.data.contenido.some((x) => x.id === entregaEppF2.data.id), `status=${eppFiltradas.status}`);

  // asignaciones-consumibles /paginado
  const consF2 = await request("/api/consumibles", {
    method: "POST",
    body: { nombre: "F2 Cons " + runId, unidad: "unidad", stock: 30 },
  });
  const asigConsF2 = await request("/api/asignaciones-consumibles", {
    method: "POST",
    body: { consumible: { id: consF2.data.id }, proyecto: { id: proyF2.data.id }, cantidad: 5, fecha: "2026-08-12", observacion: rf },
  });
  const asigConsPag = await request(`/api/asignaciones-consumibles/paginado?consumibleId=${consF2.data.id}`);
  ok("F2 asignaciones-consumibles filtro consumibleId", asigConsPag.status === 200 && asigConsPag.data.contenido.some((x) => x.id === asigConsF2.data.id), `status=${asigConsPag.status}`);
  const asigConsQ = await request(`/api/asignaciones-consumibles/paginado?q=${encodeURIComponent(rf)}`);
  ok("F2 asignaciones-consumibles q observacion", asigConsQ.status === 200 && asigConsQ.data.contenido.some((x) => x.id === asigConsF2.data.id), `status=${asigConsQ.status}`);

  // asignaciones-herramientas /paginado (reusar asig ya creada y borrada? no: se crea una)
  const empH2 = await request("/api/empleados", { method: "POST", body: { nombre: "F2 EmpH " + runId, cargo: "Oficial" } });
  await request("/api/contratos", { method: "POST", body: { empleado: { id: empH2.data.id }, fechaInicio: "2026-01-01" } });
  const herrF2 = await request("/api/herramientas", { method: "POST", body: { nombre: "F2 Herr " + runId, marca: "Marca", cantidadTotal: 5 } });
  const asigHerrF2 = await request("/api/asignaciones-herramientas", {
    method: "POST",
    body: { herramienta: { id: herrF2.data.id }, lugar: "Obra F2", fecha: "2026-08-12", devuelta: false, empleado: { id: empH2.data.id } },
  });
  const asigHerrPag = await request(`/api/asignaciones-herramientas/paginado?empleadoId=${empH2.data.id}`);
  ok("F2 asignaciones-herramientas filtro empleadoId", asigHerrPag.status === 200 && asigHerrPag.data.contenido.some((x) => x.id === asigHerrF2.data.id), `status=${asigHerrPag.status}`);
  const asigHerrDevuelta = await request("/api/asignaciones-herramientas/paginado?devuelta=false");
  ok("F2 asignaciones-herramientas filtro booleano devuelta", asigHerrDevuelta.status === 200 && asigHerrDevuelta.data.contenido.some((x) => x.id === asigHerrF2.data.id), `status=${asigHerrDevuelta.status}`);

  // proveedor + material para compras/facturas/ordenes-compra
  const provF2 = await request("/api/proveedores", { method: "POST", body: { nombre: "F2 Prov " + runId } });
  const matF2 = await request("/api/materiales", { method: "POST", body: { nombre: "F2 Mat " + runId, unidad: "bulto", stock: 20 } });

  // compras /paginado (sin factura -> no mueve ultimoCosto)
  const compraF2 = await request("/api/compras", {
    method: "POST",
    body: { fecha: "2026-08-12", observacion: rf, proveedor: { id: provF2.data.id }, lineas: [{ tipo: "MATERIAL", productoId: matF2.data.id, cantidad: 10 }] },
  });
  const compraPag = await request(`/api/compras/paginado?proveedorId=${provF2.data.id}`);
  ok("F2 compras filtro proveedorId", compraPag.status === 200 && compraPag.data.contenido.some((c) => c.id === compraF2.data.id), `status=${compraPag.status} det=${JSON.stringify(compraPag.data)}`);
  const compraQ = await request(`/api/compras/paginado?q=${encodeURIComponent(rf)}`);
  ok("F2 compras q observacion", compraQ.status === 200 && compraQ.data.contenido.some((c) => c.id === compraF2.data.id), `status=${compraQ.status}`);

  // facturas /paginado (vincula la compra -> no sube stock)
  const facturaF2 = await request("/api/facturas", {
    method: "POST",
    body: { numero: "F2-FAC-" + runId, fecha: "2026-08-12", proveedor: { id: provF2.data.id }, compraId: compraF2.data.id, lineas: [{ tipo: "MATERIAL", productoId: matF2.data.id, cantidad: 10, costoUnitario: 1500 }] },
  });
  ok("siembra factura F2", facturaF2.status === 201, `status=${facturaF2.status} det=${JSON.stringify(facturaF2.data)}`);
  const facturaPag = await request(`/api/facturas/paginado?proveedorId=${provF2.data.id}`);
  ok("F2 facturas filtro proveedorId", facturaPag.status === 200 && facturaPag.data.contenido.some((f) => f.id === facturaF2.data.id), `status=${facturaPag.status} det=${JSON.stringify(facturaPag.data)}`);
  const facturaQ = await request(`/api/facturas/paginado?q=${encodeURIComponent("F2-FAC-")}`);
  ok("F2 facturas q numero", facturaQ.status === 200 && facturaQ.data.contenido.some((f) => f.id === facturaF2.data.id), `status=${facturaQ.status}`);

  // ordenes-compra /paginado
  const ordenF2 = await request("/api/ordenes-compra", {
    method: "POST",
    body: { fecha: "2026-08-15", observacion: rf, lineas: [{ tipo: "MATERIAL", productoId: matF2.data.id, cantidad: 2, costoUnitario: 1500 }] },
  });
  const ordenPag = await request(`/api/ordenes-compra/paginado?q=${encodeURIComponent(rf)}`);
  ok("F2 ordenes-compra q observacion", ordenPag.status === 200 && ordenPag.data.contenido.some((o) => o.id === ordenF2.data.id), `status=${ordenPag.status} det=${JSON.stringify(ordenPag.data)}`);

  // determinismo / tie-break id en un recurso migrado
  const d1 = await request("/api/proyectos/paginado?orden=nombre&dir=asc&pagina=0&tamano=10");
  const d2 = await request("/api/proyectos/paginado?orden=nombre&dir=asc&pagina=1&tamano=10");
  const d1Ids = d1.data.contenido.map((x) => x.id).join(",");
  const d2Ids = d2.data.contenido.map((x) => x.id).join(",");
  ok("F2 proyectos tie-break paginas disjuntas", d1.status === 200 && d2.status === 200 && d1Ids !== d2Ids, `d1=${d1.status} d2=${d2.status}`);

  // ============================================================
  // Regresión de compatibilidad (gatekeeper): los parámetros
  // legacy que los frontends existentes siguen enviando deben
  // funcionar (200 y filtrar), sin romper el contrato /paginado.
  // La sección se siembra y limpia sola (no depende de las siembras
  // de F2) y usa fechas únicas por recurso para que `fecha=` sea
  // inequívoco.
  // ============================================================
  const lr = `LR ${runId}`;
  const provR = await request("/api/proveedores", { method: "POST", body: { nombre: "LR Prov " + runId } });
  const matR = await request("/api/materiales", { method: "POST", body: { nombre: "LR Mat " + runId, unidad: "bulto", stock: 20 } });
  const empR = await request("/api/empleados", { method: "POST", body: { nombre: "LR Emp " + runId, cargo: "Oficial" } });
  const contratoR = await request("/api/contratos", { method: "POST", body: { empleado: { id: empR.data.id }, fechaInicio: "2026-01-01" } });
  const proyR = await request("/api/proyectos", { method: "POST", body: { nombre: "LR Proy " + runId, cliente: "LR Cli", estado: "ACTIVO" } });
  const compraR = await request("/api/compras", { method: "POST", body: { fecha: "2026-08-12", observacion: lr, proveedor: { id: provR.data.id }, lineas: [{ tipo: "MATERIAL", productoId: matR.data.id, cantidad: 10 }] } });
  const facturaR = await request("/api/facturas", { method: "POST", body: { numero: "LR-FAC-" + runId, fecha: "2026-08-12", proveedor: { id: provR.data.id }, compraId: compraR.data.id, lineas: [{ tipo: "MATERIAL", productoId: matR.data.id, cantidad: 10, costoUnitario: 1500 }] } });
  const minR = await request("/api/minutas", { method: "POST", body: { proyecto: { id: proyR.data.id }, hora: "07:30", fecha: "2026-08-24", empleado: { id: empR.data.id } } });
  const ordenR = await request("/api/ordenes-compra", { method: "POST", body: { fecha: "2026-08-15", observacion: lr, lineas: [{ tipo: "MATERIAL", productoId: matR.data.id, cantidad: 2, costoUnitario: 1500, descripcion: "LR mat" }] } });
  const ropaR = await request("/api/entregas-ropa", { method: "POST", body: { fecha: "2026-08-20", observacion: lr, empleado: { id: empR.data.id } } });
  const eppR = await request("/api/epp", { method: "POST", body: { nombre: "LR EPP " + runId, stock: 5 } });
  const entregaEppR = await request("/api/entregas-epp", { method: "POST", body: { fecha: "2026-08-21", epp: { id: eppR.data.id }, observacion: lr, empleado: { id: empR.data.id } } });
  const consR = await request("/api/consumibles", { method: "POST", body: { nombre: "LR Cons " + runId, unidad: "unidad", stock: 30 } });
  const asigConsR = await request("/api/asignaciones-consumibles", { method: "POST", body: { consumible: { id: consR.data.id }, proyecto: { id: proyR.data.id }, cantidad: 5, fecha: "2026-08-22", observacion: lr } });
  const herrR = await request("/api/herramientas", { method: "POST", body: { nombre: "LR Herr " + runId, marca: "Marca", cantidadTotal: 5 } });
  const asigHerrR = await request("/api/asignaciones-herramientas", { method: "POST", body: { herramienta: { id: herrR.data.id }, lugar: "Obra", fecha: "2026-08-23", devuelta: false, empleado: { id: empR.data.id } } });

  // compras: facturada (boolean), fecha, proveedorId
  ok("legacy compras facturada=true -> 200 y filtra",
    (await request(`/api/compras/paginado?facturada=true&proveedorId=${provR.data.id}&tamano=100`))
      .data.contenido.some((c) => c.id === compraR.data.id),
    "facturada=true no incluye la compra facturada");
  ok("legacy compras facturada=false -> 200 y excluye facturada",
    !(await request(`/api/compras/paginado?facturada=false&proveedorId=${provR.data.id}&tamano=100`))
      .data.contenido.some((c) => c.id === compraR.data.id),
    "facturada=false sí incluye la compra facturada");
  ok("legacy compras fecha -> 200 y filtra",
    (await request(`/api/compras/paginado?fecha=2026-08-12&proveedorId=${provR.data.id}&tamano=100`))
      .data.contenido.some((c) => c.id === compraR.data.id),
    "fecha no devuelve la compra");
  ok("legacy compras facturada invalida -> 400",
    (await request("/api/compras/paginado?facturada=quizas")).status === 400,
    "");

  // facturas: estadoPago (PENDIENTE sin pagos), fecha, proveedorId
  const fPen = await request(`/api/facturas/paginado?estadoPago=PENDIENTE&proveedorId=${provR.data.id}&tamano=100`);
  ok("legacy facturas estadoPago=PENDIENTE -> 200 y filtra",
    fPen.status === 200 && fPen.data.contenido.some((f) => f.id === facturaR.data.id),
    `status=${fPen.status}`);
  ok("legacy facturas fecha -> 200 y filtra",
    (await request(`/api/facturas/paginado?fecha=2026-08-12&proveedorId=${provR.data.id}&tamano=100`))
      .data.contenido.some((f) => f.id === facturaR.data.id),
    "fecha no devuelve la factura");

  // minutas: fecha
  ok("legacy minutas fecha -> 200 y filtra",
    (await request(`/api/minutas/paginado?fecha=2026-08-24&empleadoId=${empR.data.id}&tamano=100`))
      .data.contenido.some((m) => m.id === minR.data.id),
    "fecha no devuelve la minuta");

  // ordenes-compra: fecha
  ok("legacy ordenes-compra fecha -> 200 y filtra",
    (await request("/api/ordenes-compra/paginado?fecha=2026-08-15&tamano=100"))
      .data.contenido.some((o) => o.id === ordenR.data.id),
    "fecha no devuelve la orden de compra");

  // empleados: contratados (boolean, con contrato ACTIVO)
  ok("legacy empleados contratados=true -> 200 y filtra",
    (await request("/api/empleados/paginado?contratados=true&tamano=100"))
      .data.contenido.some((e) => e.id === empR.data.id),
    "contratados=true no incluye al empleado contratado");
  ok("legacy empleados contratados invalido -> 400",
    (await request("/api/empleados/paginado?contratados=no")).status === 400, "");

  // proyectos: estado (valores válidos ACTIVO/FINALIZADO)
  ok("legacy proyectos estado=ACTIVO -> 200 y filtra",
    (await request(`/api/proyectos/paginado?estado=${encodeURIComponent("ACTIVO")}&tamano=100`))
      .data.contenido.some((p) => p.id === proyR.data.id),
    "estado=ACTIVO no incluye el proyecto");

  // entregas-ropa / entregas-epp / asignaciones: fecha
  ok("legacy entregas-ropa fecha -> 200 y filtra",
    (await request("/api/entregas-ropa/paginado?fecha=2026-08-20&tamano=100"))
      .data.contenido.some((r) => r.id === ropaR.data.id),
    "fecha no devuelve la entrega de ropa");
  ok("legacy entregas-epp fecha -> 200 y filtra",
    (await request("/api/entregas-epp/paginado?fecha=2026-08-21&tamano=100"))
      .data.contenido.some((x) => x.id === entregaEppR.data.id),
    "fecha no devuelve la entrega de epp");
  ok("legacy asignaciones-consumibles fecha -> 200 y filtra",
    (await request("/api/asignaciones-consumibles/paginado?fecha=2026-08-22&tamano=100"))
      .data.contenido.some((x) => x.id === asigConsR.data.id),
    "fecha no devuelve la asignación de consumible");
  ok("legacy asignaciones-herramientas fecha -> 200 y filtra",
    (await request("/api/asignaciones-herramientas/paginado?fecha=2026-08-23&tamano=100"))
      .data.contenido.some((x) => x.id === asigHerrR.data.id),
    "fecha no devuelve la asignación de herramienta");

  // contrato legacy: orden/dir y filtro desconocido siguen en 400
  ok("legacy compras filtro desconocido -> 400",
    (await request("/api/compras/paginado?campoFantasma=x")).status === 400, "");
  ok("legacy facturas orden invalido -> 400",
    (await request("/api/facturas/paginado?orden=noexiste")).status === 400, "");

  // limpieza regresión (por orden inverso de dependencias)
  await request(`/api/asignaciones-herramientas/${asigHerrR.data.id}`, { method: "DELETE" });
  await request(`/api/herramientas/${herrR.data.id}`, { method: "DELETE" });
  await request(`/api/asignaciones-consumibles/${asigConsR.data.id}`, { method: "DELETE" });
  await request(`/api/consumibles/${consR.data.id}`, { method: "DELETE" });
  await request(`/api/entregas-epp/${entregaEppR.data.id}`, { method: "DELETE" });
  await request(`/api/epp/${eppR.data.id}`, { method: "DELETE" });
  await request(`/api/entregas-ropa/${ropaR.data.id}`, { method: "DELETE" });
  await request(`/api/ordenes-compra/${ordenR.data.id}`, { method: "DELETE" });
  await request(`/api/minutas/${minR.data.id}`, { method: "DELETE" });
  await request(`/api/facturas/${facturaR.data.id}`, { method: "DELETE" });
  await request(`/api/compras/${compraR.data.id}`, { method: "DELETE" });
  await request(`/api/proyectos/${proyR.data.id}`, { method: "DELETE" });
  await request(`/api/contratos/${contratoR.data.id}`, { method: "DELETE" });
  await request(`/api/empleados/${empR.data.id}`, { method: "DELETE" });
  await request(`/api/materiales/${matR.data.id}`, { method: "DELETE" });
  await request(`/api/proveedores/${provR.data.id}`, { method: "DELETE" });

  // limpieza F2
  await request(`/api/ordenes-compra/${ordenF2.data.id}`, { method: "DELETE" });
  await request(`/api/facturas/${facturaF2.data.id}`, { method: "DELETE" });
  await request(`/api/compras/${compraF2.data.id}`, { method: "DELETE" });
  await request(`/api/asignaciones-herramientas/${asigHerrF2.data.id}`, { method: "DELETE" });
  await request(`/api/herramientas/${herrF2.data.id}`, { method: "DELETE" });
  await request(`/api/asignaciones-consumibles/${asigConsF2.data.id}`, { method: "DELETE" });
  await request(`/api/consumibles/${consF2.data.id}`, { method: "DELETE" });
  await request(`/api/entregas-epp/${entregaEppF2.data.id}`, { method: "DELETE" });
  await request(`/api/epp/${eppF2.data.id}`, { method: "DELETE" });
  await request(`/api/entregas-ropa/${ropaF2.data.id}`, { method: "DELETE" });
  await request(`/api/minutas/${minF2.data.id}`, { method: "DELETE" });
  await request(`/api/proyectos/${proyF2.data.id}`, { method: "DELETE" });
  await request(`/api/contratos/${contrF2.data.id}`, { method: "DELETE" });
  await request(`/api/empleados/${empF2.data.id}`, { method: "DELETE" });
  await request(`/api/materiales/${matF2.data.id}`, { method: "DELETE" });
  await request(`/api/proveedores/${provF2.data.id}`, { method: "DELETE" });

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
