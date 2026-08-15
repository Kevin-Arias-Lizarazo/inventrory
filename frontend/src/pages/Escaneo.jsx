import { useMemo, useState } from 'react';
import { post } from '../api';
import { useLista } from '../hooks';
import { Badge } from '../components/ui';

const ETIQUETA_OPERACION = {
  AH: 'Asignar herramienta',
  DH: 'Devolver herramienta',
  AC: 'Asignar consumible',
};

const estadoInicial = () => ({
  operacion: null,
  items: {},
  destinoCodigo: null,
});

export default function Escaneo() {
  const { lista: herramientas } = useLista('herramientas', '/api/herramientas');
  const { lista: consumibles } = useLista('consumibles', '/api/consumibles');
  const { lista: empleados } = useLista('empleados', '/api/empleados');
  const { lista: proyectos } = useLista('proyectos', '/api/proyectos');

  const mapas = useMemo(() => {
    const porCodigo = (arr) => new Map((arr || []).map((x) => [x.codigo, x]));
    return {
      H: porCodigo(herramientas),
      C: porCodigo(consumibles),
      E: porCodigo(empleados),
      P: porCodigo(proyectos),
    };
  }, [herramientas, consumibles, empleados, proyectos]);

  const [bloques, setBloques] = useState([]);
  const [actual, setActual] = useState(estadoInicial);
  const [etapa, setEtapa] = useState('operacion');
  const [entrada, setEntrada] = useState('');
  const [mensaje, setMensaje] = useState(null);
  const [resultado, setResultado] = useState(null);
  const [enviando, setEnviando] = useState(false);

  function buscar(etiqueta, codigo) {
    const lista = mapas[etiqueta];
    return lista ? lista.get(codigo) : null;
  }

  function procesarCodigo(raw) {
    const codigo = (raw || '').trim().toUpperCase();
    if (!codigo) return;
    setMensaje(null);

    if (etapa === 'operacion') {
      if (!['AH', 'DH', 'AC'].includes(codigo)) {
        setMensaje({ tipo: 'error', texto: 'Código de operación inválido. Use AH, DH o AC.' });
        return;
      }
      setActual({ ...estadoInicial(), operacion: codigo });
      setEtapa('items');
      return;
    }

    if (etapa === 'items') {
      if (codigo === 'FIN') {
        if (Object.keys(actual.items).length === 0) {
          setMensaje({ tipo: 'info', texto: 'FIN sin ítems: se ignora.' });
          return;
        }
        setEtapa('destino');
        return;
      }
      const letra = actual.operacion === 'AC' ? 'C' : 'H';
      if (!new RegExp(`^${letra}\\d+$`).test(codigo)) {
        setMensaje({ tipo: 'error', texto: `Aquí van códigos de ${letra === 'C' ? 'consumibles' : 'herramientas'} (${letra}#).` });
        return;
      }
      const item = buscar(letra, codigo);
      if (!item) {
        setMensaje({ tipo: 'error', texto: `Código desconocido: ${codigo}` });
        return;
      }
      setActual((a) => ({ ...a, items: { ...a.items, [codigo]: (a.items[codigo] || 0) + 1 } }));
      return;
    }

    if (etapa === 'destino') {
      if (codigo === 'FIN') return;
      const letra = actual.operacion === 'AC' ? 'P' : 'E';
      if (!new RegExp(`^${letra}\\d+$`).test(codigo)) {
        setMensaje({ tipo: 'error', texto: `El destino debe ser ${letra === 'P' ? 'un proyecto (P#)' : 'un empleado (E#)'}.` });
        return;
      }
      const destino = buscar(letra, codigo);
      if (!destino) {
        setMensaje({ tipo: 'error', texto: `Código de destino desconocido: ${codigo}` });
        return;
      }
      const bloque = { operacion: actual.operacion, items: actual.items, destinoCodigo: codigo };
      setBloques((b) => [...b, bloque]);
      setActual(estadoInicial());
      setEtapa('operacion');
      setMensaje({ tipo: 'ok', texto: `Bloque agregado: ${ETIQUETA_OPERACION[bloque.operacion]} → ${destino.nombre}` });
      return;
    }
  }

  function quitarBloque(indice) {
    setBloques((b) => b.filter((_, i) => i !== indice));
  }

  async function confirmar() {
    if (bloques.length === 0) return;
    setEnviando(true);
    setResultado(null);
    setMensaje(null);
    try {
      const payload = bloques.map((b) => ({
        operacion: b.operacion,
        destinoCodigo: b.destinoCodigo,
        items: Object.entries(b.items).map(([codigo, cantidad]) => ({ codigo, cantidad })),
      }));
      const res = await post('/api/escaneos', payload);
      setResultado(res);
      const todoOk = res.every((r) => r.ok);
      if (todoOk) {
        setBloques([]);
        setActual(estadoInicial());
        setEtapa('operacion');
      }
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    } finally {
      setEnviando(false);
    }
  }

  function limpiar() {
    setBloques([]);
    setActual(estadoInicial());
    setEtapa('operacion');
    setResultado(null);
    setMensaje(null);
  }

  const nombreDestino = (b) => {
    const letra = b.operacion === 'AC' ? 'P' : 'E';
    const d = buscar(letra, b.destinoCodigo);
    return d ? d.nombre : b.destinoCodigo;
  };

  const etiquetaEtapa =
    etapa === 'operacion'
      ? 'Escanee la operación (AH / DH / AC)'
      : etapa === 'items'
      ? 'Escanee los ítems y luego FIN'
      : 'Escanee el destino (empleado o proyecto)';

  return (
    <section>
      <div className="pagina-cabecera">
        <h2>Escaneo de códigos</h2>
        <div className="acciones">
          <button type="button" className="btn btn-borde" onClick={limpiar}>
            Reiniciar
          </button>
          <button
            type="button"
            className="btn btn-primario"
            onClick={confirmar}
            disabled={bloques.length === 0 || enviando}
          >
            {enviando ? 'Procesando…' : `Confirmar lote (${bloques.length})`}
          </button>
        </div>
      </div>

      <div className="escaneo-entrada">
        <input
          autoFocus
          type="text"
          value={entrada}
          placeholder={etiquetaEtapa}
          onChange={(e) => setEntrada(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              procesarCodigo(entrada);
              setEntrada('');
            }
          }}
        />
        <button type="button" className="btn btn-borde" onClick={() => { procesarCodigo(entrada); setEntrada(''); }}>
          Escanear
        </button>
      </div>

      <div className="escaneo-atajos">
        {['AH', 'DH', 'AC', 'FIN'].map((c) => (
          <button key={c} type="button" className="btn btn-borde btn-mini" onClick={() => procesarCodigo(c)}>
            {c}
          </button>
        ))}
      </div>

      {mensaje && <p className={`escaneo-mensaje ${mensaje.tipo}`}>{mensaje.texto}</p>}

      <div className="escaneo-bloque-actual">
        <strong>Bloque actual:</strong>{' '}
        {actual.operacion ? (
          <>
            <Badge tipo="azul">{ETIQUETA_OPERACION[actual.operacion]}</Badge>
            {Object.entries(actual.items).map(([codigo, cant]) => {
              const letra = actual.operacion === 'AC' ? 'C' : 'H';
              const item = buscar(letra, codigo);
              return (
                <span key={codigo} className="chip">
                  {codigo} · {item?.nombre || '?'} ×{cant}
                </span>
              );
            })}
            <span className="sin-dato">{etapa === 'destino' ? '→ esperando destino…' : ''}</span>
          </>
        ) : (
          <span className="sin-dato">sin operación</span>
        )}
      </div>

      {bloques.length > 0 && (
        <div className="escaneo-bloques">
          <h3>Visualización del lote</h3>
          {bloques.map((b, i) => {
            const letra = b.operacion === 'AC' ? 'C' : 'H';
            return (
              <div key={i} className="bloque-card">
                <div className="bloque-card-cabecera">
                  <strong>Bloque {i + 1} — {ETIQUETA_OPERACION[b.operacion]}</strong>
                  <button type="button" className="btn-icono" onClick={() => quitarBloque(i)} aria-label="Quitar">
                    &times;
                  </button>
                </div>
                <div className="bloque-items">
                  {Object.entries(b.items).map(([codigo, cant]) => {
                    const item = buscar(letra, codigo);
                    return (
                      <span key={codigo} className="chip">
                        {codigo} · {item?.nombre || '?'} ×{cant}
                      </span>
                    );
                  })}
                </div>
                <div className="bloque-destino">
                  Destino: <strong>{b.destinoCodigo}</strong> · {nombreDestino(b)}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {resultado && (
        <div className="escaneo-resultado">
          <h3>Resultado por bloque</h3>
          {resultado.map((r, i) => (
            <div key={i} className={`bloque-resultado ${r.ok ? 'ok' : 'error'}`}>
              <strong>Bloque {i + 1}</strong> — {ETIQUETA_OPERACION[r.operacion] || r.operacion} → {r.destinoCodigo}:
              {r.ok ? ` ✓ ${r.mensaje}` : ` ✗ ${r.mensaje}`}
            </div>
          ))}
        </div>
      )}
    </section>
  );
}