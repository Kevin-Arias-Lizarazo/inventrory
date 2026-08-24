import { useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/auth-contexto';
import { NAV, NAV_ADMIN } from '../secciones';

const ETIQUETA_NIVEL = { ADMIN: 'Admin', SUPERVISOR: 'Supervisor', USUARIO: 'Usuario', LECTOR: 'Lector' };

function seccionActiva(nav, path) {
  return nav.find((s) => {
    const base = s.prefijo || s.to;
    if (s.end) return path === s.to;
    return path === base || path.startsWith(base + '/') || path.startsWith(base + '?');
  }) || nav[0];
}

export default function Layout({ nivel }) {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [q, setQ] = useState('');
  const path = location.pathname + location.search;

  const nav = nivel === 'ADMIN' ? [...NAV, NAV_ADMIN] : NAV;
  const activo = seccionActiva(nav, path);

  function buscar(e) {
    e.preventDefault();
    navigate(q.trim() ? `/buscar?q=${encodeURIComponent(q.trim())}` : '/buscar');
  }

  return (
    <div className="app">
      <aside className="barra-lateral">
        <div className="logo">
          <span className="logo-icono">INV</span>
          <div>
            <strong>Inventario</strong>
            <small>Gestión integral</small>
          </div>
        </div>
        <nav>
          {nav.map((s) => {
            const esActivo = activo === s;
            return (
              <div key={s.to} className="grupo-sector">
                <NavLink
                  to={s.to}
                  end={s.end}
                  className={`nav-item ${esActivo ? 'nav-item-activo' : ''}`}
                >
                  <span className="nav-label">{s.label}</span>
                  {s.descripcion && <span className="nav-descripcion">{s.descripcion}</span>}
                </NavLink>
                {esActivo && s.items.length > 0 && (
                  <div className="submenu">
                    {s.items.map((i) => (
                      <NavLink
                        key={i.to}
                        to={i.to}
                        end={i.end}
                        className={({ isActive }) =>
                          `submenu-item ${isActive ? 'submenu-item-activo' : ''}`
                        }
                      >
                        {i.label}
                      </NavLink>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </nav>
        <div className="barra-pie">
          <div style={{ marginBottom: '8px' }}>
            <strong>{usuario.username}</strong>
            <span style={{ display: 'block', color: '#94a3b8' }}>
              {ETIQUETA_NIVEL[usuario.nivel] || usuario.nivel}
            </span>
          </div>
          <NavLink to="/mi-cuenta" className="btn btn-borde" style={{ width: '100%', marginBottom: '8px', display: 'block', textAlign: 'center' }}>
            Mi cuenta
          </NavLink>
          <button type="button" className="btn btn-borde" onClick={() => logout()} style={{ width: '100%' }}>
            Cerrar sesión
          </button>
        </div>
      </aside>
      <main className="contenido">
        <header className="barra-superior">
          <form className="busqueda-superior" onSubmit={buscar}>
            <input
              type="search"
              placeholder="Buscar en el inventario…"
              value={q}
              onChange={(e) => setQ(e.target.value)}
            />
          </form>
          <NavLink to="/buscar" className="btn btn-borde">Búsqueda</NavLink>
        </header>
        <Outlet />
      </main>
    </div>
  );
}