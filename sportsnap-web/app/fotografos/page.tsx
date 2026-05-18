"use client";

import { useEffect, useState } from "react";
import { api, MARKETPLACE_BASE, type Fotografo } from "@/lib/api";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { Button } from "@/components/Button";
import { Input } from "@/components/Input";
import { Alert } from "@/components/Alert";

export default function FotografosPage() {
  const [fotografos, setFotografos] = useState<Fotografo[]>([]);
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  async function carregar() {
    setErro(null);
    try {
      setFotografos((await api.get<Fotografo[]>(`${MARKETPLACE_BASE}/api/fotografos`)) ?? []);
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
      await api.post(`${MARKETPLACE_BASE}/api/fotografos`, { nome, email });
      setNome("");
      setEmail("");
      setAviso("Fotógrafo cadastrado.");
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Marketplace"
        title="Fotógrafos"
        subtitle="Profissionais que capturam e comercializam imagens."
      />

      {erro && <Alert tone="danger">{erro}</Alert>}
      {aviso && <Alert tone="success">{aviso}</Alert>}

      <div className="grid gap-6 lg:grid-cols-[1fr_2fr]">
        <Card title="Novo fotógrafo">
          <form onSubmit={cadastrar} className="space-y-4">
            <Input label="Nome" value={nome} onChange={(e) => setNome(e.target.value)} required />
            <Input
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <Button type="submit" className="w-full" size="lg">
              Cadastrar
            </Button>
          </form>
        </Card>

        <Card title={`Fotógrafos (${fotografos.length})`}>
          {fotografos.length === 0 ? (
            <p className="text-sm text-ink-500">Nenhum fotógrafo.</p>
          ) : (
            <ul className="divide-y divide-ink-100">
              {fotografos.map((f) => (
                <li key={f.id} className="flex items-center gap-3 py-4">
                  <span className="grid h-10 w-10 place-items-center rounded-full bg-gradient-to-br from-rose-400 to-amber-400 text-sm font-semibold text-white">
                    {f.nome.charAt(0).toUpperCase()}
                  </span>
                  <div>
                    <div className="font-semibold text-ink-900">
                      {f.nome}
                      <span className="ml-2 font-mono text-[11px] text-ink-400">#{f.id}</span>
                    </div>
                    <div className="text-[12px] text-ink-500">{f.email}</div>
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
