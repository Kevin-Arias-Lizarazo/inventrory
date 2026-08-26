import SeccionTabs from '../components/SeccionTabs';
import { TABS_PROYECTOS } from '../secciones';
import { useState } from 'react';
import { post, put, del, hoy } from '../api';
import { useLista, useListaPaginada } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge, Paginacion } from '../components/ui';

const inicial = () => ({
  consumibleId: null,
  proyectoId: null,
  cantidad: 1,
  fecha: hoy(),
  observacion: '',
});

function aDominio(f) {
  const { consumibleId, proyectoId, ...resto } = f;
  return {
    ...resto,
    consumible: consumibleId ? { id: consumibleId } : null,
    proyecto: proyectoId ? { id: proyectoId } : null,
  };
}

export default function AsignacionesConsumibles() {
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada(['asignaciones-consumibles', 'consumibles', 'proyectos'], '/api/asignaciones-consumibles/paginado');
  const { lista: consumibles } = useLista(['consumibles', 'asignaciones-consumibles'], '/api/consumibles');
  const { lista: proyectos } = useLista(['proyectos', 'asignaciones-consumibles'], '/api/proyectos');
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
    setEditando(item);
    setForm({
      consumibleId: item.consumible?.id || null,
      proyectoId: item.proyecto?.id || null,
      cantidad: item.cantidad,
      fecha: item.fecha,
      observacion: item.observacion,
    });
    setErrores(null);
    setAbierto(true);
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    try {
      const cuerpo = aDominio({ ...form, cantidad: Number(form.cantidad) });
      if (editando) {
        await put(`/api/asignaciones-consumibles/${editando.id}`, cuerpo);
      } else {
        await post('/api/asignaciones-consumibles', cuerpo);
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminar(item) {
    if (!window.confirm('¿Eliminar esta asignación? (el stock se restaurará)')) return;
    try {
      await del(`/api/asignaciones-consumibles/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const consumiblesDisponibles = consumibles.filter((c) => (c.stock ?? 0) > 0);
  const proyectosDisponibles = proyectos.filter((p) => p.estado !== 'FINALIZADO' || p.id === editando?.proyecto?.id);

  const columnas = [
    {
      clave: 'consumible',
      titulo: 'Consumible',
      render: (x) => x.consumible?.nombre || <Badge tipo="gris">—</Badge>,
    },
    {
      clave: 'proyecto',
      titulo: 'Proyecto',
      render: (x) => x.proyecto?.nombre || <Badge tipo="gris">—</Badge>,
    },
    { clave: 'cantidad', titulo: 'Cantidad' },
    { clave: 'fecha', titulo: 'Fecha' },
    { clave: 'observacion', titulo: 'Observación' },
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
      <SeccionTabs items={TABS_PROYECTOS} />
      <div className="pagina-cabecera">
        <h2>Asignación de consumibles</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevo}>
          + Asignar consumible
        </button>
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay consumibles asignados." />
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
        titulo={editando ? 'Editar asignación' : 'Nueva asignación de consumible'}
        abierto={abierto}
        onCerrar={() => setAbierto(false)}
      >
        <form className="form" onSubmit={guardar}>
          <div className="form-grid">
            <div className="campo">
              <label>Consumible *</label>
              <select
                value={form.consumibleId || ''}
                onChange={(e) =>
                  setForm({ ...form, consumibleId: e.target.value ? Number(e.target.value) : null })
                }
                required
              >
                <option value="">&mdash; Seleccione consumible disponible &mdash;</option>
                {consumiblesDisponibles.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nombre} — {c.stock} disponibles
                  </option>
                ))}
              </select>
            </div>
            <div className="campo">
              <label>Proyecto *</label>
              <select
                value={form.proyectoId || ''}
                onChange={(e) =>
                  setForm({ ...form, proyectoId: e.target.value ? Number(e.target.value) : null })
                }
                required
              >
                <option value="">&mdash; Seleccione proyecto &mdash;</option>
                {proyectosDisponibles.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nombre}
                    {p.estado === 'FINALIZADO' ? ' (finalizado)' : ''}
                  </option>
                ))}
              </select>
            </div>
            <div className="campo">
              <label>Cantidad *</label>
              <input
                type="number"
                min="0.1"
                step="0.1"
                value={form.cantidad}
                onChange={(e) => setForm({ ...form, cantidad: e.target.value })}
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
          </div>
          <div className="campo">
            <label>Observación</label>
            <input
              type="text"
              value={form.observacion}
              onChange={(e) => setForm({ ...form, observacion: e.target.value })}
            />
          </div>
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