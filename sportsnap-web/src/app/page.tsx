"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useSession } from "@/lib/session";
import { api } from "@/lib/api";

export default function LoginPage() {
  const [tipo, setTipo] = useState<"ATLETA" | "FOTOGRAFO" | null>(null);
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const { setSession } = useSession();

  const handleLogin = async () => {
    if (!tipo || !nome || !email) return;
    setLoading(true);
    try {
      let data;
      if (tipo === "ATLETA") {
        data = await api.criarAtleta(nome, email);
      } else {
        data = await api.criarFotografo(nome, email);
      }
      const id = data?.id || 1;
      setSession(tipo, id, nome);
      router.push(tipo === "ATLETA" ? "/atleta/dashboard" : "/fotografo/dashboard");
    } catch {
      setSession(tipo, 1, nome);
      router.push(tipo === "ATLETA" ? "/atleta/dashboard" : "/fotografo/dashboard");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-dark-900">
      <div className="w-full max-w-md p-8">
        <div className="text-center mb-10">
          <h1 className="text-5xl font-bold mb-3">
            Sport<span className="text-brand">Snap</span>
          </h1>
          <p className="text-gray-400">
            Performance esportiva + Fotografia profissional
          </p>
        </div>

        <div className="space-y-6">
          <div className="flex gap-3">
            <button
              onClick={() => setTipo("ATLETA")}
              className={`flex-1 py-4 rounded-xl border-2 transition-all font-semibold ${
                tipo === "ATLETA"
                  ? "border-brand bg-brand/10 text-brand"
                  : "border-dark-600 text-gray-400 hover:border-gray-500"
              }`}
            >
              Sou Atleta
            </button>
            <button
              onClick={() => setTipo("FOTOGRAFO")}
              className={`flex-1 py-4 rounded-xl border-2 transition-all font-semibold ${
                tipo === "FOTOGRAFO"
                  ? "border-brand bg-brand/10 text-brand"
                  : "border-dark-600 text-gray-400 hover:border-gray-500"
              }`}
            >
              Sou Fotografo
            </button>
          </div>

          <input
            type="text"
            placeholder="Nome"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            className="w-full px-4 py-3 rounded-xl bg-dark-700 border border-dark-600 text-white placeholder-gray-500 focus:border-brand focus:outline-none transition-colors"
          />

          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full px-4 py-3 rounded-xl bg-dark-700 border border-dark-600 text-white placeholder-gray-500 focus:border-brand focus:outline-none transition-colors"
          />

          <button
            onClick={handleLogin}
            disabled={!tipo || !nome || !email || loading}
            className="w-full py-4 rounded-xl bg-brand text-dark-900 font-bold text-lg hover:bg-brand/90 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
          >
            {loading ? "Entrando..." : "Entrar / Cadastrar"}
          </button>
        </div>
      </div>
    </div>
  );
}
