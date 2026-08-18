import { useEffect } from 'react';

export default function Modal({ titulo, abierto, onCerrar, children, ancho }) {
  useEffect(() => {
    if (!abierto) return undefined;
    const cerrarConEscape = (e) => {
      if (e.key === 'Escape') onCerrar();
    };
    document.addEventListener('keydown', cerrarConEscape);
    return () => document.removeEventListener('keydown', cerrarConEscape);
  }, [abierto, onCerrar]);

  if (!abierto) return null;
  return (
    <div className="modal-fondo" onClick={onCerrar}>
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-titulo"
        style={{ maxWidth: ancho || 560 }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-cabecera">
          <h3 id="modal-titulo">{titulo}</h3>
          <button type="button" className="btn-icono" onClick={onCerrar} aria-label="Cerrar">
            &times;
          </button>
        </div>
        <div className="modal-cuerpo">{children}</div>
      </div>
    </div>
  );
}
