"use client";

import { useEffect, useState } from "react";
import { useSession } from "@/lib/session";
import { api } from "@/lib/api";

export default function SessoesPage() {
  const { userId } = useSession();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [spots, setSpots] = useState<any[]>([]);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    api.listarSpots().then(setSpots).catch(() => setSpots([]));
  }, []);

  const handleCheckIn = async (spot: { id: number; latitude: number; longitude: number }) => {
    if (!userId) return;
    setMsg("");
    try {
      const sessions = await api.listarSessions(spot.id);
      const session = sessions?.[0];
      if (!session) { setMsg("Nenhuma sessao ativa neste spot."); return; }
      await api.realizarCheckIn(session.id, userId, spot.latitude, spot.longitude);
      setMsg(`Check-in realizado no spot ${spot.id}, sessao ${session.id}!`);
    } catch {
      setMsg("Erro ao fazer check-in.");
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Sessoes Disponiveis</h1>
      {msg && <p className="text-brand font-medium">{msg}</p>}

      <div className="bg-dark-700 rounded-2xl border border-dark-600 overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-dark-600 text-left text-sm text-gray-400 uppercase tracking-wider">
              <th className="p-4">ID</th>
              <th className="p-4">Nome</th>
              <th className="p-4">Latitude</th>
              <th className="p-4">Longitude</th>
              <th className="p-4">Acao</th>
            </tr>
          </thead>
          <tbody>
            {spots.length === 0 && (
              <tr><td colSpan={5} className="p-8 text-center text-gray-500">Nenhum spot encontrado.</td></tr>
            )}
            {spots.map((s) => (
              <tr key={s.id} className="border-b border-dark-600/50 hover:bg-dark-600/30 transition-colors">
                <td className="p-4 font-mono text-brand">{s.id}</td>
                <td className="p-4">{s.nome}</td>
                <td className="p-4 text-gray-400">{s.latitude}</td>
                <td className="p-4 text-gray-400">{s.longitude}</td>
                <td className="p-4">
                  <button
                    onClick={() => handleCheckIn(s)}
                    className="px-4 py-2 rounded-lg bg-brand/10 text-brand border border-brand/20 hover:bg-brand/20 transition-colors text-sm font-medium"
                  >
                    Check-in
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
