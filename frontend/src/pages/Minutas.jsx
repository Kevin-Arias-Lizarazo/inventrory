import SeccionTabs from '../components/SeccionTabs';
import { TABS_EMPLEADOS } from '../secciones';
import { useState } from 'react';
import { post, put, del, hoy, ahora } from '../api';
import { useLista, useListaPaginada } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge, Paginacion, FilterBar } from '../components/ui';
import SelectEmpleado from '../components/SelectEmpleado';

const inicial = () => ({
  hora: ahora(),
  fecha: hoy(),
  empleadoId: null,
  proyectoId: null,
});

function aDominio(f) {
  const { empleadoId, proyectoId, ...resto } = f;
  return {
    ...resto,
    empleado: empleadoId ? { id: empleadoId } : null,
    proyecto: proyectoId ? { id: proyectoId } : null,
  };
}

function aForm(d) {
  return {
    hora: d.hora,
    fecha: d.fecha,
    empleadoId: d.empleado?.id || null,
    proyectoId: d.proyecto?.id || null,
  };
}

export default function Minutas() {
  const { lista: empleados } = useLista('empleados', '/api/empleados?contratados=true');
  const { lista: empleadosFiltro } = useLista('empleados', '/api/empleados');
  const { lista: proyectos } = useLista('proyectos', '/api/proyectos');
  const filtrosIniciales = { q: '', fecha: '', empleadoId: '', orden: 'desc' };
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, filtros, setFiltros, setPagina, setTamano, recargar } =
    useListaPaginada(['minutas', 'proyectos'], '/api/minutas/filtradas', 30, filtrosIniciales);
  const [abierto, setAbierto] = useState(false);
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(inicial);
  const [errores, setErrores] = useState(null);

  const [diaAbierto, setDiaAbierto] = useState(false);
  const [diaFecha, setDiaFecha] = useState(hoy());
  const [diaHoraGeneral, setDiaHoraGeneral] = useState(ahora());
  const [diaFilas, setDiaFilas] = useState([]);
  const [diaResultado, setDiaResultado] = useState(null);
  const [diaEnviando, setDiaEnviando] = useState(false);

  function abrirMinutaDia() {
    setDiaFecha(hoy());
    setDiaHoraGeneral(ahora());
    setDiaFilas(
      empleados.map((e) => ({
        empleadoId: e.id,
        nombre: e.nombre,
        proyectoId: null,
        hora: ahora(),
      }))
    );
    setDiaResultado(null);
    setDiaAbierto(true);
  }

  function aplicarHoraGeneral() {
    setDiaFilas((filas) => filas.map((f) => ({ ...f, hora: diaHoraGeneral })));
  }

  function actualizarFila(indice, cambios) {
    setDiaFilas((filas) => filas.map((f, i) => (i === indice ? { ...f, ...cambios } : f)));
  }

  async function registrarMinutaDia() {
    const seleccionadas = diaFilas.filter((f) => f.proyectoId);
    if (seleccionadas.length === 0) {
      setDiaResultado({ ok: false, mensaje: 'Ningún empleado tiene proyecto seleccionado.' });
      return;
    }
    setDiaEnviando(true);
    setDiaResultado(null);
    try {
      const cuerpo = seleccionadas.map((f) => ({
        fecha: diaFecha,
        hora: f.hora,
        empleado: { id: f.empleadoId },
        proyecto: { id: f.proyectoId },
      }));
      const res = await post('/api/minutas/lote', cuerpo);
      setDiaResultado({ ok: true, mensaje: `Minuta del día registrada: ${res.creadas} empleado(s).` });
      await recargar();
    } catch (err) {
      setDiaResultado({ ok: false, mensaje: err.message });
    } finally {
      setDiaEnviando(false);
    }
  }

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
        await put(`/api/minutas/${editando.id}`, aDominio(form));
      } else {
        await post('/api/minutas', aDominio(form));
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminar(item) {
    if (!window.confirm('¿Eliminar esta minuta?')) return;
    try {
      await del(`/api/minutas/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const columnas = [
    { clave: 'fecha', titulo: 'Fecha' },
    { clave: 'hora', titulo: 'Hora' },
    {
      clave: 'proyecto',
      titulo: 'Proyecto',
      render: (m) => m.proyecto?.nombre || <Badge tipo="gris">sin proyecto</Badge>,
    },
    {
      clave: 'empleado',
      titulo: 'Empleado',
      render: (m) => m.empleado?.nombre || <Badge tipo="gris">sin asignar</Badge>,
    },
    {
      clave: 'acciones',
      titulo: '',
      render: (m) => (
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={() => abrirEdicion(m)}>
            Editar
          </button>
          <button type="button" className="btn btn-peligro" onClick={() => eliminar(m)}>
            Eliminar
          </button>
        </div>
      ),
    },
  ];

  return (
    <section>
      <SeccionTabs items={TABS_EMPLEADOS} />
      <div className="pagina-cabecera">
        <h2>Minuta de empleados</h2>
        <div className="acciones">
          <button type="button" className="btn btn-primario" onClick={abrirMinutaDia}>
            Minuta del día
          </button>
          <button type="button" className="btn btn-borde" onClick={abrirNuevo}>
            + Registrar minuta
          </button>
        </div>
      </div>

      <FilterBar
        campos={[
          { tipo: 'search', clave: 'q', etiqueta: 'Buscar por proyecto o empleado…' },
          { tipo: 'date', clave: 'fecha', etiqueta: 'Filtrar por día' },
          {
            tipo: 'select',
            clave: 'empleadoId',
            etiqueta: 'Filtrar por empleado',
            opciones: [
              { valor: '', etiqueta: 'Todos los empleados' },
              ...empleadosFiltro.map((e) => ({ valor: String(e.id), etiqueta: e.nombre })),
            ],
          },
          {
            tipo: 'orden',
            clave: 'orden',
            etiqueta: 'Orden por fecha',
            opciones: [
              { valor: 'desc', etiqueta: 'Más reciente primero' },
              { valor: 'asc', etiqueta: 'Más antigua primero' },
            ],
          },
        ]}
        filtros={filtros}
        onCambio={(nuevos) => setFiltros({ ...nuevos, q: (nuevos.q || '').trim() })}
        onLimpiar={() => setFiltros(filtrosIniciales)}
      />

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay minutas registradas." />
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
        titulo={editando ? 'Editar minuta' : 'Nueva minuta'}
        abierto={abierto}
        onCerrar={() => setAbierto(false)}
      >
        <form className="form" onSubmit={guardar}>
          <div className="form-grid">
            <div className="campo">
              <label>Fecha</label>
              <input
                type="date"
                value={form.fecha}
                onChange={(e) => setForm({ ...form, fecha: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label>Hora</label>
              <input
                type="time"
                value={form.hora}
                onChange={(e) => setForm({ ...form, hora: e.target.value })}
                required
              />
            </div>
          </div>
          <div className="campo">
            <label>Proyecto *</label>
            <select
              value={form.proyectoId || ''}
              onChange={(e) => setForm({ ...form, proyectoId: e.target.value ? Number(e.target.value) : null })}
              required
            >
              <option value="">&mdash; Seleccione proyecto &mdash;</option>
              {proyectos.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nombre}
                </option>
              ))}
            </select>
          </div>
          <div className="campo">
            <label>Empleado</label>
            <SelectEmpleado
              empleados={empleados}
              valor={form.empleadoId}
              onCambio={(id) => setForm({ ...form, empleadoId: id })}
              requerido
            />
          </div>
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              {editando ? 'Guardar cambios' : 'Registrar'}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        titulo="Minuta del día (asistencia)"
        abierto={diaAbierto}
        onCerrar={() => setDiaAbierto(false)}
        ancho={860}
      >
        <div className="form-grid">
          <div className="campo">
            <label>Fecha</label>
            <input type="date" value={diaFecha} onChange={(e) => setDiaFecha(e.target.value)} />
          </div>
          <div className="campo">
            <label>Hora general</label>
            <div className="minuta-dia-hora">
              <input type="time" value={diaHoraGeneral} onChange={(e) => setDiaHoraGeneral(e.target.value)} />
              <button type="button" className="btn btn-borde btn-mini" onClick={aplicarHoraGeneral}>
                Aplicar a todos
              </button>
            </div>
          </div>
        </div>

        <p className="minuta-dia-nota">
          Seleccione el proyecto de cada empleado. Quien no tenga proyecto seleccionado no se registra.
        </p>

        <div className="minuta-dia-lista">
          {diaFilas.length === 0 && <p className="vacio">No hay empleados contratados.</p>}
          {diaFilas.map((f, i) => (
            <div key={f.empleadoId} className={`minuta-dia-fila ${f.proyectoId ? '' : 'sin-proyecto'}`}>
              <span className="minuta-dia-nombre">{f.nombre}</span>
              <select
                value={f.proyectoId || ''}
                onChange={(e) => actualizarFila(i, { proyectoId: e.target.value ? Number(e.target.value) : null })}
              >
                <option value="">— sin proyecto —</option>
                {proyectos.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nombre}
                  </option>
                ))}
              </select>
              <input type="time" value={f.hora} onChange={(e) => actualizarFila(i, { hora: e.target.value })} />
            </div>
          ))}
        </div>

        {diaResultado && (
          <p className={`escaneo-mensaje ${diaResultado.ok ? 'ok' : 'error'}`}>{diaResultado.mensaje}</p>
        )}

        <div className="form-acciones">
          <button type="button" className="btn btn-borde" onClick={() => setDiaAbierto(false)}>
            Cerrar
          </button>
          <button type="button" className="btn btn-primario" onClick={registrarMinutaDia} disabled={diaEnviando}>
            {diaEnviando ? 'Registrando…' : 'Registrar minuta del día'}
          </button>
        </div>
      </Modal>
    </section>
  );
}