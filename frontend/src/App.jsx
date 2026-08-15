import { useState } from 'react';
import Empleados from './pages/Empleados';
import Minutas from './pages/Minutas';
import Proyectos from './pages/Proyectos';
import Contratos from './pages/Contratos';
import EntregasRopa from './pages/EntregasRopa';
import EntregasEpp from './pages/EntregasEpp';
import Asignaciones from './pages/Asignaciones';
import AsignacionesConsumibles from './pages/AsignacionesConsumibles';
import Herramientas from './pages/Herramientas';
import Epp from './pages/Epp';
import Materiales from './pages/Materiales';
import Consumibles from './pages/Consumibles';
import Compras from './pages/Compras';
import Proveedores from './pages/Proveedores';
import Escaneo from './pages/Escaneo';
import Codigos from './pages/Codigos';
import Alertas from './pages/Alertas';
import Ajustes from './pages/Ajustes';

const SECCIONES = [
  {
    grupo: 'Inicio',
    items: [{ clave: 'alertas', etiqueta: 'Alertas', componente: Alertas }],
  },
  {
    grupo: 'Proyectos',
    items: [{ clave: 'proyectos', etiqueta: 'Proyectos', componente: Proyectos }],
  },
  {
    grupo: 'Personal',
    items: [
      { clave: 'empleados', etiqueta: 'Empleados y hoja de vida', componente: Empleados },
      { clave: 'contratos', etiqueta: 'Contrataciones', componente: Contratos },
      { clave: 'minutas', etiqueta: 'Minuta de empleados', componente: Minutas },
    ],
  },
  {
    grupo: 'Equipos y herramientas',
    items: [
      { clave: 'asignaciones', etiqueta: 'Asignación de herramientas', componente: Asignaciones },
      { clave: 'asignaciones-consumibles', etiqueta: 'Asignación de consumibles', componente: AsignacionesConsumibles },
      { clave: 'ropa', etiqueta: 'Entrega de ropa', componente: EntregasRopa },
      { clave: 'epp', etiqueta: 'Entrega de EPP', componente: EntregasEpp },
    ],
  },
  {
    grupo: 'Control de inventario',
    items: [
      { clave: 'inv-herramientas', etiqueta: 'Inventario de herramientas', componente: Herramientas },
      { clave: 'inv-epp', etiqueta: 'Inventario de EPP', componente: Epp },
      { clave: 'materiales', etiqueta: 'Inventario de materiales', componente: Materiales },
      { clave: 'consumibles', etiqueta: 'Inventario de consumibles', componente: Consumibles },
      { clave: 'ajustes', etiqueta: 'Ajustes de inventario', componente: Ajustes },
    ],
  },
  {
    grupo: 'Compras',
    items: [
      { clave: 'compras', etiqueta: 'Compras y facturas', componente: Compras },
      { clave: 'proveedores', etiqueta: 'Proveedores', componente: Proveedores },
    ],
  },
  {
    grupo: 'Códigos',
    items: [
      { clave: 'escaneo', etiqueta: 'Escaneo', componente: Escaneo },
      { clave: 'codigos', etiqueta: 'Listado de códigos', componente: Codigos },
    ],
  },
];

export default function App() {
  const [activa, setActiva] = useState('empleados');
  const activaItem = SECCIONES.flatMap((s) => s.items).find((i) => i.clave === activa);
  const Pagina = activa ? activaItem.componente : null;

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
          {SECCIONES.map((s) => (
            <div key={s.grupo} className="grupo">
              <p className="grupo-titulo">{s.grupo}</p>
              {s.items.map((i) => (
                <button
                  key={i.clave}
                  type="button"
                  className={`nav-item ${activa === i.clave ? 'nav-item-activo' : ''}`}
                  onClick={() => setActiva(i.clave)}
                >
                  {i.etiqueta}
                </button>
              ))}
            </div>
          ))}
        </nav>
        <div className="barra-pie">
          <span>SQLite + Spring Boot + React</span>
        </div>
      </aside>
      <main className="contenido">
        {Pagina ? <Pagina /> : null}
      </main>
    </div>
  );
}