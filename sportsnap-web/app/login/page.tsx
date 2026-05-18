"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api, GAMIFICATION_BASE, MARKETPLACE_BASE, type Atleta, type Fotografo } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Input, Select } from "@/components/Input";
import { Alert } from "@/components/Alert";

type Modo = "atleta" | "fotografo";

export default function LoginPage() {
  const router = useRouter();
  const { login, sessao } = useAuth();
  const [modo, setModo] = useState<Modo>("atleta");
  const [atletas, setAtletas] = useState<Atleta[]>([]);
  const [fotografos, setFotografos] = useState<Fotografo[]>([]);
  const [selecionadoId, setSelecionadoId] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(true);

  // criação rápida
  const [novoNome, setNovoNome] = useState("");
  const [novoEmail, setNovoEmail] = useState("");

  useEffect(() => {
    if (sessao) router.replace("/perfil");
  }, [sessao, router]);

  async function carregar() {
    setCarregando(true);
    setErro(null);
    try {
      const [a, f] = await Promise.all([
        api.get<Atleta[]>(`${GAMIFICATION_BASE}/api/atletas`),
        api.get<Fotografo[]>(`${MARKETPLACE_BASE}/api/fotografos`),
      ]);
      setAtletas(a ?? []);
      setFotografos(f ?? []);
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  function entrar() {
    setErro(null);
    const id = parseInt(selecionadoId, 10);
    if (!id) {
      setErro("Selecione uma identidade.");
      return;
    }
    if (modo === "atleta") {
      const a = atletas.find((x) => x.id === id);
      if (!a) return setErro("Atleta não encontrado.");
      login({ role: "atleta", id: a.id, nome: a.nome, email: a.email });
    } else {
      const f = fotografos.find((x) => x.id === id);
      if (!f) return setErro("Fotógrafo não encontrado.");
      login({ role: "fotografo", id: f.id, nome: f.nome, email: f.email });
    }
    router.push("/perfil");
  }

  async function criarERentrar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    try {
      const url = modo === "atleta"
        ? `${GAMIFICATION_BASE}/api/atletas`
        : `${MARKETPLACE_BASE}/api/fotografos`;
      await api.post(url, { nome: novoNome, email: novoEmail });
      setNovoNome("");
      setNovoEmail("");
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  const lista = modo === "atleta" ? atletas : fotografos;

  return (
    <div className="mx-auto max-w-xl fade-up">
      <div className="mb-8 text-center">
        <h1 className="text-4xl font-bold tracking-tight text-ink-900">Bem-vindo</h1>
        <p className="mt-2 text-ink-500">Entre como Atleta ou Fotógrafo para continuar.</p>
      </div>

      {erro && <Alert tone="danger">{erro}</Alert>}

      <Card>
        <div className="mb-6 grid grid-cols-2 gap-1 rounded-full bg-ink-100 p-1">
          <button
            onClick={() => {
              setModo("atleta");
              setSelecionadoId("");
            }}
            className={`rounded-full py-2 text-sm font-medium transition ${
              modo === "atleta" ? "bg-white text-ink-900 shadow-sm" : "text-ink-500"
            }`}
          >
            Atleta
          </button>
          <button
            onClick={() => {
              setModo("fotografo");
              setSelecionadoId("");
            }}
            className={`rounded-full py-2 text-sm font-medium transition ${
              modo === "fotografo" ? "bg-white text-ink-900 shadow-sm" : "text-ink-500"
            }`}
          >
            Fotógrafo
          </button>
        </div>

        <Select
          label="Selecione sua conta"
          value={selecionadoId}
          onChange={(e) => setSelecionadoId(e.target.value)}
          disabled={carregando || lista.length === 0}
        >
          <option value="">
            {carregando
              ? "Carregando..."
              : lista.length === 0
                ? `Nenhum ${modo} cadastrado`
                : "Escolher..."}
          </option>
          {lista.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nome} · {p.email}
            </option>
          ))}
        </Select>

        <Button onClick={entrar} className="mt-5 w-full" size="lg" disabled={!selecionadoId}>
          Entrar
        </Button>

        <div className="my-7 flex items-center gap-3 text-[12px] uppercase tracking-wider text-ink-400">
          <span className="h-px flex-1 bg-ink-100" /> ou crie uma conta <span className="h-px flex-1 bg-ink-100" />
        </div>

        <form onSubmit={criarERentrar} className="space-y-3">
          <Input
            label={`Nome do ${modo}`}
            value={novoNome}
            onChange={(e) => setNovoNome(e.target.value)}
            placeholder="Maria Silva"
            required
          />
          <Input
            label="Email"
            type="email"
            value={novoEmail}
            onChange={(e) => setNovoEmail(e.target.value)}
            placeholder="email@dominio.com"
            required
          />
          <Button type="submit" variant="secondary" className="w-full" size="lg">
            Criar conta
          </Button>
        </form>
      </Card>
    </div>
  );
}
