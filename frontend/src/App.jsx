import { Route, Routes } from 'react-router-dom';
import { useAuth } from './auth/auth-contexto';
import Layout from './components/Layout';
import Home from './pages/Home';
import Empleados from './pages/Empleados';
import DetalleEmpleado from './pages/DetalleEmpleado';
import Contratos from './pages/Contratos';
import Minutas from './pages/Minutas';
import EntregasRopa from './pages/EntregasRopa';
import EntregasEpp from './pages/EntregasEpp';
import Asignaciones from './pages/Asignaciones';
import AsignacionesConsumibles from './pages/AsignacionesConsumibles';
import Herramientas from './pages/Herramientas';
import DetalleHerramienta from './pages/DetalleHerramienta';
import Epp from './pages/Epp';
import Materiales from './pages/Materiales';
import Consumibles from './pages/Consumibles';
import Ajustes from './pages/Ajustes';
import Codigos from './pages/Codigos';
import Escaneo from './pages/Escaneo';
import Proyectos from './pages/Proyectos';
import Compras from './pages/Compras';
import OrdenesCompra from './pages/OrdenesCompra';
import CuentasPorPagar from './pages/CuentasPorPagar';
import Proveedores from './pages/Proveedores';
import Reportes from './pages/Reportes';
import Mantenimiento from './pages/Mantenimiento';
import Usuarios from './pages/Usuarios';
import Auditoria from './pages/Auditoria';
import MiCuenta from './pages/MiCuenta';
import Busqueda from './pages/Busqueda';
import Login from './pages/Login';
import Instalacion from './pages/Instalacion';

export default function App() {
  const { cargando, usuario, instalacion } = useAuth();

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

  return (
    <Routes>
      <Route element={<Layout nivel={usuario.nivel} />}>
        <Route path="/" element={<Home nivel={usuario.nivel} />} />
        <Route path="/empleados" element={<Empleados />} />
        <Route path="/empleados/:id" element={<DetalleEmpleado />} />
        <Route path="/empleados/contratos" element={<Contratos />} />
        <Route path="/empleados/minutas" element={<Minutas />} />
        <Route path="/empleados/entregas/ropa" element={<EntregasRopa />} />
        <Route path="/empleados/entregas/epp" element={<EntregasEpp />} />
        <Route path="/empleados/asignaciones" element={<Asignaciones />} />
        <Route path="/inventario/herramientas" element={<Herramientas />} />
        <Route path="/inventario/herramientas/:id" element={<DetalleHerramienta />} />
        <Route path="/inventario/epp" element={<Epp />} />
        <Route path="/inventario/materiales" element={<Materiales />} />
        <Route path="/inventario/consumibles" element={<Consumibles />} />
        <Route path="/inventario/ajustes" element={<Ajustes />} />
        <Route path="/inventario/codigos" element={<Codigos />} />
        <Route path="/inventario/codigos/escaneo" element={<Escaneo />} />
        <Route path="/proyectos" element={<Proyectos />} />
        <Route path="/proyectos/asignaciones-consumibles" element={<AsignacionesConsumibles />} />
        <Route path="/compras" element={<Compras />} />
        <Route path="/compras/ordenes" element={<OrdenesCompra />} />
        <Route path="/compras/cuentas-pagar" element={<CuentasPorPagar />} />
        <Route path="/compras/proveedores" element={<Proveedores />} />
        {esAdmin && (
          <>
            <Route path="/admin/usuarios" element={<Usuarios />} />
            <Route path="/admin/auditoria" element={<Auditoria />} />
            <Route path="/admin/mantenimiento" element={<Mantenimiento />} />
            <Route path="/admin/reportes" element={<Reportes />} />
          </>
        )}
        <Route path="/buscar" element={<Busqueda />} />
        <Route path="/mi-cuenta" element={<MiCuenta />} />
        <Route path="*" element={<Home nivel={usuario.nivel} />} />
      </Route>
    </Routes>
  );
}