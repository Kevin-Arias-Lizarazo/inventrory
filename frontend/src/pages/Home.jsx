import { Link } from 'react-router-dom';
import Dashboard from '../pages/Dashboard';
import Alertas from '../pages/Alertas';

const SECCIONES = [
  {
    titulo: 'Empleados',
    descripcion: 'Personal, contratos, minutas, entregas y asignaciones',
    enlaces: [
      { to: '/empleados', label: 'Ver empleados' },
      { to: '/empleados/contratos', label: 'Contratos' },
      { to: '/empleados/minutas', label: 'Minutas' },
      { to: '/empleados/entregas/ropa', label: 'Entregas' },
      { to: '/empleados/asignaciones', label: 'Asignaciones de herramientas' },
    ],
  },
  {
    titulo: 'Inventario',
    descripcion: 'Herramientas, EPP, materiales, consumibles y ajustes',
    enlaces: [
      { to: '/inventario/herramientas', label: 'Herramientas' },
      { to: '/inventario/epp', label: 'EPP' },
      { to: '/inventario/materiales', label: 'Materiales' },
      { to: '/inventario/consumibles', label: 'Consumibles' },
      { to: '/inventario/ajustes', label: 'Ajustes' },
      { to: '/inventario/codigos', label: 'Códigos' },
    ],
  },
  {
    titulo: 'Proyectos',
    descripcion: 'Proyectos y asignación de consumibles',
    enlaces: [
      { to: '/proyectos', label: 'Ver proyectos' },
      { to: '/proyectos/asignaciones-consumibles', label: 'Asignaciones de consumibles' },
    ],
  },
  {
    titulo: 'Compras',
    descripcion: 'Compras, facturas, órdenes y proveedores',
    enlaces: [
      { to: '/compras', label: 'Compras y facturas' },
      { to: '/compras/ordenes', label: 'Órdenes de compra' },
      { to: '/compras/cuentas-pagar', label: 'Cuentas por pagar' },
      { to: '/compras/proveedores', label: 'Proveedores' },
    ],
  },
];

export default function Home({ nivel }) {
  const secciones = nivel === 'ADMIN'
    ? [
        ...SECCIONES,
        {
          titulo: 'Administración',
          descripcion: 'Usuarios, auditoría, mantenimiento y reportes',
          enlaces: [
            { to: '/admin/usuarios', label: 'Usuarios' },
            { to: '/admin/auditoria', label: 'Auditoría' },
            { to: '/admin/mantenimiento', label: 'Mantenimiento' },
            { to: '/admin/reportes', label: 'Reportes PDF' },
          ],
        },
      ]
    : SECCIONES;

  return (
    <section>
      <Dashboard />
      <Alertas />
      <h2>Secciones</h2>
      <div className="grid-secciones">
        {secciones.map((s) => (
          <div className="tarjeta-seccion" key={s.titulo}>
            <h3>{s.titulo}</h3>
            <p className="texto-aviso">{s.descripcion}</p>
            <ul className="lista-enlaces">
              {s.enlaces.map((e) => (
                <li key={e.to}>
                  <Link to={e.to}>{e.label}</Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </section>
  );
}