import SeccionTabs from '../components/SeccionTabs';
import { TABS_EMPLEADOS } from '../secciones';
import { useState } from 'react';
import { post, put, del, hoy } from '../api';
import { useLista, useListaPaginada } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge, Paginacion } from '../components/ui';
import SelectEmpleado from '../components/SelectEmpleado';

const inicial = () => ({
  lugar: '',
  fecha: hoy(),
  empleadoId: null,
  herramientaId: null,
  cantidad: 1,
  devuelta: false,
  fechaDevolucion: '',
});

function aDominio(f) {
  const { empleadoId, herramientaId, ...resto } = f;
  return {
    ...resto,
    empleado: empleadoId ? { id: empleadoId } : null,
    herramienta: herramientaId ? { id: herramientaId } : null,
  };
}

export default function Asignaciones() {
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada('asignaciones-herramientas', '/api/asignaciones-herramientas/paginado');
  const { lista: empleados } = useLista('empleados', '/api/empleados');
  const { lista: herramientas } = useLista(
    ['herramientas', 'asignaciones-herramientas'],
    '/api/herramientas'
  );
  const [abierto, setAbierto] = useState(false);
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(inicial);
  const [errores, setErrores] = useState(null);

  function abrirNuevo() {
    setEditando(null);
    setForm(inicial());
    setErrores(null);
    setAbierto(true);
  }

  function abrirEdicion(item) {
    const cantidad = item.cantidad != null ? item.cantidad : item.devuelta ? -1 : 1;
    setEditando(item);
    setForm({
      lugar: item.lugar,
      fecha: item.fecha,
      empleadoId: item.empleado?.id || null,
      herramientaId: item.herramienta?.id || null,
      cantidad: Math.abs(cantidad),
      devuelta: cantidad <= 0,
      fechaDevolucion: item.fechaDevolucion || '',
    });
    setErrores(null);
    setAbierto(true);
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    try {
      const cantidad = Math.abs(Number(form.cantidad)) || 1;
      const cuerpo = aDominio({
        ...form,
        cantidad: form.devuelta ? -cantidad : cantidad,
        fechaDevolucion: form.devuelta ? form.fechaDevolucion : null,
      });
      if (editando) {
        await put(`/api/asignaciones-herramientas/${editando.id}`, cuerpo);
      } else {
        await post('/api/asignaciones-herramientas', cuerpo);
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminar(item) {
    if (!window.confirm('¿Eliminar esta asignación?')) return;
    try {
      await del(`/api/asignaciones-herramientas/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const herramientasDisponibles = (editando
    ? herramientas
    : herramientas.filter((h) => (h.cantidadDisponible ?? 0) > 0))
    .filter((h) => (h.cantidadDisponible ?? 0) > 0 || h.id === editando?.herramienta?.id);

  const columnas = [
    {
      clave: 'herramienta',
      titulo: 'Herramienta',
      render: (x) => x.herramienta?.nombre || <Badge tipo="gris">sin herramienta</Badge>,
    },
    { clave: 'empleado', titulo: 'Empleado', render: (x) => x.empleado?.nombre || <Badge tipo="gris">sin asignar</Badge> },
    { clave: 'lugar', titulo: 'Lugar' },
    { clave: 'fecha', titulo: 'Fecha' },
    {
      clave: 'cantidad',
      titulo: 'Cantidad',
      render: (x) => {
        const cantidad = x.cantidad != null ? x.cantidad : x.devuelta ? -1 : 1;
        return cantidad > 0 ? (
          <Badge tipo="rojo">Pendiente · +{cantidad}</Badge>
        ) : (
          <Badge tipo="verde">Devuelta · {cantidad}</Badge>
        );
      },
    },
    {
      clave: 'fechaDevolucion',
      titulo: 'Devolución',
      render: (x) => x.fechaDevolucion || <span className="sin-dato">&mdash;</span>,
    },
    {
      clave: 'acciones',
      titulo: '',
      render: (x) => (
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={() => abrirEdicion(x)}>
            Editar
          </button>
          <button type="button" className="btn btn-peligro" onClick={() => eliminar(x)}>
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
        <h2>Asignación de herramientas</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevo}>
          + Asignar herramienta
        </button>
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay herramientas asignadas." />
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
        titulo={editando ? 'Editar asignación' : 'Nueva asignación de herramienta'}
        abierto={abierto}
        onCerrar={() => setAbierto(false)}
      >
        <form className="form" onSubmit={guardar}>
          <div className="form-grid">
            <div className="campo">
              <label>Herramienta *</label>
              <select
                value={form.herramientaId || ''}
                onChange={(e) =>
                  setForm({ ...form, herramientaId: e.target.value ? Number(e.target.value) : null })
                }
                required
              >
                <option value="">&mdash; Seleccione herramienta disponible &mdash;</option>
                {herramientasDisponibles.map((h) => (
                  <option key={h.id} value={h.id}>
                    {h.nombre}
                    {h.marca ? ` (${h.marca})` : ''} — {h.cantidadDisponible ?? 0} disponibles
                  </option>
                ))}
              </select>
            </div>
            <div className="campo">
              <label>Lugar *</label>
              <input
                type="text"
                value={form.lugar}
                onChange={(e) => setForm({ ...form, lugar: e.target.value })}
                required
              />
            </div>
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
              <label>Empleado *</label>
              <SelectEmpleado
                empleados={empleados}
                valor={form.empleadoId}
                onCambio={(id) => setForm({ ...form, empleadoId: id })}
                requerido
                soloContratados
              />
            </div>
            <div className="campo">
              <label>Cantidad</label>
              <input
                type="number"
                min="1"
                step="1"
                value={form.cantidad}
                onChange={(e) => setForm({ ...form, cantidad: e.target.value })}
                title={form.devuelta ? 'Cantidad devuelta' : 'Cantidad entregada'}
              />
            </div>
          </div>
          <label className="campo-check">
            <input
              type="checkbox"
              checked={form.devuelta}
              onChange={(e) => setForm({ ...form, devuelta: e.target.checked })}
            />
            Herramienta devuelta
          </label>
          {form.devuelta && (
            <div className="campo">
              <label>Fecha de devolución</label>
              <input
                type="date"
                value={form.fechaDevolucion || hoy()}
                onChange={(e) => setForm({ ...form, fechaDevolucion: e.target.value })}
              />
            </div>
          )}
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              {editando ? 'Guardar cambios' : 'Asignar'}
            </button>
          </div>
        </form>
      </Modal>
    </section>
  );
}