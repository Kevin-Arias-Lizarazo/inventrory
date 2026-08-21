import { useCallback, useEffect, useMemo, useState } from 'react';
import { get, post } from '../api';
import { AuthContexto } from './auth-contexto';
import { clearAccessToken, setAccessToken, setCsrfToken } from './token';

export function AuthProveedor({ children }) {
  const [cargando, setCargando] = useState(true);
  const [usuario, setUsuario] = useState(null);
  const [instalacion, setInstalacion] = useState(false);

  const recargar = useCallback(async () => {
    try {
      const d = await get('/api/auth/me');
      setUsuario(d);
    } catch {
      setUsuario(null);
    }
  }, []);

  useEffect(() => {
    (async () => {
      try {
        const res = await fetch('/api/auth/csrf', { credentials: 'include' });
        const d = await res.json();
        setCsrfToken(d.token);
      } catch {
        /* ignorar */
      }
      try {
        const estado = await get('/api/instalacion/estado');
        setInstalacion(!!estado.pendiente);
      } catch {
        setInstalacion(false);
      }
      await recargar();
      setCargando(false);
    })();
  }, [recargar]);

  useEffect(() => {
    const cerrar = () => {
      clearAccessToken();
      setUsuario(null);
    };
    window.addEventListener('auth:cerrada', cerrar);
    return () => window.removeEventListener('auth:cerrada', cerrar);
  }, []);

  const login = useCallback(async (username, password) => {
    const r = await post('/api/auth/login', { username, password });
    setAccessToken(r.accessToken);
    setUsuario(r.usuario);
    setInstalacion(false);
    return r.usuario;
  }, []);

  const logout = useCallback(async () => {
    try {
      await post('/api/auth/logout', {});
    } catch {
      /* ignorar */
    }
    clearAccessToken();
    setUsuario(null);
  }, []);

  const recuperarAdmin = useCallback(async (secretoRoot, nuevaContrasenaAdmin) => {
    await post('/api/auth/recuperar-admin', { secretoRoot, nuevaContrasenaAdmin });
  }, []);

  const cambiarContrasena = useCallback(async (username, actual, nueva) => {
    await post('/api/auth/cambiar-contrasena', { username, actual, nueva });
  }, []);

  const valor = useMemo(
    () => ({ cargando, usuario, instalacion, login, logout, recuperarAdmin, cambiarContrasena, recargar }),
    [cargando, usuario, instalacion, login, logout, recuperarAdmin, cambiarContrasena, recargar],
  );

  return <AuthContexto.Provider value={valor}>{children}</AuthContexto.Provider>;
}