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
  { to: '/inventario/ajustes', label: 'Ajustes' },
  { to: '/inventario/codigos', label: 'Códigos' },
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

export const TABS_CODIGOS = [
  { to: '/inventario/codigos', label: 'Listado', end: true },
  { to: '/inventario/codigos/escaneo', label: 'Escaneo' },
];