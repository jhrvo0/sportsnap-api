"use client";

import { useCallback, useEffect, useState } from "react";
import {
  gamificacao,
  AtletaApi,
  CartaDetalhe,
  ForcaFraqueza,
  DadosRadar,
  Projecao,
  Similar,
  Percentil,
} from "@/lib/gamificacao";
import { GamificacaoShell } from "@/components/GamificacaoShell";
import { Card } from "@/components/Card";
import { Badge } from "@/components/Badge";
import { Alert } from "@/components/Alert";
import { Select } from "@/components/Input";

const FORCA_TOM: Record<string, "success" | "danger" | "neutral"> = {
  FORTE: "success",
  FRACO: "danger",
  NEUTRO: "neutral",
};

function Secao({ titulo, descricao, erro, children }: {
  titulo: string; descricao?: string; erro: string | null; children: React.ReactNode;
}) {
  return (
    <Card title={titulo} description={descricao}>
      {erro ? <Alert tone="warning">{erro}</Alert> : children}
    </Card>
  );
}

function AnalisePainel({ atletaId, atletas }: { atletaId: number; atletas: AtletaApi[] }) {
  const nomeDe = (id: number) => atletas.find((a) => a.id === id)?.nome ?? `Atleta #${id}`;

  const [modalidades, setModalidades] = useState<string[]>([]);
  const [modalidade, setModalidade] = useState("");
  const [semCarta, setSemCarta] = useState(false);

  const [forca, setForca] = useState<ForcaFraqueza[]>([]);
  const [radar, setRadar] = useState<DadosRadar | null>(null);
  const [projecao, setProjecao] = useState<Projecao | null>(null);
  const [similares, setSimilares] = useState<Similar[]>([]);
  const [atributo, setAtributo] = useState("");
  const [percentil, setPercentil] = useState<Percentil | null>(null);

  const [errForca, setErrForca] = useState<string | null>(null);
  const [errRadar, setErrRadar] = useState<string | null>(null);
  const [errProj, setErrProj] = useState<string | null>(null);
  const [errSim, setErrSim] = useState<string | null>(null);
  const [errPct, setErrPct] = useState<string | null>(null);

  // Descobre as modalidades a partir da carta do atleta
  useEffect(() => {
    let ativo = true;
    gamificacao
      .carta(atletaId)
      .then((c: CartaDetalhe) => {
        if (!ativo) return;
        const mods = Array.from(new Set(c.atributos.map((a) => a.tipoEsporte)));
        setModalidades(mods);
        setModalidade(mods[0] ?? "");
        setSemCarta(false);
      })
      .catch(() => {
        if (!ativo) return;
        setModalidades([]);
        setSemCarta(true);
      });
    return () => {
      ativo = false;
    };
  }, [atletaId]);

  const carregar = useCallback(async () => {
    if (!modalidade) return;
    setPercentil(null);
    setErrPct(null);

    gamificacao.forcaFraqueza(atletaId, modalidade)
      .then((f) => { setForca(f); setErrForca(null); if (f[0]) setAtributo(f[0].atributo); })
      .catch((e) => { setForca([]); setErrForca((e as Error).message); });

    gamificacao.radar(atletaId).then((r) => { setRadar(r); setErrRadar(null); })
      .catch((e) => { setRadar(null); setErrRadar((e as Error).message); });

    gamificacao.projecao(atletaId).then((p) => { setProjecao(p); setErrProj(null); })
      .catch((e) => { setProjecao(null); setErrProj((e as Error).message); });

    gamificacao.similares(atletaId, modalidade).then((s) => { setSimilares(s); setErrSim(null); })
      .catch((e) => { setSimilares([]); setErrSim((e as Error).message); });
  }, [atletaId, modalidade]);

  useEffect(() => { carregar(); }, [carregar]);

  async function consultarPercentil(nome: string) {
    setAtributo(nome);
    setErrPct(null);
    try {
      setPercentil(await gamificacao.percentil(atletaId, nome));
    } catch (e) {
      setPercentil(null);
      setErrPct((e as Error).message);
    }
  }

  if (semCarta) {
    return <Alert tone="warning">Você ainda não tem uma Carta Oficial para analisar. Sincronize sua carta primeiro.</Alert>;
  }

  return (
    <div className="space-y-8">
      <div className="surface flex flex-col gap-3 rounded-[2rem] p-5 sm:flex-row sm:items-center">
        <span className="text-[11px] font-black uppercase tracking-[0.2em] text-ink-400">Modalidade</span>
        <div className="sm:w-60">
          <Select value={modalidade} onChange={(e) => setModalidade(e.target.value)}>
            {modalidades.map((m) => (
              <option key={m} value={m}>{m}</option>
            ))}
          </Select>
        </div>
        <span className="text-[12px] text-ink-400 sm:ml-auto">As análises consideram apenas atletas com carta ativa.</span>
      </div>

      <div className="grid gap-8 lg:grid-cols-2">
        <Secao titulo="Pontos fortes e fracos" descricao="Como cada atributo seu se compara à média dos outros atletas." erro={errForca}>
          {forca.length === 0 ? (
            <p className="py-6 text-center text-sm text-ink-400">Sem atributos nessa modalidade.</p>
          ) : (
            <ul className="space-y-2">
              {forca.map((f) => (
                <li key={f.atributo} className="flex items-center gap-3 rounded-xl bg-ink-50 px-4 py-2.5">
                  <span className="flex-1 font-bold text-ink-800">{f.atributo}</span>
                  <span className="font-mono text-sm text-ink-500">média {f.media.toFixed(1)}</span>
                  <span className="font-mono text-sm font-bold text-ink-900">{f.valor.toFixed(0)}</span>
                  <Badge tone={FORCA_TOM[f.classificacao] ?? "neutral"}>{f.classificacao}</Badge>
                </li>
              ))}
            </ul>
          )}
        </Secao>

        <Secao titulo="Radar dos atributos" descricao="Seus atributos numa escala comum de 0 a 100." erro={errRadar}>
          {!radar ? null : (
            <div className="space-y-3">
              {Object.entries(radar.valoresNormalizados).map(([nome, valor]) => (
                <div key={nome} className="flex items-center gap-3">
                  <span className="w-28 text-[13px] font-bold text-ink-700">{nome}</span>
                  <div className="h-2.5 flex-1 overflow-hidden rounded-full bg-ink-100">
                    <div className="h-full rounded-full bg-gradient-to-r from-accent to-violet-500" style={{ width: `${Math.min(100, valor)}%` }} />
                  </div>
                  <span className="w-10 text-right font-mono text-sm font-bold text-ink-900">{valor.toFixed(0)}</span>
                </div>
              ))}
            </div>
          )}
        </Secao>

        <Secao titulo="Percentil por atributo" descricao="Você está acima de quantos % dos atletas naquele atributo." erro={errPct}>
          <div className="flex items-end gap-4">
            <div className="flex-1">
              <Select label="Atributo" value={atributo} onChange={(e) => consultarPercentil(e.target.value)}>
                {forca.map((f) => (
                  <option key={f.atributo} value={f.atributo}>{f.atributo}</option>
                ))}
              </Select>
            </div>
            {percentil && (
              <div className="text-right">
                <p className="text-5xl font-black text-ink-900">{percentil.percentil.toFixed(0)}</p>
                <p className="text-[10px] font-bold uppercase tracking-widest text-accent">percentil</p>
              </div>
            )}
          </div>
          {!percentil && !errPct && (
            <p className="mt-3 text-[12px] text-ink-400">Selecione um atributo para calcular o percentil.</p>
          )}
        </Secao>

        <Secao titulo="Projeção de evolução" descricao="Para onde seu Overall tende, com base no seu histórico." erro={errProj}>
          {!projecao ? null : (
            <div className="grid grid-cols-3 gap-4">
              <div>
                <p className="text-[10px] font-bold uppercase tracking-widest text-ink-400">Hoje</p>
                <p className="text-3xl font-black text-ink-900">{projecao.overallAtual.toFixed(1)}</p>
              </div>
              <div>
                <p className="text-[10px] font-bold uppercase tracking-widest text-ink-400">Tendência</p>
                <p className={`text-3xl font-black ${projecao.tendencia >= 0 ? "text-emerald-600" : "text-rose-600"}`}>
                  {projecao.tendencia >= 0 ? "+" : ""}{projecao.tendencia.toFixed(1)}
                </p>
              </div>
              <div>
                <p className="text-[10px] font-bold uppercase tracking-widest text-ink-400">Projeção</p>
                <p className="text-3xl font-black text-accent">{projecao.overallProjetado.toFixed(1)}</p>
              </div>
            </div>
          )}
        </Secao>
      </div>

      <Secao titulo="Atletas parecidos com você" descricao="Quem tem o perfil de atributos mais próximo do seu nessa modalidade." erro={errSim}>
        {similares.length === 0 ? (
          <p className="py-6 text-center text-sm text-ink-400">Sem atletas parecidos nessa modalidade.</p>
        ) : (
          <ul className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {similares.map((s) => (
              <li key={s.atletaId} className="flex items-center justify-between rounded-xl bg-ink-50 px-4 py-3">
                <span className="font-bold text-ink-800">{nomeDe(s.atletaId)}</span>
                <span className="font-mono text-sm text-ink-500">dist {s.distancia.toFixed(1)}</span>
              </li>
            ))}
          </ul>
        )}
      </Secao>
    </div>
  );
}

export default function AnalisePage() {
  return (
    <GamificacaoShell
      eyebrow="Desempenho"
      title="Análise"
      subtitle="Seus pontos fortes e fracos, o radar dos atributos, atletas parecidos com você e a projeção da sua evolução."
    >
      {({ atletaId, atletas }) => <AnalisePainel key={atletaId} atletaId={atletaId} atletas={atletas} />}
    </GamificacaoShell>
  );
}
