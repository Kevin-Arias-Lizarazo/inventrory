const BASE = process.argv[2] || "http://localhost:8080";

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

async function request(path, { method = "GET", body, headers = {}, blob, bin, raw } = {}) {
  const opts = { method, headers: { ...headers } };
  if (raw !== undefined) {
    opts.body = raw;
  } else if (body !== undefined) {
    opts.body = JSON.stringify(body);
    opts.headers["Content-Type"] = "application/json";
  }
  if (blob !== undefined) {
    opts.body = blob;
  }
  const res = await fetch(`${BASE}${path}`, opts);
  if (bin) {
    const buf = await res.arrayBuffer();
    return { status: res.status, data: buf, bytes: buf.byteLength };
  }
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
  console.log(`\nAPI: ${BASE}\n`);
  const runId = Date.now();

  const listaEmpleados = await request("/api/empleados");
  ok("GET /api/empleados", listaEmpleados.status === 200 && Array.isArray(listaEmpleados.data));

  const emp = await request("/api/empleados", {
    method: "POST",
    body: {
      nombre: "María López " + runId,
      documento: "99.888.777",
      cargo: "Ayudante de obra",
      telefono: "+57 300 000 0000",
      correo: "maria@empresa.com",
      fechaIngreso: "2026-01-15",
      hojaVida: "Ingeniera civil, 5 años de experiencia.",
    },
  });
  ok("POST empleado (UTF-8)", emp.status === 201 && emp.data.id > 0, JSON.stringify(emp.data));
  ok("nombre UTF-8 intacto", emp.data.nombre === "María López " + runId, `recibido=${emp.data.nombre}`);
  const empId = emp.data.id;
  ok("código auto de empleado", emp.data.codigo === "E" + empId, `codigo=${emp.data.codigo}`);

  const empDup = await request("/api/empleados", {
    method: "POST",
    body: { nombre: "María López " + runId },
  });
  ok("rechaza nombre duplicado de empleado (400)", empDup.status === 400, `status=${empDup.status}`);

  const listaBusqueda = await request("/api/empleados?q=maria");
  ok("GET empleados?q=maria", listaBusqueda.status === 200 && listaBusqueda.data.some((e) => e.id === empId));

  const contratoEmp = await request("/api/contratos", {
    method: "POST",
    body: { empleado: { id: empId }, fechaInicio: "2026-01-01" },
  });
  ok("POST contrato ACTIVO", contratoEmp.status === 201 && contratoEmp.data.estado === "ACTIVO");
  const listaEmpContratado = await request("/api/empleados");
  ok("empleado figura contratado", listaEmpContratado.data.find((e) => e.id === empId)?.contratado === true);

  const empPut = await request(`/api/empleados/${empId}`, {
    method: "PUT",
    body: { ...emp.data, cargo: "Supervisora" },
  });
  ok("PUT empleado", empPut.status === 200 && empPut.data.cargo === "Supervisora");

  const proy = await request("/api/proyectos", {
    method: "POST",
    body: { nombre: "Edificio Norte " + runId, cliente: "Constructora XYZ", ubicacion: "Bogotá" },
  });
  ok("POST proyecto", proy.status === 201 && proy.data.id > 0);
  ok("código auto de proyecto", proy.data.codigo === "P" + proy.data.id, `codigo=${proy.data.codigo}`);

  const minuta = await request("/api/minutas", {
    method: "POST",
    body: { proyecto: { id: proy.data.id }, hora: "07:30", fecha: "2026-08-12", empleado: { id: empId } },
  });
  ok("POST minuta", minuta.status === 201 && minuta.data.proyecto?.nombre === "Edificio Norte " + runId);

  const minutaSinEmp = await request("/api/minutas", {
    method: "POST",
    body: { proyecto: { id: proy.data.id }, hora: "07:30", fecha: "2026-08-12" },
  });
  ok("rechaza minuta sin empleado (400)", minutaSinEmp.status === 400, `status=${minutaSinEmp.status}`);

  const borrarProyConMinutas = await request(`/api/proyectos/${proy.data.id}`, { method: "DELETE" });
  ok("no permite eliminar proyecto con minutas (409)", borrarProyConMinutas.status === 409, `status=${borrarProyConMinutas.status}`);

  const finalizado = await request(`/api/proyectos/${proy.data.id}/finalizar`, { method: "POST" });
  ok("finalizar proyecto", finalizado.status === 200 && finalizado.data.estado === "FINALIZADO" && !!finalizado.data.fechaFin);

  const ropa = await request("/api/entregas-ropa", {
    method: "POST",
    body: {
      fecha: "2026-08-12",
      fotoUrl: "/archivos/foto_ropa.png",
      firmaUrl: "/archivos/firma_empleado.png",
      observacion: "Botas y overol",
      empleado: { id: empId },
    },
  });
  ok("POST entrega de ropa", ropa.status === 201 && ropa.data.fotoUrl.startsWith("/archivos/"));

  const eppCat = await request("/api/epp", {
    method: "POST",
    body: { nombre: "Casco " + runId, stock: 10, fotoUrl: "/archivos/foto_eppcat.png" },
  });
  ok("POST epp (inventario)", eppCat.status === 201 && eppCat.data.stock === 10 && eppCat.data.fotoUrl === "/archivos/foto_eppcat.png");

  const epp = await request("/api/entregas-epp", {
    method: "POST",
    body: {
      fecha: "2026-08-12",
      epp: { id: eppCat.data.id },
      observacion: "Entrega trimestral",
      fotoUrl: "/archivos/foto_epp.png",
      firmaUrl: "/archivos/firma_epp.png",
      empleado: { id: empId },
    },
  });
  ok("POST entrega de EPP (con foto y firma)", epp.status === 201 && epp.data.epp?.id === eppCat.data.id && epp.data.fotoUrl === "/archivos/foto_epp.png" && epp.data.firmaUrl === "/archivos/firma_epp.png");

  const eppStock1 = await request(`/api/epp/${eppCat.data.id}`);
  ok("stock epp tras entrega = 9", eppStock1.data.stock === 9, `stock=${eppStock1.data.stock}`);

  const eppSinStock = await request("/api/epp", { method: "POST", body: { nombre: "Casco 2 " + runId, stock: 0 } });
  const entregaSinStock = await request("/api/entregas-epp", {
    method: "POST",
    body: { fecha: "2026-08-12", epp: { id: eppSinStock.data.id }, empleado: { id: empId } },
  });
  ok("bloquea entrega EPP sin stock (400)", entregaSinStock.status === 400, `status=${entregaSinStock.status}`);

  const eppDel = await request(`/api/entregas-epp/${epp.data.id}`, { method: "DELETE" });
  ok("eliminar entrega EPP (204)", eppDel.status === 204, `status=${eppDel.status}`);

  const eppStock2 = await request(`/api/epp/${eppCat.data.id}`);
  ok("stock epp restaurado = 10", eppStock2.data.stock === 10, `stock=${eppStock2.data.stock}`);

  const eppEntrega2 = await request("/api/entregas-epp", {
    method: "POST",
    body: { fecha: "2026-08-12", epp: { id: eppCat.data.id }, empleado: { id: empId } },
  });
  const eppFilt = await request("/api/entregas-epp/filtradas?fecha=2026-08-12&orden=asc&pagina=0&tamano=100");
  ok(
    "entregas-epp/filtradas por fecha (asc)",
    eppFilt.data.contenido.some((m) => m.id === eppEntrega2.data.id) && eppFilt.data.contenido.every((m) => m.fecha === "2026-08-12")
  );
  const eppFiltEmp = await request(`/api/entregas-epp/filtradas?empleadoId=${empId}&pagina=0&tamano=100`);
  ok("entregas-epp/filtradas por empleado", eppFiltEmp.data.contenido.every((m) => m.empleado?.id === empId));
  await request(`/api/entregas-epp/${eppEntrega2.data.id}`, { method: "DELETE" });

  const herr = await request("/api/herramientas", {
    method: "POST",
    body: { nombre: "Martillo " + runId, marca: "Stanley", cantidadTotal: 5, fotoUrl: "/archivos/foto_herr.png" },
  });
  ok("POST herramienta (inventario)", herr.status === 201 && herr.data.id > 0 && herr.data.cantidadTotal === 5 && herr.data.fotoUrl === "/archivos/foto_herr.png");
  const herrId = herr.data.id;
  ok("código auto de herramienta", herr.data.codigo === "H" + herrId, `codigo=${herr.data.codigo}`);

  const inv1 = await request("/api/herramientas");
  const h1 = inv1.data.find((h) => h.id === herrId);
  ok("cantidades iniciales (5 total, 0 asignada, 0 dañada, 5 disp)", h1.cantidadTotal === 5 && h1.cantidadAsignada === 0 && h1.cantidadDanada === 0 && h1.cantidadDisponible === 5);

  const asig1 = await request("/api/asignaciones-herramientas", {
    method: "POST",
    body: { herramienta: { id: herrId }, lugar: "Obra 1", fecha: "2026-08-12", devuelta: false, empleado: { id: empId } },
  });
  const asig2 = await request("/api/asignaciones-herramientas", {
    method: "POST",
    body: { herramienta: { id: herrId }, lugar: "Obra 2", fecha: "2026-08-12", devuelta: false, empleado: { id: empId } },
  });
  ok("dos asignaciones creadas", asig1.status === 201 && asig2.status === 201);

  const inv2 = await request("/api/herramientas");
  const h2 = inv2.data.find((h) => h.id === herrId);
  ok("tras asignar 2 -> asignada 2, disponible 3", h2.cantidadAsignada === 2 && h2.cantidadDisponible === 3);

  const asig3 = await request("/api/asignaciones-herramientas", {
    method: "POST",
    body: { herramienta: { id: herrId }, lugar: "Obra 3", fecha: "2026-08-12", devuelta: false, empleado: { id: empId } },
  });
  const inv3 = await request("/api/herramientas");
  const h3 = inv3.data.find((h) => h.id === herrId);
  ok("tercera asignación ok -> asignada 3, disponible 2", asig3.status === 201 && h3.cantidadAsignada === 3 && h3.cantidadDisponible === 2);

  const danada1 = await request(`/api/herramientas/${herrId}/danada`, { method: "POST" });
  ok("marcar 1 dañada", danada1.status === 200);
  const inv4 = await request("/api/herramientas");
  const h4 = inv4.data.find((h) => h.id === herrId);
  ok("tras dañada -> dañada 1, disponible 1", h4.cantidadDanada === 1 && h4.cantidadDisponible === 1);

  const perdida1 = await request(`/api/herramientas/${herrId}/perdida`, { method: "POST" });
  ok("marcar 1 pérdida (listable en API)", perdida1.status === 200 && perdida1.data.cantidadPerdida === 1);

  const invPerd = await request("/api/herramientas");
  const hPerd = invPerd.data.find((h) => h.id === herrId);
  ok("tras pérdida -> perdida 1, disponible 0", hPerd.cantidadPerdida === 1 && hPerd.cantidadDisponible === 0);

  const asignarSinDisp = await request("/api/asignaciones-herramientas", {
    method: "POST",
    body: { herramienta: { id: herrId }, lugar: "Obra 4", fecha: "2026-08-12", devuelta: false, empleado: { id: empId } },
  });
  ok("rechaza asignar sin unidades disponibles (400)", asignarSinDisp.status === 400, `status=${asignarSinDisp.status}`);

  const reparar1 = await request(`/api/herramientas/${herrId}/reparar`, { method: "POST" });
  ok("reparar 1 dañada", reparar1.status === 200 && reparar1.data.cantidadDanada === 0);
  const inv5 = await request("/api/herramientas");
  const h5 = inv5.data.find((h) => h.id === herrId);
  ok("tras reparar -> dañada 0, disponible 1", h5.cantidadDanada === 0 && h5.cantidadDisponible === 1);

  const borrarEnUso = await request(`/api/herramientas/${herrId}`, { method: "DELETE" });
  ok("no permite eliminar herramienta con asignaciones activas (409)", borrarEnUso.status === 409, `status=${borrarEnUso.status}`);

  await request(`/api/asignaciones-herramientas/${asig1.data.id}`, { method: "DELETE" });
  await request(`/api/asignaciones-herramientas/${asig2.data.id}`, { method: "DELETE" });
  await request(`/api/asignaciones-herramientas/${asig3.data.id}`, { method: "DELETE" });
  const inv6 = await request("/api/herramientas");
  const h6 = inv6.data.find((h) => h.id === herrId);
  ok("tras devolver todas -> asignada 0, disponible 4", h6.cantidadAsignada === 0 && h6.cantidadDisponible === 4);

  const borrarLibre = await request(`/api/herramientas/${herrId}`, { method: "DELETE" });
  ok("eliminar herramienta sin asignaciones activas (204)", borrarLibre.status === 204, `status=${borrarLibre.status}`);

  const borrada = await request(`/api/herramientas/${herrId}`);
  ok("herramienta ya no existe", borrada.status === 404);

  const hMov = await request("/api/herramientas", { method: "POST", body: { nombre: "Compresor " + runId, cantidadTotal: 2 } });
  const hmIng = await request(`/api/herramientas/${hMov.data.id}/movimientos`, {
    method: "POST",
    body: { tipo: "INGRESO", cantidad: 3, fecha: "2026-08-12", observacion: "Compra" },
  });
  ok("movimiento ingreso herramienta", hmIng.status === 201 && hmIng.data.cantidad === 3);

  const hMovDesp = await request(`/api/herramientas/${hMov.data.id}`);
  ok("cantidad total tras ingreso = 5", hMovDesp.data.cantidadTotal === 5, `total=${hMovDesp.data.cantidadTotal}`);

  const hmEgr = await request(`/api/herramientas/${hMov.data.id}/movimientos`, {
    method: "POST",
    body: { tipo: "EGRESO", cantidad: 1, fecha: "2026-08-12", observacion: "Baja" },
  });
  ok("movimiento egreso herramienta", hmEgr.status === 201);

  const hMovDesp2 = await request(`/api/herramientas/${hMov.data.id}`);
  ok("cantidad total tras egreso = 4", hMovDesp2.data.cantidadTotal === 4, `total=${hMovDesp2.data.cantidadTotal}`);

  const hmList = await request(`/api/herramientas/${hMov.data.id}/movimientos`);
  ok("lista movimientos herramienta", hmList.status === 200 && hmList.data.length === 2);

  const hmOver = await request(`/api/herramientas/${hMov.data.id}/movimientos`, {
    method: "POST",
    body: { tipo: "EGRESO", cantidad: 999, fecha: "2026-08-12" },
  });
  ok("rechaza egreso que deja sin unidades (400)", hmOver.status === 400, `status=${hmOver.status}`);

  const hmDel = await request(`/api/movimientos-herramientas/${hmIng.data.id}`, { method: "DELETE" });
  ok("eliminar movimiento herramienta (204)", hmDel.status === 204, `status=${hmDel.status}`);

  const hMovDesp3 = await request(`/api/herramientas/${hMov.data.id}`);
  ok("cantidad restaurada tras eliminar movimiento = 1", hMovDesp3.data.cantidadTotal === 1, `total=${hMovDesp3.data.cantidadTotal}`);

  const hMovElim = await request(`/api/herramientas/${hMov.data.id}`, { method: "DELETE" });
  ok("eliminar herramienta con movimientos (204)", hMovElim.status === 204, `status=${hMovElim.status}`);

  const empCont = await request("/api/empleados", { method: "POST", body: { nombre: "Contratado " + runId } });
  const minSinContrato = await request("/api/minutas", {
    method: "POST",
    body: { proyecto: { id: proy.data.id }, hora: "08:00", fecha: "2026-08-12", empleado: { id: empCont.data.id } },
  });
  ok("bloquea minuta sin contrato activo (400)", minSinContrato.status === 400, `status=${minSinContrato.status}`);

  const contrCont = await request("/api/contratos", {
    method: "POST",
    body: { empleado: { id: empCont.data.id }, fechaInicio: "2026-01-01" },
  });
  ok("POST contrato (empleado pasa a contratado)", contrCont.status === 201);

  const minConContrato = await request("/api/minutas", {
    method: "POST",
    body: { proyecto: { id: proy.data.id }, hora: "08:00", fecha: "2026-08-12", empleado: { id: empCont.data.id } },
  });
  ok("minuta permitida con contrato (201)", minConContrato.status === 201);

  const hRet = await request("/api/herramientas", { method: "POST", body: { nombre: "Nivel láser " + runId, cantidadTotal: 1 } });
  const asigRet = await request("/api/asignaciones-herramientas", {
    method: "POST",
    body: { herramienta: { id: hRet.data.id }, lugar: "Obra", fecha: "2026-08-12", devuelta: false, empleado: { id: empCont.data.id } },
  });
  ok("asignación permitida con contrato (201)", asigRet.status === 201);

  const concluido = await request(`/api/contratos/${contrCont.data.id}/concluir`, { method: "POST" });
  ok("concluir contrato", concluido.status === 200 && concluido.data.estado === "CONCLUIDO");

  const minPost = await request("/api/minutas", {
    method: "POST",
    body: { proyecto: { id: proy.data.id }, hora: "08:00", fecha: "2026-08-12", empleado: { id: empCont.data.id } },
  });
  ok("bloquea minuta tras concluir contrato (400)", minPost.status === 400, `status=${minPost.status}`);

  const eppPost = await request("/api/entregas-epp", {
    method: "POST",
    body: { fecha: "2026-08-12", epp: { id: eppCat.data.id }, empleado: { id: empCont.data.id } },
  });
  ok("bloquea entrega EPP tras concluir (400)", eppPost.status === 400, `status=${eppPost.status}`);

  const ropaPost = await request("/api/entregas-ropa", {
    method: "POST",
    body: { fecha: "2026-08-12", empleado: { id: empCont.data.id } },
  });
  ok("bloquea entrega ropa tras concluir (400)", ropaPost.status === 400, `status=${ropaPost.status}`);

  const asigPost = await request("/api/asignaciones-herramientas", {
    method: "POST",
    body: { herramienta: { id: hRet.data.id }, lugar: "Otra", fecha: "2026-08-12", devuelta: false, empleado: { id: empCont.data.id } },
  });
  ok("bloquea nueva asignación tras concluir (400)", asigPost.status === 400, `status=${asigPost.status}`);

  const devolucion = await request(`/api/asignaciones-herramientas/${asigRet.data.id}`, {
    method: "PUT",
    body: {
      ...asigRet.data,
      devuelta: true,
      fechaDevolucion: "2026-08-12",
      herramienta: { id: hRet.data.id },
      empleado: { id: empCont.data.id },
    },
  });
  ok("permite devolución sin contrato activo (200)", devolucion.status === 200 && devolucion.data.devuelta === true);

  const listaEmpNoContratado = await request("/api/empleados");
  ok("empleado ya no figura contratado", listaEmpNoContratado.data.find((e) => e.id === empCont.data.id)?.contratado === false);

  const soloContratados = await request("/api/empleados?contratados=true");
  ok(
    "endpoint ?contratados=true incluye contratados y excluye sin contrato",
    soloContratados.data.some((e) => e.id === empId) === true &&
      soloContratados.data.some((e) => e.id === empCont.data.id) === false
  );

  const mat = await request("/api/materiales", {
    method: "POST",
    body: { nombre: "Varilla 3/8 " + runId, unidad: "unidad", descripcion: "Acero corrugado", fotoUrl: "/archivos/foto_mat.png" },
  });
  ok("POST material (con imagen)", mat.status === 201 && mat.data.stock === 0 && mat.data.fotoUrl === "/archivos/foto_mat.png");

  const ingreso = await request(`/api/materiales/${mat.data.id}/movimientos`, {
    method: "POST",
    body: { tipo: "INGRESO", cantidad: 100, fecha: "2026-08-12", observacion: "Compra #1" },
  });
  ok("POST ingreso", ingreso.status === 201 && ingreso.data.cantidad === 100);

  const despuesDeIngreso = await request(`/api/materiales/${mat.data.id}`);
  ok("stock tras ingreso = 100", despuesDeIngreso.data.stock === 100, `stock=${despuesDeIngreso.data.stock}`);

  await request(`/api/materiales/${mat.data.id}/movimientos`, {
    method: "POST",
    body: { tipo: "EGRESO", cantidad: 30, fecha: "2026-08-12", observacion: "Uso en obra" },
  });
  const despuesDeEgreso = await request(`/api/materiales/${mat.data.id}`);
  ok("stock tras egreso = 70", despuesDeEgreso.data.stock === 70, `stock=${despuesDeEgreso.data.stock}`);

  const egresoExcesivo = await request(`/api/materiales/${mat.data.id}/movimientos`, {
    method: "POST",
    body: { tipo: "EGRESO", cantidad: 99999, fecha: "2026-08-12" },
  });
  ok("egreso excesivo bloqueado (400)", egresoExcesivo.status === 400, `status=${egresoExcesivo.status}`);

  const movs = await request(`/api/materiales/${mat.data.id}/movimientos`);
  ok("GET movimientos del material", movs.status === 200 && movs.data.length === 2);

  const cons = await request("/api/consumibles", {
    method: "POST",
    body: { nombre: "Guantes de carnaza " + runId, unidad: "par", fotoUrl: "/archivos/foto_cons.png" },
  });
  ok("código auto de consumible", cons.data.codigo === "C" + cons.data.id, `codigo=${cons.data.codigo}`);
  await request(`/api/consumibles/${cons.data.id}/movimientos`, {
    method: "POST",
    body: { tipo: "INGRESO", cantidad: 50, fecha: "2026-08-12", observacion: "Reabastecimiento" },
  });
  const consStock = await request(`/api/consumibles/${cons.data.id}`);
  ok("consumible stock tras ingreso = 50", consStock.data.stock === 50, `stock=${consStock.data.stock}`);

  const asigCons = await request("/api/asignaciones-consumibles", {
    method: "POST",
    body: { consumible: { id: cons.data.id }, proyecto: { id: proy.data.id }, cantidad: 5, fecha: "2026-08-12", observacion: "Entrega" },
  });
  ok("POST asignación consumible", asigCons.status === 201 && asigCons.data.cantidad === 5 && asigCons.data.proyecto?.id === proy.data.id);

  const consStock2 = await request(`/api/consumibles/${cons.data.id}`);
  ok("stock consumible tras asignar 5 = 45", consStock2.data.stock === 45, `stock=${consStock2.data.stock}`);

  const overCons = await request("/api/asignaciones-consumibles", {
    method: "POST",
    body: { consumible: { id: cons.data.id }, proyecto: { id: proy.data.id }, cantidad: 9999, fecha: "2026-08-12" },
  });
  ok("rechaza asignar más de lo disponible (400)", overCons.status === 400, `status=${overCons.status}`);

  const sinProyectoCons = await request("/api/asignaciones-consumibles", {
    method: "POST",
    body: { consumible: { id: cons.data.id }, cantidad: 1, fecha: "2026-08-12" },
  });
  ok("rechaza asignación sin proyecto (400)", sinProyectoCons.status === 400, `status=${sinProyectoCons.status}`);

  const delCons = await request(`/api/asignaciones-consumibles/${asigCons.data.id}`, { method: "DELETE" });
  ok("eliminar asignación consumible (204)", delCons.status === 204, `status=${delCons.status}`);

  const consStock3 = await request(`/api/consumibles/${cons.data.id}`);
  ok("stock restaurado tras eliminar = 50", consStock3.data.stock === 50, `stock=${consStock3.data.stock}`);

  const eEsc = await request("/api/empleados", { method: "POST", body: { nombre: "Escan " + runId } });
  await request("/api/contratos", { method: "POST", body: { empleado: { id: eEsc.data.id }, fechaInicio: "2026-01-01" } });
  const codEmp = "E" + eEsc.data.id;

  const hEsc = await request("/api/herramientas", { method: "POST", body: { nombre: "Martillo Esc " + runId, cantidadTotal: 4 } });
  const codHer = "H" + hEsc.data.id;

  const cEsc = await request("/api/consumibles", { method: "POST", body: { nombre: "Disco Esc " + runId, stock: 8 } });
  const codCons = "C" + cEsc.data.id;

  const pEsc = await request("/api/proyectos", { method: "POST", body: { nombre: "Proy Esc " + runId } });
  const codProy = "P" + pEsc.data.id;

  ok("códigos auto generados", codEmp === "E" + eEsc.data.id && codHer === "H" + hEsc.data.id && codCons === "C" + cEsc.data.id && codProy === "P" + pEsc.data.id);

  const proysActivos = await request("/api/proyectos?estado=ACTIVO");
  ok(
    "endpoint proyectos ?estado=ACTIVO excluye finalizados",
    proysActivos.data.some((p) => p.id === pEsc.data.id) === true &&
      proysActivos.data.some((p) => p.id === proy.data.id) === false
  );

  const escAH = await request("/api/escaneos", {
    method: "POST",
    body: [{ operacion: "AH", destinoCodigo: codEmp, items: [{ codigo: codHer, cantidad: 2 }] }],
  });
  ok("escaneo AH (2 registros)", escAH.data[0].ok === true && escAH.data[0].registrosCreados === 2);

  const hEsc1 = await request(`/api/herramientas/${hEsc.data.id}`);
  ok("disponibles tras AH = 2", hEsc1.data.cantidadDisponible === 2, `disp=${hEsc1.data.cantidadDisponible}`);

  const escDH = await request("/api/escaneos", {
    method: "POST",
    body: [{ operacion: "DH", destinoCodigo: codEmp, items: [{ codigo: codHer, cantidad: 1 }] }],
  });
  ok("escaneo DH (1 devuelta)", escDH.data[0].ok === true && escDH.data[0].registrosCreados === 1);

  const hEsc2 = await request(`/api/herramientas/${hEsc.data.id}`);
  ok("disponibles tras DH = 3", hEsc2.data.cantidadDisponible === 3, `disp=${hEsc2.data.cantidadDisponible}`);

  const escAC = await request("/api/escaneos", {
    method: "POST",
    body: [{ operacion: "AC", destinoCodigo: codProy, items: [{ codigo: codCons, cantidad: 3 }] }],
  });
  ok("escaneo AC", escAC.data[0].ok === true && escAC.data[0].registrosCreados === 1);

  const cEsc1 = await request(`/api/consumibles/${cEsc.data.id}`);
  ok("stock consumible tras AC = 5", cEsc1.data.stock === 5, `stock=${cEsc1.data.stock}`);

  const escLote = await request("/api/escaneos", {
    method: "POST",
    body: [
      { operacion: "AH", destinoCodigo: codEmp, items: [{ codigo: codHer, cantidad: 1 }] },
      { operacion: "AH", destinoCodigo: "E999999", items: [{ codigo: codHer, cantidad: 1 }] },
      { operacion: "AC", destinoCodigo: codProy, items: [{ codigo: codCons, cantidad: 1 }] },
    ],
  });
  ok("lote aplica bloque por bloque", escLote.data[0].ok === true && escLote.data[1].ok === false && escLote.data[2].ok === true);

  const escErr = await request("/api/escaneos", {
    method: "POST",
    body: [{ operacion: "AH", destinoCodigo: codEmp, items: [{ codigo: codCons, cantidad: 1 }] }],
  });
  ok("rechaza ítem de tipo equivocado (C# en AH)", escErr.data[0].ok === false);

  const escDest = await request("/api/escaneos", {
    method: "POST",
    body: [{ operacion: "AC", destinoCodigo: codEmp, items: [{ codigo: codCons, cantidad: 1 }] }],
  });
  ok("rechaza destino empleado en AC", escDest.data[0].ok === false);

  const pagCons = await request("/api/consumibles/paginado?pagina=0&tamano=30");
  ok("paginado consumibles (total>0, <=30)", pagCons.data.total > 0 && Array.isArray(pagCons.data.contenido) && pagCons.data.contenido.length <= 30);

  const pagHer = await request("/api/herramientas/paginado?pagina=0&tamano=2");
  ok("paginado herramientas (tamano 2)", pagHer.data.total > 0 && pagHer.data.contenido.length <= 2);

  const pagEmp = await request("/api/empleados/paginado?pagina=0&tamano=15");
  ok("paginado empleados", pagEmp.data.total > 0 && Array.isArray(pagEmp.data.contenido));

  const pagMax = await request("/api/materiales/paginado?pagina=0&tamano=9999");
  ok("tope de tamaño a 100", pagMax.data.tamano === 100 && pagMax.data.contenido.length <= 100);

  const pagFin = await request("/api/proyectos/paginado?estado=ACTIVO&pagina=0&tamano=30");
  ok("paginado proyectos ?estado=ACTIVO", pagFin.data.contenido.every((p) => p.estado === "ACTIVO"));

  const lote = await request("/api/minutas/lote", {
    method: "POST",
    body: [
      { fecha: "2026-08-12", hora: "08:00", empleado: { id: eEsc.data.id }, proyecto: { id: pEsc.data.id } },
      { fecha: "2026-08-12", hora: "08:05", empleado: { id: empId }, proyecto: { id: pEsc.data.id } },
    ],
  });
  ok("minuta del día en lote (2 creadas)", lote.status === 200 && lote.data.creadas === 2, JSON.stringify(lote.data));

  const recientes = await request("/api/minutas/recientes?pagina=0&tamano=100");
  const fechas = recientes.data.contenido.map((m) => `${m.fecha} ${m.hora}`);
  const ordenado = fechas.every((f, i) => i === 0 || fechas[i - 1] >= f);
  ok("endpoint /api/minutas/recientes ordenado desc", recientes.status === 200 && ordenado, JSON.stringify(fechas));

  const filtradas = await request("/api/minutas/filtradas?fecha=2026-08-12&orden=asc&pagina=0&tamano=100");
  ok(
    "minutas/filtradas por fecha y orden asc",
    filtradas.data.contenido.length > 0 &&
      filtradas.data.contenido.every((m) => m.fecha === "2026-08-12") &&
      filtradas.data.contenido
        .map((m) => `${m.fecha} ${m.hora}`)
        .every((f, i, arr) => i === 0 || arr[i - 1] <= f)
  );

  const filtEmp = await request(`/api/minutas/filtradas?empleadoId=${eEsc.data.id}&pagina=0&tamano=100`);
  ok(
    "minutas/filtradas por empleado",
    filtEmp.data.contenido.length > 0 && filtEmp.data.contenido.every((m) => m.empleado?.id === eEsc.data.id)
  );

  const subida = await request("/api/archivos", {
    method: "POST",
    blob: await crearImagenPng(),
  });
  ok("POST archivo (multipart)", subida.status === 200 && subida.data.url?.startsWith("/archivos/"), JSON.stringify(subida.data));

  const archivo = await fetch(`${BASE}${subida.data.url}`);
  ok("GET archivo servido", archivo.status === 200 && (archivo.headers.get("content-type") || "").includes("image/png"));

  await request(`/api/minutas/${minuta.data.id}`, { method: "DELETE" });
  const minutaEliminada = await request(`/api/minutas/${minuta.data.id}`);
  ok("DELETE minuta", minutaEliminada.status === 404);

  const empTmp = await request("/api/empleados", { method: "POST", body: { nombre: "Temporal " + runId } });
  const empTmpEliminado = await request(`/api/empleados/${empTmp.data.id}`, { method: "DELETE" });
  ok("DELETE empleado sin referencias", empTmpEliminado.status === 204);

  await request(`/api/empleados/${empId}`, { method: "DELETE" });
  const empConRefs = await request("/api/empleados", { method: "POST", body: { nombre: "Bloqueado " + runId } });
  await request("/api/contratos", {
    method: "POST",
    body: { empleado: { id: empConRefs.data.id }, fechaInicio: "2026-01-01" },
  });
  await request("/api/minutas", {
    method: "POST",
    body: { proyecto: { id: proy.data.id }, hora: "08:00", fecha: "2026-08-12", empleado: { id: empConRefs.data.id } },
  });
  const borradoProtegido = await request(`/api/empleados/${empConRefs.data.id}`, { method: "DELETE" });
  ok("DELETE empleado con referencias protegido (409)", borradoProtegido.status === 409, `status=${borradoProtegido.status}`);

  // ===== Proveedores =====
  const prov = await request("/api/proveedores", {
    method: "POST",
    body: {
      nombre: "Ferretería Central " + runId,
      telefono: "601 000 0000",
      correo: "ventas@ferreteria.com",
      direccion: "Calle 10 # 5-20",
    },
  });
  ok("POST proveedor", prov.status === 201 && prov.data.id > 0 && prov.data.nombre.includes("Ferretería"));
  const provId = prov.data.id;

  const provDup = await request("/api/proveedores", {
    method: "POST",
    body: { nombre: "Ferretería Central " + runId },
  });
  ok("rechaza proveedor duplicado (400)", provDup.status === 400, `status=${provDup.status}`);

  const provList = await request("/api/proveedores");
  ok("GET proveedores", provList.status === 200 && provList.data.some((p) => p.id === provId));

  const provPag = await request("/api/proveedores/paginado?pagina=0&tamano=30");
  ok("paginado proveedores", provPag.data.total > 0 && Array.isArray(provPag.data.contenido));

  const provPut = await request(`/api/proveedores/${provId}`, {
    method: "PUT",
    body: { ...prov.data, telefono: "601 111 1111" },
  });
  ok("PUT proveedor", provPut.status === 200 && provPut.data.telefono === "601 111 1111");

  // ===== Compra sin factura (entrada a stock sin precio) =====
  const mCompra = await request("/api/materiales", {
    method: "POST",
    body: { nombre: "Cemento Compra " + runId, marca: "Argos", unidad: "bulto" },
  });
  ok("POST material para compras (con marca)", mCompra.status === 201 && mCompra.data.marca === "Argos" && mCompra.data.stock === 0);
  const mCompraId = mCompra.data.id;
  ok("material sin costo inicial", mCompra.data.ultimoCosto == null);

  const compra = await request("/api/compras", {
    method: "POST",
    body: {
      fecha: "2026-08-12",
      observacion: "Compra de prueba",
      proveedor: { id: provId },
      lineas: [{ tipo: "MATERIAL", productoId: mCompraId, cantidad: 10 }],
    },
  });
  ok(
    "POST compra sin factura",
    compra.status === 201 &&
      compra.data.id > 0 &&
      compra.data.facturaId == null &&
      compra.data.lineas.length === 1 &&
      compra.data.proveedor?.id === provId
  );
  const compraId = compra.data.id;

  const mCompra1 = await request(`/api/materiales/${mCompraId}`);
  ok("stock material tras compra = 10", mCompra1.data.stock === 10, `stock=${mCompra1.data.stock}`);
  ok("costo intacto sin factura", mCompra1.data.ultimoCosto == null, `costo=${mCompra1.data.ultimoCosto}`);

  const comprasList = await request("/api/compras");
  ok("GET compras", comprasList.status === 200 && comprasList.data.some((c) => c.id === compraId));

  // ===== Factura vincula compra existente (sin doblar stock) =====
  const facturaVin = await request("/api/facturas", {
    method: "POST",
    body: {
      numero: "F-100",
      fecha: "2026-08-12",
      proveedor: { id: provId },
      compraId: compraId,
      lineas: [{ tipo: "MATERIAL", productoId: mCompraId, cantidad: 10, costoUnitario: 5000 }],
    },
  });
  ok(
    "POST factura vincula compra",
    facturaVin.status === 201 && facturaVin.data.compraId === compraId && facturaVin.data.total === 50000
  );
  const facturaVinId = facturaVin.data.id;

  const mCompra2 = await request(`/api/materiales/${mCompraId}`);
  ok("stock NO se dobla al facturar compra existente (10)", mCompra2.data.stock === 10, `stock=${mCompra2.data.stock}`);
  ok("ultimoCosto tras facturar = 5000", mCompra2.data.ultimoCosto === 5000, `costo=${mCompra2.data.ultimoCosto}`);

  const comprasEstado = await request("/api/compras");
  ok("compra pasa a facturada", comprasEstado.data.find((c) => c.id === compraId)?.facturaId === facturaVinId);

  // ===== Factura con crearCompra=true (sube stock) =====
  const facturaCrea = await request("/api/facturas", {
    method: "POST",
    body: {
      fecha: "2026-08-13",
      crearCompra: true,
      lineas: [{ tipo: "MATERIAL", productoId: mCompraId, cantidad: 5, costoUnitario: 4000 }],
    },
  });
  ok("POST factura crea compra vinculada", facturaCrea.status === 201 && facturaCrea.data.compraId != null);
  const facturaCreaId = facturaCrea.data.id;

  const mCompra3 = await request(`/api/materiales/${mCompraId}`);
  ok("stock tras factura con compra = 15", mCompra3.data.stock === 15, `stock=${mCompra3.data.stock}`);
  ok("ultimoCosto = 4000 (más reciente)", mCompra3.data.ultimoCosto === 4000, `costo=${mCompra3.data.ultimoCosto}`);

  const comprasCreadas = await request("/api/compras");
  ok("compra creada por factura queda vinculada", comprasCreadas.data.some((c) => c.facturaId === facturaCreaId));

  // ===== Factura informal (sin proveedor, sin número, standalone) =====
  const facturaInformal = await request("/api/facturas", {
    method: "POST",
    body: {
      fecha: "2026-08-14",
      lineas: [{ tipo: "MATERIAL", productoId: mCompraId, cantidad: 2, costoUnitario: 6000 }],
    },
  });
  ok(
    "POST factura informal",
    facturaInformal.status === 201 && facturaInformal.data.compraId == null && facturaInformal.data.proveedor == null
  );
  const facturaInformalId = facturaInformal.data.id;

  const mCompra4 = await request(`/api/materiales/${mCompraId}`);
  ok("informal no toca stock (15)", mCompra4.data.stock === 15, `stock=${mCompra4.data.stock}`);
  ok("ultimoCosto = 6000 (la más reciente)", mCompra4.data.ultimoCosto === 6000, `costo=${mCompra4.data.ultimoCosto}`);

  // ===== Editar factura recalcula costo =====
  const facturaInformalEdit = await request(`/api/facturas/${facturaInformalId}`, {
    method: "PUT",
    body: {
      numero: "INF-1",
      fecha: "2026-08-14",
      observacion: "Ajuste",
      lineas: [{ tipo: "MATERIAL", productoId: mCompraId, cantidad: 2, costoUnitario: 7000 }],
    },
  });
  ok("PUT factura informal", facturaInformalEdit.status === 200 && facturaInformalEdit.data.total === 14000);
  const mCompra5 = await request(`/api/materiales/${mCompraId}`);
  ok("ultimoCosto tras editar = 7000", mCompra5.data.ultimoCosto === 7000, `costo=${mCompra5.data.ultimoCosto}`);

  // ===== Rechazos =====
  const facturaVinEdit = await request(`/api/facturas/${facturaVinId}`, {
    method: "PUT",
    body: {
      fecha: "2026-08-15",
      lineas: [{ tipo: "MATERIAL", productoId: mCompraId, cantidad: 10, costoUnitario: 5000 }],
    },
  });
  ok("rechaza editar factura vinculada a compra (409)", facturaVinEdit.status === 409, `status=${facturaVinEdit.status}`);

  const compraEditFacturada = await request(`/api/compras/${compraId}`, {
    method: "PUT",
    body: { fecha: "2026-08-12", lineas: [{ tipo: "MATERIAL", productoId: mCompraId, cantidad: 3 }] },
  });
  ok("rechaza editar compra facturada (409)", compraEditFacturada.status === 409, `status=${compraEditFacturada.status}`);

  const facturaDoble = await request("/api/facturas", {
    method: "POST",
    body: {
      fecha: "2026-08-15",
      compraId: compraId,
      lineas: [{ tipo: "MATERIAL", productoId: mCompraId, cantidad: 1, costoUnitario: 100 }],
    },
  });
  ok("rechaza facturar compra ya facturada (409)", facturaDoble.status === 409, `status=${facturaDoble.status}`);

  // ===== Eliminar facturas recalcula costo =====
  const delInformal = await request(`/api/facturas/${facturaInformalId}`, { method: "DELETE" });
  ok("DELETE factura informal (204)", delInformal.status === 204, `status=${delInformal.status}`);
  const mCompra6 = await request(`/api/materiales/${mCompraId}`);
  ok("ultimoCosto tras eliminar informal = 4000", mCompra6.data.ultimoCosto === 4000, `costo=${mCompra6.data.ultimoCosto}`);

  const delFacturaCrea = await request(`/api/facturas/${facturaCreaId}`, { method: "DELETE" });
  ok("DELETE factura con compra creada (204)", delFacturaCrea.status === 204, `status=${delFacturaCrea.status}`);
  const mCompra6b = await request(`/api/materiales/${mCompraId}`);
  ok("ultimoCosto tras eliminar = 5000", mCompra6b.data.ultimoCosto === 5000, `costo=${mCompra6b.data.ultimoCosto}`);
  const compraDesvinculada = await request("/api/compras");
  ok("compra creada queda sin factura tras eliminar", compraDesvinculada.data.some((c) => c.facturaId == null && c.lineas.length === 1));

  // ===== Compra independiente: editar y eliminar revierten stock =====
  const compra2 = await request("/api/compras", {
    method: "POST",
    body: {
      fecha: "2026-08-15",
      lineas: [{ tipo: "MATERIAL", productoId: mCompraId, cantidad: 8 }],
    },
  });
  ok("POST segunda compra", compra2.status === 201 && compra2.data.id > 0);
  const compra2Id = compra2.data.id;

  const mCompra7 = await request(`/api/materiales/${mCompraId}`);
  ok("stock tras segunda compra = 23", mCompra7.data.stock === 23, `stock=${mCompra7.data.stock}`);

  const compra2Edit = await request(`/api/compras/${compra2Id}`, {
    method: "PUT",
    body: {
      fecha: "2026-08-15",
      lineas: [{ tipo: "MATERIAL", productoId: mCompraId, cantidad: 3 }],
    },
  });
  ok("PUT compra sin facturar", compra2Edit.status === 200);
  const mCompra8 = await request(`/api/materiales/${mCompraId}`);
  ok("stock tras editar compra (revierte 8, suma 3) = 18", mCompra8.data.stock === 18, `stock=${mCompra8.data.stock}`);

  const movCompra2 = await request(`/api/materiales/${mCompraId}/movimientos`);
  ok("movimientos de compra etiquetados", movCompra2.data.filter((m) => m.observacion === "Compra #" + compra2Id).length === 1);

  const delCompra2 = await request(`/api/compras/${compra2Id}`, { method: "DELETE" });
  ok("DELETE compra sin facturar (204)", delCompra2.status === 204, `status=${delCompra2.status}`);
  const mCompra9 = await request(`/api/materiales/${mCompraId}`);
  ok("stock tras eliminar compra = 15", mCompra9.data.stock === 15, `stock=${mCompra9.data.stock}`);
  const movCompra2b = await request(`/api/materiales/${mCompraId}/movimientos`);
  ok("movimientos de compra eliminados", movCompra2b.data.filter((m) => m.observacion === "Compra #" + compra2Id).length === 0);

  // ===== Ropa (solo registro, sin stock) =====
  const compraRopa = await request("/api/compras", {
    method: "POST",
    body: { fecha: "2026-08-15", lineas: [{ tipo: "ROPA", descripcion: "Overol azul", cantidad: 4 }] },
  });
  ok("POST compra de ropa", compraRopa.status === 201 && compraRopa.data.lineas[0].descripcion === "Overol azul");
  const mCompra10 = await request(`/api/materiales/${mCompraId}`);
  ok("ropa no toca stock (15)", mCompra10.data.stock === 15, `stock=${mCompra10.data.stock}`);

  // ===== Movimientos de EPP =====
  const eppMov = await request("/api/epp", { method: "POST", body: { nombre: "Arnés " + runId, marca: "3M", stock: 5 } });
  ok("POST epp con marca", eppMov.status === 201 && eppMov.data.marca === "3M");
  const eppMovId = eppMov.data.id;

  const eppIng = await request(`/api/epp/${eppMovId}/movimientos`, {
    method: "POST",
    body: { tipo: "INGRESO", cantidad: 3, fecha: "2026-08-12", observacion: "Reabastecimiento" },
  });
  ok("POST movimiento ingreso epp", eppIng.status === 201 && eppIng.data.cantidad === 3);
  const eppMov1 = await request(`/api/epp/${eppMovId}`);
  ok("stock epp tras ingreso = 8", eppMov1.data.stock === 8, `stock=${eppMov1.data.stock}`);

  const eppMovList = await request(`/api/epp/${eppMovId}/movimientos`);
  ok("GET movimientos epp", eppMovList.status === 200 && eppMovList.data.length === 1);

  const eppTodosMov = await request("/api/movimientos-epp");
  ok("GET /api/movimientos-epp", eppTodosMov.status === 200 && Array.isArray(eppTodosMov.data));

  const eppPutMov = await request(`/api/movimientos-epp/${eppIng.data.id}`, {
    method: "PUT",
    body: { tipo: "EGRESO", cantidad: 2, fecha: "2026-08-12", observacion: "Uso" },
  });
  ok("PUT movimiento epp (ingreso->egreso)", eppPutMov.status === 200);
  const eppMov2 = await request(`/api/epp/${eppMovId}`);
  ok("stock epp tras cambio = 3", eppMov2.data.stock === 3, `stock=${eppMov2.data.stock}`);

  const eppDelMov = await request(`/api/movimientos-epp/${eppIng.data.id}`, { method: "DELETE" });
  ok("DELETE movimiento epp (204)", eppDelMov.status === 204, `status=${eppDelMov.status}`);
  const eppMov3 = await request(`/api/epp/${eppMovId}`);
  ok("stock epp restaurado = 5", eppMov3.data.stock === 5, `stock=${eppMov3.data.stock}`);

  // ===== Compra de EPP sube stock =====
  const compraEpp = await request("/api/compras", {
    method: "POST",
    body: { fecha: "2026-08-15", lineas: [{ tipo: "EPP", productoId: eppMovId, cantidad: 4 }] },
  });
  ok("POST compra de EPP", compraEpp.status === 201);
  const eppMov4 = await request(`/api/epp/${eppMovId}`);
  ok("stock epp tras compra = 9", eppMov4.data.stock === 9, `stock=${eppMov4.data.stock}`);

  const delEppConMov = await request(`/api/epp/${eppMovId}`, { method: "DELETE" });
  ok("no elimina epp con movimientos/compra (409)", delEppConMov.status === 409, `status=${delEppConMov.status}`);

  await request(`/api/compras/${compraEpp.data.id}`, { method: "DELETE" });
  const eppMov5 = await request(`/api/epp/${eppMovId}`);
  ok("stock epp tras eliminar compra = 5", eppMov5.data.stock === 5, `stock=${eppMov5.data.stock}`);

  // ===== Protecciones 409 =====
  const delProvConRefs = await request(`/api/proveedores/${provId}`, { method: "DELETE" });
  ok("no elimina proveedor con compras (409)", delProvConRefs.status === 409, `status=${delProvConRefs.status}`);

  const delMatConRefs = await request(`/api/materiales/${mCompraId}`, { method: "DELETE" });
  ok("no elimina material con compras/facturas (409)", delMatConRefs.status === 409, `status=${delMatConRefs.status}`);

  // ===== Eliminar compra facturada desvincula su factura =====
  const delCompraVin = await request(`/api/compras/${compraId}`, { method: "DELETE" });
  ok("DELETE compra facturada (204)", delCompraVin.status === 204, `status=${delCompraVin.status}`);
  const facturaVinAfter = await request(`/api/facturas/${facturaVinId}`);
  ok("factura queda sin compra (compraId null)", facturaVinAfter.status === 200 && facturaVinAfter.data.compraId == null);
  const mCompra11 = await request(`/api/materiales/${mCompraId}`);
  ok("stock revertido tras eliminar compra facturada = 5", mCompra11.data.stock === 5, `stock=${mCompra11.data.stock}`);

  const delEpp = await request(`/api/epp/${eppMovId}`, { method: "DELETE" });
  ok("DELETE epp sin movimientos (204)", delEpp.status === 204, `status=${delEpp.status}`);

  // ===== F1.1 Stock mínimo y alertas =====
  const matMin = await request("/api/materiales", {
    method: "POST",
    body: {
      nombre: `MatMin-${Date.now()}`,
      marca: "X",
      unidad: "u",
      stock: 0,
      stockMinimo: 5,
    },
  });
  ok("POST material con stockMinimo", matMin.status === 201, `status=${matMin.status}`);
  const matMinId = matMin.data.id;
  await request(`/api/materiales/${matMinId}/movimientos`, {
    method: "POST",
    body: { tipo: "INGRESO", cantidad: 3, fecha: "2026-08-15", observacion: "carga" },
  });
  const alertas = await request("/api/alertas/reposicion");
  ok("GET alertas/reposicion", alertas.status === 200, `status=${alertas.status}`);
  ok(
    "alerta incluye material bajo mínimo",
    Array.isArray(alertas.data) && alertas.data.some((a) => a.productoId === matMinId && a.tipo === "MATERIAL"),
    `alertas=${JSON.stringify(alertas.data?.slice?.(0, 3))}`
  );

  // ===== F1.2 Ajustes =====
  const ajuste = await request("/api/ajustes", {
    method: "POST",
    body: {
      fecha: "2026-08-15",
      motivo: "CONTEO",
      observacion: "ajuste test",
      lineas: [
        {
          tipoMovimiento: "INGRESO",
          tipoProducto: "MATERIAL",
          productoId: matMinId,
          descripcion: "ajuste+",
          cantidad: 10,
        },
      ],
    },
  });
  ok("POST ajuste", ajuste.status === 201, `status=${ajuste.status}`);
  const matTrasAjuste = await request(`/api/materiales/${matMinId}`);
  ok("stock tras ajuste ingreso = 13", matTrasAjuste.data.stock === 13, `stock=${matTrasAjuste.data.stock}`);
  const delAjuste = await request(`/api/ajustes/${ajuste.data.id}`, { method: "DELETE" });
  ok("DELETE ajuste (204)", delAjuste.status === 204, `status=${delAjuste.status}`);
  const matTrasDelAj = await request(`/api/materiales/${matMinId}`);
  ok("stock revertido tras eliminar ajuste = 3", matTrasDelAj.data.stock === 3, `stock=${matTrasDelAj.data.stock}`);

  // ===== F1.3 Devoluciones (parcial, sin tocar costo) =====
  const provDev = await request("/api/proveedores", {
    method: "POST",
    body: { nombre: `ProvDev-${Date.now()}` },
  });
  const matDev = await request("/api/materiales", {
    method: "POST",
    body: { nombre: `MatDev-${Date.now()}`, marca: "D", unidad: "u", stock: 0 },
  });
  const matDevId = matDev.data.id;
  const compraDev = await request("/api/compras", {
    method: "POST",
    body: {
      fecha: "2026-08-15",
      proveedor: { id: provDev.data.id },
      lineas: [{ tipo: "MATERIAL", productoId: matDevId, descripcion: "x", cantidad: 20 }],
    },
  });
  ok("POST compra para devolución", compraDev.status === 201, `status=${compraDev.status}`);
  const compraDevId = compraDev.data.id;
  const facDev = await request("/api/facturas", {
    method: "POST",
    body: {
      fecha: "2026-08-15",
      compraId: compraDevId,
      proveedor: { id: provDev.data.id },
      lineas: [
        { tipo: "MATERIAL", productoId: matDevId, descripcion: "x", cantidad: 20, costoUnitario: 1000 },
      ],
    },
  });
  ok("POST factura de compra dev", facDev.status === 201, `status=${facDev.status}`);
  const matAntesDev = await request(`/api/materiales/${matDevId}`);
  ok("ultimoCosto antes devolución = 1000", matAntesDev.data.ultimoCosto === 1000, `c=${matAntesDev.data.ultimoCosto}`);
  ok("stock antes devolución = 20", matAntesDev.data.stock === 20, `s=${matAntesDev.data.stock}`);
  const dev = await request(`/api/compras/${compraDevId}/devoluciones`, {
    method: "POST",
    body: {
      fecha: "2026-08-15",
      observacion: "parcial",
      lineas: [{ tipo: "MATERIAL", productoId: matDevId, descripcion: "x", cantidad: 7 }],
    },
  });
  ok("POST devolución parcial", dev.status === 201, `status=${dev.status}`);
  const matTrasDev = await request(`/api/materiales/${matDevId}`);
  ok("stock tras devolución = 13", matTrasDev.data.stock === 13, `s=${matTrasDev.data.stock}`);
  ok("ultimoCosto intacto tras devolución = 1000", matTrasDev.data.ultimoCosto === 1000, `c=${matTrasDev.data.ultimoCosto}`);
  const putCompraConDev = await request(`/api/compras/${compraDevId}`, {
    method: "PUT",
    body: compraDev.data,
  });
  ok("rechaza editar compra con devoluciones (409)", putCompraConDev.status === 409, `status=${putCompraConDev.status}`);
  const delCompraConDev = await request(`/api/compras/${compraDevId}`, { method: "DELETE" });
  ok("rechaza eliminar compra con devoluciones (409)", delCompraConDev.status === 409, `status=${delCompraConDev.status}`);
  const delDev = await request(`/api/devoluciones/${dev.data.id}`, { method: "DELETE" });
  ok("DELETE devolución (204)", delDev.status === 204, `status=${delDev.status}`);
  const matTrasDelDev = await request(`/api/materiales/${matDevId}`);
  ok("stock restaurado tras eliminar devolución = 20", matTrasDelDev.data.stock === 20, `s=${matTrasDelDev.data.stock}`);

  // ===== F1.4 Vencimientos EPP =====
  const hoy = new Date();
  const en10 = new Date(hoy.getTime() + 10 * 86400000).toISOString().slice(0, 10);
  const eppVenc = await request("/api/epp", {
    method: "POST",
    body: {
      nombre: `EppVenc-${Date.now()}`,
      marca: "V",
      stock: 2,
      fechaVencimiento: en10,
    },
  });
  ok("POST epp con vencimiento", eppVenc.status === 201, `status=${eppVenc.status}`);
  const venc = await request("/api/alertas/epp-vencimiento?dias=30");
  ok("GET alertas/epp-vencimiento", venc.status === 200, `status=${venc.status}`);
  ok(
    "alerta vencimiento incluye epp",
    Array.isArray(venc.data) && venc.data.some((a) => a.id === eppVenc.data.id && a.tipo === "EPP"),
    `venc=${JSON.stringify(venc.data?.slice?.(0, 3))}`
  );

  // ===== F2 Pagos + órdenes =====
  const facPago = await request("/api/facturas", {
    method: "POST",
    body: {
      fecha: "2026-08-15",
      numero: "PAGO-1",
      lineas: [{ tipo: "MATERIAL", productoId: matDevId, descripcion: "x", cantidad: 1, costoUnitario: 50000 }],
    },
  });
  ok("POST factura para pagos", facPago.status === 201, `status=${facPago.status}`);
  const facPagoId = facPago.data.id;
  const facPagoGet = await request(`/api/facturas/${facPagoId}`);
  ok("factura saldo inicial = total", facPagoGet.data.saldo === 50000 && facPagoGet.data.estadoPago === "PENDIENTE",
    `saldo=${facPagoGet.data.saldo} estado=${facPagoGet.data.estadoPago}`);
  const pago1 = await request(`/api/facturas/${facPagoId}/pagos`, {
    method: "POST",
    body: { fecha: "2026-08-15", monto: 20000, observacion: "abono" },
  });
  ok("POST pago parcial", pago1.status === 201, `status=${pago1.status}`);
  const facParcial = await request(`/api/facturas/${facPagoId}`);
  ok("estado PARCIAL saldo 30000", facParcial.data.estadoPago === "PARCIAL" && facParcial.data.saldo === 30000,
    `e=${facParcial.data.estadoPago} s=${facParcial.data.saldo}`);
  const pagoExceso = await request(`/api/facturas/${facPagoId}/pagos`, {
    method: "POST",
    body: { fecha: "2026-08-15", monto: 999999 },
  });
  ok("rechaza pago que excede saldo (400)", pagoExceso.status === 400, `status=${pagoExceso.status}`);
  await request(`/api/facturas/${facPagoId}/pagos`, {
    method: "POST",
    body: { fecha: "2026-08-15", monto: 30000 },
  });
  const facPagada = await request(`/api/facturas/${facPagoId}`);
  ok("factura PAGADA", facPagada.data.estadoPago === "PAGADA" && facPagada.data.saldo === 0,
    `e=${facPagada.data.estadoPago} s=${facPagada.data.saldo}`);

  const orden = await request("/api/ordenes-compra", {
    method: "POST",
    body: {
      fecha: "2026-08-15",
      observacion: "orden test",
      lineas: [{ tipo: "MATERIAL", productoId: matDevId, descripcion: "mat", cantidad: 2, costoUnitario: 1500 }],
    },
  });
  ok("POST orden compra", orden.status === 201 && orden.data.total === 3000, `status=${orden.status} total=${orden.data?.total}`);
  const delOrden = await request(`/api/ordenes-compra/${orden.data.id}`, { method: "DELETE" });
  ok("DELETE orden (204)", delOrden.status === 204, `status=${delOrden.status}`);

  // ===== F3 Dashboard + PDF + equipamiento =====
  const dash = await request("/api/dashboard");
  ok("GET dashboard", dash.status === 200 && typeof dash.data.valorInventario === "number",
    `status=${dash.status}`);
  const pdfInv = await request("/api/reportes/inventario.pdf", { bin: true });
  ok("PDF inventario", pdfInv.status === 200 && pdfInv.bytes > 100, `status=${pdfInv.status} bytes=${pdfInv.bytes}`);
  const equip = await request(`/api/empleados/${empId}/equipamiento`);
  ok("GET equipamiento empleado", equip.status === 200 && equip.data.empleado, `status=${equip.status}`);

  // ===== F4 Import + backup =====
  const imp = await request("/api/importar/proveedores", {
    method: "POST",
    raw: `nombre,telefono\nImpProv-${Date.now()},300`,
    headers: { "Content-Type": "text/plain" },
  });
  ok("import proveedores", imp.status === 200 && imp.data.creados >= 1,
    `status=${imp.status} creados=${imp.data?.creados}`);
  const backup = await request("/api/backup", { bin: true });
  ok("GET backup", backup.status === 200 && backup.bytes > 100, `status=${backup.status} bytes=${backup.bytes}`);

  console.log(`\nResumen: ${pasos} pasos, ${fallos} fallos`);
  process.exit(fallos === 0 ? 0 : 1);
}

async function crearImagenPng() {
  const base64 =
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";
  const bytes = Uint8Array.from(Buffer.from(base64, "base64"));
  const form = new FormData();
  form.append("archivo", new Blob([bytes], { type: "image/png" }), "prueba.png");
  return form;
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});