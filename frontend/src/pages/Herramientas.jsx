import { useCallback, useEffect, useRef, useState } from 'react';
import { get, post, put, del, hoy } from '../api';
import { useListaPaginada } from '../hooks';
import { useEventos } from '../eventos-contexto';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge, MiniImagen, Paginacion } from '../components/ui';
import SubidaImagen from '../components/SubidaImagen';
import QrCodigo from '../components/QrCodigo';

const inicial = () => ({ nombre: '', marca: '', descripcion: '', cantidadTotal: 1, fotoUrl: null, stockMinimo: '' });

const inicialMov = () => ({
  tipo: 'INGRESO',
  cantidad: '',
  fecha: hoy(),
  observacion: '',
});

export default function Herramientas() {
  const { suscribir } = useEventos();
  const { lista, cargando, error, pagina, tamano, total, totalPaginas, setPagina, setTamano, recargar } =
    useListaPaginada(
      ['herramientas', 'asignaciones-herramientas', 'movimientos-herramientas'],
      '/api/herramientas/paginado'
    );
  const [abierto, setAbierto] = useState(false);
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(inicial);
  const [errores, setErrores] = useState(null);

  const [movAbierto, setMovAbierto] = useState(false);
  const [itemActivo, setItemActivo] = useState(null);
  const [movimientos, setMovimientos] = useState([]);
  const [movCarga, setMovCarga] = useState(false);
  const [editandoMov, setEditandoMov] = useState(null);
  const [formMov, setFormMov] = useState(inicialMov);
  const ultimoMovRef = useRef(0);

  const cargarMovimientos = useCallback(async (id) => {
    ultimoMovRef.current = Date.now();
    setMovCarga(true);
    try {
      setMovimientos(await get(`/api/herramientas/${id}/movimientos`));
      setErrores(null);
    } catch (err) {
      setErrores([err.message]);
    } finally {
      setMovCarga(false);
    }
  }, []);

  useEffect(() => {
    if (!movAbierto || !itemActivo) return undefined;
    return suscribir('movimientos-herramientas', () => {
      if (Date.now() - ultimoMovRef.current < 400) return;
      cargarMovimientos(itemActivo.id);
    });
  }, [movAbierto, itemActivo, suscribir, cargarMovimientos]);

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
      marca: item.marca,
      descripcion: item.descripcion,
      cantidadTotal: item.cantidadTotal ?? 1,
      fotoUrl: item.fotoUrl,
      stockMinimo: item.stockMinimo ?? '',
    });
    setErrores(null);
    setAbierto(true);
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    try {
      const cuerpo = {
        ...form,
        cantidadTotal: Number(form.cantidadTotal),
        stockMinimo: form.stockMinimo === '' || form.stockMinimo == null ? null : Number(form.stockMinimo),
      };
      if (editando) {
        await put(`/api/herramientas/${editando.id}`, cuerpo);
      } else {
        await post('/api/herramientas', cuerpo);
      }
      setAbierto(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function accion(item, ruta) {
    try {
      await post(`/api/herramientas/${item.id}${ruta}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  async function eliminar(item) {
    if (!window.confirm(`¿Eliminar la herramienta "${item.nombre}"?`)) return;
    try {
      await del(`/api/herramientas/${item.id}`);
      await recargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  async function abrirMovimientos(item) {
    setItemActivo(item);
    setEditandoMov(null);
    setFormMov(inicialMov());
    setMovAbierto(true);
    await cargarMovimientos(item.id);
  }

  function editarMov(mov) {
    setEditandoMov(mov);
    setFormMov({ tipo: mov.tipo, cantidad: mov.cantidad, fecha: mov.fecha, observacion: mov.observacion });
  }

  async function guardarMov(e) {
    e.preventDefault();
    setErrores(null);
    try {
      const cuerpo = { ...formMov, cantidad: Number(formMov.cantidad) };
      if (editandoMov) {
        await put(`/api/movimientos-herramientas/${editandoMov.id}`, cuerpo);
      } else {
        await post(`/api/herramientas/${itemActivo.id}/movimientos`, cuerpo);
      }
      setEditandoMov(null);
      setFormMov(inicialMov());
      await Promise.all([cargarMovimientos(itemActivo.id), recargar()]);
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminarMov(mov) {
    if (!window.confirm('¿Eliminar este movimiento? (la cantidad total se ajustará)')) return;
    try {
      await del(`/api/movimientos-herramientas/${mov.id}`);
      setErrores(null);
      await Promise.all([cargarMovimientos(itemActivo.id), recargar()]);
    } catch (err) {
      setErrores([err.message]);
    }
  }

  function disponibilidadBadge(h) {
    const disponible = h.cantidadDisponible ?? 0;
    const bajo = h.stockMinimo != null && h.stockMinimo > 0 && disponible <= h.stockMinimo;
    if (bajo) return <Badge tipo="rojo">{disponible} ⚠</Badge>;
    if (disponible > 0) return <Badge tipo="verde">{disponible}</Badge>;
    return <Badge tipo="rojo">0</Badge>;
  }

  const columnas = [
    { clave: 'fotoUrl', titulo: 'Foto', render: (h) => <MiniImagen url={h.fotoUrl} alt={h.nombre} /> },
    { clave: 'codigo', titulo: 'Código', render: (h) => <QrCodigo codigo={h.codigo} tamano={40} /> },
    { clave: 'nombre', titulo: 'Herramienta' },
    { clave: 'marca', titulo: 'Marca' },
    { clave: 'cantidadTotal', titulo: 'Cantidad' },
    {
      clave: 'cantidadDisponible',
      titulo: 'Disponible',
      render: (h) => disponibilidadBadge(h),
    },
    {
      clave: 'stockMinimo',
      titulo: 'Mín.',
      render: (h) => (h.stockMinimo != null && h.stockMinimo > 0 ? h.stockMinimo : <span className="sin-dato">&mdash;</span>),
    },
    { clave: 'cantidadAsignada', titulo: 'Asignada' },
    {
      clave: 'cantidadDanada',
      titulo: 'Dañada',
      render: (h) => (
        <div className="acciones">
          <span className="cantidad-num">{h.cantidadDanada ?? 0}</span>
          <button
            type="button"
            className="btn btn-borde btn-mini"
            title="Marcar 1 unidad como dañada"
            onClick={() => accion(h, '/danada')}
          >
            +1
          </button>
          <button
            type="button"
            className="btn btn-borde btn-mini"
            title="Reparar 1 unidad"
            onClick={() => accion(h, '/reparar')}
          >
            -1
          </button>
          <button
            type="button"
            className="btn btn-peligro btn-mini"
            title="Desechar 1 unidad dañada"
            onClick={() => accion(h, '/desechar-danada')}
          >
            Desechar
          </button>
        </div>
      ),
    },
    {
      clave: 'cantidadPerdida',
      titulo: 'Perdida',
      render: (h) => (
        <div className="acciones">
          <span className="cantidad-num">{h.cantidadPerdida ?? 0}</span>
          <button
            type="button"
            className="btn btn-peligro btn-mini"
            title="Marcar 1 unidad como perdida"
            onClick={() => accion(h, '/perdida')}
          >
            +1
          </button>
        </div>
      ),
    },
    { clave: 'descripcion', titulo: 'Descripción' },
    {
      clave: 'acciones',
      titulo: '',
      render: (h) => (
        <div className="acciones">
          <button type="button" className="btn btn-primario" onClick={() => abrirMovimientos(h)}>
            Movimientos
          </button>
          <button type="button" className="btn btn-borde" onClick={() => abrirEdicion(h)}>
            Editar
          </button>
          <button type="button" className="btn btn-peligro" onClick={() => eliminar(h)}>
            Eliminar
          </button>
        </div>
      ),
    },
  ];

  const colMov = [
    {
      clave: 'tipo',
      titulo: 'Tipo',
      render: (m) =>
        m.tipo === 'INGRESO' ? <Badge tipo="verde">Ingreso</Badge> : <Badge tipo="rojo">Egreso</Badge>,
    },
    { clave: 'cantidad', titulo: 'Cantidad' },
    { clave: 'fecha', titulo: 'Fecha' },
    { clave: 'observacion', titulo: 'Observación' },
    {
      clave: 'acciones',
      titulo: '',
      render: (m) => (
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={() => editarMov(m)}>
            Editar
          </button>
          <button type="button" className="btn btn-peligro" onClick={() => eliminarMov(m)}>
            Eliminar
          </button>
        </div>
      ),
    },
  ];

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Inventario de herramientas</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevo}>
          + Nueva herramienta
        </button>
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla
          columnas={columnas}
          filas={lista}
          vacio="No hay herramientas registradas. Crea la primera."
        />
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
        titulo={editando ? 'Editar herramienta' : 'Nueva herramienta'}
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
              <label>Marca</label>
              <input
                type="text"
                value={form.marca}
                onChange={(e) => setForm({ ...form, marca: e.target.value })}
              />
            </div>
          </div>
          <div className="form-grid">
            <div className="campo">
              <label>Cantidad total *</label>
              <input
                type="number"
                min="1"
                value={form.cantidadTotal}
                onChange={(e) => setForm({ ...form, cantidadTotal: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label>Stock mínimo</label>
              <input
                type="number"
                min="0"
                value={form.stockMinimo}
                placeholder="0 = sin alerta"
                onChange={(e) => setForm({ ...form, stockMinimo: e.target.value })}
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
              {editando ? 'Guardar cambios' : 'Crear herramienta'}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        titulo={`Movimientos — ${itemActivo?.nombre || ''}`}
        abierto={movAbierto}
        onCerrar={() => setMovAbierto(false)}
        ancho={860}
      >
        {movCarga ? (
          <p className="vacio">Cargando…</p>
        ) : (
          <Tabla columnas={colMov} filas={movimientos} vacio="No hay movimientos." />
        )}

        <div className="separador">
          <h4>{editandoMov ? 'Editar movimiento' : 'Nuevo movimiento'}</h4>
        </div>

        <form className="form" onSubmit={guardarMov}>
          <div className="form-grid">
            <div className="campo">
              <label>Tipo</label>
              <select
                value={formMov.tipo}
                onChange={(e) => setFormMov({ ...formMov, tipo: e.target.value })}
              >
                <option value="INGRESO">Ingreso</option>
                <option value="EGRESO">Egreso</option>
              </select>
            </div>
            <div className="campo">
              <label>Cantidad *</label>
              <input
                type="number"
                min="1"
                value={formMov.cantidad}
                onChange={(e) => setFormMov({ ...formMov, cantidad: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label>Fecha</label>
              <input
                type="date"
                value={formMov.fecha}
                onChange={(e) => setFormMov({ ...formMov, fecha: e.target.value })}
                required
              />
            </div>
          </div>
          <div className="campo">
            <label>Observación</label>
            <input
              type="text"
              value={formMov.observacion}
              placeholder="ej. Compra #45, Baja, Transferencia…"
              onChange={(e) => setFormMov({ ...formMov, observacion: e.target.value })}
            />
          </div>
          <Microsofto errores={errores} />
          <div className="form-acciones">
            {editandoMov && (
              <button
                type="button"
                className="btn btn-borde"
                onClick={() => {
                  setEditandoMov(null);
                  setFormMov(inicialMov());
                }}
              >
                Cancelar edición
              </button>
            )}
            <button type="submit" className="btn btn-primario">
              {editandoMov ? 'Guardar movimiento' : 'Registrar movimiento'}
            </button>
          </div>
        </form>
      </Modal>
    </section>
  );
}
