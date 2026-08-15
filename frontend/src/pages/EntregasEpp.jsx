import { useState } from 'react';
import { post, put, del, hoy, subirArchivo, firmaAArchivo } from '../api';
import { useLista, useListaPaginada, useDebounce } from '../hooks';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge, MiniImagen, Paginacion } from '../components/ui';
import SelectEmpleado from '../components/SelectEmpleado';
import SubidaImagen from '../components/SubidaImagen';
import SignaturePad from '../components/SignaturePad';

const inicial = () => ({ fecha: hoy(), empleadoId: null, eppId: null, observacion: '', fotoUrl: null, firma: null });

function aDominio(f) {
  const { empleadoId, eppId, ...resto } = f;
  return {
    ...resto,
    empleado: empleadoId ? { id: empleadoId } : null,
    epp: eppId ? { id: eppId } : null,
  };
}

export default function EntregasEpp() {
  const { lista: empleados } = useLista('empleados', '/api/empleados?contratados=true');
  const { lista: empleadosFiltro } = useLista('empleados', '/api/empleados');
  const { lista: epps } = useLista(['epp', 'entregas-epp'], '/api/epp');
  const [fechaFiltro, setFechaFiltro] = useState('');
  const [empleadoFiltro, setEmpleadoFiltro] = useState('');
  const [eppFiltro, setEppFiltro] = useState('');
  const [orden, setOrden] = useState('desc');
  const fecha = useDebounce(fechaFiltro, 300);
  const parametros = new URLSearchParams();
  if (fecha) parametros.set('fecha', fecha);
  if (empleadoFiltro) parametros.set('empleadoId', empleadoFiltro);
  if (eppFiltro) parametros.set('eppId', eppFiltro);
  parametros.set('orden', orden);
  const url = `/api/entregas-epp/filtradas?${parametros.toString()}`;
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada(['entregas-epp', 'epp'], url);
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
      eppId: item.epp?.id || null,
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
        await put(`/api/entregas-epp/${editando.id}`, cuerpo);
      } else {
        await post('/api/entregas-epp', cuerpo);
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminar(item) {
    if (!window.confirm('¿Eliminar esta entrega de EPP? (el stock se restaurará)')) return;
    try {
      await del(`/api/entregas-epp/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const eppsDisponibles = (editando
    ? epps
    : epps.filter((e) => (e.stock ?? 0) > 0))
    .filter((e) => (e.stock ?? 0) > 0 || e.id === editando?.epp?.id);

  const columnas = [
    { clave: 'fecha', titulo: 'Fecha' },
    { clave: 'empleado', titulo: 'Empleado', render: (x) => x.empleado?.nombre || <Badge tipo="gris">sin asignar</Badge> },
    {
      clave: 'epp',
      titulo: 'EPP',
      render: (x) => (x.epp?.nombre ? <Badge tipo="azul">{x.epp.nombre}</Badge> : <Badge tipo="gris">sin epp</Badge>),
    },
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
      <div className="pagina-cabecera">
        <h2>Entrega de EPP</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevo}>
          + Registrar entrega
        </button>
      </div>

      <div className="minuta-filtros">
        <input type="date" value={fechaFiltro} onChange={(e) => setFechaFiltro(e.target.value)} title="Filtrar por día" />
        <select value={empleadoFiltro} onChange={(e) => setEmpleadoFiltro(e.target.value)} title="Filtrar por empleado">
          <option value="">Todos los empleados</option>
          {empleadosFiltro.map((e) => (
            <option key={e.id} value={e.id}>
              {e.nombre}
            </option>
          ))}
        </select>
        <select value={eppFiltro} onChange={(e) => setEppFiltro(e.target.value)} title="Filtrar por EPP">
          <option value="">Todos los EPP</option>
          {epps.map((e) => (
            <option key={e.id} value={e.id}>
              {e.nombre}
            </option>
          ))}
        </select>
        <select value={orden} onChange={(e) => setOrden(e.target.value)} title="Orden por fecha">
          <option value="desc">Más reciente primero</option>
          <option value="asc">Más antigua primero</option>
        </select>
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay entregas de EPP registradas." />
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
        titulo={editando ? 'Editar entrega de EPP' : 'Nueva entrega de EPP'}
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
              />
            </div>
          </div>
          <div className="campo">
            <label>EPP *</label>
            <select
              value={form.eppId || ''}
              onChange={(e) => setForm({ ...form, eppId: e.target.value ? Number(e.target.value) : null })}
              required
            >
              <option value="">&mdash; Seleccione EPP disponible &mdash;</option>
              {eppsDisponibles.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.nombre} — {e.stock ?? 0} disponibles
                </option>
              ))}
            </select>
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