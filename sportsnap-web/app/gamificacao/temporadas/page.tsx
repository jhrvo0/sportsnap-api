"use client";

import { useEffect, useState } from "react";
import { gamificacao, Temporada } from "@/lib/gamificacao";
import { GamificacaoShell } from "@/components/GamificacaoShell";
import { Card } from "@/components/Card";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";
import { Input } from "@/components/Input";

const STATUS_TOM: Record<string, "info" | "success" | "neutral" | "danger"> = {
  AGENDADA: "info",
  ATIVA: "success",
  ENCERRADA: "neutral",
  CANCELADA: "danger",
};

function paraInput(d: Date) {
  // YYYY-MM-DDTHH:mm para <input type="datetime-local">
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function dataCurta(iso: string) {
  return new Date(iso).toLocaleDateString("pt-BR");
}

function TemporadasPainel() {
  const [temporadas, setTemporadas] = useState<Temporada[]>([]);
  const [modalidade, setModalidade] = useState("NATACAO");
  const [inicio, setInicio] = useState(paraInput(new Date(Date.now() + 86400000)));
  const [fim, setFim] = useState(paraInput(new Date(Date.now() + 31 * 86400000)));
  const [erro, setErro] = useState<string | null>(null);
  const [ok, setOk] = useState<string | null>(null);

  async function carregar() {
    setErro(null);
    try {
      setTemporadas(await gamificacao.listarTemporadas());
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  async function criar() {
    setErro(null);
    setOk(null);
    try {
      await gamificacao.criarTemporada(modalidade, inicio, fim);
      setOk("Temporada criada.");
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  async function acao(fn: () => Promise<unknown>, msg: string) {
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

  const agora = Date.now();
  const ehVigente = (t: Temporada) =>
    (t.status === "ATIVA" || t.status === "AGENDADA") &&
    new Date(t.inicio).getTime() <= agora &&
    new Date(t.fim).getTime() >= agora;
  const ehFutura = (t: Temporada) => new Date(t.inicio).getTime() > agora;

  return (
    <div className="space-y-8">
      <Alert tone="info">
        Uma <b>temporada</b> é um período de disputa de uma modalidade. Os duelos da <b>Arena</b> contam para a
        temporada vigente. Ao <b>encerrar</b>, o pódio é congelado e os pontos são parcialmente reiniciados para a
        próxima temporada.
      </Alert>

      {erro && <Alert tone="danger">{erro}</Alert>}
      {ok && <Alert tone="success">{ok}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_1.4fr]">
        <Card title="Abrir nova temporada" description="Escolha a modalidade e o período. Não pode haver duas temporadas sobrepostas na mesma modalidade.">
          <div className="space-y-4">
            <Input label="Modalidade" value={modalidade} onChange={(e) => setModalidade(e.target.value.toUpperCase())} />
            <Input label="Início" type="datetime-local" value={inicio} onChange={(e) => setInicio(e.target.value)} />
            <Input label="Fim" type="datetime-local" value={fim} onChange={(e) => setFim(e.target.value)} />
            <Button variant="accent" className="w-full" onClick={criar}>Criar temporada</Button>
          </div>
        </Card>

        <Card title="Temporadas" description="A vigente é onde os duelos contam agora.">
          {temporadas.length === 0 ? (
            <p className="py-10 text-center text-sm text-ink-400">Nenhuma temporada ainda. Abra a primeira ao lado.</p>
          ) : (
            <ul className="space-y-3">
              {temporadas.map((t) => (
                <li key={t.id} className="flex flex-wrap items-center gap-4 rounded-2xl border border-ink-100 bg-white p-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="font-black text-ink-900">{t.modalidade}</span>
                      <Badge tone={STATUS_TOM[t.status] ?? "neutral"}>{t.status}</Badge>
                      {ehVigente(t) && <Badge tone="accent">Vigente</Badge>}
                    </div>
                    <p className="text-[12px] text-ink-500">
                      {dataCurta(t.inicio)} → {dataCurta(t.fim)}
                      {t.tamanhoSnapshot > 0 && ` · pódio final: ${t.tamanhoSnapshot} atletas`}
                    </p>
                  </div>
                  <div className="flex gap-2">
                    {ehFutura(t) && t.status === "AGENDADA" && (
                      <Button size="sm" variant="ghost" className="text-rose-500"
                        onClick={() => acao(() => gamificacao.cancelarTemporada(t.id), "Temporada cancelada.")}>
                        Cancelar
                      </Button>
                    )}
                    {(t.status === "ATIVA" || t.status === "AGENDADA") && !ehFutura(t) && (
                      <Button size="sm" variant="secondary"
                        onClick={() => acao(() => gamificacao.encerrarTemporada(t.id), "Temporada encerrada — pódio congelado.")}>
                        Encerrar
                      </Button>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </div>
  );
}

export default function TemporadasPage() {
  return (
    <GamificacaoShell
      eyebrow="Competição"
      title="Temporadas"
      subtitle="O calendário da disputa: período vigente, abertura de novas temporadas e encerramento com pódio congelado."
    >
      {() => <TemporadasPainel />}
    </GamificacaoShell>
  );
}
