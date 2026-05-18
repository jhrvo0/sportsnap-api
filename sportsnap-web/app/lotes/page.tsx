"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api, MARKETPLACE_BASE, SESSION_BASE, type Lote, type Sessao, type Spot } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Input, Select } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";

export default function LotesPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [spots, setSpots] = useState<Spot[]>([]);
  const [sessoes, setSessoes] = useState<Sessao[]>([]);
  const [spotId, setSpotId] = useState("");
  const [sessaoId, setSessaoId] = useState("");
  const [descricao, setDescricao] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
    if (sessao && sessao.role !== "fotografo") router.replace("/perfil");
  }, [sessao, carregando, router]);

  async function carregar() {
    if (!sessao || sessao.role !== "fotografo") return;
    setErro(null);
    try {
      const [ls, ss, sps] = await Promise.all([
        api.get<Lote[]>(`${MARKETPLACE_BASE}/api/lotes?fotografoId=${sessao.id}`),
        api.get<Sessao[]>(`${SESSION_BASE}/api/sessoes`),
        api.get<Spot[]>(`${SESSION_BASE}/api/spots`),
      ]);
      setLotes(ls ?? []);
      setSessoes(ss ?? []);
      setSpots(sps ?? []);
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  useEffect(() => {
    if (sessao) carregar();
  }, [sessao]);

  async function criar(e: React.FormEvent) {
    e.preventDefault();
    if (!sessao) return;
    setErro(null);
    setAviso(null);
    try {
      await api.post(`${MARKETPLACE_BASE}/api/lotes`, {
        fotografoId: sessao.id,
        sessaoId: parseInt(sessaoId, 10),
        spotId: parseInt(spotId, 10),
        descricao,
      });
      setAviso("Lote criado com sucesso.");
      setSpotId("");
      setSessaoId("");
      setDescricao("");
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  async function arquivar(id: number) {
    setErro(null);
    setAviso(null);
    try {
      await api.post(`${MARKETPLACE_BASE}/api/lotes/${id}/arquivar`, {});
      setAviso(`Lote #${id} arquivado.`);
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  if (!sessao) return null;

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Fotógrafo"
        title="Meus Lotes"
        subtitle="Cada lote agrupa fotos de uma sessão em um spot."
      />

      {erro && <Alert tone="danger">{erro}</Alert>}
      {aviso && <Alert tone="success">{aviso}</Alert>}

      <div className="grid gap-6 lg:grid-cols-[1fr_2fr]">
        <Card title="Novo Lote">
          <form onSubmit={criar} className="space-y-4">
            <Select
              label="Spot"
              value={spotId}
              onChange={(e) => setSpotId(e.target.value)}
              required
            >
              <option value="">Selecione...</option>
              {spots.map((s) => (
                <option key={s.id} value={s.id}>
                  #{s.id} – {s.nome}
                </option>
              ))}
            </Select>
            <Select
              label="Sessão"
              value={sessaoId}
              onChange={(e) => setSessaoId(e.target.value)}
              required
            >
              <option value="">Selecione...</option>
              {sessoes.map((s) => (
                <option key={s.id} value={s.id}>
                  #{s.id} – {s.descricao}
                </option>
              ))}
            </Select>
            <Input
              label="Descrição"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              placeholder="Ex: Surf matinal Maracaípe"
              required
            />
            <Button type="submit" className="w-full" size="lg">
              Criar Lote
            </Button>
          </form>
        </Card>

        <Card title={`Seus Lotes (${lotes.length})`}>
          {lotes.length === 0 ? (
            <p className="text-sm text-ink-500">Nenhum lote ainda. Crie o primeiro ao lado.</p>
          ) : (
            <ul className="divide-y divide-ink-100">
              {lotes.map((l) => (
                <li key={l.id} className="flex items-center justify-between py-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-[12px] text-ink-400">#{l.id}</span>
                      <h3 className="font-semibold text-ink-900">{l.descricao}</h3>
                      {l.arquivado && <Badge tone="warning">Arquivado</Badge>}
                    </div>
                    <p className="mt-0.5 text-[12px] text-ink-500">
                      Spot #{l.spotId} · Sessão #{l.sessaoId} ·{" "}
                      {new Date(l.criadoEm).toLocaleString("pt-BR")}
                    </p>
                  </div>
                  {!l.arquivado && (
                    <Button variant="ghost" size="sm" onClick={() => arquivar(l.id)}>
                      Arquivar
                    </Button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </div>
  );
}
