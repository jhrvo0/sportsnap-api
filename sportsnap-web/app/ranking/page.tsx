"use client";

import { useEffect, useState } from "react";
import { api, GAMIFICATION_BASE, type CartaResumo } from "@/lib/api";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";

export default function RankingPage() {
  const [ranking, setRanking] = useState<CartaResumo[]>([]);
  const [erro, setErro] = useState<string | null>(null);

  async function carregar() {
    setErro(null);
    try {
      const data = await api.get<CartaResumo[]>(`${GAMIFICATION_BASE}/api/ranking`);
      setRanking(data ?? []);
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Leaderboard"
        title="Ranking Global"
        subtitle="Cartas Oficiais sincronizadas, ordenadas por Overall."
      >
        <Button variant="secondary" onClick={carregar}>
          Atualizar
        </Button>
      </PageHeader>

      {erro && <Alert tone="danger">{erro}</Alert>}

      {ranking.length === 0 ? (
        <Card>
          <div className="py-10 text-center">
            <div className="mx-auto mb-4 grid h-16 w-16 place-items-center rounded-full bg-ink-100">
              <span className="text-2xl">🏆</span>
            </div>
            <h3 className="text-lg font-semibold text-ink-900">Ranking ainda vazio</h3>
            <p className="mt-1 text-ink-500">
              Cadastre atletas, compre fotos e dispare a Sincronização para entrar no leaderboard.
            </p>
          </div>
        </Card>
      ) : (
        <ol className="space-y-3">
          {ranking.map((c, i) => {
            const medalha = i === 0 ? "🥇" : i === 1 ? "🥈" : i === 2 ? "🥉" : null;
            return (
              <li key={c.atletaId} className="surface flex items-center gap-5 rounded-3xl p-5">
                <div className="flex h-12 w-12 items-center justify-center text-2xl font-bold text-ink-900">
                  {medalha ?? <span className="text-ink-400">#{i + 1}</span>}
                </div>
                <div className="flex-1">
                  <div className="font-semibold text-ink-900">Atleta #{c.atletaId}</div>
                  <div className="text-[12px] text-ink-500">
                    Última sync:{" "}
                    {c.ultimaSincronizacao
                      ? new Date(c.ultimaSincronizacao).toLocaleString("pt-BR")
                      : "—"}
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-3xl font-bold tracking-tight text-ink-900">
                    {c.overall.toFixed(1)}
                  </div>
                  <div className="text-[10px] uppercase tracking-wider text-ink-400">Overall</div>
                </div>
              </li>
            );
          })}
        </ol>
      )}
    </div>
  );
}
