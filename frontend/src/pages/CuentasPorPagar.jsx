import { useCallback, useEffect, useState } from 'react';
import { get, post, del, hoy } from '../api';
import { useEventos } from '../eventos-contexto';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge } from '../components/ui';

const COP = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const fmt = (n) => COP.format(n || 0);

export default function CuentasPorPagar() {
  const { suscribir } = useEventos();
  const [facturas, setFacturas] = useState([]);
  const [filtro, setFiltro] = useState('PENDIENTE');
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [abierto, setAbierto] = useState(false);
  const [factura, setFactura] = useState(null);
  const [form, setForm] = useState({ fecha: hoy(), monto: '', observacion: '' });
  const [errores, setErrores] = useState(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      setFacturas(await get('/api/facturas'));
      setError(null);
    } catch (e) {
      setError(e.message);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => { cargar(); }, [cargar]);
  useEffect(() => suscribir(['facturas', 'pagos-factura'], () => cargar()), [suscribir, cargar]);

  const lista = facturas.filter((f) => {
    if (filtro === 'TODAS') return true;
    if (filtro === 'PENDIENTE') return (f.estadoPago || 'PENDIENTE') !== 'PAGADA';
    return f.estadoPago === filtro;
  });

  function abrirPago(f) {
    setFactura(f);
    setForm({ fecha: hoy(), monto: f.saldo ?? '', observacion: '' });
    setErrores(null);
    setAbierto(true);
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    try {
      await post(`/api/facturas/${factura.id}/pagos`, { ...form, monto: Number(form.monto) });
      setAbierto(false);
      await cargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminarPago(id) {
    if (!window.confirm('¿Eliminar este abono?')) return;
    try {
      await del(`/api/pagos-factura/${id}`);
      await cargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const columnas = [
    { clave: 'fecha', titulo: 'Fecha' },
    { clave: 'numero', titulo: 'N°', render: (f) => f.numero || '—' },
    { clave: 'proveedor', titulo: 'Proveedor', render: (f) => f.proveedor?.nombre || 'Informal' },
    { clave: 'total', titulo: 'Total', render: (f) => fmt(f.total) },
    { clave: 'totalPagado', titulo: 'Pagado', render: (f) => fmt(f.totalPagado) },
    { clave: 'saldo', titulo: 'Saldo', render: (f) => fmt(f.saldo) },
    {
      clave: 'estadoPago',
      titulo: 'Estado',
      render: (f) => {
        const e = f.estadoPago || 'PENDIENTE';
        const t = e === 'PAGADA' ? 'verde' : e === 'PARCIAL' ? 'amarillo' : 'rojo';
        return <Badge tipo={t}>{e}</Badge>;
      },
    },
    {
      clave: 'acciones',
      titulo: '',
      render: (f) => (
        <div className="acciones">
          {(f.saldo || 0) > 0 && (
            <button type="button" className="btn btn-primario" onClick={() => abrirPago(f)}>Abonar</button>
          )}
          {(f.pagos || []).length > 0 && (
            <button type="button" className="btn btn-borde" onClick={() => {
              const ultimo = f.pagos[f.pagos.length - 1];
              eliminarPago(ultimo.id);
            }}>Quitar último abono</button>
          )}
        </div>
      ),
    },
  ];

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Cuentas por pagar</h2>
        <select value={filtro} onChange={(e) => setFiltro(e.target.value)}>
          <option value="PENDIENTE">Pendientes / parciales</option>
          <option value="PARCIAL">Parciales</option>
          <option value="PAGADA">Pagadas</option>
          <option value="TODAS">Todas</option>
        </select>
      </div>
      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && <Tabla columnas={columnas} filas={lista} vacio="No hay facturas." />}
      <Modal titulo={factura ? `Abono factura #${factura.id}` : 'Abono'} abierto={abierto} onCerrar={() => setAbierto(false)}>
        <form className="form" onSubmit={guardar}>
          <p className="texto-aviso">Saldo pendiente: {fmt(factura?.saldo)}</p>
          <div className="form-grid">
            <div className="campo"><label>Fecha *</label>
              <input type="date" value={form.fecha} onChange={(e) => setForm({ ...form, fecha: e.target.value })} required />
            </div>
            <div className="campo"><label>Monto *</label>
              <input type="number" min="1" step="1" value={form.monto} onChange={(e) => setForm({ ...form, monto: e.target.value })} required />
            </div>
          </div>
          <div className="campo"><label>Observación</label>
            <input type="text" value={form.observacion} onChange={(e) => setForm({ ...form, observacion: e.target.value })} />
          </div>
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbierto(false)}>Cancelar</button>
            <button type="submit" className="btn btn-primario">Registrar abono</button>
          </div>
        </form>
      </Modal>
    </section>
  );
}
