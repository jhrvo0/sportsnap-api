"use client";

import { useEffect, useState } from "react";
import { db } from "@/lib/db";
import { type CartaResumo } from "@/lib/api";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { Button } from "@/components/Button";

export default function RankingPage() {
  const [ranking, setRanking] = useState<CartaResumo[]>([]);

  async function carregar() {
    // Busca as cartas ranqueadas no banco local do protótipo.
    const data = db.getRankedCartas();
    setRanking(data);
  }

  useEffect(() => {
    carregar();
  }, []);

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Ranking"
        title="Ranking Global"
        subtitle="Cartas Oficiais sincronizadas, ordenadas por Overall."
      >
        <Button variant="secondary" onClick={carregar}>
          Atualizar Ranking
        </Button>
      </PageHeader>

      {ranking.length === 0 ? (
        <Card>
          <div className="py-20 text-center">
            <div className="mx-auto mb-6 grid h-20 w-20 place-items-center rounded-full bg-ink-100">
              <span className="text-3xl">🏆</span>
            </div>
            <h3 className="text-xl font-semibold text-ink-900">O pódio está vazio</h3>
            <p className="mt-2 text-ink-500 max-w-sm mx-auto">
              Seja o primeiro a sincronizar sua carta e assumir a liderança do SportSnap.
            </p>
          </div>
        </Card>
      ) : (
        <ol className="space-y-4">
          {ranking.map((c, i) => {
            const medalha = i === 0 ? "🥇" : i === 1 ? "🥈" : i === 2 ? "🥉" : null;
            const cartaCompleta = db.find("cartas", cart => cart.atletaId === c.atletaId);
            
            return (
              <li key={c.atletaId} className="surface flex items-center gap-6 rounded-[2rem] p-6 transition-all hover:border-accent/30 hover:shadow-soft">
                <div className="flex h-14 w-14 items-center justify-center text-3xl font-black text-ink-900">
                  {medalha ?? <span className="text-ink-200 font-mono text-xl">#{i + 1}</span>}
                </div>
                
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-ink-50 overflow-hidden border border-ink-100">
                   <span className="text-xl">👤</span>
                </div>

                <div className="flex-1">
                  <div className="font-bold text-lg text-ink-900">{cartaCompleta?.nome || `Atleta #${c.atletaId}`}</div>
                  <div className="text-[11px] font-medium uppercase tracking-wider text-ink-400">
                    Sincronizado em:{" "}
                    {c.ultimaSincronizacao
                      ? new Date(c.ultimaSincronizacao).toLocaleDateString("pt-BR")
                      : "—"}
                  </div>
                </div>

                <div className="text-right">
                  <div className="text-4xl font-black italic tracking-tighter text-ink-900">
                    {c.overall.toFixed(1)}
                  </div>
                  <div className="text-[10px] font-bold uppercase tracking-[0.2em] text-accent">Overall</div>
                </div>
              </li>
            );
          })}
        </ol>
      )}
    </div>
  );
}
