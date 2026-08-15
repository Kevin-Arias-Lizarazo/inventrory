import { createContext, useContext } from 'react';

export const EventosContexto = createContext(null);

export function useEventos() {
  const ctx = useContext(EventosContexto);
  if (!ctx) throw new Error('useEventos debe usarse dentro de <EventosProveedor>');
  return ctx;
}