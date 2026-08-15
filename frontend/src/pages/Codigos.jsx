import { useMemo, useState } from 'react';
import { useLista } from '../hooks';
import QrCodigo from '../components/QrCodigo';

const ACCIONES = [
  { codigo: 'AH', tipo: 'Acción', nombre: 'Asignar herramienta' },
  { codigo: 'DH', tipo: 'Acción', nombre: 'Devolver herramienta' },
  { codigo: 'AC', tipo: 'Acción', nombre: 'Asignar consumible' },
  { codigo: 'FIN', tipo: 'Acción', nombre: 'Fin de escaneo' },
];

const POR_PAGINA = 12;

export default function Codigos() {
  const { lista: herramientas } = useLista('herramientas', '/api/herramientas');
  const { lista: consumibles } = useLista('consumibles', '/api/consumibles');
  const { lista: empleados } = useLista('empleados', '/api/empleados?contratados=true');
  const { lista: proyectos } = useLista('proyectos', '/api/proyectos?estado=ACTIVO');

  const [seccion, setSeccion] = useState('todos');
  const [pagina, setPagina] = useState(1);

  const secciones = useMemo(
    () => [
      {
        id: 'acciones',
        titulo: 'Acciones',
        entradas: ACCIONES,
      },
      {
        id: 'herramientas',
        titulo: 'Herramientas',
        entradas: herramientas.map((h) => ({
          codigo: h.codigo,
          tipo: 'Herramienta',
          nombre: h.nombre + (h.marca ? ` (${h.marca})` : ''),
        })),
      },
      {
        id: 'consumibles',
        titulo: 'Consumibles',
        entradas: consumibles.map((c) => ({ codigo: c.codigo, tipo: 'Consumible', nombre: c.nombre })),
      },
      {
        id: 'empleados',
        titulo: 'Empleados',
        entradas: empleados.map((e) => ({ codigo: e.codigo, tipo: 'Empleado', nombre: e.nombre })),
      },
      {
        id: 'proyectos',
        titulo: 'Proyectos',
        entradas: proyectos.map((p) => ({ codigo: p.codigo, tipo: 'Proyecto', nombre: p.nombre })),
      },
    ],
    [herramientas, consumibles, empleados, proyectos]
  );

  const todas = useMemo(() => secciones.flatMap((s) => s.entradas), [secciones]);

  const visibles = useMemo(() => {
    if (seccion === 'todos') return todas;
    const s = secciones.find((x) => x.id === seccion);
    return s ? s.entradas : [];
  }, [seccion, todas, secciones]);

  const totalPaginas = Math.max(1, Math.ceil(visibles.length / POR_PAGINA));
  const paginaActual = Math.min(pagina, totalPaginas);

  function cambiarSeccion(nueva) {
    setSeccion(nueva);
    setPagina(1);
  }

  return (
    <section className="codigos">
      <div className="pagina-cabecera">
        <h2>Listado de códigos</h2>
        <button type="button" className="btn btn-primario" onClick={() => window.print()}>
          Imprimir etiquetas
        </button>
      </div>

      <div className="codigos-filtros">
        <button
          type="button"
          className={`btn ${seccion === 'todos' ? 'btn-primario' : 'btn-borde'}`}
          onClick={() => cambiarSeccion('todos')}
        >
          Todos ({todas.length})
        </button>
        {secciones.map((s) => (
          <button
            key={s.id}
            type="button"
            className={`btn ${seccion === s.id ? 'btn-primario' : 'btn-borde'}`}
            onClick={() => cambiarSeccion(s.id)}
          >
            {s.titulo} ({s.entradas.length})
          </button>
        ))}
      </div>

      {visibles.length === 0 && <p className="vacio">No hay códigos en esta sección.</p>}

      <div className="codigos-cartones">
        {visibles.map((e, i) => {
          const enPagina = i >= (paginaActual - 1) * POR_PAGINA && i < paginaActual * POR_PAGINA;
          return (
            <div key={e.codigo + '-' + e.nombre} className={`codigo-carton ${enPagina ? '' : 'oculto'}`}>
              <QrCodigo codigo={e.codigo} tamano={72} mostrarTexto={false} />
              <div className="codigo-texto">{e.codigo}</div>
              <div className="codigo-tipo">{e.tipo}</div>
              <div className="codigo-nombre">{e.nombre}</div>
            </div>
          );
        })}
      </div>

      {totalPaginas > 1 && (
        <div className="paginacion">
          <button
            type="button"
            className="btn btn-borde"
            disabled={paginaActual <= 1}
            onClick={() => setPagina((p) => Math.max(1, p - 1))}
          >
            ‹ Anterior
          </button>
          <span>
            Página {paginaActual} de {totalPaginas} ({visibles.length} códigos)
          </span>
          <button
            type="button"
            className="btn btn-borde"
            disabled={paginaActual >= totalPaginas}
            onClick={() => setPagina((p) => Math.min(totalPaginas, p + 1))}
          >
            Siguiente ›
          </button>
        </div>
      )}
    </section>
  );
}