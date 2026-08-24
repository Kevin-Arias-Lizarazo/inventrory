import { Link } from 'react-router-dom';
import Dashboard from '../pages/Dashboard';
import Alertas from '../pages/Alertas';
import { NAV, NAV_ADMIN } from '../secciones';

export default function Home({ nivel }) {
  const nav = nivel === 'ADMIN' ? [...NAV, NAV_ADMIN] : NAV;
  const secciones = nav.filter((s) => s.to !== '/' && s.items.length > 0);

  return (
    <section>
      <Dashboard />
      <Alertas />
      <h2>Secciones</h2>
      <div className="grid-secciones">
        {secciones.map((s) => (
          <div className="tarjeta-seccion" key={s.to}>
            <h3>{s.label}</h3>
            {s.descripcion && <p className="texto-aviso">{s.descripcion}</p>}
            <ul className="lista-enlaces">
              {s.items.map((i) => (
                <li key={i.to}>
                  <Link to={i.to}>{i.label}</Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </section>
  );
}