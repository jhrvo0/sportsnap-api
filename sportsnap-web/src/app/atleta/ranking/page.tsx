"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";

export default function RankingPage() {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [ranking, setRanking] = useState<any[]>([]);

  useEffect(() => {
    api.buscarRanking().then(setRanking).catch(() => setRanking([]));
  }, []);

  const medals = ["🥇", "🥈", "🥉"];

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Ranking</h1>

      <div className="bg-dark-700 rounded-2xl border border-dark-600 overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-dark-600 text-left text-sm text-gray-400 uppercase tracking-wider">
              <th className="p-4 w-20">#</th>
              <th className="p-4">Atleta</th>
              <th className="p-4 text-right">Overall</th>
            </tr>
          </thead>
          <tbody>
            {ranking.length === 0 && (
              <tr><td colSpan={3} className="p-8 text-center text-gray-500">Nenhum atleta no ranking.</td></tr>
            )}
            {ranking.map((r, i) => (
              <tr key={i} className="border-b border-dark-600/50 hover:bg-dark-600/30 transition-colors">
                <td className="p-4">
                  <span className="text-2xl">{medals[i] || `#${i + 1}`}</span>
                </td>
                <td className="p-4 font-medium">{r.atleta?.nome || `Atleta ${i + 1}`}</td>
                <td className="p-4 text-right">
                  <span className="text-2xl font-black text-brand">{r.overall}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
