import { NavLink, useLocation } from 'react-router-dom';

export default function SeccionTabs({ items }) {
  const location = useLocation();
  const actual = location.pathname + location.search;

  return (
    <nav className="seccion-tabs" aria-label="Subsecciones">
      {items.map((i) => {
        const activo = i.end ? actual === i.to : actual.startsWith(i.to.split('?')[0]);
        return (
          <NavLink
            key={i.to}
            to={i.to}
            end={i.end}
            className={`seccion-tab ${activo ? 'seccion-tab-activo' : ''}`}
          >
            {i.label}
          </NavLink>
        );
      })}
    </nav>
  );
}