import { useCallback, useEffect, useState } from 'react';
import { get, post, del, hoy } from '../api';
import { useEventos } from '../eventos-contexto';
import Modal from '../components/Modal';
import { Tabla, Microsofto, Badge } from '../components/ui';
import SelectorProducto from '../components/SelectorProducto';

const MOTIVOS = [
  { valor: 'CONTEO', etiqueta: 'Conteo físico' },
  { valor: 'MERMA', etiqueta: 'Merma' },
  { valor: 'SOBRANTE', etiqueta: 'Sobrante' },
  { valor: 'DANO', etiqueta: 'Daño' },
];

const TIPOS = [
  { valor: 'MATERIAL', etiqueta: 'Material' },
  { valor: 'CONSUMIBLE', etiqueta: 'Consumible' },
  { valor: 'EPP', etiqueta: 'EPP' },
  { valor: 'HERRAMIENTA', etiqueta: 'Herramienta' },
];

const lineaVacia = () => ({
  tipoProducto: 'MATERIAL',
  productoId: '',
  nombre: '',
  descripcion: '',
  disponibleActual: null,
  cantidadDisponible: '',
});

const inicial = () => ({
  fecha: hoy(),
  motivo: 'CONTEO',
  observacion: '',
  lineas: [lineaVacia()],
});

export default function Ajustes() {
  const { suscribir } = useEventos();
  const [lista, setLista] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [abierto, setAbierto] = useState(false);
  const [form, setForm] = useState(inicial);
  const [errores, setErrores] = useState(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      setLista(await get('/api/ajustes'));
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  useEffect(() => suscribir(['ajustes'], () => cargar()), [suscribir, cargar]);

  function setLinea(i, patch) {
    setForm((f) => {
      const lineas = [...f.lineas];
      lineas[i] = { ...lineas[i], ...patch };
      return { ...f, lineas };
    });
  }

  async function guardar(e) {
    e.preventDefault();
    setErrores(null);
    const sinProducto = form.lineas.some((l) => !l.productoId);
    if (sinProducto) {
      setErrores(['Seleccione un producto en cada línea (usa el buscador "Usar")']);
      return;
    }
    try {
      const cuerpo = {
        fecha: form.fecha,
        motivo: form.motivo,
        observacion: form.observacion,
        lineas: form.lineas.map((l) => ({
          tipoProducto: l.tipoProducto,
          productoId: Number(l.productoId),
          descripcion: l.descripcion || l.nombre || '',
          cantidadDisponible: Number(l.cantidadDisponible),
        })),
      };
      await post('/api/ajustes', cuerpo);
      setAbierto(false);
      setForm(inicial());
      await cargar();
    } catch (err) {
      setErrores([err.message]);
    }
  }

  async function eliminar(item) {
    if (!window.confirm(`¿Eliminar el ajuste #${item.id}? (se revierte el stock)`)) return;
    try {
      await del(`/api/ajustes/${item.id}`);
      await cargar();
    } catch (err) {
      window.alert(err.message);
    }
  }

  const columnas = [
    { clave: 'id', titulo: '#' },
    { clave: 'fecha', titulo: 'Fecha' },
    {
      clave: 'motivo',
      titulo: 'Motivo',
      render: (a) => <Badge tipo="amarillo">{a.motivo}</Badge>,
    },
    {
      clave: 'lineas',
      titulo: 'Líneas',
      render: (a) =>
        (a.lineas || [])
          .map((l) => l.cantidadDisponible != null
            ? `${l.descripcion || l.tipoProducto} → disponible ${l.cantidadDisponible}`
            : `${l.tipoMovimiento} ${l.cantidad}× ${l.descripcion || l.tipoProducto}`)
          .join('; ') || '—',
    },
    { clave: 'observacion', titulo: 'Obs.' },
    {
      clave: 'acciones',
      titulo: '',
      render: (a) => (
        <button type="button" className="btn btn-peligro" onClick={() => eliminar(a)}>
          Eliminar
        </button>
      ),
    },
  ];

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Ajustes de inventario</h2>
        <button
          type="button"
          className="btn btn-primario"
          onClick={() => {
            setForm(inicial());
            setErrores(null);
            setAbierto(true);
          }}
        >
          + Nuevo ajuste
        </button>
      </div>
      {cargando && <p className="vacio">Cargando…</p>}
      {!cargando && error && <p className="texto-error">{error}</p>}
      {!cargando && !error && (
        <Tabla columnas={columnas} filas={lista} vacio="No hay ajustes registrados." />
      )}

      <Modal titulo="Nuevo ajuste" abierto={abierto} onCerrar={() => setAbierto(false)}>
        <form className="form" onSubmit={guardar}>
          <div className="form-grid">
            <div className="campo">
              <label>Fecha *</label>
              <input
                type="date"
                value={form.fecha}
                onChange={(e) => setForm({ ...form, fecha: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label>Motivo *</label>
              <select value={form.motivo} onChange={(e) => setForm({ ...form, motivo: e.target.value })}>
                {MOTIVOS.map((m) => (
                  <option key={m.valor} value={m.valor}>
                    {m.etiqueta}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="campo">
            <label>Observación</label>
            <input
              type="text"
              value={form.observacion}
              onChange={(e) => setForm({ ...form, observacion: e.target.value })}
            />
          </div>

          {form.lineas.map((l, i) => (
            <div key={i} className="linea-articulo">
              <div className="form-grid">
                <div className="campo">
                  <label>Tipo</label>
                  <select
                    value={l.tipoProducto}
                    onChange={(e) =>
                      setLinea(i, { tipoProducto: e.target.value, productoId: '', nombre: '', descripcion: '', disponibleActual: null })
                    }
                  >
                    {TIPOS.map((t) => (
                      <option key={t.valor} value={t.valor}>
                        {t.etiqueta}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="campo">
                  <label>Disponible objetivo *</label>
                  <input
                    type="number"
                    min="0"
                    value={l.cantidadDisponible}
                    onChange={(e) => setLinea(i, { cantidadDisponible: e.target.value })}
                    required
                  />
                </div>
              </div>
              <SelectorProducto
                tipo={l.tipoProducto}
                onUsar={({ productoId, nombre, disponibleActual }) =>
                  setLinea(i, { productoId, nombre, descripcion: nombre, disponibleActual })
                }
              />
              {l.nombre && <p className="texto-aviso">Producto: {l.nombre}</p>}
              {l.nombre && l.disponibleActual != null && (
                <p className="sin-dato">Disponible actual: {l.disponibleActual}</p>
              )}
              {form.lineas.length > 1 && (
                <button
                  type="button"
                  className="btn btn-borde btn-sm"
                  onClick={() =>
                    setForm((f) => ({ ...f, lineas: f.lineas.filter((_, j) => j !== i) }))
                  }
                >
                  Quitar línea
                </button>
              )}
            </div>
          ))}
          <button
            type="button"
            className="btn btn-borde"
            onClick={() => setForm((f) => ({ ...f, lineas: [...f.lineas, lineaVacia()] }))}
          >
            + Línea
          </button>
          <Microsofto errores={errores} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setAbierto(false)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              Guardar
            </button>
          </div>
        </form>
      </Modal>
    </section>
  );
}
