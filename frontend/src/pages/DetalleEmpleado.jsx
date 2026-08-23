import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { get } from '../api';
import { Badge, MiniImagen } from '../components/ui';

export default function DetalleEmpleado() {
  const { id } = useParams();
  const [emp, setEmp] = useState(null);
  const [equipo, setEquipo] = useState(null);
  const [minutas, setMinutas] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const e = await get(`/api/empleados/${id}`);
        setEmp(e);
        setEquipo(await get(`/api/empleados/${id}/equipamiento`));
        const mins = await get('/api/minutas');
        setMinutas(mins.filter((m) => m.empleado?.id === Number(id)));
        setError(null);
      } catch (err) {
        setError(err.message);
      }
    })();
  }, [id]);

  if (error) return <p className="texto-error">{error}</p>;
  if (!emp) return <p className="vacio">Cargando…</p>;

  const herramientas = equipo?.asignacionesHerramientas || [];
  const pendientes = herramientas.filter((h) => !h.devuelta);
  const devueltas = herramientas.filter((h) => h.devuelta);

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>{emp.nombre}</h2>
        <Link to="/empleados" className="btn btn-borde">← Volver a empleados</Link>
      </div>

      <div className="grid-detalle">
        <div className="panel-detalle">
          <h3>Datos</h3>
          <dl className="dl-detalle">
            <dt>Código</dt><dd>{emp.codigo}</dd>
            <dt>Documento</dt><dd>{emp.documento || '—'}</dd>
            <dt>Cargo</dt><dd>{emp.cargo || '—'}</dd>
            <dt>Teléfono</dt><dd>{emp.telefono || '—'}</dd>
            <dt>Correo</dt><dd>{emp.correo || '—'}</dd>
            <dt>Ingreso</dt><dd>{emp.fechaIngreso || '—'}</dd>
            <dt>Contrato</dt>
            <dd>{emp.contratado ? <Badge tipo="verde">Contratado</Badge> : <Badge tipo="gris">Sin contrato</Badge>}</dd>
            <dt>Foto</dt><dd>{emp.fotoUrl ? <MiniImagen url={emp.fotoUrl} alto={60} /> : '—'}</dd>
          </dl>
          {emp.hojaVida && (
            <>
              <h3>Hoja de vida</h3>
              <p className="texto-aviso" style={{ whiteSpace: 'pre-wrap' }}>{emp.hojaVida}</p>
            </>
          )}
        </div>

        <div className="panel-detalle">
          <h3>Herramientas asignadas</h3>
          <h4 style={{ color: '#b91c1c' }}>Pendientes de devolución ({pendientes.length})</h4>
          {pendientes.length === 0 ? (
            <p className="sin-dato">Sin herramientas pendientes.</p>
          ) : (
            <ul className="lista-detalle">
              {pendientes.map((a) => (
                <li key={a.id}>
                  {a.herramienta?.nombre || 'Herramienta'} · {a.lugar || 'sin lugar'} · {a.fecha}
                </li>
              ))}
            </ul>
          )}
          <h4 style={{ color: '#15803d' }}>Devueltas ({devueltas.length})</h4>
          {devueltas.length === 0 ? (
            <p className="sin-dato">Sin devoluciones.</p>
          ) : (
            <ul className="lista-detalle">
              {devueltas.map((a) => (
                <li key={a.id}>
                  {a.herramienta?.nombre || 'Herramienta'} · devuelta {a.fechaDevolucion}
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="panel-detalle">
          <h3>Ropa y EPP</h3>
          <h4>Ropa entregada</h4>
          {(equipo?.entregasRopa || []).length === 0 ? (
            <p className="sin-dato">Sin entregas de ropa.</p>
          ) : (
            <ul className="lista-detalle">
              {equipo.entregasRopa.map((x) => (
                <li key={`r-${x.id}`}>{x.observacion || 'Entrega'} · {x.fecha}</li>
              ))}
            </ul>
          )}
          <h4>EPP entregado</h4>
          {(equipo?.entregasEpp || []).length === 0 ? (
            <p className="sin-dato">Sin entregas de EPP.</p>
          ) : (
            <ul className="lista-detalle">
              {equipo.entregasEpp.map((x) => (
                <li key={`e-${x.id}`}>{x.epp?.nombre || 'EPP'} · vence {x.fechaVencimiento || '—'}</li>
              ))}
            </ul>
          )}
        </div>

        <div className="panel-detalle">
          <h3>Minutas ({minutas.length})</h3>
          {minutas.length === 0 ? (
            <p className="sin-dato">Sin minutas registradas.</p>
          ) : (
            <ul className="lista-detalle">
              {minutas.map((m) => (
                <li key={m.id}>{m.fecha} · {m.hora} · {m.proyecto?.nombre || 'Sin proyecto'}</li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </section>
  );
}