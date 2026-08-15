import { useCallback, useEffect, useRef, useState } from 'react';
import { get, post, put, del, hoy } from '../api';
import { useEventos } from '../eventos-contexto';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge, MiniImagen, Paginacion } from '../components/ui';
import SubidaImagen from '../components/SubidaImagen';
import QrCodigo from '../components/QrCodigo';

const inicialItem = () => ({ nombre: '', unidad: '', descripcion: '', fotoUrl: null });

const inicialMov = () => ({
  tipo: 'INGRESO',
  cantidad: '',
  fecha: hoy(),
  observacion: '',
});

export default function Inventario({ config, titulo }) {
  const { suscribir } = useEventos();
  const { lista, cargando, error, recargar } = config;
  const [abiertoItem, setAbiertoItem] = useState(false);
  const [editandoItem, setEditandoItem] = useState(null);
  const [formItem, setFormItem] = useState(inicialItem);
  const [errores, setErrores] = useState(null);

  const [movAbierto, setMovAbierto] = useState(false);
  const [itemActivo, setItemActivo] = useState(null);
  const [movimientos, setMovimientos] = useState([]);
  const [movCarga, setMovCarga] = useState(false);
  const [editandoMov, setEditandoMov] = useState(null);
  const [formMov, setFormMov] = useState(inicialMov);
  const ultimoMovRef = useRef(0);

  const cargarMovimientos = useCallback(
    async (id) => {
      ultimoMovRef.current = Date.now();
      setMovCarga(true);
      try {
        setMovimientos(await get(`${config.base}/${id}/movimientos`));
        setErrores(null);
      } catch (err) {
        setErrores([err.message]);
      } finally {
        setMovCarga(false);
      }
    },
    [config.base]
  );

  useEffect(() => {
    if (!movAbierto || !itemActivo) return undefined;
    return suscribir(config.recursoMovimientos, () => {
      if (Date.now() - ultimoMovRef.current < 400) return;
      cargarMovimientos(itemActivo.id);
    });
  }, [movAbierto, itemActivo, suscribir, config.recursoMovimientos, cargarMovimientos]);

  function abrirNuevoItem() {
    setEditandoItem(null);
    setFormItem(inicialItem());
    setErrores(null);
    setAbiertoItem(true);
  }

  function abrirEdicionItem(item) {
    setEditandoItem(item);
    setFormItem({ nombre: item.nombre, unidad: item.unidad, descripcion: item.descripcion, fotoUrl: item.fotoUrl });
    setErrores(null);
    setAbiertoItem(true);
  }

  async function guardarItem(e) {
    e.preventDefault();
    setErrores(null);
    try {
      if (editandoItem) {
        await put(`${config.base}/${editandoItem.id}`, formItem);
      } else {
        await post(config.base, formItem);
      }
      setAbiertoItem(false);
      await recargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminarItem(item) {
    if (!window.confirm(`¿Eliminar ${config.nombreSingular} "${item.nombre}"?`)) return;
    try {
      await del(`${config.base}/${item.id}`);
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
        await put(`${config.baseMovimientos}/${editandoMov.id}`, cuerpo);
      } else {
        await post(`${config.base}/${itemActivo.id}/movimientos`, cuerpo);
      }
      setEditandoMov(null);
      setFormMov(inicialMov());
      await Promise.all([cargarMovimientos(itemActivo.id), recargar()]);
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminarMov(mov) {
    if (!window.confirm('¿Eliminar este movimiento? (el stock se ajustará)')) return;
    try {
      await del(`${config.baseMovimientos}/${mov.id}`);
      setErrores(null);
      await Promise.all([cargarMovimientos(itemActivo.id), recargar()]);
    } catch (err) {
      setErrores([err.message]);
    }
  }

  const columnas = [
    { clave: 'fotoUrl', titulo: 'Foto', render: (x) => <MiniImagen url={x.fotoUrl} alt={x.nombre} /> },
    ...(config.mostrarCodigo
      ? [{ clave: 'codigo', titulo: 'Código', render: (x) => <QrCodigo codigo={x.codigo} tamano={40} /> }]
      : []),
    { clave: 'nombre', titulo: config.nombreSingular === 'material' ? 'Material' : 'Consumible' },
    {
      clave: 'stock',
      titulo: 'Stock',
      render: (x) => (
        <Badge tipo={x.stock > 0 ? 'verde' : 'rojo'}>
          {x.stock} {x.unidad ? ` ${x.unidad}` : ''}
        </Badge>
      ),
    },
    { clave: 'unidad', titulo: 'Unidad' },
    { clave: 'descripcion', titulo: 'Descripción' },
    {
      clave: 'acciones',
      titulo: '',
      render: (x) => (
        <div className="acciones">
          <button type="button" className="btn btn-primario" onClick={() => abrirMovimientos(x)}>
            Movimientos
          </button>
          <button type="button" className="btn btn-borde" onClick={() => abrirEdicionItem(x)}>
            Editar
          </button>
          <button type="button" className="btn btn-peligro" onClick={() => eliminarItem(x)}>
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
        <h2>{titulo}</h2>
        <button type="button" className="btn btn-primario" onClick={abrirNuevoItem}>
          + Nuevo {config.nombreSingular}
        </button>
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla
          columnas={columnas}
          filas={lista}
          vacio={`No hay ${config.nombrePlural} registrados.`}
        />
      )}
      <Paginacion
        pagina={config.pagina}
        total={config.total}
        totalPaginas={config.totalPaginas}
        tamano={config.tamano}
        onPagina={config.setPagina}
        onTamano={config.setTamano}
      />

      <Modal
        titulo={editandoItem ? `Editar ${config.nombreSingular}` : `Nuevo ${config.nombreSingular}`}
        abierto={abiertoItem}
        onCerrar={() => setAbiertoItem(false)}
      >
        <form className="form" onSubmit={guardarItem}>
          <div className="campo">
            <label>Nombre *</label>
            <input
              type="text"
              value={formItem.nombre}
              onChange={(e) => setFormItem({ ...formItem, nombre: e.target.value })}
              required
            />
          </div>
          <div className="campo">
            <label>Unidad</label>
            <input
              type="text"
              value={formItem.unidad}
              placeholder="ej. bulto, unidad, litro"
              onChange={(e) => setFormItem({ ...formItem, unidad: e.target.value })}
            />
          </div>
          <div className="campo">
            <label>Descripción</label>
            <input
              type="text"
              value={formItem.descripcion}
              onChange={(e) => setFormItem({ ...formItem, descripcion: e.target.value })}
            />
          </div>
          <SubidaImagen
            etiqueta="Imagen (opcional)"
            valor={formItem.fotoUrl}
            onCambio={(u) => setFormItem({ ...formItem, fotoUrl: u })}
          />
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbiertoItem(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              {editandoItem ? 'Guardar cambios' : 'Crear'}
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
              placeholder="ej. Compra #45, Uso en obra…"
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