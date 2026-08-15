import { useState } from 'react';
import { hoy } from '../api';

const REPORTES = [
  { id: 'inventario', etiqueta: 'Inventario completo', url: () => '/api/reportes/inventario.pdf' },
  { id: 'valor', etiqueta: 'Valor del inventario', url: () => '/api/reportes/valor-inventario.pdf' },
  { id: 'alertas', etiqueta: 'Alertas de reposición', url: () => '/api/reportes/alertas-reposicion.pdf' },
  { id: 'facturas', etiqueta: 'Facturas (rango)', url: (d, h) => {
    const q = new URLSearchParams(); if (d) q.set('desde', d); if (h) q.set('hasta', h);
    return `/api/reportes/facturas.pdf?${q}`;
  }},
];

export default function Reportes() {
  const [desde, setDesde] = useState(() => { const d = new Date(); d.setDate(1); return d.toISOString().slice(0, 10); });
  const [hasta, setHasta] = useState(hoy());

  return (
    <section>
      <div className="pagina-cabecera"><h2>Reportes PDF</h2></div>
      <div className="filtros" style={{ marginBottom: '1rem' }}>
        <label>Desde <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} /></label>
        <label>Hasta <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} /></label>
      </div>
      <div className="panel-valor">
        {REPORTES.map((r) => (
          <div className="valor-item" key={r.id}>
            <span>{r.etiqueta}</span>
            <a className="btn btn-primario" href={r.url(desde, hasta)} target="_blank" rel="noreferrer">Descargar PDF</a>
          </div>
        ))}
      </div>
    </section>
  );
}
