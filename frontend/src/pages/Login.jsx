import { useState } from 'react';
import { useAuth } from '../auth/auth-contexto';

export default function Login() {
  const { login, recuperarAdmin } = useAuth();
  const [usuario, setUsuario] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [cargando, setCargando] = useState(false);

  const [modoRecuperar, setModoRecuperar] = useState(false);
  const [secretoRoot, setSecretoRoot] = useState('');
  const [nuevaClave, setNuevaClave] = useState('');
  const [confirmar, setConfirmar] = useState('');
  const [msg, setMsg] = useState(null);

  async function entrar(e) {
    e.preventDefault();
    setError(null);
    setCargando(true);
    try {
      await login(usuario, password);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  async function recuperar(e) {
    e.preventDefault();
    setError(null);
    setMsg(null);
    if (nuevaClave !== confirmar) {
      setError('Las contraseñas no coinciden');
      return;
    }
    try {
      await recuperarAdmin(secretoRoot, nuevaClave);
      setMsg('Contraseña del administrador restablecida. Ya puede iniciar sesión.');
      setModoRecuperar(false);
      setSecretoRoot('');
      setNuevaClave('');
      setConfirmar('');
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="login-pantalla">
      <div className="login-tarjeta">
        <div className="login-logo">INV</div>
        <h1 className="login-titulo">Inventario</h1>

        {!modoRecuperar ? (
          <form onSubmit={entrar} className="form">
            <div className="campo">
              <label htmlFor="login-usuario">Usuario</label>
              <input
                id="login-usuario"
                value={usuario}
                onChange={(e) => setUsuario(e.target.value)}
                autoComplete="username"
                required
              />
            </div>
            <div className="campo">
              <label htmlFor="login-clave">Contraseña</label>
              <input
                id="login-clave"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                required
              />
            </div>
            {error && <p className="texto-error" role="alert">{error}</p>}
            <button type="submit" className="btn btn-primario" style={{ width: '100%' }} disabled={cargando}>
              {cargando ? 'Ingresando…' : 'Iniciar sesión'}
            </button>
          </form>
        ) : (
          <form onSubmit={recuperar} className="form">
            <p className="texto-aviso">
              Se solicita la <strong>credencial raíz</strong> (secreto de recuperación) para restablecer la
              contraseña del administrador.
            </p>
            <div className="campo">
              <label htmlFor="rec-secreto">Credencial raíz</label>
              <input
                id="rec-secreto"
                type="password"
                value={secretoRoot}
                onChange={(e) => setSecretoRoot(e.target.value)}
                autoComplete="off"
                required
              />
            </div>
            <div className="campo">
              <label htmlFor="rec-nueva">Nueva contraseña del admin</label>
              <input
                id="rec-nueva"
                type="password"
                value={nuevaClave}
                onChange={(e) => setNuevaClave(e.target.value)}
                autoComplete="new-password"
                required
              />
            </div>
            <div className="campo">
              <label htmlFor="rec-confirmar">Confirmar contraseña</label>
              <input
                id="rec-confirmar"
                type="password"
                value={confirmar}
                onChange={(e) => setConfirmar(e.target.value)}
                autoComplete="new-password"
                required
              />
            </div>
            {error && <p className="texto-error" role="alert">{error}</p>}
            {msg && <p className="texto-aviso" role="alert">{msg}</p>}
            <button type="submit" className="btn btn-primario" style={{ width: '100%' }}>
              Restablecer
            </button>
            <button type="button" className="btn btn-borde" style={{ width: '100%' }} onClick={() => {
              setModoRecuperar(false);
              setError(null);
            }}>
              Volver al inicio de sesión
            </button>
          </form>
        )}

        {!modoRecuperar && (
          <button type="button" className="btn-enlace" onClick={() => {
            setModoRecuperar(true);
            setError(null);
          }}>
            Recuperar contraseña
          </button>
        )}
      </div>
    </div>
  );
}