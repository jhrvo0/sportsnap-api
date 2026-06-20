"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/lib/auth";
import { listarFotografos, type FotografoDto } from "@/lib/marketplace";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Select } from "@/components/Input";
import { Alert } from "@/components/Alert";

type Modo = "atleta" | "fotografo";
type AtletaItem = { id: number; nome: string; email: string };

const GAMI_URL = process.env.NEXT_PUBLIC_SOCIAL_URL || "http://localhost:8081";

async function listarAtletasBackend(): Promise<AtletaItem[]> {
  try {
    const r = await fetch(`${GAMI_URL}/api/atletas`, { cache: "no-store" });
    if (!r.ok) return [];
    return r.json();
  } catch {
    return [];
  }
}

export default function LoginPage() {
  const router = useRouter();
  const { login, sessao } = useAuth();
  const [modo, setModo] = useState<Modo>("atleta");

  const [atletas, setAtletas] = useState<AtletaItem[]>([]);
  const [fotografos, setFotografos] = useState<FotografoDto[]>([]);
  const [selecionadoId, setSelecionadoId] = useState("");
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (sessao) {
      router.replace(sessao.role === "atleta" ? "/atletas" : "/lotes");
    }
  }, [sessao, router]);

  useEffect(() => {
    listarAtletasBackend().then(setAtletas);
    listarFotografos()
      .then(setFotografos)
      .catch(() => console.warn("Marketplace offline — fotógrafos não carregados"));
  }, []);

  function entrar() {
    setErro(null);
    const id = parseInt(selecionadoId, 10);
    if (!id) { setErro("Selecione uma conta."); return; }

    if (modo === "atleta") {
      const a = atletas.find(x => x.id === id);
      if (!a) { setErro("Atleta não encontrado."); return; }
      login({ role: "atleta", id: a.id, nome: a.nome, email: a.email });
      router.push("/atletas");
    } else {
      const f = fotografos.find(x => x.id === id);
      if (!f) { setErro("Fotógrafo não encontrado."); return; }
      login({ role: "fotografo", id: f.id, nome: f.nome, email: f.email });
      router.push("/lotes");
    }
  }

  const lista = modo === "atleta"
    ? atletas.map(a => ({ id: a.id, nome: a.nome }))
    : fotografos.map(f => ({ id: f.id, nome: f.nome }));

  return (
    <div className="mx-auto max-w-md fade-up">
      <div className="mb-10 text-center">
        <h1 className="text-5xl font-black tracking-tighter text-ink-900">SportSnap</h1>
        <p className="mt-2 text-lg text-ink-500 font-medium">Performance real, captura profissional.</p>
      </div>

      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}

      <Card>
        <div className="mb-8 grid grid-cols-2 gap-2 rounded-[1.5rem] bg-ink-50 p-1.5 border border-ink-100">
          {(["atleta", "fotografo"] as Modo[]).map(m => (
            <button
              key={m}
              onClick={() => { setModo(m); setSelecionadoId(""); }}
              className={`rounded-2xl py-3 text-sm font-bold transition-all ${modo === m ? "bg-white text-ink-900 shadow-md ring-1 ring-black/5" : "text-ink-400 hover:text-ink-600"}`}
            >
              {m === "atleta" ? "Sou Atleta" : "Sou Fotógrafo"}
            </button>
          ))}
        </div>

        {lista.length === 0 ? (
          <p className="py-4 text-center text-sm text-ink-400">
            {modo === "fotografo" ? "Nenhum fotógrafo cadastrado ainda." : "Backend offline ou sem atletas."}
          </p>
        ) : (
          <Select
            label="Escolha sua conta"
            value={selecionadoId}
            onChange={e => setSelecionadoId(e.target.value)}
          >
            <option value="">Selecione...</option>
            {lista.map(p => (
              <option key={p.id} value={p.id}>{p.nome}</option>
            ))}
          </Select>
        )}

        <Button onClick={entrar} className="mt-6 w-full" size="lg" disabled={!selecionadoId}>
          Entrar
        </Button>

        <p className="mt-6 text-center text-sm text-ink-400">
          Ainda não tem conta?{" "}
          <Link href="/cadastro" className="font-bold text-accent hover:underline">
            Criar conta
          </Link>
        </p>
      </Card>
    </div>
  );
}
