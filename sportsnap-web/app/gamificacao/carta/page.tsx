"use client";

import { useCallback, useEffect, useState } from "react";
import {
  gamificacao,
  CartaDetalhe,
  Orcamento,
  RegistroSincronizacao,
  Evolucao,
} from "@/lib/gamificacao";
import { GamificacaoShell } from "@/components/GamificacaoShell";
import { Card } from "@/components/Card";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";

const TETO_TIER: Record<string, number> = { BRONZE: 79, PRATA: 89, OURO: 99, LENDARIA: 99 };

function dataCurta(iso: string | null) {
  return iso ? new Date(iso).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" }) : "—";
}
function custoPonto(valor: number) {
  return 1 + Math.floor(valor / 10);
}
function custoIncremento(valorAtual: number, inc: number) {
  let c = 0;
  for (let i = 0; i < inc; i++) c += custoPonto(valorAtual + i);
  return c;
}
function tierDeOverall(o: number) {
  if (o >= 96) return "LENDARIA";
  if (o >= 90) return "OURO";
  if (o >= 80) return "PRATA";
  return "BRONZE";
}

function CartaPainel({ atletaId, nome }: { atletaId: number; nome: string }) {
  const [carta, setCarta] = useState<CartaDetalhe | null>(null);
  const [orcamento, setOrcamento] = useState<Orcamento | null>(null);
  const [motivoSemReveal, setMotivoSemReveal] = useState<string | null>(null);
  const [incrementos, setIncrementos] = useState<Record<string, number>>({});
  const [ultimoReveal, setUltimoReveal] = useState<RegistroSincronizacao | null>(null);
  const [evolucao, setEvolucao] = useState<Evolucao[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [ok, setOk] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(true);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    setIncrementos({});
    try {
      const c = await gamificacao.carta(atletaId);
      setCarta(c);
      setEvolucao(await gamificacao.evolucao(atletaId));
      try {
        setOrcamento(await gamificacao.orcamento(atletaId));
        setMotivoSemReveal(null);
      } catch (e) {
        setOrcamento(null);
        setMotivoSemReveal((e as Error).message);
      }
    } catch (e) {
      setCarta(null);
      setErro((e as Error).message);
    } finally {
      setCarregando(false);
    }
  }, [atletaId]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  function ajustar(nomeAttr: string, valorAtual: number, delta: number) {
    setIncrementos((p) => {
      const atual = p[nomeAttr] ?? 0;
      const tetoInc = (TETO_TIER[carta!.tier] ?? 99) - valorAtual;
      const novo = Math.min(Math.max(0, atual + delta), Math.max(0, tetoInc));
      return { ...p, [nomeAttr]: novo };
    });
  }

  async function confirmar() {
    setErro(null);
    setOk(null);
    const alocacao = Object.fromEntries(Object.entries(incrementos).filter(([, v]) => v > 0));
    try {
      const r = await gamificacao.confirmarReveal(atletaId, alocacao);
      setUltimoReveal(r);
      setOk(`Reveal confirmado! Overall ${r.overallAnterior.toFixed(1)} → ${r.overallNovo.toFixed(1)}.`);
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  if (carregando) return <div className="surface rounded-[2rem] p-12 text-center text-ink-400">Carregando carta…</div>;
  if (erro && !carta) return <Alert tone="danger">{erro}</Alert>;
  if (!carta) return null;

  // Cálculos ao vivo
  const custoTotal = carta.atributos.reduce((s, a) => s + custoIncremento(a.valor, incrementos[a.nome] ?? 0), 0);
  const orcamentoPts = orcamento?.pontosDisponiveis ?? 0;
  const restante = orcamentoPts - custoTotal;
  const somaPesos = carta.atributos.reduce((s, a) => s + a.peso, 0);
  const overallProj =
    somaPesos > 0
      ? carta.atributos.reduce((s, a) => s + (a.valor + (incrementos[a.nome] ?? 0)) * a.peso, 0) / somaPesos
      : carta.overall;
  const tierProj = tierDeOverall(overallProj);
  const promove = tierProj !== carta.tier && TETO_TIER[tierProj] > TETO_TIER[carta.tier];
  const algumaAlocacao = custoTotal > 0;
  const excede = custoTotal > orcamentoPts;

  return (
    <div className="space-y-8">
      {erro && <Alert tone="danger">{erro}</Alert>}
      {ok && <Alert tone="success">{ok}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_1.25fr]">
        {/* Carta — identidade visual da plataforma */}
        <div className="relative overflow-hidden rounded-[3rem] border border-white/5 bg-[#0a0a0c] p-8 shadow-2xl">
          <div className="absolute -right-24 -top-24 h-72 w-72 rounded-full bg-accent opacity-10 blur-[100px]" />
          <div className="absolute -bottom-24 -left-24 h-72 w-72 rounded-full bg-violet-600 opacity-10 blur-[100px]" />
          <div className="relative flex flex-col gap-8">
            <div className="flex items-start justify-between gap-4">
              <div>
                <div className="mb-3 flex flex-wrap items-center gap-2">
                  <Badge tone="accent" className="border-white/10 bg-white/10 text-white">{carta.tier}</Badge>
                  {carta.sincronizada ? (
                    <Badge tone="success">Sincronizada</Badge>
                  ) : (
                    <Badge className="border-white/10 bg-white/10 text-white/70">Latente</Badge>
                  )}
                </div>
                <h2 className="text-3xl font-black leading-tight tracking-tight text-white">{nome}</h2>
                <p className="mt-1 text-[12px] font-medium text-ink-500">
                  Atualizada {dataCurta(carta.ultimaSincronizacao)}
                </p>
              </div>
              <div className="text-right">
                <div className="text-6xl font-black italic leading-none tracking-tighter text-accent">
                  {carta.overall.toFixed(1)}
                </div>
                <div className="mt-2 text-[10px] font-black uppercase tracking-[0.25em] text-ink-500">Overall</div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-x-8 gap-y-5">
              {carta.atributos.map((a) => {
                const inc = incrementos[a.nome] ?? 0;
                return (
                  <div key={a.nome} className="space-y-2">
                    <div className="flex items-center justify-between text-[10px] font-black uppercase tracking-[0.15em] text-ink-500">
                      <span>{a.nome}</span>
                      <span className="font-bold text-white">
                        {a.valor.toFixed(0)}
                        {inc > 0 && <span className="text-accent"> +{inc}</span>}
                      </span>
                    </div>
                    <div className="h-1 w-full overflow-hidden rounded-full bg-white/5">
                      <div className="h-full rounded-full bg-accent transition-all duration-700" style={{ width: `${Math.min(100, a.valor + inc)}%` }} />
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="flex items-center justify-between gap-4 rounded-2xl border border-white/5 bg-white/5 px-5 py-4">
              <div>
                <p className="text-[10px] font-bold uppercase tracking-widest text-ink-500">Saldo guardado</p>
                <p className="text-2xl font-black text-white">{carta.saldoPontos.toFixed(0)} <span className="text-sm text-ink-500">pts</span></p>
              </div>
              <span className="max-w-[180px] text-right text-[11px] leading-relaxed text-ink-500">
                Pontos que sobram no Reveal ficam guardados para a próxima vez.
              </span>
            </div>
          </div>
        </div>

        {/* Reveal guiado */}
        <Card title="Reveal — invista seu progresso" description="O XP dos seus treinos vira um orçamento de pontos. Distribua entre os atributos e confirme para evoluir a carta.">
          {!orcamento ? (
            <Alert tone="warning">
              <b>Reveal indisponível agora.</b> Ele precisa de XP acumulado em treinos e de uma licença de imagem válida.
              <span className="mt-1 block text-[12px] opacity-80">Detalhe: {motivoSemReveal}</span>
            </Alert>
          ) : (
            <>
              {/* Passo 1: orçamento */}
              <div className="mb-5 rounded-2xl border border-ink-100 bg-ink-50 p-5">
                <div className="flex items-end justify-between">
                  <div>
                    <p className="text-[10px] font-black uppercase tracking-widest text-ink-400">Orçamento do Reveal</p>
                    <p className="text-4xl font-black text-ink-900">{orcamentoPts} <span className="text-base font-bold text-ink-400">pts</span></p>
                  </div>
                  <div className="text-right">
                    <p className="text-[10px] font-black uppercase tracking-widest text-ink-400">Restante</p>
                    <p className={`text-2xl font-black ${excede ? "text-rose-600" : "text-emerald-600"}`}>{restante}</p>
                  </div>
                </div>
                <div className="mt-3 h-2 overflow-hidden rounded-full bg-ink-100">
                  <div
                    className={`h-full rounded-full transition-all ${excede ? "bg-rose-500" : "bg-accent"}`}
                    style={{ width: `${Math.min(100, (custoTotal / Math.max(1, orcamentoPts)) * 100)}%` }}
                  />
                </div>
              </div>

              {/* Passo 2: distribuir */}
              <p className="mb-3 text-[11px] font-black uppercase tracking-widest text-ink-400">
                Distribua os pontos · custo sobe quanto maior o atributo
              </p>
              <div className="space-y-2.5">
                {carta.atributos.map((a) => {
                  const inc = incrementos[a.nome] ?? 0;
                  const teto = TETO_TIER[carta.tier] ?? 99;
                  const custoProx = custoPonto(a.valor + inc);
                  return (
                    <div key={a.nome} className="flex items-center gap-3 rounded-2xl border border-ink-100 px-4 py-3">
                      <div className="w-28">
                        <p className="text-[13px] font-bold text-ink-900">{a.nome}</p>
                        <p className="text-[11px] text-ink-400">prox. ponto: {custoProx} pts</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => ajustar(a.nome, a.valor, -1)}
                          disabled={inc <= 0}
                          className="grid h-9 w-9 place-items-center rounded-full border border-ink-200 text-lg font-black text-ink-700 transition hover:bg-ink-50 disabled:opacity-30"
                        >
                          −
                        </button>
                        <span className="w-8 text-center font-mono text-lg font-bold text-ink-900">{inc}</span>
                        <button
                          onClick={() => ajustar(a.nome, a.valor, +1)}
                          disabled={a.valor + inc >= teto}
                          className="grid h-9 w-9 place-items-center rounded-full border border-ink-200 text-lg font-black text-ink-700 transition hover:bg-ink-50 disabled:opacity-30"
                        >
                          +
                        </button>
                      </div>
                      <span className="ml-auto font-mono text-sm font-bold text-ink-900">
                        {a.valor.toFixed(0)} <span className="text-accent">→ {(a.valor + inc).toFixed(0)}</span>
                      </span>
                    </div>
                  );
                })}
              </div>

              {/* Passo 3: prévia + confirmar */}
              <div className="mt-5 flex flex-wrap items-center gap-x-6 gap-y-2 rounded-2xl bg-ink-50 px-5 py-4">
                <span className="text-sm text-ink-600">
                  Overall <b className="text-ink-900">{carta.overall.toFixed(1)}</b> → <b className="text-accent">{overallProj.toFixed(1)}</b>
                </span>
                <span className="text-sm text-ink-600">
                  Tier <b className="text-ink-900">{carta.tier}</b>{tierProj !== carta.tier && <> → <b className="text-accent">{tierProj}</b></>}
                </span>
                {promove && <Badge tone="success">⬆ Sobe de tier!</Badge>}
              </div>

              {excede && <Alert tone="danger" className="mt-4">A alocação ({custoTotal} pts) passou do orçamento. Reduza algum atributo.</Alert>}

              <Button variant="accent" size="lg" className="mt-4 w-full" onClick={confirmar} disabled={!algumaAlocacao || excede}>
                Confirmar Reveal{algumaAlocacao ? ` · ${custoTotal} pts` : ""}
              </Button>
              <p className="mt-2 text-center text-[12px] text-ink-400">Ao confirmar, o progresso é aplicado de forma definitiva.</p>
            </>
          )}
        </Card>
      </div>

      {/* Históricos */}
      <div className="grid gap-8 md:grid-cols-2">
        <Card title="Evolução do Overall" description="O histórico de cada vez que sua carta cresceu.">
          {evolucao.length === 0 ? (
            <p className="py-8 text-center text-sm text-ink-400">Ainda sem evolução registrada.</p>
          ) : (
            <ul className="divide-y divide-ink-100">
              {evolucao.map((e, i) => (
                <li key={i} className="flex items-center justify-between py-3">
                  <span className="text-[12px] text-ink-500">{dataCurta(e.ocorridoEm)}</span>
                  <span className="font-mono text-sm">
                    {e.overallAnterior.toFixed(1)} → {e.overallNovo.toFixed(1)}{" "}
                    <span className={e.delta >= 0 ? "text-emerald-600" : "text-rose-600"}>
                      ({e.delta >= 0 ? "+" : ""}{e.delta.toFixed(1)})
                    </span>
                  </span>
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card title="Último Reveal" description="Resumo da última vez que você investiu seus pontos.">
          {!ultimoReveal ? (
            <p className="py-8 text-center text-sm text-ink-400">Confirme um Reveal para ver o resumo aqui.</p>
          ) : (
            <div className="grid grid-cols-2 gap-4">
              <Metric label="Orçamento" value={`${ultimoReveal.orcamentoPontos} pts`} />
              <Metric label="Custo" value={`${ultimoReveal.custoTotal} pts`} />
              <Metric label="Overall" value={`${ultimoReveal.overallAnterior.toFixed(1)} → ${ultimoReveal.overallNovo.toFixed(1)}`} />
              <Metric label="Ganho" value={`+${ultimoReveal.variacaoOverall.toFixed(1)}`} />
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-[10px] font-bold uppercase tracking-widest text-ink-400">{label}</p>
      <p className="text-lg font-black text-ink-900">{value}</p>
    </div>
  );
}

export default function CartaPage() {
  return (
    <GamificacaoShell
      eyebrow="Minha Carta"
      title="Carta Oficial"
      subtitle="Seu tier, seu Overall e seus atributos. Use o Reveal para investir o XP dos treinos onde quiser evoluir."
    >
      {({ atletaId, nome }) => <CartaPainel key={atletaId} atletaId={atletaId} nome={nome} />}
    </GamificacaoShell>
  );
}
