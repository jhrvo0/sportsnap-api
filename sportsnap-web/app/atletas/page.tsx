"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { db, CheckIn, RegistroAtividade } from "@/lib/db";
import { Sessao, Spot } from "@/lib/api";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";

export default function DashboardAtleta() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();

  const [sessoes, setSessoes] = useState<Sessao[]>([]);
  const [spots, setSpots] = useState<Spot[]>([]);
  const [meusCheckIns, setMeusCheckIns] = useState<CheckIn[]>([]);
  const [minhasAtividades, setMinhasAtividades] = useState<RegistroAtividade[]>([]);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
    if (sessao && sessao.role !== "atleta") router.replace("/perfil");
  }, [sessao, carregando, router]);

  useEffect(() => {
    if (sessao) {
      setSessoes(db.get("sessoes"));
      setSpots(db.get("spots"));

      const checkins = db.filter("checkins", c => c.atletaId === sessao.id);
      setMeusCheckIns(checkins);

      const checkInIds = new Set(checkins.map(c => c.id));
      const activities = db.filter("atividades", a => a.checkInId ? checkInIds.has(a.checkInId) : false);
      setMinhasAtividades(activities);
    }
  }, [sessao]);

  if (carregando || !sessao) return null;

  return (
    <div className="fade-up space-y-10">
      <PageHeader
        eyebrow="Dashboard"
        title={`Olá, ${sessao.nome.split(" ")[0]}`}
        subtitle="Acompanhe seu progresso e registre suas atividades."
      />

      <div className="grid gap-8 md:grid-cols-2">
        {/* Quick Actions */}
        <Card title="Ações Rápidas">
          <div className="grid grid-cols-1 gap-4 h-full content-start pt-2">
            <Button variant="secondary" size="lg" className="justify-start gap-4" onClick={() => router.push("/checkin")}>
              <span className="text-xl">📍</span> Fazer Check-in de Treino
            </Button>
            <Button variant="secondary" size="lg" className="justify-start gap-4" onClick={() => router.push("/atividades/analise")}>
              <span className="text-xl">📊</span> Analisar Minha Evolução
            </Button>
            <Button variant="secondary" size="lg" className="justify-start gap-4" onClick={() => router.push("/loja")}>
              <span className="text-xl">🛒</span> Ver Fotos Sugeridas
            </Button>
            <Button variant="secondary" size="lg" className="justify-start gap-4" onClick={() => router.push("/ranking")}>
              <span className="text-xl">🏆</span> Ver Ranking Global
            </Button>
          </div>
        </Card>

        {/* Resumo Mensal */}
        <Card title="Resumo Mensal">
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-100">
                <p className="text-[10px] font-bold uppercase tracking-widest text-emerald-600">Total Distância</p>
                <p className="text-2xl font-black text-emerald-900">
                  {minhasAtividades.reduce((acc, a) => acc + (a.metricas?.distancia || 0), 0).toFixed(1)}{" "}
                  <span className="text-sm font-medium">km</span>
                </p>
              </div>
              <div className="p-4 rounded-2xl bg-violet-50 border border-violet-100">
                <p className="text-[10px] font-bold uppercase tracking-widest text-violet-600">Total Tempo</p>
                <p className="text-2xl font-black text-violet-900">
                  {Math.floor(minhasAtividades.reduce((acc, a) => acc + a.duracao, 0) / 60)}h{" "}
                  {minhasAtividades.reduce((acc, a) => acc + a.duracao, 0) % 60}m
                </p>
              </div>
            </div>

            <div className="pt-2">
              <p className="text-[11px] font-bold uppercase tracking-wider text-ink-400 mb-3">Intensidade Predominante</p>
              <div className="flex gap-1 h-2 rounded-full overflow-hidden bg-ink-100">
                {(() => {
                  const counts = minhasAtividades.reduce((acc, a) => {
                    const key = a.intensidade || "media";
                    acc[key] = (acc[key] || 0) + 1;
                    return acc;
                  }, { baixa: 0, media: 0, alta: 0 } as Record<string, number>);
                  const total = Math.max(1, minhasAtividades.length);
                  return (
                    <>
                      <div className="bg-emerald-400" style={{ width: `${(counts.baixa / total) * 100}%` }} />
                      <div className="bg-amber-400" style={{ width: `${(counts.media / total) * 100}%` }} />
                      <div className="bg-rose-400" style={{ width: `${(counts.alta / total) * 100}%` }} />
                    </>
                  );
                })()}
              </div>
              <div className="mt-2 flex justify-between text-[9px] font-bold uppercase tracking-widest text-ink-500">
                <span className="flex items-center gap-1"><span className="w-1.5 h-1.5 rounded-full bg-emerald-400" /> Baixa</span>
                <span className="flex items-center gap-1"><span className="w-1.5 h-1.5 rounded-full bg-amber-400" /> Média</span>
                <span className="flex items-center gap-1"><span className="w-1.5 h-1.5 rounded-full bg-rose-400" /> Alta</span>
              </div>
            </div>
          </div>
        </Card>
      </div>

      {/* Performance Diária */}
      <Card title="Atividades Diárias (Últimos 7 dias)">
        <div className="h-[200px] flex items-end justify-between gap-2 px-2 pt-4">
          {Array.from({ length: 7 }).map((_, i) => {
            const date = new Date();
            date.setDate(date.getDate() - (6 - i));
            const dateStr = date.toLocaleDateString("pt-BR", { weekday: "short" });

            const dayCheckins = meusCheckIns.filter(
              c => new Date(c.horario).toDateString() === date.toDateString()
            );
            const dayCheckInIds = new Set(dayCheckins.map(c => c.id));
            const dayActivities = minhasAtividades.filter(
              a => a.checkInId ? dayCheckInIds.has(a.checkInId) : false
            );
            const count = dayActivities.length;
            const maxCount = 5;
            const height = Math.min(100, (count / maxCount) * 100);

            return (
              <div key={i} className="flex-1 flex flex-col items-center gap-2 group relative">
                <div className="w-full bg-ink-50 rounded-t-lg relative overflow-hidden flex flex-col justify-end" style={{ height: "150px" }}>
                  <div
                    className="w-full bg-accent transition-all duration-500 rounded-t-lg group-hover:bg-accent-600"
                    style={{ height: `${height}%` }}
                  />
                  {count > 0 && (
                    <div className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-full opacity-0 group-hover:opacity-100 transition-opacity bg-ink-900 text-white text-[10px] py-1 px-2 rounded-md mb-1 z-10 whitespace-nowrap">
                      {count} atividade{count > 1 ? "s" : ""}
                    </div>
                  )}
                </div>
                <span className="text-[10px] font-bold uppercase tracking-wider text-ink-400">{dateStr}</span>
              </div>
            );
          })}
        </div>
      </Card>

      {/* Próximas Sessões */}
      <Card title="Próximas Sessões no Spot">
        <ul className="divide-y divide-ink-100">
          {sessoes.slice(0, 3).map(s => (
            <li key={s.id} className="flex items-center justify-between py-4">
              <div>
                <h4 className="font-semibold text-ink-900">{s.descricao}</h4>
                <p className="text-[12px] text-ink-500">
                  {(spots.find(sp => sp.id === s.spotId) || db.getDefaultSpots?.().find(sp => sp.id === s.spotId))?.nome} · {new Date(s.periodoInicio).toLocaleDateString("pt-BR")}
                </p>
              </div>
              <Button size="sm" variant="ghost" onClick={() => router.push("/checkin")}>Check-in</Button>
            </li>
          ))}
        </ul>
      </Card>
    </div>
  );
}
