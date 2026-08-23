import { useState } from 'react';
import { get } from '../api';

const SECCION_POR_RECURSO = {
  proyecto: 'proyectos',
  proveedor: 'proveedores',
  material: 'materiales',
  consumible: 'consumibles',
  epp: 'inv-epp',
  herramienta: 'inv-herramientas',
  contrato: 'contratos',
  minuta: 'minutas',
};

const NOMBRE_RECURSO = {
  proyecto: 'Proyecto',
  proveedor: 'Proveedor',
  material: 'Material',
  consumible: 'Consumible',
  epp: 'EPP',
  herramienta: 'Herramienta',
  contrato: 'Contrato',
  minuta: 'Minuta',
};

export default function Busqueda({ onNavegar }) {
  const [q, setQ] = useState('');
  const [resultados, setResultados] = useState([]);
  const [error, setError] = useState(null);
  const [buscado, setBuscado] = useState(false);

  async function buscar(e) {
    e.preventDefault();
    setError(null);
    setBuscado(true);
    try {
      const r = await get(`/api/buscar?q=${encodeURIComponent(q)}`);
      setResultados(r);
    } catch (err) {
      setError(err.message);
      setResultados([]);
    }
  }

  function abrir(recurso) {
    const clave = SECCION_POR_RECURSO[recurso];
    if (clave && onNavegar) {
      onNavegar(clave);
    }
  }

  return (
    <section>
      <div className="pagina-cabecera"><h2>Búsqueda global</h2></div>
      <form className="form" onSubmit={buscar} style={{ maxWidth: '520px' }}>
        <div className="campo">
          <label htmlFor="buscar-q">Buscar en el inventario</label>
          <input
            id="buscar-q"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Nombre, código, proveedor, empleado…"
            autoComplete="off"
          />
        </div>
        {error && <p className="texto-error" role="alert">{error}</p>}
        <button type="submit" className="btn btn-primario">Buscar</button>
      </form>

      {buscado && !error && resultados.length === 0 && (
        <p className="texto-aviso">Sin resultados para &quot;{q}&quot;.</p>
      )}

      {resultados.length > 0 && (
        <table className="tabla" style={{ marginTop: '1rem' }}>
          <thead>
            <tr>
              <th>Recurso</th>
              <th>Resultado</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {resultados.map((r, i) => (
              <tr key={`${r.recurso}-${r.id}-${i}`}>
                <td>{NOMBRE_RECURSO[r.recurso] || r.recurso}</td>
                <td>{r.etiqueta}</td>
                <td>
                  <button type="button" className="btn btn-borde" onClick={() => abrir(r.recurso)}>
                    Abrir sección
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}