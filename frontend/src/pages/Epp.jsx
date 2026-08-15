import { useState } from 'react';
import { post, put, del } from '../api';
import { useListaPaginada } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge, MiniImagen, Paginacion } from '../components/ui';
import SubidaImagen from '../components/SubidaImagen';

const inicial = () => ({ nombre: '', descripcion: '', stock: 0, fotoUrl: null });

export default function Epp() {
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada(['epp', 'entregas-epp'], '/api/epp/paginado');
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
    setForm({ nombre: item.nombre, descripcion: item.descripcion, stock: item.stock ?? 0, fotoUrl: item.fotoUrl });
    setErrores(null);
    setAbierto(true);
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    try {
      const cuerpo = { ...form, stock: Number(form.stock) };
      if (editando) {
        await put(`/api/epp/${editando.id}`, cuerpo);
      } else {
        await post('/api/epp', cuerpo);
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminar(item) {
    if (!window.confirm(`¿Eliminar el EPP "${item.nombre}"?`)) return;
    try {
      await del(`/api/epp/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const columnas = [
    { clave: 'fotoUrl', titulo: 'Foto', render: (e) => <MiniImagen url={e.fotoUrl} alt={e.nombre} /> },
    { clave: 'nombre', titulo: 'EPP' },
    {
      clave: 'stock',
      titulo: 'Stock',
      render: (e) =>
        e.stock > 0 ? <Badge tipo="verde">{e.stock}</Badge> : <Badge tipo="rojo">0</Badge>,
    },
    { clave: 'descripcion', titulo: 'Descripción' },
    {
      clave: 'acciones',
      titulo: '',
      render: (e) => (
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={() => abrirEdicion(e)}>
            Editar
          </button>
          <button type="button" className="btn btn-peligro" onClick={() => eliminar(e)}>
            Eliminar
          </button>
        </div>
      ),
    },
  ];

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Inventario de EPP</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevo}>
          + Nuevo EPP
        </button>
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay EPP registrados. Crea el primero." />
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
        titulo={editando ? 'Editar EPP' : 'Nuevo EPP'}
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
              <label>Stock inicial</label>
              <input
                type="number"
                min="0"
                value={form.stock}
                onChange={(e) => setForm({ ...form, stock: e.target.value })}
              />
            </div>
          </div>
          <div className="campo">
            <label>Descripción</label>
            <input
              type="text"
              value={form.descripcion}
              onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
            />
          </div>
          <SubidaImagen
            etiqueta="Imagen (opcional)"
            valor={form.fotoUrl}
            onCambio={(u) => setForm({ ...form, fotoUrl: u })}
          />
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              {editando ? 'Guardar cambios' : 'Crear EPP'}
            </button>
          </div>
        </form>
      </Modal>
    </section>
  );
}