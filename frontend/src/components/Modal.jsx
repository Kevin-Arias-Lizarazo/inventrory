export default function Modal({ titulo, abierto, onCerrar, children, ancho }) {
  if (!abierto) return null;
  return (
    <div className="modal-fondo" onClick={onCerrar}>
      <div
        className="modal"
        style={{ maxWidth: ancho || 560 }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-cabecera">
          <h3>{titulo}</h3>
          <button type="button" className="btn-icono" onClick={onCerrar} aria-label="Cerrar">
            &times;
          </button>
        </div>
        <div className="modal-cuerpo">{children}</div>
      </div>
    </div>
  );
}