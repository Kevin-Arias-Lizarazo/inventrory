import { createContext, useContext } from 'react';

export const AuthContexto = createContext(null);

export function useAuth() {
  const ctx = useContext(AuthContexto);
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProveedor>');
  return ctx;
}