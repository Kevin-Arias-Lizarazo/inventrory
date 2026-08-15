import { useListaPaginada } from '../hooks';
import Inventario from './Inventario';

export default function Epp() {
  const hook = useListaPaginada(
    ['epp', 'movimientos-epp', 'entregas-epp'],
    '/api/epp/paginado'
  );
  return (
    <Inventario
      config={{
        ...hook,
        base: '/api/epp',
        baseMovimientos: '/api/movimientos-epp',
        recursoMovimientos: 'movimientos-epp',
        nombreSingular: 'EPP',
        nombrePlural: 'EPP',
        tituloNombre: 'EPP',
        mostrarCodigo: false,
        mostrarMarca: true,
        mostrarUnidad: false,
      }}
      titulo="Inventario de EPP"
    />
  );
}