import SeccionTabs from '../components/SeccionTabs';
import { TABS_EMPLEADOS } from '../secciones';
import { useState } from 'react';
import { post, put, del, get, hoy } from '../api';
import { useLista, useListaPaginada, useDebounce } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge, Paginacion } from '../components/ui';

const CATEGORIA_APRENDIZAJE = new Set(['APRENDIZAJE', 'PRACTICAS_LABORALES']);

const inicial = () => ({
  empleadoId: null,
  fechaInicio: hoy(),
  fechaFin: '',
  estado: 'ACTIVO',
  tipoContratoId: null,
  remuneracionMensual: '',
  faseAprendizaje: '',
});

const extraInicial = () => ({
  concepto: '',
  tipo: 'EVENTUAL',
  valor: '',
  fecha: '',
  vigenciaDesde: '',
  vigenciaHasta: '',
  observacion: '',
});

function aDominio(f) {
  const { empleadoId, tipoContratoId, ...resto } = f;
  return {
    ...resto,
    empleado: empleadoId ? { id: empleadoId } : null,
    tipoContrato: tipoContratoId ? { id: tipoContratoId } : null,
    remuneracionMensual: resto.remuneracionMensual !== ''
      ? Number(resto.remuneracionMensual)
      : null,
  };
}

function aForm(d) {
  return {
    empleadoId: d.empleado?.id || null,
    fechaInicio: d.fechaInicio,
    fechaFin: d.fechaFin || '',
    estado: d.estado || 'ACTIVO',
    tipoContratoId: d.tipoContrato?.id || null,
    remuneracionMensual: d.remuneracionMensual != null ? String(d.remuneracionMensual) : '',
    faseAprendizaje: d.faseAprendizaje || '',
  };
}

function numero(n) {
  return n == null ? 0 : Number(n);
}

function formatoMoneda(n) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(
    numero(n)
  );
}

export default function Contratos() {
  const { lista: empleados } = useLista('empleados', '/api/empleados');
  const { lista: tiposContrato } = useLista('tipos-contrato', '/api/tipos-contrato');
  const [busqueda, setBusqueda] = useState('');
  const q = useDebounce(busqueda, 300);
  const url = q.trim() ? `/api/contratos/paginado?q=${encodeURIComponent(q.trim())}` : '/api/contratos/paginado';
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada(['contratos', 'empleados'], url);
  const [abierto, setAbierto] = useState(false);
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(inicial);
  const [errores, setErrores] = useState(null);

  const [prestaciones, setPrestaciones] = useState(null);
  const [contratoPrest, setContratoPrest] = useState(null);
  const [extraForm, setExtraForm] = useState(extraInicial);
  const [extraErrores, setExtraErrores] = useState(null);
  const [prestacionesCargando, setPrestacionesCargando] = useState(false);

  const tipoSeleccionado = tiposContrato.find((t) => t.id === form.tipoContratoId);
  const esAprendizaje = tipoSeleccionado && CATEGORIA_APRENDIZAJE.has(tipoSeleccionado.nombre);

  function abrirNuevo() {
    setEditando(null);
    setForm(inicial());
    setErrores(null);
    setAbierto(true);
  }

  function abrirEdicion(item) {
    setEditando(item);
    setForm(aForm(item));
    setErrores(null);
    setAbierto(true);
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    try {
      if (editando) {
        await put(`/api/contratos/${editando.id}`, aDominio(form));
      } else {
        await post('/api/contratos', aDominio(form));
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function abrirPrestaciones(item) {
    setContratoPrest(item);
    setExtraForm(extraInicial());
    setExtraErrores(null);
    setPrestaciones(null);
    setPrestacionesCargando(true);
    try {
      setPrestaciones(await get(`/api/contratos/${item.id}/prestaciones`));
    } catch (err) {
      setPrestaciones({ error: err.message });
    } finally {
      setPrestacionesCargando(false);
    }
  }

  async function recalcular() {
    if (!contratoPrest) return;
    setPrestacionesCargando(true);
    try {
      setPrestaciones(await post(`/api/contratos/${contratoPrest.id}/calcular-prestaciones`));
      setExtraErrores(null);
    } catch (err) {
      setPrestaciones({ error: err.message });
    } finally {
      setPrestacionesCargando(false);
    }
  }

  async function agregarExtra(e) {
    e.preventDefault();
    setExtraErrores(null);
    try {
      await post(`/api/contratos/${contratoPrest.id}/prestaciones`, extraForm);
      setExtraForm(extraInicial());
      setPrestaciones(await get(`/api/contratos/${contratoPrest.id}/prestaciones`));
    } catch (err) {
      setExtraErrores([err.message]);
    }
  }

  async function eliminarExtra(extra) {
    if (!window.confirm(`¿Eliminar la prestación extra "${extra.concepto}"?`)) return;
    try {
      await del(`/api/contratos/${contratoPrest.id}/prestaciones/${extra.id}`);
      setPrestaciones(await get(`/api/contratos/${contratoPrest.id}/prestaciones`));
    } catch (err) {
      window.alert(err.message);
    }
  }

  function concluir(item) {
    if (!window.confirm(`¿Concluir el contrato de "${item.empleado?.nombre}"?`)) return;
    post(`/api/contratos/${item.id}/concluir`)
      .then(() => recargar())
      .catch((err) => window.alert(err.message));
  }

  function eliminar(item) {
    if (!window.confirm('¿Eliminar este contrato?')) return;
    del(`/api/contratos/${item.id}`)
      .then(() => recargar())
      .catch((err) => window.alert(err.message));
  }

  const columnas = [
    { clave: 'empleado', titulo: 'Empleado', render: (c) => c.empleado?.nombre || <Badge tipo="gris">—</Badge> },
    { clave: 'fechaInicio', titulo: 'Inicio' },
    {
      clave: 'fechaFin',
      titulo: 'Fin',
      render: (c) => c.fechaFin || <span className="sin-dato">Indefinido</span>,
    },
    {
      clave: 'tipoContrato',
      titulo: 'Tipo',
      render: (c) => c.tipoContrato?.nombre || <Badge tipo="gris">Sin tipo</Badge>,
    },
    {
      clave: 'remuneracionMensual',
      titulo: 'Remuneración',
      render: (c) => (c.remuneracionMensual != null ? formatoMoneda(c.remuneracionMensual) : '—'),
    },
    {
      clave: 'estado',
      titulo: 'Estado',
      render: (c) =>
        c.estado === 'ACTIVO' ? <Badge tipo="verde">Activo</Badge> : <Badge tipo="gris">Concluido</Badge>,
    },
    {
      clave: 'acciones',
      titulo: '',
      render: (c) => (
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={() => abrirPrestaciones(c)}>
            Prestaciones
          </button>
          <button type="button" className="btn btn-borde" onClick={() => abrirEdicion(c)}>
            Editar
          </button>
          {c.estado === 'ACTIVO' && (
            <button type="button" className="btn btn-peligro" onClick={() => concluir(c)}>
              Concluir
            </button>
          )}
          <button type="button" className="btn btn-peligro" onClick={() => eliminar(c)}>
            Eliminar
          </button>
        </div>
      ),
    },
  ];

  const columnasDesglose = [
    { clave: 'concepto', titulo: 'Concepto' },
    {
      clave: 'quienPaga',
      titulo: 'Quién paga',
      render: (l) =>
        l.quienPaga === 'EMPLEADOR' ? <Badge tipo="azul">Empleador</Badge> : <Badge tipo="gris">Contratista</Badge>,
    },
    {
      clave: 'base',
      titulo: 'Base',
      render: (l) => (l.base != null ? formatoMoneda(l.base) : '—'),
    },
    {
      clave: 'porcentaje',
      titulo: '%',
      render: (l) => (l.porcentaje != null ? `${l.porcentaje}%` : '—'),
    },
    {
      clave: 'valorMensual',
      titulo: 'Mensual',
      render: (l) => formatoMoneda(l.valorMensual),
    },
    {
      clave: 'valorAnual',
      titulo: 'Anual',
      render: (l) => formatoMoneda(l.valorAnual),
    },
  ];

  const calculadas = prestaciones && prestaciones.calculadas ? prestaciones.calculadas : [];
  const extras = prestaciones && prestaciones.extras ? prestaciones.extras : [];

  return (
    <section>
      <SeccionTabs items={TABS_EMPLEADOS} />
      <div className="pagina-cabecera">
        <h2>Contrataciones</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevo}>
          + Nuevo contrato
        </button>
      </div>

      <div className="busqueda">
        <input
          type="search"
          placeholder="Buscar por empleado…"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
        />
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay contratos registrados." />
      )}
      <Paginacion
        pagina={pagina}
        total={total}
        totalPaginas={totalPaginas}
        tamano={tamano}
        onPagina={setPagina}
        onTamano={setTamano}
      />

      <Modal
        titulo={editando ? 'Editar contrato' : 'Nuevo contrato'}
        abierto={abierto}
        onCerrar={() => setAbierto(false)}
      >
        <form className="form" onSubmit={guardar}>
          <div className="campo">
            <label>Empleado *</label>
            <select
              value={form.empleadoId || ''}
              onChange={(e) => setForm({ ...form, empleadoId: e.target.value ? Number(e.target.value) : null })}
              required
            >
              <option value="">&mdash; Seleccione empleado &mdash;</option>
              {empleados.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.nombre}
                </option>
              ))}
            </select>
          </div>
          <div className="form-grid">
            <div className="campo">
              <label>Fecha de inicio *</label>
              <input
                type="date"
                value={form.fechaInicio}
                onChange={(e) => setForm({ ...form, fechaInicio: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label>Fecha de fin (vacío = indefinido)</label>
              <input
                type="date"
                value={form.fechaFin}
                onChange={(e) => setForm({ ...form, fechaFin: e.target.value })}
              />
            </div>
          </div>
          <div className="form-grid">
            <div className="campo">
              <label>Tipo de contrato</label>
              <select
                value={form.tipoContratoId || ''}
                onChange={(e) => setForm({ ...form, tipoContratoId: e.target.value ? Number(e.target.value) : null })}
              >
                <option value="">&mdash; Sin tipo &mdash;</option>
                {tiposContrato.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.nombre}
                  </option>
                ))}
              </select>
            </div>
            <div className="campo">
              <label>Remuneración mensual (COP)</label>
              <input
                type="number"
                min="0"
                step="any"
                value={form.remuneracionMensual}
                onChange={(e) => setForm({ ...form, remuneracionMensual: e.target.value })}
              />
            </div>
          </div>
          {esAprendizaje && (
            <div className="campo">
              <label>Fase de aprendizaje</label>
              <select
                value={form.faseAprendizaje}
                onChange={(e) => setForm({ ...form, faseAprendizaje: e.target.value })}
              >
                <option value="">&mdash; Seleccione fase &mdash;</option>
                <option value="LECTIVA">Lectiva</option>
                <option value="PRACTICA">Práctica</option>
              </select>
            </div>
          )}
          {editando && (
            <div className="campo">
              <label>Estado</label>
              <select
                value={form.estado}
                onChange={(e) => setForm({ ...form, estado: e.target.value })}
              >
                <option value="ACTIVO">Activo</option>
                <option value="CONCLUIDO">Concluido</option>
              </select>
            </div>
          )}
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              {editando ? 'Guardar cambios' : 'Crear contrato'}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        titulo={`Prestaciones — ${contratoPrest?.empleado?.nombre || ''}`}
        abierto={prestaciones !== null}
        onCerrar={() => setPrestaciones(null)}
      >
        {prestacionesCargando && <p className="vacio">Cargando…</p>}
        {!prestacionesCargando && prestaciones && prestaciones.error && (
          <p className="texto-error">{prestaciones.error}</p>
        )}
        {!prestacionesCargando && prestaciones && !prestaciones.error && (
          <div className="stack">
            <div className="campo">
              <label>Desglose calculado</label>
              {calculadas.length === 0 ? (
                <p className="sin-dato">Sin cálculo. Presione «Calcular».</p>
              ) : (
                <Tabla columnas={columnasDesglose} filas={calculadas} vacio="Sin líneas." />
              )}
            </div>
            <p>
              <strong>Total costo empleador:</strong> {formatoMoneda(prestaciones.totalEmpleador)}
            </p>
            <div className="form-acciones">
              <button type="button" className="btn btn-primario" onClick={recalcular} disabled={prestacionesCargando}>
                Calcular / Recalcular
              </button>
            </div>

            <h3>Prestaciones extra</h3>
            {extras.length === 0 ? (
              <p className="sin-dato">Sin prestaciones extra.</p>
            ) : (
              <Tabla
                columnas={[
                  { clave: 'concepto', titulo: 'Concepto' },
                  { clave: 'tipo', titulo: 'Tipo' },
                  { clave: 'valor', titulo: 'Valor', render: (x) => formatoMoneda(x.valor) },
                  { clave: 'fecha', titulo: 'Fecha', render: (x) => x.fecha || '—' },
                  {
                    clave: 'vigencia',
                    titulo: 'Vigencia',
                    render: (x) => (x.vigenciaDesde || x.vigenciaHasta ? `${x.vigenciaDesde || '?'} → ${x.vigenciaHasta || '?'}` : '—'),
                  },
                  {
                    clave: 'acciones',
                    titulo: '',
                    render: (x) => (
                      <button type="button" className="btn btn-peligro" onClick={() => eliminarExtra(x)}>
                        Eliminar
                      </button>
                    ),
                  },
                ]}
                filas={extras}
                vacio="Sin prestaciones extra."
              />
            )}

            <form className="form" onSubmit={agregarExtra}>
              <div className="form-grid">
                <div className="campo">
                  <label>Concepto *</label>
                  <input
                    type="text"
                    value={extraForm.concepto}
                    onChange={(e) => setExtraForm({ ...extraForm, concepto: e.target.value })}
                    required
                  />
                </div>
                <div className="campo">
                  <label>Tipo</label>
                  <select
                    value={extraForm.tipo}
                    onChange={(e) => setExtraForm({ ...extraForm, tipo: e.target.value })}
                  >
                    <option value="EVENTUAL">Eventual (viático)</option>
                    <option value="RECURRENTE">Recurrente (prima)</option>
                  </select>
                </div>
              </div>
              <div className="form-grid">
                <div className="campo">
                  <label>Valor (COP) *</label>
                  <input
                    type="number"
                    min="0"
                    step="any"
                    value={extraForm.valor}
                    onChange={(e) => setExtraForm({ ...extraForm, valor: e.target.value })}
                    required
                  />
                </div>
                {extraForm.tipo === 'EVENTUAL' ? (
                  <div className="campo">
                    <label>Fecha</label>
                    <input
                      type="date"
                      value={extraForm.fecha}
                      onChange={(e) => setExtraForm({ ...extraForm, fecha: e.target.value })}
                    />
                  </div>
                ) : (
                  <div className="form-grid">
                    <div className="campo">
                      <label>Vigencia desde</label>
                      <input
                        type="date"
                        value={extraForm.vigenciaDesde}
                        onChange={(e) => setExtraForm({ ...extraForm, vigenciaDesde: e.target.value })}
                      />
                    </div>
                    <div className="campo">
                      <label>Vigencia hasta</label>
                      <input
                        type="date"
                        value={extraForm.vigenciaHasta}
                        onChange={(e) => setExtraForm({ ...extraForm, vigenciaHasta: e.target.value })}
                      />
                    </div>
                  </div>
                )}
              </div>
              <div className="campo">
                <label>Observación</label>
                <input
                  type="text"
                  value={extraForm.observacion}
                  onChange={(e) => setExtraForm({ ...extraForm, observacion: e.target.value })}
                />
              </div>
              <Microsofto errores={extraErrores} />
              <div className="form-acciones">
                <button type="submit" className="btn btn-primario">
                  Agregar prestación extra
                </button>
              </div>
            </form>
          </div>
        )}
      </Modal>
    </section>
  );
}
