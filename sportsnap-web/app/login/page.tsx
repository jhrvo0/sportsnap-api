"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { db } from "@/lib/db";
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
  
  const [atletas, setAtletas] = useState(db.get("atletas"));
  const [fotografos, setFotografos] = useState(db.get("fotografos"));
  
  const [selecionadoId, setSelecionadoId] = useState("");
  const [erro, setErro] = useState<string | null>(null);

  const [novoNome, setNovoNome] = useState("");
  const [novoEmail, setNovoEmail] = useState("");

  useEffect(() => {
    if (sessao) {
      if (sessao.role === "atleta") router.replace("/atletas");
      else router.replace("/lotes");
    }
  }, [sessao, router]);

  function entrar() {
    setErro(null);
    const id = parseInt(selecionadoId, 10);
    if (!id) return setErro("Selecione uma identidade.");
    
    if (modo === "atleta") {
      const a = atletas.find((x) => x.id === id);
      if (!a) return setErro("Atleta não encontrado.");
      login({ role: "atleta", id: a.id, nome: a.nome, email: a.email });
      router.push("/atletas");
    } else {
      const f = fotografos.find((x) => x.id === id);
      if (!f) return setErro("Fotógrafo não encontrado.");
      login({ role: "fotografo", id: f.id, nome: f.nome, email: f.email });
      router.push("/lotes");
    }
  }

  function criarERentrar(e: React.FormEvent) {
    e.preventDefault();
    const nome = novoNome.trim();
    const email = novoEmail.trim();
    if (!nome || !email) {
      setErro("Informe nome e email para criar o perfil.");
      return;
    }

    if (modo === "atleta") {
      const a = db.add("atletas", { nome, email });
      db.add("cartas", { 
        atletaId: a.id, 
        nome: a.nome, 
        overall: 60, resistencia: 60, velocidade: 60, tecnica: 60, explosao: 60,
        ultimaSincronizacao: null 
      });
      setAtletas(db.get("atletas"));
      login({ role: "atleta", id: a.id, nome: a.nome, email: a.email });
      router.push("/atletas");
    } else {
      const f = db.add("fotografos", { nome, email });
      setFotografos(db.get("fotografos"));
      login({ role: "fotografo", id: f.id, nome: f.nome, email: f.email });
      router.push("/lotes");
    }
    setNovoNome("");
    setNovoEmail("");
  }

  const lista = modo === "atleta" ? atletas : fotografos;

  return (
    <div className="mx-auto max-w-xl fade-up">
      <div className="mb-10 text-center">
        <h1 className="text-5xl font-black tracking-tighter text-ink-900">SportSnap</h1>
        <p className="mt-2 text-lg text-ink-500 font-medium">Performance real, captura profissional.</p>
      </div>

      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}

      <Card>
        <div className="mb-8 grid grid-cols-2 gap-2 rounded-[1.5rem] bg-ink-50 p-1.5 border border-ink-100">
          <button
            onClick={() => {
              setModo("atleta");
              setSelecionadoId("");
            }}
            className={`rounded-2xl py-3 text-sm font-bold transition-all ${
              modo === "atleta" ? "bg-white text-ink-900 shadow-md ring-1 ring-black/5" : "text-ink-400 hover:text-ink-600"
            }`}
          >
            Sou Atleta
          </button>
          <button
            onClick={() => {
              setModo("fotografo");
              setSelecionadoId("");
            }}
            className={`rounded-2xl py-3 text-sm font-bold transition-all ${
              modo === "fotografo" ? "bg-white text-ink-900 shadow-md ring-1 ring-black/5" : "text-ink-400 hover:text-ink-600"
            }`}
          >
            Sou Fotógrafo
          </button>
        </div>

        <Select
          label="Identidade para a demonstração"
          value={selecionadoId}
          onChange={(e) => setSelecionadoId(e.target.value)}
        >
          <option value="">Escolha um perfil...</option>
          {lista.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nome} ({modo === "atleta" ? "atleta" : "fotógrafo"})
            </option>
          ))}
        </Select>

        <Button onClick={entrar} className="mt-6 w-full shadow-lg shadow-accent/20" size="lg" disabled={!selecionadoId}>
          Entrar no Ecossistema
        </Button>

        <div className="my-10 flex items-center gap-4 text-[11px] font-bold uppercase tracking-[0.2em] text-ink-300">
          <span className="h-px flex-1 bg-ink-100" /> Ou crie um novo <span className="h-px flex-1 bg-ink-100" />
        </div>

        <form onSubmit={criarERentrar} className="space-y-4">
          <Input
            label="Nome Completo"
            value={novoNome}
            onChange={(e) => setNovoNome(e.target.value)}
            placeholder="Ex: Gabriel Medina"
            required
          />
          <Input
            label="Email de Acesso"
            type="email"
            value={novoEmail}
            onChange={(e) => setNovoEmail(e.target.value)}
            placeholder="exemplo@sportsnap.com"
            required
          />
          <Button type="submit" variant="secondary" className="w-full" size="lg">
            Criar Perfil de Demonstração
          </Button>
        </form>
      </Card>
      
      <p className="mt-8 text-center text-[12px] text-ink-400 px-10 leading-relaxed">
        Este é um protótipo de alta fidelidade. Todos os dados são persistidos localmente no seu navegador para demonstração das regras de negócio.
      </p>
    </div>
  );
}
