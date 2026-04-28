"use client";

import { useEffect, useState } from "react";
import { useSession } from "@/lib/session";
import { api } from "@/lib/api";

export default function DashboardAtleta() {
  const { userId, userName } = useSession();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [carta, setCarta] = useState<any>(null);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [shadow, setShadow] = useState<any>(null);
  const [syncing, setSyncing] = useState(false);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    if (!userId) return;
    api.buscarCarta(userId).then(setCarta).catch(() => {});
    api.buscarStatusPotencial(userId).then(setShadow).catch(() => {});
  }, [userId]);

  const handleSync = async () => {
    if (!userId) return;
    setSyncing(true);
    setMsg("");
    try {
      await api.sincronizarCarta(userId);
      const c = await api.buscarCarta(userId);
      setCarta(c);
      setMsg(`Novo Overall: ${c?.overall || "?"}`);
    } catch {
      setMsg("Erro ao sincronizar. Verifique se possui licenca valida.");
    } finally {
      setSyncing(false);
    }
  };

  return (
    <div className="space-y-8">
      <h1 className="text-3xl font-bold">Dashboard</h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-dark-700 rounded-2xl p-6 border border-dark-600 hover:border-brand/30 transition-colors">
          <h2 className="text-sm uppercase tracking-wider text-gray-400 mb-4">Carta Oficial</h2>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xl font-semibold">{userName}</p>
              <p className="text-gray-400 text-sm mt-1">Atleta</p>
            </div>
            <div className="text-right">
              <p className="text-5xl font-black text-brand">{carta?.overall ?? "—"}</p>
              <p className="text-xs text-gray-400 mt-1">OVERALL</p>
            </div>
          </div>
          {carta?.ultimaSincronizacao && (
            <p className="text-xs text-gray-500 mt-4">
              Ultima sync: {new Date(carta.ultimaSincronizacao).toLocaleString("pt-BR")}
            </p>
          )}
        </div>

        <div className="bg-dark-700 rounded-2xl p-6 border border-dark-600 hover:border-brand/30 transition-colors">
          <h2 className="text-sm uppercase tracking-wider text-gray-400 mb-4">Shadow Stats</h2>
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <span className="text-gray-300">XP Acumulado</span>
              <span className="text-2xl font-bold text-brand">{shadow?.xpAcumulado ?? 0}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-300">Streak de Consistencia</span>
              <span className="text-2xl font-bold text-brand">{shadow?.streakDeConsistencia ?? 0} dias</span>
            </div>
          </div>
        </div>
      </div>

      <div>
        <button
          onClick={handleSync}
          disabled={syncing}
          className="px-8 py-4 rounded-xl bg-brand text-dark-900 font-bold text-lg hover:bg-brand/90 disabled:opacity-40 transition-all"
        >
          {syncing ? "Sincronizando..." : "Sincronizar Carta (Reveal)"}
        </button>
        {msg && <p className="mt-3 text-brand font-medium">{msg}</p>}
      </div>
    </div>
  );
}
