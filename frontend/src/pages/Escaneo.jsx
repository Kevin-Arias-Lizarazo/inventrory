import SeccionTabs from '../components/SeccionTabs';
import { TABS_INVENTARIO } from '../secciones';
import { useEffect, useMemo, useState } from 'react';
import { post } from '../api';
import { useLista } from '../hooks';
import { Badge, Microsofto } from '../components/ui';
import Modal from '../components/Modal';

const CLAVE_BUFFER = 'escaneo-lotes-v1';

const ETIQUETA_TIPO = {
  ASIGNACION: 'Asignación',
  DEVOLUCION: 'Devolución',
};

const MOTIVO_ETIQUETA = {
  ITEM_NO_REGISTRADO: 'Ítem no registrado',
  STOCK_INSUFICIENTE: 'Stock insuficiente',
  SIN_DISPONIBILIDAD: 'Sin disponibilidad',
  CONTRATO_INACTIVO: 'Contrato inactivo',
  PROYECTO_INACTIVO: 'Proyecto inactivo',
  TIPO_CRUZADO: 'Tipo cruzado',
  DESTINO_NO_REGISTRADO: 'Destino no registrado',
  CANTIDAD_INVALIDA: 'Cantidad inválida',
  EXCESO_DEVOLUCION: 'Exceso de devolución',
  ASIGNACION_REMAP_INVALIDA: 'Cuadre de devolución inválido',
  ERROR_INTERNO: 'Error interno',
};

const ETIQUETA_ESTADO = {
  cerrado: 'Listo para confirmar',
  pendiente: 'Pendiente de acomodar',
  error: 'Error al confirmar',
};

let contadorId = 0;
function nuevoId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID();
  contadorId += 1;
  return `lote-${Date.now()}-${contadorId}`;
}

const tipoDeCodigo = (codigo) => (codigo.startsWith('C') ? 'CONSUMIBLE' : 'HERRAMIENTA');

export default function Escaneo() {
  const { lista: herramientas } = useLista('herramientas', '/api/herramientas');
  const { lista: consumibles } = useLista('consumibles', '/api/consumibles');
  const { lista: empleados } = useLista('empleados', '/api/empleados');
  const { lista: proyectos } = useLista('proyectos', '/api/proyectos');
  const { lista: asignacionesHerramientas } = useLista(
    ['asignaciones-herramientas', 'herramientas'],
    '/api/asignaciones-herramientas'
  );

  const mapas = useMemo(() => {
    const porCodigo = (arr) => new Map((arr || []).map((x) => [x.codigo, x]));
    return {
      H: porCodigo(herramientas),
      C: porCodigo(consumibles),
      E: porCodigo(empleados),
      P: porCodigo(proyectos),
    };
  }, [herramientas, consumibles, empleados, proyectos]);

  const [loteActual, setLoteActual] = useState(null);
  const [lotes, setLotes] = useState([]);
  const [modoDevolucion, setModoDevolucion] = useState(false);
  const [entrada, setEntrada] = useState('');
  const [mensaje, setMensaje] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const [selectorDestino, setSelectorDestino] = useState(null);
  const [filtroDestino, setFiltroDestino] = useState('');

  const [creacionExpress, setCreacionExpress] = useState(null);
  const [formCreacion, setFormCreacion] = useState({ nombre: '', marca: '', unidad: '', cantidadTotal: 1 });

  const [incrementoStock, setIncrementoStock] = useState(null);
  const [formIncremento, setFormIncremento] = useState({ cantidad: '' });

  const [cuadrar, setCuadrar] = useState(null);
  const [formCuadrar, setFormCuadrar] = useState({});
  const [erroresCuadrar, setErroresCuadrar] = useState(null);

  const [erroresForm, setErroresForm] = useState(null);

  // Buffer de lotes en localStorage: sobrevive recargas y cierres de pestaña.
  useEffect(() => {
    try {
      localStorage.setItem(CLAVE_BUFFER, JSON.stringify({ version: 1, lotes }));
    } catch {
      /* almacenamiento no disponible: el buffer queda solo en memoria */
    }
  }, [lotes]);

  useEffect(() => {
    try {
      const crudo = localStorage.getItem(CLAVE_BUFFER);
      if (!crudo) return;
      const datos = JSON.parse(crudo);
      if (datos && datos.version === 1 && Array.isArray(datos.lotes)) {
        setLotes(datos.lotes.filter((l) => l.estado !== 'aprobado' && l.estado !== 'abierto'));
      }
    } catch {
      /* buffer corrupto: se descarta y se empieza vacío */
    }
  }, []);

  const nombreDestino = (lote) => {
    const letra = lote.destinoCodigo.charAt(0);
    const destino = mapas[letra]?.get(lote.destinoCodigo);
    return destino ? destino.nombre : lote.destinoCodigo;
  };

  const nombreItem = (codigo) => {
    const letra = codigo.charAt(0);
    const item = mapas[letra]?.get(codigo);
    return item ? item.nombre : codigo;
  };

  const estadoCierre = (lote) =>
    lote && lote.pendientes && lote.pendientes.length > 0 ? 'pendiente' : 'cerrado';

  function cerrarLoteActual() {
    if (!loteActual || Object.keys(loteActual.items).length === 0) {
      setMensaje({ tipo: 'info', texto: 'FIN sin ítems: se ignora.' });
      return;
    }
    const cerrado = { ...loteActual, estado: estadoCierre(loteActual) };
    setLotes((ls) => [...ls, cerrado]);
    setLoteActual(null);
    setMensaje({
      tipo: cerrado.estado === 'pendiente' ? 'info' : 'ok',
      texto:
        cerrado.estado === 'pendiente'
          ? 'Lote cerrado pendiente de acomodar (hay ítems no registrados).'
          : `Lote cerrado (${Object.keys(cerrado.items).length} ítem(s)).`,
    });
  }

  function autoCerrarActual() {
    if (loteActual && Object.keys(loteActual.items).length > 0) {
      setLotes((ls) => [...ls, { ...loteActual, estado: estadoCierre(loteActual) }]);
    }
    setLoteActual(null);
  }

  function abrirConDestino(letra, codigo, textoOk) {
    // La devolución solo aplica con destino empleado (E#); P# siempre abre asignación.
    const tipo = modoDevolucion && letra === 'E' ? 'DEVOLUCION' : 'ASIGNACION';
    setModoDevolucion(false);
    autoCerrarActual();
    setLoteActual({
      id: nuevoId(),
      tipo,
      destinoCodigo: codigo,
      items: {},
      estado: 'abierto',
      pendientes: [],
      resultado: null,
      cuadrar: {},
    });
    setMensaje({ tipo: 'ok', texto: textoOk });
  }

  function procesarCodigo(raw) {
    const codigo = (raw || '').trim().toUpperCase();
    if (!codigo) return;
    setMensaje(null);

    if (codigo === 'FIN') {
      cerrarLoteActual();
      return;
    }
    if (codigo === 'DV') {
      autoCerrarActual();
      setModoDevolucion(true);
      setMensaje({
        tipo: 'info',
        texto: 'Devolución armada: escanee el empleado (E#) al que se devuelve.',
      });
      return;
    }

    const match = /^([EPHC])(\d+)$/.exec(codigo);
    if (!match) {
      setMensaje({
        tipo: 'error',
        texto: `Código inválido: ${codigo}. Use E#/P# (destino), H#/C# (ítem), FIN o DV.`,
      });
      return;
    }
    const letra = match[1];

    if (letra === 'E' || letra === 'P') {
      const destino = mapas[letra].get(codigo);
      if (!destino) {
        // R-EU-4: destino no registrado → selector con los existentes, nunca se crea.
        setFiltroDestino('');
        setSelectorDestino({ letra, codigo });
        return;
      }
      const etiqueta = letra === 'E' ? 'empleado' : 'proyecto';
      const esDevolucion = modoDevolucion && letra === 'E';
      abrirConDestino(
        letra,
        codigo,
        `Lote de ${esDevolucion ? 'devolución' : 'asignación'} para ${etiqueta} ${destino.nombre || codigo}.`
      );
      return;
    }

    agregarItem(letra, codigo);
  }

  function agregarItem(letra, codigo) {
    if (!loteActual) {
      setMensaje({
        tipo: 'error',
        texto: 'Escanee primero un destino (E# empleado o P# proyecto) o DV para devoluciones.',
      });
      return;
    }

    // R-EU-2: guard de tipo cruzado, sin cambio de estado.
    if (loteActual.tipo === 'DEVOLUCION' && letra !== 'H') {
      setMensaje({ tipo: 'error', texto: 'En una devolución solo se escanean herramientas (H#).' });
      return;
    }
    if (loteActual.tipo === 'ASIGNACION') {
      const esperada = loteActual.destinoCodigo.startsWith('E') ? 'H' : 'C';
      if (letra !== esperada) {
        setMensaje({
          tipo: 'error',
          texto:
            esperada === 'H'
              ? 'En este lote se escanean herramientas (H#); los consumibles (C#) no corresponden.'
              : 'En este lote se escanean consumibles (C#); las herramientas (H#) no corresponden.',
        });
        return;
      }
    }

    const item = mapas[letra].get(codigo);
    if (!item) {
      // R-EU-3: ítem desconocido → lote bloqueado y mini-form de creación express.
      const loteId = loteActual.id;
      setLoteActual((l) => {
        const yaPendiente = (l.pendientes || []).some((p) => p.codigo === codigo);
        return {
          ...l,
          items: { ...l.items, [codigo]: (l.items[codigo] || 0) + 1 },
          estado: 'pendiente',
          pendientes: yaPendiente
            ? l.pendientes
            : [...(l.pendientes || []), { codigo, motivo: 'ITEM_NO_REGISTRADO', mensaje: `Ítem no registrado: ${codigo}` }],
        };
      });
      abrirCreacion(codigo, loteId);
      setMensaje({ tipo: 'info', texto: `Código desconocido ${codigo}: el lote quedó pendiente de acomodar.` });
      return;
    }

    const duplicado = (loteActual.items[codigo] || 0) > 0;
    setLoteActual((l) => ({ ...l, items: { ...l.items, [codigo]: (l.items[codigo] || 0) + 1 } }));
    setMensaje(
      duplicado
        ? { tipo: 'info', texto: `${codigo} ya estaba en el lote: ahora ×${(loteActual.items[codigo] || 0) + 1}.` }
        : { tipo: 'ok', texto: `${codigo} agregado al lote.` }
    );
  }

  // --- Mini-form: creación express (nada automático) ---
  function abrirCreacion(codigo, loteId) {
    const lote =
      lotes.find((l) => l.id === loteId) ||
      (loteActual && loteActual.id === loteId ? loteActual : null);
    const escaneada = lote?.items?.[codigo] || 1;
    setFormCreacion({ nombre: '', marca: '', unidad: '', cantidadTotal: Math.max(1, escaneada) });
    setErroresForm(null);
    setCreacionExpress({ codigo, loteId });
  }

  async function confirmarCreacion(e) {
    e.preventDefault();
    setErroresForm(null);
    try {
      const esConsumible = creacionExpress.codigo.startsWith('C');
      const cuerpo = {
        tipo: esConsumible ? 'CONSUMIBLE' : 'HERRAMIENTA',
        codigo: creacionExpress.codigo,
        nombre: formCreacion.nombre,
        marca: formCreacion.marca,
      };
      if (esConsumible) {
        cuerpo.unidad = formCreacion.unidad;
      } else {
        cuerpo.cantidadTotal = Math.max(1, Number(formCreacion.cantidadTotal) || 1);
      }
      await post('/api/escaneos/items', cuerpo);
      const codigo = creacionExpress.codigo;
      const loteId = creacionExpress.loteId;
      setLotes((ls) =>
        ls.map((l) => {
          if (l.id !== loteId) return l;
          const pendientes = (l.pendientes || []).filter((p) => p.codigo !== codigo);
          return { ...l, pendientes, estado: pendientes.length === 0 ? 'cerrado' : 'pendiente' };
        })
      );
      if (loteActual && loteActual.id === loteId) {
        setLoteActual((l) => {
          if (!l) return l;
          const pendientes = (l.pendientes || []).filter((p) => p.codigo !== codigo);
          return { ...l, pendientes, estado: pendientes.length === 0 ? 'abierto' : 'pendiente' };
        });
      }
      setCreacionExpress(null);
      setMensaje({ tipo: 'ok', texto: `Ítem ${codigo} creado. Ya puede confirmar el lote.` });
    } catch (err) {
      setErroresForm([err.message]);
    }
  }

  // --- Mini-form: incrementar stock (nada automático) ---
  function abrirIncremento(codigo, loteId) {
    setFormIncremento({ cantidad: '' });
    setErroresForm(null);
    setIncrementoStock({ codigo, loteId });
  }

  async function confirmarIncremento(e) {
    e.preventDefault();
    setErroresForm(null);
    try {
      const cuerpo = {
        tipo: tipoDeCodigo(incrementoStock.codigo),
        codigo: incrementoStock.codigo,
        cantidad: Number(formIncremento.cantidad),
      };
      await post('/api/escaneos/incrementar-stock', cuerpo);
      const loteId = incrementoStock.loteId;
      setIncrementoStock(null);
      setMensaje({ tipo: 'ok', texto: `Stock de ${cuerpo.codigo} incrementado. Reconfirmando el lote…` });
      await confirmarLote(loteId);
    } catch (err) {
      setErroresForm([err.message]);
    }
  }

  // --- Confirmación por lote y de todos los lotes ---
  function loteAPayload(lote) {
    return {
      tipo: lote.tipo,
      destinoCodigo: lote.destinoCodigo,
      items: Object.entries(lote.items).map(([codigo, cantidad]) => {
        const asignaciones = lote.cuadrar?.[codigo];
        return asignaciones && asignaciones.length > 0
          ? { codigo, cantidad, asignaciones }
          : { codigo, cantidad };
      }),
    };
  }

  function abrirMiniFormasSegunResultado(lote) {
    if (!lote || !lote.resultado) return;
    const pendientes = lote.resultado.pendientes || [];
    if (pendientes.length > 0) {
      abrirCreacion(pendientes[0].codigo, lote.id);
      return;
    }
    const conStock = (lote.resultado.errores || []).find((e) =>
      ['STOCK_INSUFICIENTE', 'SIN_DISPONIBILIDAD'].includes(e.motivo)
    );
    if (conStock) abrirIncremento(conStock.codigo, lote.id);
  }

  function aplicarResultado(loteId, r) {
    const original = lotes.find((l) => l.id === loteId);
    if (!original) return null;
    const pendientes = r.pendientes || [];
    const actualizado = {
      ...original,
      estado: pendientes.length > 0 ? 'pendiente' : 'error',
      resultado: r,
      pendientes: pendientes.map((p) => ({ codigo: p.codigo, motivo: p.motivo, mensaje: p.mensaje })),
    };
    setLotes((ls) => ls.map((l) => (l.id === loteId ? actualizado : l)));
    return actualizado;
  }

  async function confirmarLote(loteId) {
    const lote = lotes.find((l) => l.id === loteId);
    if (!lote || enviando) return;
    setEnviando(true);
    setMensaje(null);
    try {
      const res = await post('/api/escaneos/lote', [loteAPayload(lote)]);
      const r = res && res[0];
      if (r && r.ok) {
        setLotes((ls) => ls.filter((l) => l.id !== loteId));
        setMensaje({ tipo: 'ok', texto: r.mensaje || `Lote procesado (${r.registrosCreados} registro(s)).` });
      } else if (r) {
        const actualizado = aplicarResultado(loteId, r);
        abrirMiniFormasSegunResultado(actualizado);
        setMensaje({ tipo: 'error', texto: r.mensaje || 'El lote no pudo procesarse.' });
      } else {
        setMensaje({ tipo: 'error', texto: 'Respuesta inesperada del servidor.' });
      }
    } catch (err) {
      setLotes((ls) => ls.map((l) => (l.id === loteId ? { ...l, estado: 'error' } : l)));
      setMensaje({ tipo: 'error', texto: err.message });
    } finally {
      setEnviando(false);
    }
  }

  async function confirmarTodos() {
    const aConfirmar = lotes.filter((l) => l.estado !== 'aprobado');
    if (aConfirmar.length === 0 || enviando) return;
    setEnviando(true);
    setMensaje(null);
    try {
      const res = await post('/api/escaneos/lote', aConfirmar.map(loteAPayload));
      let okCount = 0;
      let primerFallido = null;
      res.forEach((r, i) => {
        const lote = aConfirmar[i];
        if (!r || !lote) return;
        if (r.ok) {
          okCount += 1;
          setLotes((ls) => ls.filter((l) => l.id !== lote.id));
        } else if (!primerFallido) {
          primerFallido = aplicarResultado(lote.id, r);
        } else {
          aplicarResultado(lote.id, r);
        }
      });
      if (primerFallido) abrirMiniFormasSegunResultado(primerFallido);
      setMensaje({
        tipo: okCount > 0 ? 'ok' : 'error',
        texto: `${okCount} de ${res.length} lote(s) confirmado(s) correctamente.`,
      });
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    } finally {
      setEnviando(false);
    }
  }

  function quitarLote(id) {
    setLotes((ls) => ls.filter((l) => l.id !== id));
  }

  function limpiar() {
    setLotes([]);
    setLoteActual(null);
    setModoDevolucion(false);
    setSelectorDestino(null);
    setCreacionExpress(null);
    setIncrementoStock(null);
    setCuadrar(null);
    setMensaje(null);
    setEntrada('');
  }

  // --- Selector de destino no registrado (R-EU-4) ---
  const opcionesDestino = useMemo(() => {
    if (!selectorDestino) return [];
    const lista = selectorDestino.letra === 'E' ? empleados || [] : proyectos || [];
    const f = filtroDestino.trim().toLowerCase();
    if (!f) return lista;
    return lista.filter(
      (x) =>
        (x.nombre || '').toLowerCase().includes(f) || (x.codigo || '').toLowerCase().includes(f)
    );
  }, [selectorDestino, filtroDestino, empleados, proyectos]);

  function elegirDestino(opcion) {
    abrirConDestino(
      selectorDestino.letra,
      opcion.codigo,
      `Destino elegido: ${opcion.nombre} (${opcion.codigo}).`
    );
    setSelectorDestino(null);
    setFiltroDestino('');
  }

  // --- Modal 'Cuadrar devoluciones' (R-EU-9, opcional) ---
  const asignacionesCuadrar = useMemo(() => {
    if (!cuadrar) return [];
    const lote = lotes.find((l) => l.id === cuadrar.loteId);
    const empleado = lote ? mapas.E.get(lote.destinoCodigo) : null;
    return (asignacionesHerramientas || []).filter(
      (a) =>
        empleado &&
        a.empleado?.id === empleado.id &&
        a.herramienta?.codigo === cuadrar.codigo &&
        (a.cantidad ?? 0) > 0
    );
  }, [cuadrar, lotes, asignacionesHerramientas, mapas]);

  function abrirCuadrar(loteId, codigo) {
    const lote = lotes.find((l) => l.id === loteId);
    const empleado = lote ? mapas.E.get(lote.destinoCodigo) : null;
    const abiertas = (asignacionesHerramientas || []).filter(
      (a) =>
        empleado &&
        a.empleado?.id === empleado.id &&
        a.herramienta?.codigo === codigo &&
        (a.cantidad ?? 0) > 0
    );
    const previas = lote?.cuadrar?.[codigo] || [];
    const iniciales = {};
    abiertas.forEach((a) => {
      const previa = previas.find((p) => p.id === a.id);
      iniciales[a.id] = previa ? previa.cantidad : '';
    });
    setErroresCuadrar(null);
    setFormCuadrar(iniciales);
    setCuadrar({ loteId, codigo });
  }

  function guardarCuadrar(e) {
    e.preventDefault();
    const lote = lotes.find((l) => l.id === cuadrar.loteId);
    if (!lote) return;
    const escaneada = lote.items[cuadrar.codigo] || 0;
    const asignaciones = Object.entries(formCuadrar)
      .map(([id, cantidad]) => ({ id: Number(id), cantidad: Number(cantidad) }))
      .filter((a) => a.cantidad > 0);
    const suma = asignaciones.reduce((acc, a) => acc + a.cantidad, 0);
    if (suma !== escaneada) {
      setErroresCuadrar([
        `La suma cuadrada (${suma}) debe ser igual a la cantidad escaneada (${escaneada}).`,
      ]);
      return;
    }
    setLotes((ls) =>
      ls.map((l) =>
        l.id === cuadrar.loteId
          ? { ...l, cuadrar: { ...(l.cuadrar || {}), [cuadrar.codigo]: asignaciones } }
          : l
      )
    );
    setCuadrar(null);
    setMensaje({ tipo: 'ok', texto: `Devoluciones cuadradas para ${cuadrar.codigo}.` });
  }

  const etiquetaEntrada = !loteActual
    ? modoDevolucion
      ? 'Escanee el empleado para la devolución (E#)'
      : 'Escanee el destino (E# empleado / P# proyecto) o DV'
    : 'Escanee ítems (H# / C#) y luego FIN';

  const erroresDe = (lote) =>
    lote.resultado && !lote.resultado.ok ? lote.resultado.errores || [] : [];

  return (
    <section>
      <SeccionTabs items={TABS_INVENTARIO} />
      <div className="pagina-cabecera">
        <h2>Escaneo de códigos</h2>
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={limpiar}>
            Reiniciar
          </button>
          <button
            type="button"
            className="btn btn-primario"
            onClick={confirmarTodos}
            disabled={lotes.length === 0 || enviando}
          >
            {enviando ? 'Procesando…' : `Confirmar todos (${lotes.length})`}
          </button>
        </div>
      </div>

      <div className="escaneo-entrada">
        <input
          autoFocus
          type="text"
          value={entrada}
          placeholder={etiquetaEntrada}
          onChange={(e) => setEntrada(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              procesarCodigo(entrada);
              setEntrada('');
            }
          }}
        />
        <button
          type="button"
          className="btn btn-borde"
          onClick={() => {
            procesarCodigo(entrada);
            setEntrada('');
          }}
        >
          Escanear
        </button>
      </div>

      <div className="escaneo-atajos">
        {['FIN', 'DV'].map((c) => (
          <button
            key={c}
            type="button"
            className="btn btn-borde btn-mini"
            onClick={() => procesarCodigo(c)}
          >
            {c}
          </button>
        ))}
      </div>

      {mensaje && <p className={`escaneo-mensaje ${mensaje.tipo}`}>{mensaje.texto}</p>}

      <div className="escaneo-bloque-actual">
        <strong>Lote actual:</strong>{' '}
        {loteActual ? (
          <>
            <Badge tipo={loteActual.tipo === 'DEVOLUCION' ? 'amarillo' : 'azul'}>
              {ETIQUETA_TIPO[loteActual.tipo]}
            </Badge>
            <span>
              → {loteActual.destinoCodigo} · {nombreDestino(loteActual)}
            </span>
            {Object.entries(loteActual.items).map(([codigo, cant]) => (
              <span key={codigo} className="chip">
                {codigo} · {nombreItem(codigo)} ×{cant}
              </span>
            ))}
            {loteActual.estado === 'pendiente' && (
              <Badge tipo="amarillo">Pendiente de acomodar</Badge>
            )}
          </>
        ) : (
          <span className="sin-dato">
            {modoDevolucion ? 'devolución armada: escanee el empleado…' : 'sin lote abierto'}
          </span>
        )}
      </div>

      {lotes.length > 0 && (
        <div className="escaneo-bloques">
          <h3>Lotes escaneados</h3>
          {lotes.map((lote, i) => {
            const errores = erroresDe(lote);
            const erroresStock = errores.filter((e) =>
              ['STOCK_INSUFICIENTE', 'SIN_DISPONIBILIDAD'].includes(e.motivo)
            );
            return (
              <div key={lote.id} className="bloque-card">
                <div className="bloque-card-cabecera">
                  <strong>
                    Lote {i + 1} — {ETIQUETA_TIPO[lote.tipo]} → {lote.destinoCodigo} ·{' '}
                    {nombreDestino(lote)}
                  </strong>
                  <div className="acciones">
                    <Badge
                      tipo={
                        lote.estado === 'pendiente'
                          ? 'amarillo'
                          : lote.estado === 'error'
                          ? 'rojo'
                          : 'azul'
                      }
                    >
                      {ETIQUETA_ESTADO[lote.estado] || lote.estado}
                    </Badge>
                    <button
                      type="button"
                      className="btn-icono"
                      onClick={() => quitarLote(lote.id)}
                      aria-label="Quitar lote"
                    >
                      &times;
                    </button>
                  </div>
                </div>
                <div className="bloque-items">
                  {Object.entries(lote.items).map(([codigo, cant]) => (
                    <span
                      key={codigo}
                      style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}
                    >
                      <span className="chip">
                        {codigo} · {nombreItem(codigo)} ×{cant}
                      </span>
                      {lote.tipo === 'DEVOLUCION' && (
                        <button
                          type="button"
                          className="btn btn-borde btn-mini"
                          onClick={() => abrirCuadrar(lote.id, codigo)}
                          title="Elegir manualmente qué asignación cierra esta devolución"
                        >
                          Cuadrar
                        </button>
                      )}
                    </span>
                  ))}
                </div>
                {(lote.pendientes || []).map((p) => (
                  <p key={p.codigo} className="escaneo-mensaje info">
                    <Badge tipo="amarillo">{MOTIVO_ETIQUETA[p.motivo] || p.motivo}</Badge>{' '}
                    {p.codigo} — {p.mensaje}{' '}
                    <button
                      type="button"
                      className="btn btn-borde btn-mini"
                      onClick={() => abrirCreacion(p.codigo, lote.id)}
                    >
                      Crear ítem {p.codigo}
                    </button>
                  </p>
                ))}
                {lote.resultado && (
                  <div className={`bloque-resultado ${lote.resultado.ok ? 'ok' : 'error'}`}>
                    <strong>
                      {lote.resultado.ok ? '✓' : '✗'} {lote.resultado.mensaje}
                    </strong>
                    {lote.resultado.ok && (
                      <span> · {lote.resultado.registrosCreados} registro(s)</span>
                    )}
                  </div>
                )}
                {errores.length > 0 && (
                  <div className="bloque-items">
                    {errores.map((err) => (
                      <span key={`${err.codigo}-${err.motivo}`} className="chip">
                        {err.codigo} · {MOTIVO_ETIQUETA[err.motivo] || err.motivo}: {err.mensaje}
                      </span>
                    ))}
                  </div>
                )}
                <div className="bloque-destino">
                  {erroresStock.length > 0 && (
                    <button
                      type="button"
                      className="btn btn-borde"
                      onClick={() => abrirIncremento(erroresStock[0].codigo, lote.id)}
                    >
                      Incrementar stock
                    </button>
                  )}{' '}
                  <button
                    type="button"
                    className="btn btn-primario"
                    onClick={() => confirmarLote(lote.id)}
                    disabled={enviando}
                  >
                    Confirmar lote
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      <Modal
        titulo={`Destino no registrado: ${selectorDestino?.codigo || ''}`}
        abierto={!!selectorDestino}
        onCerrar={() => {
          setSelectorDestino(null);
          setFiltroDestino('');
        }}
      >
        <p className="texto-aviso">
          El código {selectorDestino?.codigo} no existe. Elija un{' '}
          {selectorDestino?.letra === 'E' ? 'empleado' : 'proyecto'} de la lista (no se crea nada).
        </p>
        <div className="campo">
          <input
            type="search"
            placeholder="Filtrar por nombre o código…"
            value={filtroDestino}
            onChange={(e) => setFiltroDestino(e.target.value)}
            autoFocus
          />
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 8 }}>
          {opcionesDestino.map((x) => (
            <button key={x.id} type="button" className="btn btn-borde" onClick={() => elegirDestino(x)}>
              {x.codigo} — {x.nombre}
            </button>
          ))}
          {opcionesDestino.length === 0 && <p className="sin-dato">Sin resultados.</p>}
        </div>
      </Modal>

      <Modal
        titulo={`Creación express — ${creacionExpress?.codigo || ''}`}
        abierto={!!creacionExpress}
        onCerrar={() => setCreacionExpress(null)}
      >
        <form className="form" onSubmit={confirmarCreacion}>
          <p className="texto-aviso">
            El código {creacionExpress?.codigo} no está registrado. Complete el formulario para
            crearlo; después podrá confirmar el lote.
          </p>
          <div className="campo">
            <label>Nombre *</label>
            <input
              type="text"
              value={formCreacion.nombre}
              onChange={(e) => setFormCreacion({ ...formCreacion, nombre: e.target.value })}
              required
            />
          </div>
          <div className="campo">
            <label>Marca</label>
            <input
              type="text"
              value={formCreacion.marca}
              onChange={(e) => setFormCreacion({ ...formCreacion, marca: e.target.value })}
            />
          </div>
          {creacionExpress?.codigo?.startsWith('C') ? (
            <div className="campo">
              <label>Unidad *</label>
              <input
                type="text"
                value={formCreacion.unidad}
                placeholder="ej. bulto, unidad, litro"
                onChange={(e) => setFormCreacion({ ...formCreacion, unidad: e.target.value })}
                required
              />
            </div>
          ) : (
            <div className="campo">
              <label>Cantidad total *</label>
              <input
                type="number"
                min="1"
                step="1"
                value={formCreacion.cantidadTotal}
                onChange={(e) => setFormCreacion({ ...formCreacion, cantidadTotal: e.target.value })}
                required
              />
            </div>
          )}
          <Microsofto errores={erroresForm} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setCreacionExpress(null)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              Crear ítem
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        titulo={`Incrementar stock — ${incrementoStock?.codigo || ''}`}
        abierto={!!incrementoStock}
        onCerrar={() => setIncrementoStock(null)}
      >
        <form className="form" onSubmit={confirmarIncremento}>
          <p className="texto-aviso">
            No hay stock o disponibilidad suficiente para {incrementoStock?.codigo}. Si las
            unidades físicas fueron entregadas, registre el incremento manualmente.
          </p>
          <div className="campo">
            <label>Cantidad a sumar *</label>
            <input
              type="number"
              min={incrementoStock?.codigo?.startsWith('C') ? '0.1' : '1'}
              step={incrementoStock?.codigo?.startsWith('C') ? '0.1' : '1'}
              value={formIncremento.cantidad}
              onChange={(e) => setFormIncremento({ ...formIncremento, cantidad: e.target.value })}
              required
            />
          </div>
          <Microsofto errores={erroresForm} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setIncrementoStock(null)}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primario">
              Incrementar y reconfirmar
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        titulo={`Cuadrar devoluciones — ${cuadrar?.codigo || ''}`}
        abierto={!!cuadrar}
        onCerrar={() => setCuadrar(null)}
        ancho={680}
      >
        <p className="texto-aviso">
          Distribuya manualmente la devolución de {cuadrar?.codigo} entre las asignaciones abiertas
          del empleado. Si no cuadra nada, se usa el orden de entrega (FIFO).
        </p>
        <form className="form" onSubmit={guardarCuadrar}>
          {asignacionesCuadrar.length === 0 ? (
            <p className="sin-dato">
              No hay asignaciones abiertas de {cuadrar?.codigo} para este empleado.
            </p>
          ) : (
            <div className="form-grid">
              {asignacionesCuadrar.map((a) => (
                <div key={a.id} className="campo">
                  <label>
                    Asignación #{a.id} — {a.fecha} (+{a.cantidad})
                  </label>
                  <input
                    type="number"
                    min="0"
                    step="1"
                    value={formCuadrar[a.id] ?? ''}
                    placeholder="cantidad a devolver"
                    onChange={(e) => setFormCuadrar({ ...formCuadrar, [a.id]: e.target.value })}
                  />
                </div>
              ))}
            </div>
          )}
          <Microsofto errores={erroresCuadrar} />
          <div className="form-acciones">
            <button type="button" className="btn btn-borde" onClick={() => setCuadrar(null)}>
              Cancelar
            </button>
            <button
              type="submit"
              className="btn btn-primario"
              disabled={asignacionesCuadrar.length === 0}
            >
              Guardar cuadre
            </button>
          </div>
        </form>
      </Modal>
    </section>
  );
}