export const TABS_EMPLEADOS = [
  { to: '/empleados', label: 'Empleados', end: true },
  { to: '/empleados/contratos', label: 'Contratos' },
  { to: '/empleados/minutas', label: 'Minutas' },
  { to: '/empleados/entregas/ropa', label: 'Entregas' },
  { to: '/empleados/asignaciones', label: 'Asignaciones' },
];

export const TABS_INVENTARIO = [
  { to: '/inventario/herramientas', label: 'Herramientas', end: true },
  { to: '/inventario/epp', label: 'EPP' },
  { to: '/inventario/materiales', label: 'Materiales' },
  { to: '/inventario/consumibles', label: 'Consumibles' },
  { to: '/inventario/codigos/escaneo', label: 'Lector de códigos' },
];

export const TABS_PROYECTOS = [
  { to: '/proyectos', label: 'Proyectos', end: true },
  { to: '/proyectos/asignaciones-consumibles', label: 'Asignaciones de consumibles' },
];

export const TABS_COMPRAS = [
  { to: '/compras', label: 'Compras y facturas', end: true },
  { to: '/compras/ordenes', label: 'Órdenes' },
  { to: '/compras/cuentas-pagar', label: 'Cuentas por pagar' },
  { to: '/compras/proveedores', label: 'Proveedores' },
];

export const NAV = [
  {
    to: '/',
    label: 'Inicio',
    end: true,
    prefijo: '/',
    descripcion: 'Resumen y alertas',
    items: [
      { to: '/', label: 'Inicio', end: true },
      { to: '/buscar', label: 'Búsqueda' },
      { to: '/mi-cuenta', label: 'Mi cuenta' },
    ],
  },
  { to: '/empleados', label: 'Empleados', prefijo: '/empleados', descripcion: 'Personal y equipos', items: TABS_EMPLEADOS },
  { to: '/inventario/herramientas', label: 'Inventario', prefijo: '/inventario', descripcion: 'Control de stock', items: TABS_INVENTARIO },
  { to: '/proyectos', label: 'Proyectos', prefijo: '/proyectos', descripcion: 'Proyectos y consumibles', items: TABS_PROYECTOS },
  { to: '/compras', label: 'Compras', prefijo: '/compras', descripcion: 'Compras y proveedores', items: TABS_COMPRAS },
];

export const NAV_ADMIN = {
  to: '/admin/usuarios',
  label: 'Administración',
  prefijo: '/admin',
  descripcion: 'Usuarios, auditoría y reportes',
  items: [
    { to: '/admin/usuarios', label: 'Usuarios', end: true },
    { to: '/admin/auditoria', label: 'Auditoría' },
    { to: '/admin/mantenimiento', label: 'Mantenimiento' },
    { to: '/admin/reportes', label: 'Reportes' },
  ],
};