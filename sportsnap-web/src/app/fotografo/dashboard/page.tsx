"use client";

import { useEffect, useState } from "react";
import { useSession } from "@/lib/session";
import { api } from "@/lib/api";

export default function DashboardFotografo() {
  const { userId, userName } = useSession();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [lotes, setLotes] = useState<any[]>([]);

  useEffect(() => {
    if (!userId) return;
    api.listarLotes(userId).then(setLotes).catch(() => setLotes([]));
  }, [userId]);

  return (
    <div className="space-y-8">
      <h1 className="text-3xl font-bold">Dashboard</h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-dark-700 rounded-2xl p-6 border border-dark-600">
          <h2 className="text-sm uppercase tracking-wider text-gray-400 mb-4">Resumo Financeiro</h2>
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <span className="text-gray-300">Vendas este mes</span>
              <span className="text-2xl font-bold text-brand">R$ 0,00</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-300">Saldo disponivel</span>
              <span className="text-2xl font-bold text-brand">R$ 0,00</span>
            </div>
          </div>
        </div>

        <div className="bg-dark-700 rounded-2xl p-6 border border-dark-600">
          <h2 className="text-sm uppercase tracking-wider text-gray-400 mb-4">Lotes Recentes</h2>
          {lotes.length === 0 ? (
            <p className="text-gray-500">Nenhum lote. Va para Upload para criar.</p>
          ) : (
            <div className="space-y-2">
              {lotes.slice(0, 5).map((l, i) => (
                <div key={i} className="flex justify-between items-center p-3 rounded-lg bg-dark-600/50">
                  <span className="font-mono text-brand">Lote #{l.id}</span>
                  <span className="text-sm text-gray-400">Sessao: {l.sessionId}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <p className="text-sm text-gray-500">Logado como: {userName}</p>
    </div>
  );
}
