import { useState } from 'react';

export default function Instalacion() {
  const [rootPassword, setRootPassword] = useState('');
  const [confirmar, setConfirmar] = useState('');
  const [adminPassword, setAdminPassword] = useState('');
  const [dbArchivo, setDbArchivo] = useState(null);
  const [uploadsArchivo, setUploadsArchivo] = useState(null);
  const [sobrescribir, setSobrescribir] = useState('');
  const [error, setError] = useState(null);
  const [resultado, setResultado] = useState(null);
  const [cargando, setCargando] = useState(false);

  const hayImportacion = !!dbArchivo || !!uploadsArchivo;

  async function completar(e) {
    e.preventDefault();
    setError(null);
    if (rootPassword !== confirmar) {
      setError('Las contraseñas no coinciden');
      return;
    }
    if (hayImportacion && sobrescribir !== 'SOBRESCRIBIR') {
      setError('Para importar una base o uploads debe escribir SOBRESCRIBIR');
      return;
    }
    setCargando(true);
    try {
      const fd = new FormData();
      fd.append('rootPassword', rootPassword);
      if (adminPassword) fd.append('adminPassword', adminPassword);
      if (dbArchivo) fd.append('db', dbArchivo);
      if (uploadsArchivo) fd.append('uploads', uploadsArchivo);
      const res = await fetch('/api/instalacion/completar', { method: 'POST', body: fd });
      const d = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(d.mensaje || 'No se pudo completar la instalación');
      }
      setResultado(d);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  if (resultado) {
    return (
      <div className="login-pantalla">
        <div className="login-tarjeta">
          <h1 className="login-titulo">Instalación completada</h1>
          <p className="texto-aviso">
            Usuario raíz: <strong>{resultado.usuario?.username}</strong>
          </p>
          {resultado.adminPasswordTemporal && (
            <div className="campo">
              <label>Contraseña temporal del admin</label>
              <textarea readOnly value={resultado.adminPasswordTemporal} rows={1}
                style={{ width: '100%', fontFamily: 'monospace' }} />
              <p className="texto-aviso" role="alert">
                Guárdela ahora: se genera automáticamente y solo se muestra una vez.
              </p>
            </div>
          )}
          <div className="campo">
            <label>Secreto de recuperación</label>
            <textarea readOnly value={resultado.secretoRecuperacion || ''} rows={2}
              style={{ width: '100%', fontFamily: 'monospace' }} />
          </div>
          <p className="texto-aviso" role="alert">
            Guarde este secreto en un lugar seguro. Solo sirve para restablecer la contraseña del
            administrador desde la pantalla de inicio de sesión.
          </p>
          <button type="button" className="btn btn-primario" style={{ width: '100%' }}
            onClick={() => window.location.reload()}>
            Continuar al inicio de sesión
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="login-pantalla">
      <div className="login-tarjeta">
        <h1 className="login-titulo">Instalación inicial</h1>
        <p className="texto-aviso">
          Cree el usuario raíz y opcionalmente importe una base de datos o archivos existentes.
        </p>
        <form className="form" onSubmit={completar}>
          <div className="campo">
            <label htmlFor="inst-clave">Contraseña de root (mínimo 8 caracteres)</label>
            <input id="inst-clave" type="password" value={rootPassword}
              onChange={(e) => setRootPassword(e.target.value)} autoComplete="new-password" required />
          </div>
          <div className="campo">
            <label htmlFor="inst-confirmar">Confirmar contraseña</label>
            <input id="inst-confirmar" type="password" value={confirmar}
              onChange={(e) => setConfirmar(e.target.value)} autoComplete="new-password" required />
          </div>
          <div className="campo">
            <label htmlFor="inst-admin">Contraseña del admin (opcional; si se deja vacío se genera una automática)</label>
            <input id="inst-admin" type="password" value={adminPassword}
              onChange={(e) => setAdminPassword(e.target.value)} autoComplete="new-password" />
          </div>

          <h3 style={{ margin: '1rem 0 .5rem' }}>Importar datos (opcional)</h3>
          <div className="campo">
            <label htmlFor="inst-db">Base de datos SQLite existente (.db)</label>
            <input id="inst-db" type="file" accept=".db,application/octet-stream"
              onChange={(e) => setDbArchivo(e.target.files?.[0] || null)} />
          </div>
          <div className="campo">
            <label htmlFor="inst-uploads">Carpeta de uploads comprimida (.zip)</label>
            <input id="inst-uploads" type="file" accept=".zip,application/zip"
              onChange={(e) => setUploadsArchivo(e.target.files?.[0] || null)} />
          </div>
          {hayImportacion && (
            <div className="campo">
              <label htmlFor="inst-sobrescribir">
                Esto reemplazará la base de datos y/o los uploads actuales. Escriba{' '}
                <strong>SOBRESCRIBIR</strong>:
              </label>
              <input id="inst-sobrescribir" value={sobrescribir}
                onChange={(e) => setSobrescribir(e.target.value)} />
            </div>
          )}

          {error && <p className="texto-error" role="alert">{error}</p>}
          <button type="submit" className="btn btn-primario" style={{ width: '100%' }} disabled={cargando}>
            {cargando ? 'Instalando…' : 'Completar instalación'}
          </button>
        </form>
      </div>
    </div>
  );
}