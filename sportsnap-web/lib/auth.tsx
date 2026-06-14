"use client";

import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";

type Role = "atleta" | "fotografo";

export type Sessao = {
  role: Role;
  id: number;
  nome: string;
  email: string;
};

type AuthContextValue = {
  sessao: Sessao | null;
  carregando: boolean;
  login: (s: Sessao) => void;
  logout: () => void;
};

const STORAGE_KEY = "sportsnap.sessao";
const AuthCtx = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) setSessao(JSON.parse(raw) as Sessao);
    } catch {}
    setCarregando(false);
  }, []);

  const login = useCallback((s: Sessao) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(s));
    setSessao(s);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    setSessao(null);
  }, []);

  return (
    <AuthCtx.Provider value={{ sessao, carregando, login, logout }}>
      {children}
    </AuthCtx.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthCtx);
  if (!ctx) throw new Error("useAuth fora de AuthProvider");
  return ctx;
}
