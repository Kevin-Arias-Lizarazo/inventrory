import { useListaPaginada } from '../hooks';
import Inventario from './Inventario';

export default function Consumibles() {
  const hook = useListaPaginada(
    ['consumibles', 'movimientos-consumibles', 'asignaciones-consumibles'],
    '/api/consumibles/paginado'
  );
  return (
    <Inventario
      config={{
        ...hook,
        base: '/api/consumibles',
        baseMovimientos: '/api/movimientos-consumibles',
        recursoMovimientos: 'movimientos-consumibles',
        nombreSingular: 'consumible',
        nombrePlural: 'consumibles',
        mostrarCodigo: true,
      }}
      titulo="Inventario de consumibles"
    />
  );
}