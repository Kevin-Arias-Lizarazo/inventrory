import { useCallback, useEffect, useRef, useState } from 'react';
import { get } from './api';
import { useEventos } from './eventos-contexto';

export function useLista(recurso, url) {
  const { suscribir } = useEventos();
  const [lista, setLista] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const ultimaRef = useRef(0);

  const recargar = useCallback(
    async ({ silencioso = false } = {}) => {
      ultimaRef.current = Date.now();
      if (!silencioso) setCargando(true);
      try {
        setLista(await get(url));
        setError(null);
      } catch (e) {
        if (!silencioso) setError(e.message);
      } finally {
        if (!silencioso) setCargando(false);
      }
    },
    [url]
  );

  useEffect(() => {
    recargar();
  }, [recargar]);

  useEffect(
    () =>
      suscribir(recurso, () => {
        if (Date.now() - ultimaRef.current < 400) return;
        recargar({ silencioso: true });
      }),
    [recurso, suscribir, recargar]
  );

  return { lista, cargando, error, recargar };
}

export function useListaPaginada(recurso, url, tamanoInicial = 30) {
  const { suscribir } = useEventos();
  const [lista, setLista] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [pagina, setPagina] = useState(0);
  const [tamano, setTamano] = useState(tamanoInicial);
  const [total, setTotal] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const ultimaRef = useRef(0);
  const urlRef = useRef(url);

  useEffect(() => {
    if (urlRef.current !== url) {
      urlRef.current = url;
      setPagina(0);
    }
  }, [url]);

  const cargar = useCallback(
    async (p, t, { silencioso = false } = {}) => {
      ultimaRef.current = Date.now();
      if (!silencioso) setCargando(true);
      const sep = url.includes('?') ? '&' : '?';
      try {
        const d = await get(`${url}${sep}pagina=${p}&tamano=${t}`);
        setLista(d.contenido);
        setTotal(d.total);
        setTotalPaginas(d.totalPaginas);
        setError(null);
      } catch (e) {
        if (!silencioso) setError(e.message);
      } finally {
        if (!silencioso) setCargando(false);
      }
    },
    [url]
  );

  useEffect(() => {
    cargar(pagina, tamano);
  }, [cargar, pagina, tamano]);

  useEffect(
    () =>
      suscribir(recurso, () => {
        if (Date.now() - ultimaRef.current < 400) return;
        cargar(pagina, tamano, { silencioso: true });
      }),
    [recurso, suscribir, pagina, tamano, cargar]
  );

  return {
    lista,
    cargando,
    error,
    pagina,
    tamano,
    total,
    totalPaginas,
    setPagina,
    setTamano,
    recargar: () => cargar(pagina, tamano),
  };
}

export function useDebounce(valor, ms = 300) {
  const [deb, setDeb] = useState(valor);
  useEffect(() => {
    const id = setTimeout(() => setDeb(valor), ms);
    return () => clearTimeout(id);
  }, [valor, ms]);
  return deb;
}