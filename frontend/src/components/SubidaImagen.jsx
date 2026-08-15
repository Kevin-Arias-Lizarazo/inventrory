import { useRef, useState } from 'react';
import { subirArchivo } from '../api';

export default function SubidaImagen({ valor, onCambio, etiqueta, aceptar }) {
  const inputRef = useRef(null);
  const [subiendo, setSubiendo] = useState(false);
  const [error, setError] = useState(null);

  async function seleccionar(e) {
    const archivo = e.target.files[0];
    if (!archivo) return;
    setSubiendo(true);
    setError(null);
    try {
      const url = await subirArchivo(archivo);
      onCambio(url);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubiendo(false);
      if (inputRef.current) inputRef.current.value = '';
    }
  }

  return (
    <div className="campo">
      <label>{etiqueta}</label>
      <div className="subida-fila">
        <button
          type="button"
          className="btn btn-borde"
          onClick={() => inputRef.current && inputRef.current.click()}
          disabled={subiendo}
        >
          {subiendo ? 'Subiendo…' : valor ? 'Cambiar imagen' : 'Seleccionar imagen'}
        </button>
        {valor && (
          <a href={valor} target="_blank" rel="noreferrer">
            <img src={valor} alt="preview" className="preview-imagen" />
          </a>
        )}
        {valor && (
          <button type="button" className="btn btn-peligro" onClick={() => onCambio(null)}>
            Quitar
          </button>
        )}
      </div>
      <input
        ref={inputRef}
        type="file"
        accept={aceptar || 'image/*'}
        style={{ display: 'none' }}
        onChange={seleccionar}
      />
      {error && <small className="texto-error">{error}</small>}
    </div>
  );
}