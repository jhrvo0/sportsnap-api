"use client";

import { useEffect, useState } from "react";
import { api, SESSION_BASE, type Sessao, type Spot } from "@/lib/api";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { Button } from "@/components/Button";
import { Input, Select } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";

export default function SessoesPage() {
  const [sessoes, setSessoes] = useState<Sessao[]>([]);
  const [spots, setSpots] = useState<Spot[]>([]);
  const [spotId, setSpotId] = useState("");
  const [inicio, setInicio] = useState("");
  const [fim, setFim] = useState("");
  const [descricao, setDescricao] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  async function carregar() {
    setErro(null);
    try {
      const [s, sp] = await Promise.all([
        api.get<Sessao[]>(`${SESSION_BASE}/api/sessoes`),
        api.get<Spot[]>(`${SESSION_BASE}/api/spots`),
      ]);
      setSessoes(s ?? []);
      setSpots(sp ?? []);
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  async function cadastrar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setAviso(null);
    try {
      await api.post(`${SESSION_BASE}/api/sessoes`, {
        spotId: parseInt(spotId, 10),
        inicio,
        fim,
        descricao,
      });
      setInicio("");
      setFim("");
      setDescricao("");
      setAviso("Sessão cadastrada.");
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Sessão & Spot Context"
        title="Sessões"
        subtitle="Janelas temporais associadas a um Spot."
      />

      {erro && <Alert tone="danger">{erro}</Alert>}
      {aviso && <Alert tone="success">{aviso}</Alert>}

      <div className="grid gap-6 lg:grid-cols-[1fr_2fr]">
        <Card title="Nova Sessão">
          <form onSubmit={cadastrar} className="space-y-4">
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
            <Input
              label="Início"
              type="datetime-local"
              value={inicio}
              onChange={(e) => setInicio(e.target.value)}
              required
            />
            <Input
              label="Fim"
              type="datetime-local"
              value={fim}
              onChange={(e) => setFim(e.target.value)}
              required
            />
            <Input
              label="Descrição"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              required
            />
            <Button type="submit" className="w-full" size="lg">
              Cadastrar
            </Button>
          </form>
        </Card>

        <Card title={`Sessões (${sessoes.length})`}>
          {sessoes.length === 0 ? (
            <p className="text-sm text-ink-500">Nenhuma sessão.</p>
          ) : (
            <ul className="divide-y divide-ink-100">
              {sessoes.map((s) => (
                <li key={s.id} className="py-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-[11px] text-ink-400">#{s.id}</span>
                      <h3 className="font-semibold text-ink-900">{s.descricao}</h3>
                    </div>
                    <Badge tone="info">Spot #{s.spotId}</Badge>
                  </div>
                  <div className="mt-1 text-[12px] text-ink-500">
                    {new Date(s.periodoInicio).toLocaleString("pt-BR")} →{" "}
                    {new Date(s.periodoFim).toLocaleString("pt-BR")}
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
