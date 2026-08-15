import { useListaPaginada } from '../hooks';
import Inventario from './Inventario';

export default function Materiales() {
  const hook = useListaPaginada(['materiales', 'movimientos-materiales'], '/api/materiales/paginado');
  return (
    <Inventario
      config={{
        ...hook,
        base: '/api/materiales',
        baseMovimientos: '/api/movimientos-materiales',
        recursoMovimientos: 'movimientos-materiales',
        nombreSingular: 'material',
        nombrePlural: 'materiales',
        mostrarCodigo: false,
      }}
      titulo="Inventario de materiales"
    />
  );
}