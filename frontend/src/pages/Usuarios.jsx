import { useCallback, useEffect, useState } from 'react';
import { get, patch, post } from '../api';
import { Badge, Microsofto, Tabla } from '../components/ui';

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [error, setError] = useState(null);
  const [msg, setMsg] = useState(null);

  const [username, setUsername] = useState('');
  const [nombre, setNombre] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [rol, setRol] = useState('USUARIO');

  const cargar = useCallback(async () => {
    try {
      setUsuarios(await get('/api/usuarios'));
    } catch (err) {
      setError(err.message);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  async function crear(e) {
    e.preventDefault();
    setError(null);
    setMsg(null);
    try {
      await post('/api/usuarios', { username, nombre, contrasena, rol });
      setUsername('');
      setNombre('');
      setContrasena('');
      setRol('USUARIO');
      setMsg(`Usuario ${username} creado`);
      cargar();
    } catch (err) {
      setError(err.message);
    }
  }

  async function cambiarRol(u, nuevoRol) {
    try {
      await patch(`/api/usuarios/${u.id}/rol`, { rol: nuevoRol });
      setMsg(`Rol de ${u.username} actualizado`);
      cargar();
    } catch (err) {
      setError(err.message);
    }
  }

  async function bloquear(u) {
    try {
      await post(`/api/usuarios/${u.id}/bloquear`, {});
      setMsg(`Usuario ${u.username} bloqueado`);
      cargar();
    } catch (err) {
      setError(err.message);
    }
  }

  async function desbloquear(u) {
    try {
      await post(`/api/usuarios/${u.id}/desbloquear`, {});
      setMsg(`Usuario ${u.username} desbloqueado`);
      cargar();
    } catch (err) {
      setError(err.message);
    }
  }

  async function reestablecer(u) {
    const clave = window.prompt(`Nueva contraseña para ${u.username} (mínimo 8 caracteres):`);
    if (!clave) return;
    try {
      await post(`/api/usuarios/${u.id}/reestablecer-contrasena`, { contrasena: clave });
      setMsg(`Contraseña de ${u.username} restablecida`);
      cargar();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <section>
      <div className="pagina-cabecera"><h2>Usuarios</h2></div>

      <h3>Crear usuario</h3>
      <form className="form" onSubmit={crear}>
        <div className="fila-form">
          <div className="campo">
            <label htmlFor="u-usuario">Usuario</label>
            <input id="u-usuario" value={username} onChange={(e) => setUsername(e.target.value)} required />
          </div>
          <div className="campo">
            <label htmlFor="u-nombre">Nombre</label>
            <input id="u-nombre" value={nombre} onChange={(e) => setNombre(e.target.value)} />
          </div>
          <div className="campo">
            <label htmlFor="u-contrasena">Contraseña</label>
            <input id="u-contrasena" type="password" value={contrasena} onChange={(e) => setContrasena(e.target.value)} required />
          </div>
          <div className="campo">
            <label htmlFor="u-rol">Rol</label>
            <select id="u-rol" value={rol} onChange={(e) => setRol(e.target.value)}>
              <option value="USUARIO">Usuario</option>
              <option value="LECTOR">Lector</option>
            </select>
          </div>
        </div>
        <Microsofto errores={error ? [error] : null} />
        {msg && <p className="texto-aviso" role="alert">{msg}</p>}
        <button type="submit" className="btn btn-primario">Crear usuario</button>
      </form>

      <h3>Cuentas</h3>
      <Tabla
        vacio="No hay usuarios"
        columnas={[
          { titulo: 'Usuario', render: (u) => u.username },
          { titulo: 'Nombre', clave: 'nombre' },
          {
            titulo: 'Rol',
            render: (u) => (
              <select value={u.rol} onChange={(e) => cambiarRol(u, e.target.value)}>
                <option value="USUARIO">Usuario</option>
                <option value="LECTOR">Lector</option>
              </select>
            ),
          },
          {
            titulo: 'Estado',
            render: (u) => (u.activo ? <Badge tipo="verde">Activo</Badge> : <Badge tipo="rojo">Bloqueado</Badge>),
          },
          {
            titulo: 'Último acceso',
            render: (u) => (u.ultimoAcceso ? new Date(u.ultimoAcceso).toLocaleString() : '—'),
          },
          {
            titulo: 'Acciones',
            render: (u) => (
              <div className="acciones" style={{ gap: '6px' }}>
                {u.activo
                  ? <button type="button" className="btn btn-borde" onClick={() => bloquear(u)}>Bloquear</button>
                  : <button type="button" className="btn btn-borde" onClick={() => desbloquear(u)}>Desbloquear</button>}
                <button type="button" className="btn btn-borde" onClick={() => reestablecer(u)}>Cambiar clave</button>
              </div>
            ),
          },
        ]}
        filas={usuarios}
      />
    </section>
  );
}