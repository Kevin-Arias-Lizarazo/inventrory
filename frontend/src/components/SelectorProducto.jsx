import { useEffect, useId, useState } from 'react';
import { get, post } from '../api';
import { Badge } from './ui';

const CATALOGO = {
  HERRAMIENTA: { base: '/api/herramientas', crear: (d) => ({ ...d, cantidadTotal: 1 }) },
  EPP: { base: '/api/epp', crear: (d) => ({ ...d }) },
  CONSUMIBLE: { base: '/api/consumibles', crear: (d) => ({ ...d }) },
  MATERIAL: { base: '/api/materiales', crear: (d) => ({ ...d }) },
};

export default function SelectorProducto({ tipo, onUsar, etiqueta = 'Producto', seleccionado = '' }) {
  const [lista, setLista] = useState([]);
  const [busqueda, setBusqueda] = useState('');
  const [creando, setCreando] = useState(false);
  const [formNuevo, setFormNuevo] = useState({ nombre: '', marca: '' });
  const [error, setError] = useState(null);
  const inputId = useId();

  useEffect(() => {
    if (tipo === 'ROPA' || !CATALOGO[tipo]) {
      setLista([]);
      return;
    }
    let activo = true;
    get(CATALOGO[tipo].base)
      .then((d) => {
        if (activo) setLista(d || []);
      })
      .catch(() => {
        /* ignore */
      });
    return () => {
      activo = false;
    };
  }, [tipo]);

  const q = busqueda.trim().toLowerCase();
  const resultados = q
    ? lista
        .filter(
          (x) =>
            (x.nombre || '').toLowerCase().includes(q) ||
            (x.marca || '').toLowerCase().includes(q) ||
            (x.codigo || '').toLowerCase().includes(q)
        )
        .slice(0, 8)
    : lista.slice(0, 8);

  function elegir(x) {
    onUsar({
      tipo,
      productoId: x.id,
      nombre: x.nombre,
      disponibleActual: tipo === 'HERRAMIENTA' ? x.cantidadDisponible : x.stock,
    });
    setBusqueda('');
    setCreando(false);
  }

  async function crearProducto(e) {
    e.preventDefault();
    setError(null);
    try {
      const cuerpo = CATALOGO[tipo].crear({ ...formNuevo });
      const creado = await post(CATALOGO[tipo].base, cuerpo);
      elegir(creado);
      setFormNuevo({ nombre: '', marca: '' });
    } catch (err) {
      setError(err.message);
    }
  }

  function teclaEnter(e) {
    if (e.key !== 'Enter') return;
    e.preventDefault();
    if (resultados.length > 0) elegir(resultados[0]);
  }

  if (tipo === 'ROPA' || !CATALOGO[tipo]) return null;

  return (
    <div className="selector-producto">
      <label htmlFor={inputId}>{etiqueta} (buscar o crear)</label>
      {seleccionado && !busqueda.trim() && (
        <div className="selector-seleccionado">
          <Badge tipo="verde">Seleccionado: {seleccionado}</Badge>
        </div>
      )}
      <input
        id={inputId}
        type="text"
        value={busqueda}
        placeholder={seleccionado ? 'Escribe para cambiar…' : 'Escribe para buscar…'}
        onKeyDown={teclaEnter}
        onChange={(e) => {
          setBusqueda(e.target.value);
          setCreando(false);
        }}
      />
      {busqueda.trim() && resultados.length === 0 && (
        <button type="button" className="btn btn-primario btn-sm" onClick={() => setCreando(true)}>
          Crear nuevo «{busqueda.trim()}»
        </button>
      )}
      {resultados.length > 0 && (
        <ul className="selector-lista">
          {resultados.map((x) => (
            <li key={x.id}>
              <span>
                {x.nombre}
                {x.marca ? <small> · {x.marca}</small> : null}
                {x.codigo ? <small> · {x.codigo}</small> : null}
              </span>
              <button type="button" className="btn btn-primario btn-sm" onClick={() => elegir(x)}>
                Usar
              </button>
            </li>
          ))}
        </ul>
      )}
      {creando && (
        <form className="form form-nuevo-producto" onSubmit={crearProducto}>
          <div className="campo">
            <label>Nombre *</label>
            <input
              type="text"
              value={formNuevo.nombre}
              placeholder={busqueda.trim()}
              onChange={(e) => setFormNuevo({ ...formNuevo, nombre: e.target.value })}
              required
            />
          </div>
          <div className="campo">
            <label>Marca</label>
            <input
              type="text"
              value={formNuevo.marca}
              onChange={(e) => setFormNuevo({ ...formNuevo, marca: e.target.value })}
            />
          </div>
          {error && <p className="texto-error">{error}</p>}
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setCreando(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              Crear y usar
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
