"use client";

import { useCallback, useEffect, useState } from "react";
import { gamificacao, CartaDetalhe, Desafio, Progresso, Insignia } from "@/lib/gamificacao";
import { GamificacaoShell } from "@/components/GamificacaoShell";
import { Card } from "@/components/Card";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";
import { Input, Select } from "@/components/Input";
import { Modal } from "@/components/Modal";

const STATUS_TOM: Record<string, "info" | "success" | "neutral" | "danger"> = {
  ATIVO: "info",
  CONCLUIDO: "success",
  EXPIRADO: "neutral",
  CANCELADO: "danger",
};

const TIPOS: [string, string][] = [
  ["CONTAGEM_SINCRONIZACOES", "Treinar X vezes"],
  ["LIMIAR_OVERALL", "Alcançar um Overall"],
  ["LIMIAR_ATRIBUTO", "Alcançar um atributo"],
];

const DICA_TIPO: Record<string, string> = {
  CONTAGEM_SINCRONIZACOES: "Conclui ao registrar a quantidade de treinos definida na meta.",
  LIMIAR_OVERALL: "Conclui quando o Overall da carta atinge a meta.",
  LIMIAR_ATRIBUTO: "Conclui quando o atributo escolhido atinge a meta.",
};

function DesafiosPainel({ atletaId }: { atletaId: number }) {
  const [disponiveis, setDisponiveis] = useState<Desafio[]>([]);
  const [progressos, setProgressos] = useState<Progresso[]>([]);
  const [insignias, setInsignias] = useState<Insignia[]>([]);
  const [modalidades, setModalidades] = useState<string[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [ok, setOk] = useState<string | null>(null);

  // criação (modal)
  const [aberto, setAberto] = useState(false);
  const [titulo, setTitulo] = useState("");
  const [tipo, setTipo] = useState("CONTAGEM_SINCRONIZACOES");
  const [meta, setMeta] = useState(2);
  const [alvo, setAlvo] = useState("");
  const [insignia, setInsignia] = useState("");

  // sugestão
  const [modalidade, setModalidade] = useState("");
  const [sugestao, setSugestao] = useState<Desafio | null | undefined>(undefined);

  const carregar = useCallback(async () => {
    setErro(null);
    try {
      setDisponiveis(await gamificacao.desafiosDisponiveis(atletaId));
      setProgressos(await gamificacao.progressos(atletaId));
      setInsignias(await gamificacao.insignias(atletaId));
      try {
        const c: CartaDetalhe = await gamificacao.carta(atletaId);
        const mods = Array.from(new Set(c.atributos.map((a) => a.tipoEsporte)));
        setModalidades(mods);
        setModalidade((prev) => prev || mods[0] || "");
        setAlvo((prev) => prev || c.atributos[0]?.nome || "");
      } catch {
        /* sem carta — sugestão fica indisponível */
      }
    } catch (e) {
      setErro((e as Error).message);
    }
  }, [atletaId]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  async function agir(fn: () => Promise<unknown>, msg: string) {
    setErro(null);
    setOk(null);
    try {
      await fn();
      setOk(msg);
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  async function definir() {
    if (!titulo.trim() || !insignia.trim()) {
      setErro("Dê um título e um código de insígnia para o desafio.");
      return;
    }
    await agir(
      () =>
        gamificacao.definirDesafio({
          titulo: titulo.trim(),
          criterios: [{ tipo, meta, alvoAtributo: tipo === "LIMIAR_ATRIBUTO" ? alvo : null }],
          permanente: true,
          insigniaCodigo: insignia.trim().toUpperCase(),
          prerequisitos: [],
          cadencia: "NENHUMA",
          repetivel: false,
        }),
      "Desafio criado e já disponível.",
    );
    setTitulo("");
    setInsignia("");
    setAberto(false);
  }

  async function buscarSugestao() {
    setErro(null);
    try {
      setSugestao(await gamificacao.sugestao(atletaId, modalidade));
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  const ativos = progressos.filter((p) => p.status === "ATIVO");

  return (
    <div className="space-y-8">
      <Alert tone="info">
        Desafios são missões. <b>Aceite</b> um desafio, <b>treine</b> para avançar e ganhe uma <b>insígnia</b> ao concluir.
      </Alert>

      {erro && <Alert tone="danger">{erro}</Alert>}
      {ok && <Alert tone="success">{ok}</Alert>}

      {/* Treino: avança os desafios */}
      <div className="surface flex flex-col gap-3 rounded-[2rem] p-6 sm:flex-row sm:items-center">
        <div className="flex-1">
          <p className="font-black text-ink-900">Registrou um treino?</p>
          <p className="text-[13px] text-ink-500">
            Cada treino conta <b>+1</b> nos desafios de contagem e revê os de Overall/atributo.
          </p>
        </div>
        <Button variant="accent" onClick={() => agir(() => gamificacao.registrarTreino(atletaId), "Treino registrado! Veja seus progressos abaixo.")}>
          ⚡ Treinei agora
        </Button>
      </div>

      <div className="grid gap-8 lg:grid-cols-2">
        {/* Em andamento */}
        <Card title="Em andamento" description="Seus desafios aceitos e o quanto falta para concluir.">
          {ativos.length === 0 && progressos.length === 0 ? (
            <p className="py-8 text-center text-sm text-ink-400">Aceite um desafio ao lado para começar.</p>
          ) : (
            <ul className="space-y-3">
              {progressos.map((p) => (
                <li key={p.id} className="rounded-2xl border border-ink-100 p-4">
                  <div className="mb-2 flex items-center gap-2">
                    <span className="flex-1 font-bold text-ink-900">{p.titulo}</span>
                    <Badge tone={STATUS_TOM[p.status] ?? "neutral"}>{p.status}</Badge>
                    {p.insigniaConcedida && <Badge tone="accent">🏅 {p.insigniaCodigo}</Badge>}
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="h-2 flex-1 overflow-hidden rounded-full bg-ink-100">
                      <div className="h-full rounded-full bg-accent" style={{ width: `${Math.min(100, p.percentual)}%` }} />
                    </div>
                    <span className="font-mono text-sm font-bold text-ink-900">{p.percentual.toFixed(0)}%</span>
                    {p.status === "ATIVO" && (
                      <Button size="sm" variant="ghost" className="text-rose-500"
                        onClick={() => agir(() => gamificacao.cancelarProgresso(p.id), "Desafio abandonado.")}>
                        Abandonar
                      </Button>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>

        {/* Disponíveis */}
        <Card
          title="Disponíveis"
          description="Aceite para começar a acompanhar."
        >
          <div className="mb-4 flex justify-end">
            <Button size="sm" variant="secondary" onClick={() => setAberto(true)}>+ Criar desafio</Button>
          </div>
          {disponiveis.length === 0 ? (
            <p className="py-8 text-center text-sm text-ink-400">Nenhum desafio disponível no momento.</p>
          ) : (
            <ul className="space-y-3">
              {disponiveis.map((d) => (
                <li key={d.id} className="flex items-center gap-3 rounded-2xl border border-ink-100 p-4">
                  <div className="flex-1">
                    <p className="font-black text-ink-900">{d.titulo}</p>
                    <p className="text-[12px] text-ink-500">Recompensa: insígnia <b>{d.insigniaCodigo}</b></p>
                  </div>
                  <Button size="sm" variant="accent" onClick={() => agir(() => gamificacao.aceitarDesafio(d.id, atletaId), "Desafio aceito! Treine para avançar.")}>
                    Aceitar
                  </Button>
                </li>
              ))}
            </ul>
          )}
        </Card>

        {/* Sugestão */}
        <Card title="Sugestão para seu ponto fraco" description="Indicamos um desafio focado no seu atributo mais abaixo da média.">
          <div className="flex items-end gap-3">
            <div className="flex-1">
              <Select label="Modalidade" value={modalidade} onChange={(e) => setModalidade(e.target.value)}>
                {modalidades.map((m) => <option key={m} value={m}>{m}</option>)}
              </Select>
            </div>
            <Button variant="secondary" onClick={buscarSugestao} disabled={!modalidade}>Sugerir</Button>
          </div>
          {sugestao !== undefined && (
            <div className="mt-4">
              {sugestao ? (
                <div className="flex items-center gap-3 rounded-2xl border border-accent/20 bg-accent/5 p-4">
                  <div className="flex-1">
                    <p className="font-black text-ink-900">{sugestao.titulo}</p>
                    <p className="text-[12px] text-ink-500">Insígnia {sugestao.insigniaCodigo}</p>
                  </div>
                  <Button size="sm" variant="accent"
                    onClick={() => agir(() => gamificacao.aceitarDesafio(sugestao.id, atletaId), "Desafio sugerido aceito!")}>
                    Aceitar
                  </Button>
                </div>
              ) : (
                <p className="text-sm text-ink-400">Sem sugestão agora — seus atributos estão equilibrados nessa modalidade.</p>
              )}
            </div>
          )}
        </Card>

        {/* Insígnias */}
        <Card title="Minhas insígnias" description="Conquistas que ficam no seu perfil.">
          {insignias.length === 0 ? (
            <p className="py-8 text-center text-sm text-ink-400">Conclua desafios para ganhar insígnias.</p>
          ) : (
            <div className="flex flex-wrap gap-3">
              {insignias.map((i, k) => (
                <div key={k} className="flex items-center gap-2 rounded-2xl border border-accent/10 bg-accent/5 px-4 py-3">
                  <span className="text-2xl">🏅</span>
                  <div>
                    <p className="font-black text-ink-900">{i.codigo}</p>
                    <p className="text-[11px] text-ink-400">{new Date(i.concedidaEm).toLocaleDateString("pt-BR")}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      <Modal isOpen={aberto} onClose={() => setAberto(false)} title="Criar desafio">
        <div className="space-y-4">
          <Input label="Título" value={titulo} onChange={(e) => setTitulo(e.target.value)} placeholder="Ex: Treino Firme" />
          <Select label="O que precisa fazer" value={tipo} onChange={(e) => setTipo(e.target.value)}>
            {TIPOS.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
          </Select>
          <p className="text-[12px] text-ink-400">{DICA_TIPO[tipo]}</p>
          <div className="grid grid-cols-2 gap-4">
            <Input label="Meta" type="number" min={1} value={meta} onChange={(e) => setMeta(Math.max(1, Number(e.target.value) || 1))} />
            {tipo === "LIMIAR_ATRIBUTO" && (
              <Input label="Atributo alvo" value={alvo} onChange={(e) => setAlvo(e.target.value)} placeholder="Ex: Velocidade" />
            )}
          </div>
          <Input label="Código da insígnia" value={insignia} onChange={(e) => setInsignia(e.target.value)} placeholder="Ex: ESFORCO" />
          <div className="flex gap-3 pt-2">
            <Button variant="ghost" className="flex-1" onClick={() => setAberto(false)}>Cancelar</Button>
            <Button variant="accent" className="flex-1" onClick={definir}>Criar</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

export default function DesafiosPage() {
  return (
    <GamificacaoShell
      eyebrow="Conquistas"
      title="Desafios & Insígnias"
      subtitle="Missões que premiam consistência. Aceite objetivos, treine para avançar e colecione insígnias."
    >
      {({ atletaId }) => <DesafiosPainel key={atletaId} atletaId={atletaId} />}
    </GamificacaoShell>
  );
}
