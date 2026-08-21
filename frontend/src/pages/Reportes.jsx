import { useState } from 'react';
import { descargar, hoy, primerDiaMes } from '../api';

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
  const [desde, setDesde] = useState(primerDiaMes);
  const [hasta, setHasta] = useState(hoy());

  async function bajar(r) {
    try {
      await descargar(r.url(desde, hasta));
    } catch (err) {
      window.alert(err.message);
    }
  }

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
            <button type="button" className="btn btn-primario" onClick={() => bajar(r)}>Descargar PDF</button>
          </div>
        ))}
      </div>
    </section>
  );
}