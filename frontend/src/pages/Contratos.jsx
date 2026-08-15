import { useState } from 'react';
import { post, put, del, hoy } from '../api';
import { useLista, useListaPaginada, useDebounce } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge, Paginacion } from '../components/ui';

const inicial = () => ({ empleadoId: null, fechaInicio: hoy(), fechaFin: '', estado: 'ACTIVO' });

function aDominio(f) {
  const { empleadoId, ...resto } = f;
  return { ...resto, empleado: empleadoId ? { id: empleadoId } : null };
}

function aForm(d) {
  return {
    empleadoId: d.empleado?.id || null,
    fechaInicio: d.fechaInicio,
    fechaFin: d.fechaFin || '',
    estado: d.estado || 'ACTIVO',
  };
}

export default function Contratos() {
  const { lista: empleados } = useLista('empleados', '/api/empleados');
  const [busqueda, setBusqueda] = useState('');
  const q = useDebounce(busqueda, 300);
  const url = q.trim() ? `/api/contratos/paginado?q=${encodeURIComponent(q.trim())}` : '/api/contratos/paginado';
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada(['contratos', 'empleados'], url);
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

  async function concluir(item) {
    if (!window.confirm(`¿Concluir el contrato de "${item.empleado?.nombre}"?`)) return;
    try {
      await post(`/api/contratos/${item.id}/concluir`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  async function eliminar(item) {
    if (!window.confirm('¿Eliminar este contrato?')) return;
    try {
      await del(`/api/contratos/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
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

  return (
    <section>
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
    </section>
  );
}