import { useCallback, useEffect, useState } from 'react';
import { get } from '../api';
import { useEventos } from '../eventos-contexto';
import { Tabla, Badge } from '../components/ui';

export default function Alertas() {
  const { suscribir } = useEventos();
  const [reposicion, setReposicion] = useState([]);
  const [vencimientos, setVencimientos] = useState([]);
  const [dias, setDias] = useState(30);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      const [r, v] = await Promise.all([
        get('/api/alertas/reposicion'),
        get(`/api/alertas/epp-vencimiento?dias=${dias}`),
      ]);
      setReposicion(r);
      setVencimientos(v);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }, [dias]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  useEffect(() => {
    return suscribir(
      [
        'materiales',
        'consumibles',
        'epp',
        'herramientas',
        'movimientos-materiales',
        'movimientos-consumibles',
        'movimientos-epp',
        'movimientos-herramientas',
        'entregas-epp',
        'ajustes',
      ],
      () => cargar()
    );
  }, [suscribir, cargar]);

  const colRepo = [
    { clave: 'tipo', titulo: 'Tipo', render: (a) => <Badge tipo="amarillo">{a.tipo}</Badge> },
    { clave: 'nombre', titulo: 'Producto' },
    { clave: 'marca', titulo: 'Marca', render: (a) => a.marca || <span className="sin-dato">&mdash;</span> },
    { clave: 'stock', titulo: 'Stock', render: (a) => <Badge tipo="rojo">{a.stock}</Badge> },
    { clave: 'stockMinimo', titulo: 'Mínimo' },
  ];

  const colVenc = [
    { clave: 'tipo', titulo: 'Tipo', render: (a) => <Badge tipo="amarillo">{a.tipo}</Badge> },
    { clave: 'nombre', titulo: 'EPP' },
    {
      clave: 'empleadoNombre',
      titulo: 'Empleado',
      render: (a) => a.empleadoNombre || <span className="sin-dato">&mdash;</span>,
    },
    { clave: 'fechaVencimiento', titulo: 'Vence' },
    {
      clave: 'diasRestantes',
      titulo: 'Días',
      render: (a) => <Badge tipo={a.diasRestantes <= 7 ? 'rojo' : 'amarillo'}>{a.diasRestantes}</Badge>,
    },
  ];

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Alertas</h2>
        <button type="button" className="btn btn-borde" onClick={cargar}>
          Actualizar
        </button>
      </div>
      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}

      <h3>Reposición de stock</h3>
      <p className="texto-aviso">Productos con stock ≤ mínimo configurado.</p>
      {!cargando && !error && (
        <Tabla columnas={colRepo} filas={reposicion} vacio="No hay productos bajo el stock mínimo." />
      )}

      <div className="pagina-cabecera" style={{ marginTop: '1.5rem' }}>
        <h3>Vencimientos de EPP</h3>
        <label className="campo" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          Ventana (días)
          <input
            type="number"
            min="1"
            value={dias}
            onChange={(e) => setDias(Number(e.target.value) || 30)}
            style={{ width: '5rem' }}
          />
        </label>
      </div>
      {!cargando && !error && (
        <Tabla
          columnas={colVenc}
          filas={vencimientos}
          vacio="No hay EPP ni entregas por vencer en la ventana."
        />
      )}
    </section>
  );
}
