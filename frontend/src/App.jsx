import { useState } from 'react';
import { useAuth } from './auth/auth-contexto';
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
import Dashboard from './pages/Dashboard';
import CuentasPorPagar from './pages/CuentasPorPagar';
import OrdenesCompra from './pages/OrdenesCompra';
import Reportes from './pages/Reportes';
import Mantenimiento from './pages/Mantenimiento';
import Usuarios from './pages/Usuarios';
import Auditoria from './pages/Auditoria';
import MiCuenta from './pages/MiCuenta';
import Login from './pages/Login';
import Instalacion from './pages/Instalacion';

const SECCIONES_BASE = [
  {
    grupo: 'Inicio',
    items: [
      { clave: 'dashboard', etiqueta: 'Dashboard', componente: Dashboard },
      { clave: 'alertas', etiqueta: 'Alertas', componente: Alertas },
    ],
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
      { clave: 'ordenes-compra', etiqueta: 'Órdenes de compra', componente: OrdenesCompra },
      { clave: 'cuentas-pagar', etiqueta: 'Cuentas por pagar', componente: CuentasPorPagar },
      { clave: 'proveedores', etiqueta: 'Proveedores', componente: Proveedores },
    ],
  },
  {
    grupo: 'Reportes',
    items: [
      { clave: 'reportes', etiqueta: 'Reportes PDF', componente: Reportes },
      { clave: 'mantenimiento', etiqueta: 'Mantenimiento', componente: Mantenimiento, admin: true },
    ],
  },
  {
    grupo: 'Códigos',
    items: [
      { clave: 'escaneo', etiqueta: 'Escaneo', componente: Escaneo },
      { clave: 'codigos', etiqueta: 'Listado de códigos', componente: Codigos },
    ],
  },
  {
    grupo: 'Administración',
    items: [
      { clave: 'usuarios', etiqueta: 'Usuarios', componente: Usuarios, admin: true },
      { clave: 'auditoria', etiqueta: 'Auditoría', componente: Auditoria, admin: true },
    ],
  },
  {
    grupo: 'Cuenta',
    items: [{ clave: 'mi-cuenta', etiqueta: 'Mi cuenta', componente: MiCuenta }],
  },
];

const ETIQUETA_ROL = { ADMIN: 'Admin', USUARIO: 'Usuario', LECTOR: 'Lector' };

export default function App() {
  const { cargando, usuario, instalacion, logout } = useAuth();
  const [activa, setActiva] = useState('dashboard');

  if (cargando) {
    return <div className="login-pantalla"><p>Cargando…</p></div>;
  }

  if (instalacion) {
    return <Instalacion />;
  }

  if (!usuario) {
    return <Login />;
  }

  const esAdmin = usuario.nivel === 'ADMIN';
  const secciones = SECCIONES_BASE
    .map((s) => ({
      ...s,
      items: s.items.filter((i) => !i.admin || esAdmin),
    }))
    .filter((s) => s.items.length > 0);

  const activaItem = secciones.flatMap((s) => s.items).find((i) => i.clave === activa);
  const Pagina = activaItem ? activaItem.componente : null;

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
          {secciones.map((s) => (
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
          <div style={{ marginBottom: '8px' }}>
            <strong>{usuario.username}</strong>
            <span style={{ display: 'block', color: '#94a3b8' }}>{ETIQUETA_ROL[usuario.nivel] || usuario.rol}</span>
          </div>
          <button type="button" className="btn btn-borde" onClick={() => logout()} style={{ width: '100%' }}>
            Cerrar sesión
          </button>
        </div>
      </aside>
      <main className="contenido">
        {Pagina ? <Pagina /> : null}
      </main>
    </div>
  );
}