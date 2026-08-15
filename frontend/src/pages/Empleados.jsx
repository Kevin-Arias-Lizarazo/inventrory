import { useState } from 'react';
import { post, put, del, hoy } from '../api';
import { useListaPaginada, useDebounce } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, MiniImagen, Badge, Paginacion } from '../components/ui';
import SubidaImagen from '../components/SubidaImagen';
import QrCodigo from '../components/QrCodigo';

const inicial = () => ({
  nombre: '',
  documento: '',
  cargo: '',
  telefono: '',
  correo: '',
  direccion: '',
  fechaIngreso: hoy(),
  hojaVida: '',
  fotoUrl: null,
});

export default function Empleados() {
  const [busqueda, setBusqueda] = useState('');
  const q = useDebounce(busqueda, 300);
  const url = q.trim() ? `/api/empleados/paginado?q=${encodeURIComponent(q.trim())}` : '/api/empleados/paginado';
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada('empleados', url);
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
    setForm({ ...item });
    setErrores(null);
    setAbierto(true);
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    try {
      if (editando) {
        await put(`/api/empleados/${editando.id}`, form);
      } else {
        await post('/api/empleados', form);
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminar(item) {
    if (!window.confirm(`¿Eliminar al empleado "${item.nombre}"?`)) return;
    try {
      await del(`/api/empleados/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const columnas = [
    {
      clave: 'foto',
      titulo: 'Foto',
      render: (e) => <MiniImagen url={e.fotoUrl} alto={40} />,
    },
    { clave: 'codigo', titulo: 'Código', render: (e) => <QrCodigo codigo={e.codigo} tamano={40} /> },
    { clave: 'nombre', titulo: 'Nombre' },
    { clave: 'documento', titulo: 'Documento' },
    { clave: 'cargo', titulo: 'Cargo' },
    {
      clave: 'contratado',
      titulo: 'Contrato',
      render: (e) =>
        e.contratado ? <Badge tipo="verde">Contratado</Badge> : <Badge tipo="gris">Sin contrato</Badge>,
    },
    { clave: 'telefono', titulo: 'Teléfono' },
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

  const campo = (nombre, tipo = 'text', requerido = false) => (
    <input
      type={tipo}
      value={form[nombre] || ''}
      onChange={(e) => setForm({ ...form, [nombre]: e.target.value })}
      required={requerido}
    />
  );

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Empleados y hoja de vida</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevo}>
          + Nuevo empleado
        </button>
      </div>

      <div className="busqueda">
        <input
          type="search"
          placeholder="Buscar por nombre, documento o cargo…"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
        />
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay empleados registrados." />
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
        titulo={editando ? 'Editar empleado' : 'Nuevo empleado'}
        abierto={abierto}
        onCerrar={() => setAbierto(false)}
        ancho={640}
      >
        <form className="form" onSubmit={guardar}>
          <div className="form-grid">
            <div className="campo">
              <label>Nombre *</label>
              {campo('nombre', 'text', true)}
            </div>
            <div className="campo">
              <label>Documento</label>
              {campo('documento')}
            </div>
            <div className="campo">
              <label>Cargo</label>
              {campo('cargo')}
            </div>
            <div className="campo">
              <label>Teléfono</label>
              {campo('telefono')}
            </div>
            <div className="campo">
              <label>Correo</label>
              {campo('correo', 'email')}
            </div>
            <div className="campo">
              <label>Fecha de ingreso</label>
              {campo('fechaIngreso', 'date')}
            </div>
          </div>
          <div className="campo">
            <label>Dirección</label>
            {campo('direccion')}
          </div>
          <SubidaImagen etiqueta="Foto del empleado" valor={form.fotoUrl} onCambio={(u) => setForm({ ...form, fotoUrl: u })} />
          <div className="campo">
            <label>Hoja de vida</label>
            <textarea
              rows={7}
              value={form.hojaVida || ''}
              onChange={(e) => setForm({ ...form, hojaVida: e.target.value })}
              placeholder="Experiencia, formación, observaciones…"
            />
          </div>
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              {editando ? 'Guardar cambios' : 'Crear empleado'}
            </button>
          </div>
        </form>
      </Modal>
    </section>
  );
}