import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/auth-contexto';

const ETIQUETA_NIVEL = { ADMIN: 'Admin', SUPERVISOR: 'Supervisor', USUARIO: 'Usuario', LECTOR: 'Lector' };

const ITEMS = [
  { to: '/', etiqueta: 'Inicio', end: true },
  { to: '/empleados', etiqueta: 'Empleados' },
  { to: '/inventario/herramientas', etiqueta: 'Inventario' },
  { to: '/proyectos', etiqueta: 'Proyectos' },
  { to: '/compras', etiqueta: 'Compras' },
];

export default function Layout({ nivel }) {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const [q, setQ] = useState('');

  function buscar(e) {
    e.preventDefault();
    navigate(q.trim() ? `/buscar?q=${encodeURIComponent(q.trim())}` : '/buscar');
  }

  const items = nivel === 'ADMIN' ? [...ITEMS, { to: '/admin/usuarios', etiqueta: 'Admin' }] : ITEMS;

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
          {items.map((i) => (
            <NavLink
              key={i.to}
              to={i.to}
              end={i.end}
              className={({ isActive }) => `nav-item ${isActive ? 'nav-item-activo' : ''}`}
            >
              {i.etiqueta}
            </NavLink>
          ))}
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