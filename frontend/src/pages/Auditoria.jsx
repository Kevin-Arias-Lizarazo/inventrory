import { useCallback, useEffect, useState } from 'react';
import { get } from '../api';
import { Paginacion, Tabla } from '../components/ui';

export default function Auditoria() {
  const [eventos, setEventos] = useState([]);
  const [total, setTotal] = useState(0);
  const [pagina, setPagina] = useState(0);
  const [tamano, setTamano] = useState(30);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [fecha, setFecha] = useState('');
  const [usuario, setUsuario] = useState('');
  const [accion, setAccion] = useState('');
  const [fechas, setFechas] = useState([]);

  const cargar = useCallback(async () => {
    const q = new URLSearchParams();
    if (fecha) q.set('fecha', fecha);
    if (usuario) q.set('usuario', usuario);
    if (accion) q.set('accion', accion);
    q.set('pagina', String(pagina));
    q.set('tamano', String(tamano));
    try {
      const r = await get(`/api/auditoria?${q.toString()}`);
      setEventos(r.contenido);
      setTotal(r.total);
      setTotalPaginas(r.totalPaginas);
    } catch {
      /* ignorar */
    }
  }, [fecha, usuario, accion, pagina, tamano]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  useEffect(() => {
    get('/api/auditoria/fechas').then(setFechas).catch(() => {});
  }, []);

  return (
    <section>
      <div className="pagina-cabecera"><h2>Auditoría</h2></div>
      <div className="fila-form" style={{ marginBottom: '1rem' }}>
        <div className="campo">
          <label htmlFor="au-fecha">Fecha</label>
          <select id="au-fecha" value={fecha} onChange={(e) => { setFecha(e.target.value); setPagina(0); }}>
            <option value="">Todas</option>
            {fechas.map((f) => (
              <option key={f} value={f}>{f}</option>
            ))}
          </select>
        </div>
        <div className="campo">
          <label htmlFor="au-usuario">Usuario</label>
          <input id="au-usuario" value={usuario} onChange={(e) => { setUsuario(e.target.value); setPagina(0); }} />
        </div>
        <div className="campo">
          <label htmlFor="au-accion">Acción</label>
          <input id="au-accion" value={accion} onChange={(e) => { setAccion(e.target.value); setPagina(0); }} />
        </div>
      </div>
      <Tabla
        vacio="No hay eventos de auditoría"
        columnas={[
          { titulo: 'Fecha', render: (e) => new Date(e.fecha).toLocaleString() },
          { titulo: 'Usuario', clave: 'usuario' },
          { titulo: 'Rol', clave: 'rol' },
          { titulo: 'Método', clave: 'metodo' },
          { titulo: 'Ruta', clave: 'ruta' },
          { titulo: 'Acción', clave: 'accion' },
          { titulo: 'Resultado', render: (e) => (e.resultado === 'OK' ? 'OK' : e.resultado) },
          { titulo: 'ms', clave: 'duracionMs' },
          { titulo: 'Detalle', clave: 'detalle' },
        ]}
        filas={eventos}
      />
      <Paginacion
        pagina={pagina}
        total={total}
        totalPaginas={totalPaginas}
        tamano={tamano}
        onPagina={setPagina}
        onTamano={(t) => { setTamano(t); setPagina(0); }}
      />
    </section>
  );
}