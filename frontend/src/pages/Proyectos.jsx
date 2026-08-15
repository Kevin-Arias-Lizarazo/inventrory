import { useState } from 'react';
import { post, put, del, hoy } from '../api';
import { useListaPaginada, useDebounce } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge, Paginacion } from '../components/ui';
import QrCodigo from '../components/QrCodigo';

const inicial = () => ({ nombre: '', cliente: '', ubicacion: '', descripcion: '', fechaInicio: hoy(), fechaFin: '', estado: 'ACTIVO' });

export default function Proyectos() {
  const [busqueda, setBusqueda] = useState('');
  const q = useDebounce(busqueda, 300);
  const url = q.trim() ? `/api/proyectos/paginado?q=${encodeURIComponent(q.trim())}` : '/api/proyectos/paginado';
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada(['proyectos', 'minutas'], url);
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
      nombre: item.nombre,
      cliente: item.cliente,
      ubicacion: item.ubicacion,
      descripcion: item.descripcion,
      fechaInicio: item.fechaInicio || hoy(),
      fechaFin: item.fechaFin || '',
      estado: item.estado || 'ACTIVO',
    });
    setErrores(null);
    setAbierto(true);
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    try {
      if (editando) {
        await put(`/api/proyectos/${editando.id}`, form);
      } else {
        await post('/api/proyectos', form);
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function finalizar(item) {
    if (!window.confirm(`¿Marcar como finalizado el proyecto "${item.nombre}"?`)) return;
    try {
      await post(`/api/proyectos/${item.id}/finalizar`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  async function eliminar(item) {
    if (!window.confirm(`¿Eliminar el proyecto "${item.nombre}"?`)) return;
    try {
      await del(`/api/proyectos/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const columnas = [
    { clave: 'nombre', titulo: 'Proyecto' },
    { clave: 'codigo', titulo: 'Código', render: (p) => <QrCodigo codigo={p.codigo} tamano={40} /> },
    { clave: 'cliente', titulo: 'Cliente' },
    { clave: 'ubicacion', titulo: 'Ubicación' },
    { clave: 'fechaInicio', titulo: 'Inicio' },
    {
      clave: 'fechaFin',
      titulo: 'Fin',
      render: (p) => p.fechaFin || <span className="sin-dato">—</span>,
    },
    {
      clave: 'estado',
      titulo: 'Estado',
      render: (p) =>
        p.estado === 'FINALIZADO' ? <Badge tipo="gris">Finalizado</Badge> : <Badge tipo="verde">Activo</Badge>,
    },
    { clave: 'descripcion', titulo: 'Descripción' },
    {
      clave: 'acciones',
      titulo: '',
      render: (p) => (
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={() => abrirEdicion(p)}>
            Editar
          </button>
          {p.estado !== 'FINALIZADO' && (
            <button type="button" className="btn btn-peligro" onClick={() => finalizar(p)}>
              Finalizar
            </button>
          )}
          <button type="button" className="btn btn-peligro" onClick={() => eliminar(p)}>
            Eliminar
          </button>
        </div>
      ),
    },
  ];

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Proyectos</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevo}>
          + Nuevo proyecto
        </button>
      </div>

      <div className="busqueda">
        <input
          type="search"
          placeholder="Buscar por nombre, cliente o ubicación…"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
        />
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay proyectos registrados." />
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
        titulo={editando ? 'Editar proyecto' : 'Nuevo proyecto'}
        abierto={abierto}
        onCerrar={() => setAbierto(false)}
      >
        <form className="form" onSubmit={guardar}>
          <div className="form-grid">
            <div className="campo">
              <label>Nombre *</label>
              <input
                type="text"
                value={form.nombre}
                onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label>Cliente</label>
              <input
                type="text"
                value={form.cliente}
                onChange={(e) => setForm({ ...form, cliente: e.target.value })}
              />
            </div>
          </div>
          <div className="form-grid">
            <div className="campo">
              <label>Ubicación</label>
              <input
                type="text"
                value={form.ubicacion}
                onChange={(e) => setForm({ ...form, ubicacion: e.target.value })}
              />
            </div>
            <div className="campo">
              <label>Estado</label>
              <select
                value={form.estado}
                onChange={(e) => setForm({ ...form, estado: e.target.value })}
              >
                <option value="ACTIVO">Activo</option>
                <option value="FINALIZADO">Finalizado</option>
              </select>
            </div>
          </div>
          <div className="form-grid">
            <div className="campo">
              <label>Fecha de inicio</label>
              <input
                type="date"
                value={form.fechaInicio}
                onChange={(e) => setForm({ ...form, fechaInicio: e.target.value })}
              />
            </div>
            <div className="campo">
              <label>Fecha de finalización</label>
              <input
                type="date"
                value={form.fechaFin}
                onChange={(e) => setForm({ ...form, fechaFin: e.target.value })}
              />
            </div>
          </div>
          <div className="campo">
            <label>Descripción</label>
            <textarea
              rows={3}
              value={form.descripcion}
              onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
            />
          </div>
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              {editando ? 'Guardar cambios' : 'Crear proyecto'}
            </button>
          </div>
        </form>
      </Modal>
    </section>
  );
}