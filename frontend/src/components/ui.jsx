export function Badge({ tipo, children }) {
  return <span className={`badge badge-${tipo || 'neutro'}`}>{children}</span>;
}

export function Tabla({ columnas, filas, vacio }) {
  if (filas.length === 0) {
    return <p className="vacio">{vacio || 'No hay registros todavía.'}</p>;
  }
  return (
    <div className="tabla-envoltura">
      <table className="tabla">
        <thead>
          <tr>
            {columnas.map((c) => (
              <th key={c.clave || c.titulo}>{c.titulo}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {filas.map((fila, i) => (
            <tr key={fila.id ?? i}>
              {columnas.map((c) => (
                <td key={c.clave || c.titulo}>
                  {c.render ? c.render(fila, i) : fila[c.clave]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function MiniImagen({ url, alt, alto }) {
  if (!url) return <span className="sin-dato">&mdash;</span>;
  return (
    <a href={url} target="_blank" rel="noreferrer" title="Ampliar">
      <img src={url} alt={alt || 'imagen'} className="mini-imagen" style={{ height: alto || 34 }} />
    </a>
  );
}

export function Microsofto({ errores }) {
  if (!errores) return null;
  return (
    <div className="alerta" role="alert" aria-live="polite">
      <ul>
        {errores.map((e, i) => (
          <li key={i}>{e}</li>
        ))}
      </ul>
    </div>
  );
}

export function Paginacion({ pagina, total, totalPaginas, tamano, onPagina, onTamano }) {
  if (total === 0) return null;
  return (
    <div className="paginacion">
      <button
        type="button"
        className="btn btn-borde"
        disabled={pagina <= 0}
        onClick={() => onPagina(pagina - 1)}
      >
        ‹ Anterior
      </button>
      <span>
        Página {pagina + 1} de {totalPaginas} ({total} registros)
      </span>
      <button
        type="button"
        className="btn btn-borde"
        disabled={pagina + 1 >= totalPaginas}
        onClick={() => onPagina(pagina + 1)}
      >
        Siguiente ›
      </button>
      <select value={tamano} onChange={(e) => onTamano(Number(e.target.value))}>
        {[15, 30, 50, 100].map((n) => (
          <option key={n} value={n}>
            {n} por página
          </option>
        ))}
      </select>
    </div>
  );
}

export function FilterBar({ campos, filtros = {}, onCambio, onLimpiar }) {
  return (
    <div className="minuta-filtros">
      {campos.map((campo) => {
        const valor = filtros[campo.clave] ?? '';
        const cambiar = (e) => onCambio({ ...filtros, [campo.clave]: e.target.value });
        if (campo.tipo === 'search') {
          return (
            <input
              key={campo.clave}
              type="search"
              placeholder={campo.etiqueta}
              value={valor}
              onChange={cambiar}
            />
          );
        }
        if (campo.tipo === 'date') {
          return (
            <input
              key={campo.clave}
              type="date"
              value={valor}
              title={campo.etiqueta}
              onChange={cambiar}
            />
          );
        }
        if (campo.tipo === 'select' || campo.tipo === 'orden') {
          return (
            <select key={campo.clave} value={valor} title={campo.etiqueta} onChange={cambiar}>
              {(campo.opciones || []).map((op) => (
                <option key={op.valor} value={op.valor}>
                  {op.etiqueta}
                </option>
              ))}
            </select>
          );
        }
        return null;
      })}
      <button type="button" className="btn btn-borde" onClick={onLimpiar}>
        Limpiar
      </button>
    </div>
  );
}
