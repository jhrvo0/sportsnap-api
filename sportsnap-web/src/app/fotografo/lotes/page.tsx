"use client";

import { useEffect, useState } from "react";
import { useSession } from "@/lib/session";
import { api } from "@/lib/api";

export default function MeusLotesPage() {
  const { userId } = useSession();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [lotes, setLotes] = useState<any[]>([]);

  useEffect(() => {
    if (!userId) return;
    api.listarLotes(userId).then(setLotes).catch(() => setLotes([]));
  }, [userId]);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Meus Lotes</h1>

      {lotes.length === 0 ? (
        <div className="bg-dark-700 rounded-2xl p-8 border border-dark-600 text-center">
          <p className="text-gray-400">Nenhum lote encontrado. Va para Upload para criar seu primeiro lote.</p>
        </div>
      ) : (
        <div className="bg-dark-700 rounded-2xl border border-dark-600 overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-dark-600 text-left text-sm text-gray-400 uppercase tracking-wider">
                <th className="p-4">ID</th>
                <th className="p-4">Session</th>
                <th className="p-4">Spot</th>
                <th className="p-4">Criado em</th>
              </tr>
            </thead>
            <tbody>
              {lotes.map((l, i) => (
                <tr key={i} className="border-b border-dark-600/50 hover:bg-dark-600/30 transition-colors">
                  <td className="p-4 font-mono text-brand">{l.id}</td>
                  <td className="p-4">{l.sessionId}</td>
                  <td className="p-4">{l.spotId}</td>
                  <td className="p-4 text-gray-400">{l.criadoEm ? new Date(l.criadoEm).toLocaleDateString("pt-BR") : "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
