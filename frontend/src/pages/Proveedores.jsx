import { useState } from 'react';
import { post, put, del } from '../api';
import { useListaPaginada } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Paginacion } from '../components/ui';

const inicial = () => ({ nombre: '', telefono: '', correo: '', direccion: '' });

export default function Proveedores() {
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada(['proveedores'], '/api/proveedores/paginado');
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
      telefono: item.telefono || '',
      correo: item.correo || '',
      direccion: item.direccion || '',
    });
    setErrores(null);
    setAbierto(true);
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    try {
      if (editando) {
        await put(`/api/proveedores/${editando.id}`, form);
      } else {
        await post('/api/proveedores', form);
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminar(item) {
    if (!window.confirm(`¿Eliminar el proveedor "${item.nombre}"?`)) return;
    try {
      await del(`/api/proveedores/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const columnas = [
    { clave: 'nombre', titulo: 'Proveedor' },
    { clave: 'telefono', titulo: 'Teléfono' },
    { clave: 'correo', titulo: 'Correo' },
    { clave: 'direccion', titulo: 'Dirección' },
    {
      clave: 'acciones',
      titulo: '',
      render: (p) => (
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={() => abrirEdicion(p)}>
            Editar
          </button>
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
        <h2>Proveedores</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevo}>
          + Nuevo proveedor
        </button>
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay proveedores registrados. Crea el primero." />
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
        titulo={editando ? 'Editar proveedor' : 'Nuevo proveedor'}
        abierto={abierto}
        onCerrar={() => setAbierto(false)}
      >
        <form className="form" onSubmit={guardar}>
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
            <label>Teléfono</label>
            <input
              type="text"
              value={form.telefono}
              onChange={(e) => setForm({ ...form, telefono: e.target.value })}
            />
          </div>
          <div className="campo">
            <label>Correo</label>
            <input
              type="email"
              value={form.correo}
              onChange={(e) => setForm({ ...form, correo: e.target.value })}
            />
          </div>
          <div className="campo">
            <label>Dirección</label>
            <input
              type="text"
              value={form.direccion}
              onChange={(e) => setForm({ ...form, direccion: e.target.value })}
            />
          </div>
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              {editando ? 'Guardar cambios' : 'Crear'}
            </button>
          </div>
        </form>
      </Modal>
    </section>
  );
}