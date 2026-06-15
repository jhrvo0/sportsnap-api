"use client";

import { useCallback, useEffect, useState } from "react";
import { gamificacao, AtletaApi, Pontuacao, Posicao, Confronto, Temporada } from "@/lib/gamificacao";
import { GamificacaoShell } from "@/components/GamificacaoShell";
import { Card } from "@/components/Card";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";
import { Select } from "@/components/Input";

const LIGA_TOM: Record<string, "warning" | "neutral" | "info" | "accent"> = {
  BRONZE: "warning",
  PRATA: "neutral",
  OURO: "info",
  DIAMANTE: "accent",
};

function temporadaVigente(ts: Temporada[]): Temporada | null {
  const agora = Date.now();
  return (
    ts.find(
      (t) =>
        (t.status === "ATIVA" || t.status === "AGENDADA") &&
        new Date(t.inicio).getTime() <= agora &&
        new Date(t.fim).getTime() >= agora,
    ) ?? null
  );
}

function ArenaPainel({ atletaId, atletas }: { atletaId: number; atletas: AtletaApi[] }) {
  const nomeDe = (id: number) => atletas.find((a) => a.id === id)?.nome ?? `Atleta #${id}`;

  const [classificacao, setClassificacao] = useState<Pontuacao[]>([]);
  const [posicao, setPosicao] = useState<Posicao | null>(null);
  const [oponentes, setOponentes] = useState<Pontuacao[]>([]);
  const [vigente, setVigente] = useState<Temporada | null>(null);
  const [adversario, setAdversario] = useState<number>(0);
  const [resultado, setResultado] = useState<Confronto | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [ok, setOk] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setErro(null);
    try {
      const temporadas = await gamificacao.listarTemporadas();
      setVigente(temporadaVigente(temporadas));

      let pos = await gamificacao.posicao(atletaId);
      if (!pos.classificado) {
        try {
          await gamificacao.registrarElegivel(atletaId);
          pos = await gamificacao.posicao(atletaId);
        } catch {
          /* carta nao elegivel — segue mostrando 'sem ranking' */
        }
      }
      setPosicao(pos);
      setClassificacao(await gamificacao.classificacao());
      try {
        setOponentes(await gamificacao.oponentes(atletaId));
      } catch {
        setOponentes([]);
      }
    } catch (e) {
      setErro((e as Error).message);
    }
  }, [atletaId]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  useEffect(() => {
    const outro = atletas.find((a) => a.id !== atletaId);
    if (outro) setAdversario((prev) => prev || outro.id);
  }, [atletaId, atletas]);

  async function confrontar() {
    setErro(null);
    setOk(null);
    setResultado(null);
    if (!vigente) return;
    try {
      const c = await gamificacao.resolverConfronto(atletaId, adversario, vigente.modalidade);
      setResultado(c);
      const venci = c.vencedorId === atletaId;
      setOk(
        venci
          ? `Você venceu ${nomeDe(c.perdedorId)} e ganhou ${c.prTransferida.toFixed(0)} PR!`
          : `${nomeDe(c.vencedorId)} venceu o duelo. Você perdeu ${c.prTransferida.toFixed(0)} PR.`,
      );
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  return (
    <div className="space-y-8">
      <Alert tone="info">
        Na <b>Arena</b> você ganha <b>Pontos de Ranking (PR)</b> vencendo duelos. Quem tem o melhor <b>Overall</b> vence;
        acumule PR para subir de <b>liga</b> (Bronze → Prata → Ouro → Diamante).
      </Alert>

      {erro && <Alert tone="danger">{erro}</Alert>}
      {ok && <Alert tone={resultado && resultado.vencedorId === atletaId ? "success" : "warning"}>{ok}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_1.3fr]">
        <div className="space-y-8">
          {/* Sua posição (hero) */}
          <div className="relative overflow-hidden rounded-[2.5rem] border border-white/5 bg-[#0a0a0c] p-8 text-white shadow-2xl">
            <div className="absolute -right-20 -top-20 h-56 w-56 rounded-full bg-accent opacity-10 blur-[90px]" />
            <p className="text-[11px] font-black uppercase tracking-[0.2em] text-accent">Sua posição</p>
            {posicao?.classificado ? (
              <div className="relative mt-4 flex items-end gap-8">
                <div>
                  <p className="text-6xl font-black leading-none">#{posicao.posicao}</p>
                  <p className="mt-2 text-[10px] font-bold uppercase tracking-widest text-ink-500">no ranking</p>
                </div>
                <div className="ml-auto text-right">
                  <p className="text-4xl font-black text-accent">{posicao.pr.toFixed(0)}</p>
                  <p className="text-[10px] font-bold uppercase tracking-widest text-ink-500">PR</p>
                </div>
                <div className="absolute right-0 top-0">
                  <Badge tone={LIGA_TOM[posicao.liga ?? ""] ?? "neutral"}>{posicao.liga}</Badge>
                </div>
              </div>
            ) : (
              <p className="mt-4 text-sm text-ink-400">
                Sua carta precisa estar sincronizada para entrar no ranking.
              </p>
            )}
          </div>

          {/* Duelo */}
          <Card title="Desafiar um atleta">
            {!vigente ? (
              <Alert tone="warning">
                Nenhuma temporada vigente. Abra uma em <b>Temporadas</b> para liberar os duelos.
              </Alert>
            ) : (
              <div className="space-y-4">
                <p className="text-[12px] text-ink-500">
                  Temporada vigente: <b className="text-ink-900">{vigente.modalidade}</b>
                </p>
                <Select label="Escolha o adversário" value={adversario || ""} onChange={(e) => setAdversario(Number(e.target.value))}>
                  {atletas
                    .filter((a) => a.id !== atletaId)
                    .map((a) => (
                      <option key={a.id} value={a.id}>{a.nome}</option>
                    ))}
                </Select>
                <Button variant="accent" size="lg" className="w-full" onClick={confrontar} disabled={!adversario}>
                  ⚔️ Desafiar {adversario ? nomeDe(adversario).split(" ")[0] : ""}
                </Button>
                {oponentes.length > 0 && (
                  <div>
                    <p className="mb-2 text-[11px] font-bold uppercase tracking-widest text-ink-400">Sugeridos (PR parecido)</p>
                    <div className="flex flex-wrap gap-2">
                      {oponentes.map((o) => (
                        <button
                          key={o.atletaId}
                          onClick={() => setAdversario(o.atletaId)}
                          className={`rounded-full border px-3 py-1.5 text-[12px] font-bold transition ${
                            adversario === o.atletaId
                              ? "border-accent bg-accent/10 text-accent"
                              : "border-ink-200 text-ink-600 hover:bg-ink-50"
                          }`}
                        >
                          {nomeDe(o.atletaId).split(" ")[0]} · {o.pr.toFixed(0)}
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}
          </Card>
        </div>

        {/* Classificação */}
        <Card title="Classificação" description="Todos os atletas no ranking, do maior para o menor PR.">
          {classificacao.length === 0 ? (
            <p className="py-10 text-center text-sm text-ink-400">Ninguém no ranking ainda.</p>
          ) : (
            <ol className="space-y-3">
              {classificacao.map((p, i) => (
                <li
                  key={p.atletaId}
                  className={`flex items-center gap-4 rounded-2xl border p-4 ${
                    p.atletaId === atletaId ? "border-accent/40 bg-accent/5" : "border-ink-100 bg-white"
                  }`}
                >
                  <span className="w-10 text-center text-2xl font-black text-ink-300">
                    {i === 0 ? "🥇" : i === 1 ? "🥈" : i === 2 ? "🥉" : `#${i + 1}`}
                  </span>
                  <span className="flex-1 font-bold text-ink-900">
                    {nomeDe(p.atletaId)} {p.atletaId === atletaId && <span className="text-accent">(você)</span>}
                  </span>
                  <Badge tone={LIGA_TOM[p.liga] ?? "neutral"}>{p.liga}</Badge>
                  <span className="w-16 text-right text-xl font-black text-ink-900">{p.pr.toFixed(0)}</span>
                </li>
              ))}
            </ol>
          )}
        </Card>
      </div>
    </div>
  );
}

export default function CompeticaoPage() {
  return (
    <GamificacaoShell
      eyebrow="Arena"
      title="Ranking Competitivo"
      subtitle="Enfrente outros atletas, acumule pontos a cada vitória, suba de liga e encontre adversários do seu nível."
    >
      {({ atletaId, atletas }) => <ArenaPainel key={atletaId} atletaId={atletaId} atletas={atletas} />}
    </GamificacaoShell>
  );
}
