import { useState } from 'react';
import { descargar, post } from '../api';
import { Microsofto } from '../components/ui';

export default function Mantenimiento() {
  const [recurso, setRecurso] = useState('proveedores');
  const [csv, setCsv] = useState('nombre,telefono,correo,direccion\n');
  const [resultado, setResultado] = useState(null);
  const [errores, setErrores] = useState(null);
  const [msg, setMsg] = useState(null);

  async function importar(e) {
    e.preventDefault();
    setErrores(null); setResultado(null);
    try {
      const r = await post(`/api/importar/${recurso}`, csv);
      setResultado(r);
    } catch (err) { setErrores([err.message]); }
  }

  async function restaurar(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    e.target.value = '';
    if (!window.confirm('¿Restaurar la base de datos con este archivo? Reinicia el backend después.')) return;
    setMsg(null);
    try {
      const fd = new FormData();
      fd.append('archivo', file);
      const r = await post('/api/backup/restaurar', fd);
      setMsg(r?.mensaje || 'Backup restaurado. Reinicia el contenedor del backend.');
    } catch (err) { setMsg(err.message); }
  }

  async function restaurarUploads(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    e.target.value = '';
    if (!window.confirm('¿Restaurar las imágenes/archivos subidos con este ZIP?')) return;
    setMsg(null);
    try {
      const fd = new FormData();
      fd.append('archivo', file);
      const r = await post('/api/backup/restaurar-uploads', fd);
      setMsg(r?.mensaje || 'Uploads restaurados.');
    } catch (err) { setMsg(err.message); }
  }

  return (
    <section>
      <div className="pagina-cabecera"><h2>Mantenimiento</h2></div>

      <h3>Backup</h3>
      <div className="acciones" style={{ marginBottom: '1.5rem' }}>
        <button type="button" className="btn btn-primario" onClick={() => descargar('/api/backup')}>Descargar backup (.db)</button>
        <button
          type="button"
          className="btn btn-primario"
          onClick={() => descargar('/api/backup/exportar-uploads', { metodo: 'POST' })}
        >
          Descargar uploads (.zip)
        </button>
        <label className="btn btn-borde">
          Restaurar backup
          <input type="file" accept=".db,application/octet-stream" style={{ display: 'none' }} onChange={restaurar} />
        </label>
        <label className="btn btn-borde">
          Restaurar uploads (.zip)
          <input type="file" accept=".zip,application/zip" style={{ display: 'none' }} onChange={restaurarUploads} />
        </label>
      </div>
      {msg && <p className="texto-aviso">{msg}</p>}

      <h3>Importar CSV</h3>
      <p className="texto-aviso">Recursos: proveedores, materiales, consumibles, epp. Primera fila = encabezados.</p>
      <form className="form" onSubmit={importar}>
        <div className="campo">
          <label>Recurso</label>
          <select value={recurso} onChange={(e) => setRecurso(e.target.value)}>
            <option value="proveedores">Proveedores (nombre,telefono,correo,direccion)</option>
            <option value="materiales">Materiales (nombre,marca,unidad,descripcion)</option>
            <option value="consumibles">Consumibles (nombre,marca,unidad,descripcion)</option>
            <option value="epp">EPP (nombre,marca,descripcion)</option>
          </select>
        </div>
        <div className="campo">
          <label>CSV</label>
          <textarea rows={8} value={csv} onChange={(e) => setCsv(e.target.value)} style={{ width: '100%', fontFamily: 'monospace' }} />
        </div>
        <Microsofto errores={errores} />
        <button type="submit" className="btn btn-primario">Importar</button>
      </form>
      {resultado && (
        <p className="texto-aviso">
          Creados: {resultado.creados}. Errores: {(resultado.errores || []).length}
          {(resultado.errores || []).length > 0 && (
            <ul>{resultado.errores.map((e, i) => <li key={i}>{e}</li>)}</ul>
          )}
        </p>
      )}
    </section>
  );
}
