import SeccionTabs from '../components/SeccionTabs';
import { TABS_EMPLEADOS } from '../secciones';
import { useState } from 'react';
import { post, put, del, hoy, subirArchivo, firmaAArchivo } from '../api';
import { useLista, useListaPaginada } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, MiniImagen, Paginacion } from '../components/ui';
import SelectEmpleado from '../components/SelectEmpleado';
import SubidaImagen from '../components/SubidaImagen';
import SignaturePad from '../components/SignaturePad';

const inicial = () => ({
  fecha: hoy(),
  empleadoId: null,
  observacion: '',
  fotoUrl: null,
  firma: null,
});

function aDominio(f) {
  const { empleadoId, ...resto } = f;
  return { ...resto, empleado: empleadoId ? { id: empleadoId } : null };
}

export default function EntregasRopa() {
const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada('entregas-ropa', '/api/entregas-ropa/paginado');
const { lista: empleados } = useLista('empleados', '/api/empleados');
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
      fecha: item.fecha,
      empleadoId: item.empleado?.id || null,
      observacion: item.observacion,
      fotoUrl: item.fotoUrl,
      firma: item.firmaUrl,
    });
    setErrores(null);
    setAbierto(true);
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    try {
      let firmaUrl = form.firma;
      if (firmaUrl && firmaUrl.startsWith('data:')) {
        firmaUrl = await subirArchivo(await firmaAArchivo(firmaUrl));
      }
      const cuerpo = aDominio({ ...form, firmaUrl, firma: undefined, fotoUrl: form.fotoUrl });
      if (editando) {
        await put(`/api/entregas-ropa/${editando.id}`, cuerpo);
      } else {
        await post('/api/entregas-ropa', cuerpo);
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminar(item) {
    if (!window.confirm('¿Eliminar esta entrega de ropa?')) return;
    try {
      await del(`/api/entregas-ropa/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const columnas = [
    { clave: 'fecha', titulo: 'Fecha' },
    { clave: 'empleado', titulo: 'Empleado', render: (x) => x.empleado?.nombre || <span className="sin-dato">&mdash;</span> },
    { clave: 'fotoUrl', titulo: 'Foto', render: (x) => <MiniImagen url={x.fotoUrl} alt="foto de la entrega" /> },
    { clave: 'firmaUrl', titulo: 'Firma', render: (x) => <MiniImagen url={x.firmaUrl} alt="firma" /> },
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
      <SeccionTabs items={TABS_EMPLEADOS} />
      <div className="pagina-cabecera">
        <h2>Entrega de ropa</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevo}>
          + Registrar entrega
        </button>
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay entregas de ropa registradas." />
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
        titulo={editando ? 'Editar entrega de ropa' : 'Nueva entrega de ropa'}
        abierto={abierto}
        onCerrar={() => setAbierto(false)}
        ancho={680}
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
              <label>Empleado *</label>
              <SelectEmpleado
                empleados={empleados}
                valor={form.empleadoId}
                onCambio={(id) => setForm({ ...form, empleadoId: id })}
                requerido
                soloContratados
              />
            </div>
          </div>
          <SubidaImagen
            etiqueta="Foto de la entrega"
            valor={form.fotoUrl}
            onCambio={(u) => setForm({ ...form, fotoUrl: u })}
          />
          <div className="campo">
            <label>Firma del empleado</label>
            <SignaturePad
              valor={form.firma}
              onCambio={(f) => setForm({ ...form, firma: f })}
            />
          </div>
          <div className="campo">
            <label>Observación</label>
            <input
              type="text"
              value={form.observacion || ''}
              onChange={(e) => setForm({ ...form, observacion: e.target.value })}
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
    </section>
  );
}