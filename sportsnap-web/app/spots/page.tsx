"use client";

import { useEffect, useState } from "react";
import { api, SESSION_BASE, type Spot } from "@/lib/api";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { Button } from "@/components/Button";
import { Input, Textarea } from "@/components/Input";
import { Alert } from "@/components/Alert";

export default function SpotsPage() {
  const [spots, setSpots] = useState<Spot[]>([]);
  const [nome, setNome] = useState("");
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [descricao, setDescricao] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  async function carregar() {
    setErro(null);
    try {
      setSpots((await api.get<Spot[]>(`${SESSION_BASE}/api/spots`)) ?? []);
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
      await api.post(`${SESSION_BASE}/api/spots`, {
        nome,
        latitude: parseFloat(latitude),
        longitude: parseFloat(longitude),
        descricao,
      });
      setNome("");
      setLatitude("");
      setLongitude("");
      setDescricao("");
      setAviso("Spot cadastrado.");
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Sessão & Spot Context"
        title="Spots"
        subtitle="Picos geográficos onde o esporte acontece."
      />

      {erro && <Alert tone="danger">{erro}</Alert>}
      {aviso && <Alert tone="success">{aviso}</Alert>}

      <div className="grid gap-6 lg:grid-cols-[1fr_2fr]">
        <Card title="Cadastrar Spot">
          <form onSubmit={cadastrar} className="space-y-4">
            <Input label="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <div className="grid grid-cols-2 gap-3">
              <Input
                label="Latitude"
                type="number"
                step="any"
                value={latitude}
                onChange={(e) => setLatitude(e.target.value)}
                required
              />
              <Input
                label="Longitude"
                type="number"
                step="any"
                value={longitude}
                onChange={(e) => setLongitude(e.target.value)}
                required
              />
            </div>
            <Textarea
              label="Descrição"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              rows={2}
            />
            <Button type="submit" className="w-full" size="lg">
              Cadastrar
            </Button>
          </form>
        </Card>

        <Card title={`Spots (${spots.length})`}>
          {spots.length === 0 ? (
            <p className="text-sm text-ink-500">Nenhum spot.</p>
          ) : (
            <ul className="grid gap-3 sm:grid-cols-2">
              {spots.map((s) => (
                <li key={s.id} className="rounded-2xl bg-ink-50 p-4">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-[11px] text-ink-400">#{s.id}</span>
                    <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-medium text-emerald-700">
                      Spot
                    </span>
                  </div>
                  <h3 className="mt-1 font-semibold text-ink-900">{s.nome}</h3>
                  <p className="mt-1 text-[12px] text-ink-500">
                    {s.latitude.toFixed(4)}, {s.longitude.toFixed(4)}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </div>
  );
}
