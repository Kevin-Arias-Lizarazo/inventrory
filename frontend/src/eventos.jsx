import { useCallback, useEffect, useMemo, useRef } from 'react';
import { EventosContexto } from './eventos-contexto';

export function EventosProveedor({ children }) {
  const subsRef = useRef([]);

  useEffect(() => {
    const es = new EventSource('/api/cambios/suscripcion');
    es.onmessage = (ev) => {
      try {
        const d = JSON.parse(ev.data);
        if (!d.recurso) return;
        const subs = subsRef.current;
        for (const s of subs) {
          if (s.recurso === d.recurso) s.fn();
        }
      } catch {
        /* ignorar */
      }
    };
    es.onerror = () => {};
    return () => es.close();
  }, []);

  const suscribir = useCallback((recurso, fn) => {
    const recursos = Array.isArray(recurso) ? recurso : [recurso];
    const registros = recursos.map((r) => ({ recurso: r, fn }));
    subsRef.current.push(...registros);
    return () => {
      subsRef.current = subsRef.current.filter((s) => !registros.includes(s));
    };
  }, []);

  const valor = useMemo(() => ({ suscribir }), [suscribir]);
  return <EventosContexto.Provider value={valor}>{children}</EventosContexto.Provider>;
}