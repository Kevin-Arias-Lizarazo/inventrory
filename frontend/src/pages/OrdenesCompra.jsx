import { useCallback, useEffect, useState } from 'react';
import { get, post, put, del, hoy } from '../api';
import { useEventos } from '../eventos-contexto';
import Modal from '../components/Modal';
import { Tabla, Microsofto } from '../components/ui';
import SelectorProducto from '../components/SelectorProducto';

const TIPOS = [
  { valor: 'MATERIAL', etiqueta: 'Material' },
  { valor: 'CONSUMIBLE', etiqueta: 'Consumible' },
  { valor: 'EPP', etiqueta: 'EPP' },
  { valor: 'HERRAMIENTA', etiqueta: 'Herramienta' },
  { valor: 'ROPA', etiqueta: 'Ropa' },
];
const COP = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const fmt = (n) => COP.format(n || 0);
const lineaVacia = () => ({ tipo: 'MATERIAL', productoId: '', descripcion: '', cantidad: '', costoUnitario: '' });
const inicial = () => ({ fecha: hoy(), observacion: '', proveedorId: '', lineas: [lineaVacia()] });

export default function OrdenesCompra() {
  const { suscribir } = useEventos();
  const [lista, setLista] = useState([]);
  const [proveedores, setProveedores] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [abierto, setAbierto] = useState(false);
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(inicial);
  const [errores, setErrores] = useState(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      const [o, p] = await Promise.all([get('/api/ordenes-compra'), get('/api/proveedores')]);
      setLista(o); setProveedores(p); setError(null);
    } catch (e) { setError(e.message); }
    finally { setCargando(false); }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);
  useEffect(() => suscribir(['ordenes-compra'], () => cargar()), [suscribir, cargar]);

  function setLinea(i, patch) {
    setForm((f) => { const lineas = [...f.lineas]; lineas[i] = { ...lineas[i], ...patch }; return { ...f, lineas }; });
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    const sinProducto = form.lineas.some((l) => l.tipo !== 'ROPA' && !l.productoId);
    if (sinProducto) {
      setErrores(['Seleccione un producto en cada artículo (usa el buscador "Usar")']);
      return;
    }
    try {
      const cuerpo = {
        fecha: form.fecha,
        observacion: form.observacion,
        proveedor: form.proveedorId ? { id: Number(form.proveedorId) } : null,
        lineas: form.lineas.map((l) => ({
          tipo: l.tipo,
          productoId: l.productoId ? Number(l.productoId) : null,
          descripcion: l.descripcion,
          cantidad: Number(l.cantidad),
          costoUnitario: Number(l.costoUnitario),
        })),
      };
      if (editando) await put(`/api/ordenes-compra/${editando.id}`, cuerpo);
      else await post('/api/ordenes-compra', cuerpo);
      setAbierto(false); await cargar();
    } catch (err) { setErrores([err.message]); }
  }

  async function eliminar(item) {
    if (!window.confirm(`¿Eliminar orden #${item.id}?`)) return;
    try { await del(`/api/ordenes-compra/${item.id}`); await cargar(); }
    catch (err) { window.alert(err.message); }
  }

  const columnas = [
    { clave: 'id', titulo: '#' },
    { clave: 'fecha', titulo: 'Fecha' },
    { clave: 'proveedor', titulo: 'Proveedor', render: (o) => o.proveedor?.nombre || '—' },
    { clave: 'lineas', titulo: 'Artículos', render: (o) => (o.lineas || []).length },
    { clave: 'total', titulo: 'Total est.', render: (o) => fmt(o.total) },
    {
      clave: 'acciones', titulo: '',
      render: (o) => (
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={() => {
            setEditando(o);
            setForm({
              fecha: o.fecha,
              observacion: o.observacion || '',
              proveedorId: o.proveedor ? String(o.proveedor.id) : '',
              lineas: (o.lineas || []).map((l) => ({
                tipo: l.tipo, productoId: l.productoId || '', descripcion: l.descripcion || '',
                cantidad: l.cantidad, costoUnitario: l.costoUnitario,
              })),
            });
            setErrores(null); setAbierto(true);
          }}>Editar</button>
          <button type="button" className="btn btn-peligro" onClick={() => eliminar(o)}>Eliminar</button>
        </div>
      ),
    },
  ];

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Órdenes de compra</h2>
        <button type="button" className="btn btn-primario" onClick={() => { setEditando(null); setForm(inicial()); setErrores(null); setAbierto(true); }}>+ Nueva orden</button>
      </div>
      <p className="texto-aviso">Documento simple con costos estimados. No mueve stock.</p>
      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && <Tabla columnas={columnas} filas={lista} vacio="No hay órdenes." />}
      <Modal titulo={editando ? 'Editar orden' : 'Nueva orden'} abierto={abierto} onCerrar={() => setAbierto(false)} ancho={760}>
        <form className="form" onSubmit={guardar}>
          <div className="form-grid">
            <div className="campo"><label>Fecha *</label>
              <input type="date" value={form.fecha} onChange={(e) => setForm({ ...form, fecha: e.target.value })} required />
            </div>
            <div className="campo"><label>Proveedor</label>
              <select value={form.proveedorId} onChange={(e) => setForm({ ...form, proveedorId: e.target.value })}>
                <option value="">Sin proveedor</option>
                {proveedores.map((p) => <option key={p.id} value={p.id}>{p.nombre}</option>)}
              </select>
            </div>
          </div>
          <div className="campo"><label>Observación</label>
            <input type="text" value={form.observacion} onChange={(e) => setForm({ ...form, observacion: e.target.value })} />
          </div>
          {form.lineas.map((l, i) => (
            <div key={i} className="linea-articulo">
              <div className="form-grid">
                <div className="campo"><label>Tipo</label>
                  <select value={l.tipo} onChange={(e) => setLinea(i, { tipo: e.target.value, productoId: '', descripcion: '' })}>
                    {TIPOS.map((t) => <option key={t.valor} value={t.valor}>{t.etiqueta}</option>)}
                  </select>
                </div>
                <div className="campo"><label>Cantidad *</label>
                  <input type="number" min="1" value={l.cantidad} onChange={(e) => setLinea(i, { cantidad: e.target.value })} required />
                </div>
                <div className="campo"><label>Costo unit. *</label>
                  <input type="number" min="0" value={l.costoUnitario} onChange={(e) => setLinea(i, { costoUnitario: e.target.value })} required />
                </div>
              </div>
              {l.tipo === 'ROPA' ? (
                <div className="campo"><label>Descripción *</label>
                  <input type="text" value={l.descripcion} onChange={(e) => setLinea(i, { descripcion: e.target.value })} required />
                </div>
              ) : (
                <SelectorProducto
                  tipo={l.tipo}
                  seleccionado={l.descripcion}
                  onUsar={({ productoId, nombre }) => setLinea(i, { productoId, descripcion: nombre })}
                />
              )}
              {l.descripcion && <p className="texto-aviso">{l.descripcion}</p>}
            </div>
          ))}
          <button type="button" className="btn btn-borde" onClick={() => setForm((f) => ({ ...f, lineas: [...f.lineas, lineaVacia()] }))}>+ Línea</button>
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbierto(false)}>Cancelar</button>
            <button type="submit" className="btn btn-primario">Guardar</button>
          </div>
        </form>
      </Modal>
    </section>
  );
}
