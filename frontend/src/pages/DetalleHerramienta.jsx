import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { get } from '../api';
import { Badge } from '../components/ui';

export default function DetalleHerramienta() {
  const { id } = useParams();
  const [herr, setHerr] = useState(null);
  const [movimientos, setMovimientos] = useState([]);
  const [asignaciones, setAsignaciones] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        setHerr(await get(`/api/herramientas/${id}`));
        setMovimientos(await get(`/api/herramientas/${id}/movimientos`));
        const asignaciones = await get('/api/asignaciones-herramientas');
        setAsignaciones(asignaciones.filter((a) => a.herramienta?.id === Number(id)));
        setError(null);
      } catch (err) {
        setError(err.message);
      }
    })();
  }, [id]);

  if (error) return <p className="texto-error">{error}</p>;
  if (!herr) return <p className="vacio">Cargando…</p>;

  const badges = [
    { label: 'Total', valor: herr.cantidadTotal ?? 0, tipo: null },
    { label: 'Disponible', valor: herr.cantidadDisponible ?? 0, tipo: 'verde' },
    { label: 'Asignada', valor: herr.cantidadAsignada ?? 0, tipo: 'azul' },
    { label: 'Dañada', valor: herr.cantidadDanada ?? 0, tipo: 'amarillo' },
    { label: 'Perdida', valor: herr.cantidadPerdida ?? 0, tipo: 'rojo' },
  ];

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>{herr.nombre}</h2>
        <Link to="/inventario/herramientas" className="btn btn-borde">← Volver a herramientas</Link>
      </div>

      <div className="panel-valor" style={{ marginBottom: '1rem' }}>
        {badges.map((b) => (
          <div className="valor-item" key={b.label}>
            <span>{b.label}</span>
            <strong>{b.tipo ? <Badge tipo={b.tipo}>{b.valor}</Badge> : b.valor}</strong>
          </div>
        ))}
      </div>
      <p className="texto-aviso">
        {herr.marca ? `Marca: ${herr.marca} · ` : ''}
        {herr.codigo ? `Código: ${herr.codigo} · ` : ''}
        {herr.descripcion || 'Sin descripción'}
      </p>

      <h3>Movimientos ({movimientos.length})</h3>
      {movimientos.length === 0 ? (
        <p className="sin-dato">Sin movimientos registrados.</p>
      ) : (
        <table className="tabla">
          <thead>
            <tr><th>Fecha</th><th>Tipo</th><th>Cantidad</th><th>Observación</th></tr>
          </thead>
          <tbody>
            {movimientos.map((m) => (
              <tr key={m.id}>
                <td>{m.fecha}</td>
                <td>{m.tipo}</td>
                <td>{m.cantidad}</td>
                <td>{m.observacion || '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <h3>Asignaciones ({asignaciones.length})</h3>
      {asignaciones.length === 0 ? (
        <p className="sin-dato">Sin asignaciones.</p>
      ) : (
        <table className="tabla">
          <thead>
            <tr><th>Empleado</th><th>Lugar</th><th>Fecha</th><th>Estado</th></tr>
          </thead>
          <tbody>
            {asignaciones.map((a) => (
              <tr key={a.id}>
                <td>{a.empleado?.nombre || '—'}</td>
                <td>{a.lugar || '—'}</td>
                <td>{a.fecha}</td>
                <td>
                  {a.devuelta
                    ? <Badge tipo="verde">Devuelta {a.fechaDevolucion}</Badge>
                    : <Badge tipo="rojo">Asignada</Badge>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}