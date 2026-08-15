import { useCallback, useEffect, useState } from 'react';
import { get, hoy } from '../api';
import { useEventos } from '../eventos-contexto';
import { Badge } from '../components/ui';

const COP = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const fmt = (n) => COP.format(n || 0);

export default function Dashboard() {
  const { suscribir } = useEventos();
  const [data, setData] = useState(null);
  const [desde, setDesde] = useState(() => {
    const d = new Date(); d.setDate(1); return d.toISOString().slice(0, 10);
  });
  const [hasta, setHasta] = useState(hoy());
  const [error, setError] = useState(null);

  const cargar = useCallback(async () => {
    try {
      const q = new URLSearchParams();
      if (desde) q.set('desde', desde);
      if (hasta) q.set('hasta', hasta);
      setData(await get(`/api/dashboard?${q}`));
      setError(null);
    } catch (e) { setError(e.message); }
  }, [desde, hasta]);

  useEffect(() => { cargar(); }, [cargar]);
  useEffect(() => suscribir(
    ['materiales', 'consumibles', 'epp', 'herramientas', 'facturas', 'compras', 'ajustes'],
    () => cargar()
  ), [suscribir, cargar]);

  if (error) return <p className="texto-error">{error}</p>;
  if (!data) return <p className="vacio">Cargando…</p>;

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Dashboard</h2>
        <div className="filtros">
          <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} />
          <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} />
          <button type="button" className="btn btn-borde" onClick={cargar}>Actualizar</button>
        </div>
      </div>
      <div className="panel-valor">
        <div className="valor-item"><span>Valor inventario</span><strong>{fmt(data.valorInventario)}</strong></div>
        <div className="valor-item"><span>Productos</span><strong>{data.totalProductos}</strong></div>
        <div className="valor-item"><span>Sin costo</span><strong>{data.productosSinCosto}</strong></div>
        <div className="valor-item"><span>Alertas stock</span><strong><Badge tipo="rojo">{data.alertasReposicion}</Badge></strong></div>
        <div className="valor-item"><span>EPP por vencer</span><strong><Badge tipo="amarillo">{data.alertasVencimientoEpp}</Badge></strong></div>
        <div className="valor-item"><span>Gasto facturas (rango)</span><strong>{fmt(data.gastoFacturasRango)}</strong></div>
        <div className="valor-item"><span>Compras (rango)</span><strong>{data.comprasRango}</strong></div>
      </div>
      <h3>Valor por categoría</h3>
      <div className="panel-valor">
        {(data.valorPorCategoria || []).map((c) => (
          <div className="valor-item" key={c.tipo}>
            <span>{c.tipo} ({c.cantidad})</span>
            <strong>{fmt(c.valor)}</strong>
          </div>
        ))}
      </div>
    </section>
  );
}
