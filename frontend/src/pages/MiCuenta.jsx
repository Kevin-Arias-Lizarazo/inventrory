import { useState } from 'react';
import { useAuth } from '../auth/auth-contexto';
import { Microsofto } from '../components/ui';

export default function MiCuenta() {
  const { usuario, cambiarContrasena } = useAuth();
  const [actual, setActual] = useState('');
  const [nueva, setNueva] = useState('');
  const [confirmar, setConfirmar] = useState('');
  const [error, setError] = useState(null);
  const [msg, setMsg] = useState(null);

  async function guardar(e) {
    e.preventDefault();
    setError(null);
    setMsg(null);
    if (nueva !== confirmar) {
      setError('Las contraseñas no coinciden');
      return;
    }
    try {
      await cambiarContrasena(usuario.username, actual, nueva);
      setActual('');
      setNueva('');
      setConfirmar('');
      setMsg('Contraseña actualizada');
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <section>
      <div className="pagina-cabecera"><h2>Mi cuenta</h2></div>
      <p className="texto-aviso">
        Usuario: <strong>{usuario?.username}</strong> · Rol: <strong>{usuario?.rol}</strong>
      </p>
      <form className="form" onSubmit={guardar} style={{ maxWidth: '420px' }}>
        <div className="campo">
          <label htmlFor="mc-actual">Contraseña actual</label>
          <input id="mc-actual" type="password" value={actual} onChange={(e) => setActual(e.target.value)}
            autoComplete="current-password" required />
        </div>
        <div className="campo">
          <label htmlFor="mc-nueva">Nueva contraseña</label>
          <input id="mc-nueva" type="password" value={nueva} onChange={(e) => setNueva(e.target.value)}
            autoComplete="new-password" required />
        </div>
        <div className="campo">
          <label htmlFor="mc-confirmar">Confirmar nueva contraseña</label>
          <input id="mc-confirmar" type="password" value={confirmar} onChange={(e) => setConfirmar(e.target.value)}
            autoComplete="new-password" required />
        </div>
        <Microsofto errores={error ? [error] : null} />
        {msg && <p className="texto-aviso" role="alert">{msg}</p>}
        <button type="submit" className="btn btn-primario">Cambiar contraseña</button>
      </form>
    </section>
  );
}