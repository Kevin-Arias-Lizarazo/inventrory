import { useCallback, useEffect, useState } from 'react';
import { get, post, put, del, hoy } from '../api';
import { useEventos } from '../eventos-contexto';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge } from '../components/ui';
import SelectorProducto from '../components/SelectorProducto';

const TIPOS = [
  { valor: 'HERRAMIENTA', etiqueta: 'Herramienta' },
  { valor: 'EPP', etiqueta: 'EPP' },
  { valor: 'CONSUMIBLE', etiqueta: 'Consumible' },
  { valor: 'MATERIAL', etiqueta: 'Material' },
  { valor: 'ROPA', etiqueta: 'Ropa' },
];

const COP = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

const fmt = (n) => COP.format(n || 0);

const proveedorObj = (id) => (id ? { id: Number(id) } : null);

const inicialLinea = (tipo = 'HERRAMIENTA') => ({
  tipo,
  productoId: '',
  nombre: '',
  descripcion: '',
  cantidad: '',
  costoUnitario: '',
});

const inicialCompra = () => ({ fecha: hoy(), observacion: '', proveedorId: '', lineas: [inicialLinea()] });

const inicialFactura = () => ({
  numero: '',
  fecha: hoy(),
  observacion: '',
  proveedorId: '',
  crearCompra: false,
  compraId: '',
  lineas: [inicialLinea()],
});

export default function Compras() {
  const { suscribir } = useEventos();
  const [pestana, setPestana] = useState('compras');
  const [compras, setCompras] = useState([]);
  const [facturas, setFacturas] = useState([]);
  const [proveedores, setProveedores] = useState([]);
  const [comprasSinFacturar, setComprasSinFacturar] = useState([]);
  const [valor, setValor] = useState({ total: 0, sinPrecio: 0, sinFacturar: 0 });
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  const [filtroEstado, setFiltroEstado] = useState('todas');
  const [filtroProveedor, setFiltroProveedor] = useState('');
  const [filtroFecha, setFiltroFecha] = useState('');
  const [busqueda, setBusqueda] = useState('');

  const [compraAbierto, setCompraAbierto] = useState(false);
  const [editandoCompra, setEditandoCompra] = useState(null);
  const [formCompra, setFormCompra] = useState(inicialCompra);
  const [erroresCompra, setErroresCompra] = useState(null);

  const [facturaAbierto, setFacturaAbierto] = useState(false);
  const [editandoFactura, setEditandoFactura] = useState(null);
  const [formFactura, setFormFactura] = useState(inicialFactura);
  const [erroresFactura, setErroresFactura] = useState(null);

  const [devAbierto, setDevAbierto] = useState(false);
  const [compraDev, setCompraDev] = useState(null);
  const [formDev, setFormDev] = useState({ fecha: hoy(), observacion: '', lineas: [] });
  const [erroresDev, setErroresDev] = useState(null);

  const cargarTodo = useCallback(async ({ silencioso = false } = {}) => {
    if (!silencioso) setCargando(true);
    try {
      const [cs, fs, ps, hs, es, csu, ms] = await Promise.all([
        get('/api/compras'),
        get('/api/facturas'),
        get('/api/proveedores'),
        get('/api/herramientas'),
        get('/api/epp'),
        get('/api/consumibles'),
        get('/api/materiales'),
      ]);
      setCompras(cs || []);
      setFacturas(fs || []);
      setProveedores(ps || []);
      setComprasSinFacturar((cs || []).filter((c) => !c.facturaId));
      const productos = [...(hs || []), ...(es || []), ...(csu || []), ...(ms || [])];
      const total = productos.reduce((acc, p) => acc + (p.stock || 0) * (p.ultimoCosto || 0), 0);
      const sinPrecio = productos.filter((p) => (p.stock || 0) > 0 && p.ultimoCosto == null).length;
      const sinFacturar = (cs || []).filter((c) => !c.facturaId).length;
      setValor({ total, sinPrecio, sinFacturar });
      setError(null);
    } catch (e) {
      if (!silencioso) setError(e.message);
    } finally {
      if (!silencioso) setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargarTodo();
  }, [cargarTodo]);

  useEffect(
    () =>
      suscribir(
        ['compras', 'facturas', 'proveedores', 'materiales', 'consumibles', 'epp', 'herramientas', 'movimientos-epp'],
        () => cargarTodo({ silencioso: true })
      ),
    [suscribir, cargarTodo]
  );

  function cambiarLineaCompra(idx, campo, valor) {
    setFormCompra((f) => ({
      ...f,
      lineas: f.lineas.map((l, i) => (i === idx ? { ...l, [campo]: valor } : l)),
    }));
  }

  function cambiarLineaFactura(idx, campo, valor) {
    setFormFactura((f) => ({
      ...f,
      lineas: f.lineas.map((l, i) => (i === idx ? { ...l, [campo]: valor } : l)),
    }));
  }

  async function guardarCompra(e) {
    e.preventDefault();
    setErroresCompra(null);
    try {
      const cuerpo = {
        fecha: formCompra.fecha,
        observacion: formCompra.observacion,
        proveedor: proveedorObj(formCompra.proveedorId),
        lineas: formCompra.lineas.map((l) => ({
          tipo: l.tipo,
          productoId: l.tipo === 'ROPA' ? null : Number(l.productoId),
          descripcion: l.tipo === 'ROPA' ? l.descripcion : null,
          cantidad: Number(l.cantidad),
        })),
      };
      if (editandoCompra) {
        await put(`/api/compras/${editandoCompra.id}`, cuerpo);
      } else {
        await post('/api/compras', cuerpo);
      }
      setCompraAbierto(false);
      setErroresCompra(null);
    } catch (err) {
      setErroresCompra([err.message]);
    }
  }

  async function eliminarCompra(item) {
    if (!window.confirm(`¿Eliminar la compra del ${item.fecha}? Se revertirá el stock.`)) return;
    try {
      await del(`/api/compras/${item.id}`);
    } catch (err) {
      window.alert(err.message);
    }
  }

  function abrirDevolucion(compra) {
    setCompraDev(compra);
    setFormDev({
      fecha: hoy(),
      observacion: '',
      lineas: (compra.lineas || [])
        .filter((l) => l.tipo !== 'ROPA')
        .map((l) => ({
          tipo: l.tipo,
          productoId: l.productoId,
          descripcion: l.descripcion || '',
          cantidadMax: l.cantidad,
          cantidad: '',
        })),
    });
    setErroresDev(null);
    setDevAbierto(true);
  }

  async function guardarDevolucion(e) {
    e.preventDefault();
    setErroresDev(null);
    try {
      const lineas = formDev.lineas
        .filter((l) => Number(l.cantidad) > 0)
        .map((l) => ({
          tipo: l.tipo,
          productoId: l.productoId,
          descripcion: l.descripcion,
          cantidad: Number(l.cantidad),
        }));
      if (lineas.length === 0) {
        setErroresDev(['Indica al menos una cantidad a devolver']);
        return;
      }
      await post(`/api/compras/${compraDev.id}/devoluciones`, {
        fecha: formDev.fecha,
        observacion: formDev.observacion,
        lineas,
      });
      setDevAbierto(false);
      await cargarTodo({ silencioso: true });
    } catch (err) {
      setErroresDev([err.message]);
    }
  }

  function abrirNuevaCompra() {
    setEditandoCompra(null);
    setFormCompra(inicialCompra());
    setErroresCompra(null);
    setCompraAbierto(true);
  }

  function abrirEdicionCompra(item) {
    setEditandoCompra(item);
    setFormCompra({
      fecha: item.fecha,
      observacion: item.observacion || '',
      proveedorId: item.proveedor ? String(item.proveedor.id) : '',
      lineas: item.lineas.map((l) => ({
        tipo: l.tipo,
        productoId: l.productoId || '',
        nombre: l.descripcion || '',
        descripcion: l.descripcion || '',
        cantidad: String(l.cantidad),
      })),
    });
    setErroresCompra(null);
    setCompraAbierto(true);
  }

  async function guardarFactura(e) {
    e.preventDefault();
    setErroresFactura(null);
    try {
      const cuerpo = {
        numero: formFactura.numero,
        fecha: formFactura.fecha,
        observacion: formFactura.observacion,
        proveedor: proveedorObj(formFactura.proveedorId),
        crearCompra: editandoFactura ? false : formFactura.crearCompra,
        compraId: formFactura.compraId ? Number(formFactura.compraId) : null,
        lineas: formFactura.lineas.map((l) => ({
          tipo: l.tipo,
          productoId: l.tipo === 'ROPA' ? null : Number(l.productoId),
          descripcion: l.tipo === 'ROPA' ? l.descripcion : null,
          cantidad: Number(l.cantidad),
          costoUnitario: Number(l.costoUnitario),
        })),
      };
      if (editandoFactura) {
        await put(`/api/facturas/${editandoFactura.id}`, cuerpo);
      } else {
        await post('/api/facturas', cuerpo);
      }
      setFacturaAbierto(false);
      setErroresFactura(null);
    } catch (err) {
      setErroresFactura([err.message]);
    }
  }

  async function eliminarFactura(item) {
    if (!window.confirm(`¿Eliminar la factura ${item.numero || `del ${item.fecha}`}?`)) return;
    try {
      await del(`/api/facturas/${item.id}`);
    } catch (err) {
      window.alert(err.message);
    }
  }

  function abrirNuevaFactura() {
    setEditandoFactura(null);
    setFormFactura(inicialFactura());
    setErroresFactura(null);
    setFacturaAbierto(true);
  }

  function abrirEdicionFactura(item) {
    setEditandoFactura(item);
    setFormFactura({
      numero: item.numero || '',
      fecha: item.fecha,
      observacion: item.observacion || '',
      proveedorId: item.proveedor ? String(item.proveedor.id) : '',
      crearCompra: false,
      compraId: '',
      lineas: item.lineas.map((l) => ({
        tipo: l.tipo,
        productoId: l.productoId || '',
        nombre: l.descripcion || '',
        descripcion: l.descripcion || '',
        cantidad: String(l.cantidad),
        costoUnitario: String(l.costoUnitario),
      })),
    });
    setErroresFactura(null);
    setFacturaAbierto(true);
  }

  const comprasFiltradas = compras.filter((c) => {
    if (filtroEstado === 'sin-facturar' && c.facturaId) return false;
    if (filtroEstado === 'facturadas' && !c.facturaId) return false;
    if (filtroProveedor && (!c.proveedor || String(c.proveedor.id) !== filtroProveedor)) return false;
    if (filtroFecha && c.fecha !== filtroFecha) return false;
    return true;
  });

  const facturasFiltradas = facturas.filter((f) => {
    if (filtroProveedor && (!f.proveedor || String(f.proveedor.id) !== filtroProveedor)) return false;
    if (filtroFecha && f.fecha !== filtroFecha) return false;
    if (busqueda.trim()) {
      const q = busqueda.toLowerCase();
      if (!((f.numero || '') + (f.observacion || '')).toLowerCase().includes(q)) return false;
    }
    return true;
  });

  const colCompra = [
    { clave: 'fecha', titulo: 'Fecha' },
    { clave: 'proveedor', titulo: 'Proveedor', render: (c) => (c.proveedor ? c.proveedor.nombre : <span className="sin-dato">&mdash;</span>) },
    { clave: 'articulos', titulo: 'Artículos', render: (c) => (c.lineas || []).length },
    {
      clave: 'estado',
      titulo: 'Estado',
      render: (c) =>
        c.facturaId ? (
          <Badge tipo="verde">Facturada</Badge>
        ) : (
          <Badge tipo="amarillo">Sin facturar</Badge>
        ),
    },
    { clave: 'observacion', titulo: 'Observación' },
    {
      clave: 'acciones',
      titulo: '',
      render: (c) => (
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={() => abrirDevolucion(c)}>
            Devolver
          </button>
          {!c.facturaId && (
            <button type="button" className="btn btn-borde" onClick={() => abrirEdicionCompra(c)}>
              Editar
            </button>
          )}
          <button type="button" className="btn btn-peligro" onClick={() => eliminarCompra(c)}>
            Eliminar
          </button>
        </div>
      ),
    },
  ];

  const colFactura = [
    { clave: 'numero', titulo: 'N° factura', render: (f) => f.numero || <span className="sin-dato">&mdash;</span> },
    { clave: 'fecha', titulo: 'Fecha' },
    { clave: 'proveedor', titulo: 'Proveedor', render: (f) => (f.proveedor ? f.proveedor.nombre : <span className="sin-dato">Informal</span>) },
    { clave: 'articulos', titulo: 'Artículos', render: (f) => (f.lineas || []).length },
    { clave: 'total', titulo: 'Total', render: (f) => fmt(f.total) },
    {
      clave: 'estado',
      titulo: 'Estado',
      render: (f) => (f.compraId ? <Badge tipo="verde">Vinculada a compra</Badge> : <Badge tipo="neutro">Sin compra</Badge>),
    },
    {
      clave: 'acciones',
      titulo: '',
      render: (f) => (
        <div className="acciones">
          {!f.compraId && (
            <button type="button" className="btn btn-borde" onClick={() => abrirEdicionFactura(f)}>
              Editar
            </button>
          )}
          <button type="button" className="btn btn-peligro" onClick={() => eliminarFactura(f)}>
            Eliminar
          </button>
        </div>
      ),
    },
  ];

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Compras</h2>
        <div className="acciones">
          {pestana === 'compras' ? (
            <button type="button" className="btn btn-primario" onClick={abrirNuevaCompra}>
              + Nueva compra
            </button>
          ) : (
            <button type="button" className="btn btn-primario" onClick={abrirNuevaFactura}>
              + Nueva factura
            </button>
          )}
        </div>
      </div>

      <div className="panel-valor">
        <div className="valor-item">
          <span>Valor del inventario (facturado)</span>
          <strong>{fmt(valor.total)}</strong>
        </div>
        <div className="valor-item">
          <span>Compras sin facturar</span>
          <strong>{valor.sinFacturar}</strong>
        </div>
        <div className="valor-item">
          <span>Productos con stock sin precio</span>
          <strong className={valor.sinPrecio > 0 ? 'texto-peligro' : ''}>{valor.sinPrecio}</strong>
        </div>
        {valor.sinPrecio > 0 && (
          <p className="texto-aviso">
            Hay {valor.sinPrecio} producto(s) con stock sin facturar: registra la factura para fijar su costo.
          </p>
        )}
      </div>

      <div className="pestanas">
        <button
          type="button"
          className={`pestana ${pestana === 'compras' ? 'pestana-activa' : ''}`}
          onClick={() => setPestana('compras')}
        >
          Compras
        </button>
        <button
          type="button"
          className={`pestana ${pestana === 'facturas' ? 'pestana-activa' : ''}`}
          onClick={() => setPestana('facturas')}
        >
          Facturas
        </button>
      </div>

      <div className="filtros">
        <select value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
          <option value="todas">Todos los estados</option>
          <option value="sin-facturar">Sin facturar</option>
          <option value="facturadas">Facturadas</option>
        </select>
        <select value={filtroProveedor} onChange={(e) => setFiltroProveedor(e.target.value)}>
          <option value="">Todos los proveedores</option>
          {proveedores.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nombre}
            </option>
          ))}
        </select>
        <input type="date" value={filtroFecha} onChange={(e) => setFiltroFecha(e.target.value)} />
        {pestana === 'facturas' && (
          <input
            type="text"
            placeholder="Buscar por N° u observación…"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
        )}
      </div>

      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && pestana === 'compras' && (
        <Tabla columnas={colCompra} filas={comprasFiltradas} vacio="No hay compras registradas." />
      )}
      {!cargando && !error && pestana === 'facturas' && (
        <Tabla columnas={colFactura} filas={facturasFiltradas} vacio="No hay facturas registradas." />
      )}

      <Modal
        titulo={editandoCompra ? 'Editar compra' : 'Nueva compra'}
        abierto={compraAbierto}
        onCerrar={() => setCompraAbierto(false)}
        ancho={820}
      >
        <form className="form" onSubmit={guardarCompra}>
          <div className="form-grid">
            <div className="campo">
              <label>Fecha *</label>
              <input
                type="date"
                value={formCompra.fecha}
                onChange={(e) => setFormCompra({ ...formCompra, fecha: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label>Proveedor</label>
              <select
                value={formCompra.proveedorId}
                onChange={(e) => setFormCompra({ ...formCompra, proveedorId: e.target.value })}
              >
                <option value="">Sin proveedor</option>
                {proveedores.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nombre}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="campo">
            <label>Observación</label>
            <input
              type="text"
              value={formCompra.observacion}
              onChange={(e) => setFormCompra({ ...formCompra, observacion: e.target.value })}
            />
          </div>

          <div className="separador">
            <h4>Artículos</h4>
          </div>
          {formCompra.lineas.map((l, i) => (
            <div key={i} className="linea-articulo">
              <div className="campo">
                <label>Tipo</label>
                <select
                  value={l.tipo}
                  onChange={(e) => {
                    const nuevo = inicialLinea(e.target.value);
                    cambiarLineaCompra(i, 'tipo', e.target.value);
                    cambiarLineaCompra(i, 'productoId', nuevo.productoId);
                    cambiarLineaCompra(i, 'descripcion', nuevo.descripcion);
                  }}
                >
                  {TIPOS.map((t) => (
                    <option key={t.valor} value={t.valor}>
                      {t.etiqueta}
                    </option>
                  ))}
                </select>
              </div>
              {l.tipo === 'ROPA' ? (
                <div className="campo">
                  <label>Descripción *</label>
                  <input
                    type="text"
                    value={l.descripcion}
                    onChange={(e) => cambiarLineaCompra(i, 'descripcion', e.target.value)}
                    required
                  />
                </div>
              ) : (
                <SelectorProducto
                  tipo={l.tipo}
                  onUsar={(a) => {
                    cambiarLineaCompra(i, 'productoId', a.productoId);
                    cambiarLineaCompra(i, 'nombre', a.nombre);
                  }}
                />
              )}
              <div className="campo">
                <label>Cantidad *</label>
                <input
                  type="number"
                  min="1"
                  value={l.cantidad}
                  onChange={(e) => cambiarLineaCompra(i, 'cantidad', e.target.value)}
                  required
                />
              </div>
              <button
                type="button"
                className="btn btn-peligro btn-sm"
                onClick={() =>
                  setFormCompra((f) => ({ ...f, lineas: f.lineas.filter((_, j) => j !== i) }))
                }
              >
                Quitar
              </button>
            </div>
          ))}
          <button
            type="button"
            className="btn btn-borde"
            onClick={() =>
              setFormCompra((f) => ({ ...f, lineas: [...f.lineas, inicialLinea()] }))
            }
          >
            + Agregar artículo
          </button>

          <Microsofto errores={erroresCompra} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setCompraAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              {editandoCompra ? 'Guardar cambios' : 'Registrar compra'}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        titulo={editandoFactura ? 'Editar factura' : 'Nueva factura'}
        abierto={facturaAbierto}
        onCerrar={() => setFacturaAbierto(false)}
        ancho={880}
      >
        <form className="form" onSubmit={guardarFactura}>
          <div className="form-grid">
            <div className="campo">
              <label>N° factura (opcional)</label>
              <input
                type="text"
                value={formFactura.numero}
                onChange={(e) => setFormFactura({ ...formFactura, numero: e.target.value })}
              />
            </div>
            <div className="campo">
              <label>Fecha *</label>
              <input
                type="date"
                value={formFactura.fecha}
                onChange={(e) => setFormFactura({ ...formFactura, fecha: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label>Proveedor</label>
              <select
                value={formFactura.proveedorId}
                onChange={(e) => setFormFactura({ ...formFactura, proveedorId: e.target.value })}
              >
                <option value="">Informal (sin proveedor)</option>
                {proveedores.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nombre}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="campo">
            <label>Observación</label>
            <input
              type="text"
              value={formFactura.observacion}
              onChange={(e) => setFormFactura({ ...formFactura, observacion: e.target.value })}
            />
          </div>

          {!editandoFactura && (
            <div className="opciones-factura">
              <label className="check">
                <input
                  type="checkbox"
                  checked={formFactura.crearCompra}
                  onChange={(e) =>
                    setFormFactura({
                      ...formFactura,
                      crearCompra: e.target.checked,
                      compraId: e.target.checked ? '' : formFactura.compraId,
                    })
                  }
                />
                Crear la compra y aumentar stock con esta factura
              </label>
              {!formFactura.crearCompra && (
                <div className="campo">
                  <label>O vincular una compra existente</label>
                  <select
                    value={formFactura.compraId}
                    onChange={(e) => setFormFactura({ ...formFactura, compraId: e.target.value })}
                  >
                    <option value="">No vincular compra</option>
                    {comprasSinFacturar.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.fecha} · {c.proveedor ? c.proveedor.nombre : 'Sin proveedor'}
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>
          )}

          <div className="separador">
            <h4>Artículos</h4>
          </div>
          {formFactura.lineas.map((l, i) => (
            <div key={i} className="linea-articulo">
              <div className="campo">
                <label>Tipo</label>
                <select
                  value={l.tipo}
                  onChange={(e) => {
                    const nuevo = inicialLinea(e.target.value);
                    cambiarLineaFactura(i, 'tipo', e.target.value);
                    cambiarLineaFactura(i, 'productoId', nuevo.productoId);
                    cambiarLineaFactura(i, 'descripcion', nuevo.descripcion);
                  }}
                >
                  {TIPOS.map((t) => (
                    <option key={t.valor} value={t.valor}>
                      {t.etiqueta}
                    </option>
                  ))}
                </select>
              </div>
              {l.tipo === 'ROPA' ? (
                <div className="campo">
                  <label>Descripción *</label>
                  <input
                    type="text"
                    value={l.descripcion}
                    onChange={(e) => cambiarLineaFactura(i, 'descripcion', e.target.value)}
                    required
                  />
                </div>
              ) : (
                <SelectorProducto
                  tipo={l.tipo}
                  onUsar={(a) => {
                    cambiarLineaFactura(i, 'productoId', a.productoId);
                    cambiarLineaFactura(i, 'nombre', a.nombre);
                  }}
                />
              )}
              <div className="campo">
                <label>Cantidad *</label>
                <input
                  type="number"
                  min="1"
                  value={l.cantidad}
                  onChange={(e) => cambiarLineaFactura(i, 'cantidad', e.target.value)}
                  required
                />
              </div>
              <div className="campo">
                <label>Costo unitario *</label>
                <input
                  type="number"
                  min="0"
                  value={l.costoUnitario}
                  onChange={(e) => cambiarLineaFactura(i, 'costoUnitario', e.target.value)}
                  required
                />
              </div>
              <button
                type="button"
                className="btn btn-peligro btn-sm"
                onClick={() =>
                  setFormFactura((f) => ({ ...f, lineas: f.lineas.filter((_, j) => j !== i) }))
                }
              >
                Quitar
              </button>
            </div>
          ))}
          <button
            type="button"
            className="btn btn-borde"
            onClick={() =>
              setFormFactura((f) => ({ ...f, lineas: [...f.lineas, inicialLinea()] }))
            }
          >
            + Agregar artículo
          </button>

          <Microsofto errores={erroresFactura} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setFacturaAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              {editandoFactura ? 'Guardar cambios' : 'Registrar factura'}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        titulo={compraDev ? `Devolución de compra #${compraDev.id}` : 'Devolución'}
        abierto={devAbierto}
        onCerrar={() => setDevAbierto(false)}
        ancho={640}
      >
        <form className="form" onSubmit={guardarDevolucion}>
          <p className="texto-aviso">
            Baja stock de lo devuelto (parcial OK). No modifica el costo aunque la compra esté facturada.
          </p>
          <div className="form-grid">
            <div className="campo">
              <label>Fecha *</label>
              <input
                type="date"
                value={formDev.fecha}
                onChange={(e) => setFormDev({ ...formDev, fecha: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label>Observación</label>
              <input
                type="text"
                value={formDev.observacion}
                onChange={(e) => setFormDev({ ...formDev, observacion: e.target.value })}
              />
            </div>
          </div>
          {(formDev.lineas || []).map((l, i) => (
            <div key={i} className="linea-articulo form-grid">
              <div className="campo">
                <label>{l.descripcion || l.tipo}</label>
                <small className="sin-dato">Comprado: {l.cantidadMax}</small>
              </div>
              <div className="campo">
                <label>Devolver</label>
                <input
                  type="number"
                  min="0"
                  max={l.cantidadMax}
                  value={l.cantidad}
                  onChange={(e) => {
                    const lineas = [...formDev.lineas];
                    lineas[i] = { ...lineas[i], cantidad: e.target.value };
                    setFormDev({ ...formDev, lineas });
                  }}
                />
              </div>
            </div>
          ))}
          <Microsofto errores={erroresDev} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setDevAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              Registrar devolución
            </button>
          </div>
        </form>
      </Modal>
    </section>
  );
}