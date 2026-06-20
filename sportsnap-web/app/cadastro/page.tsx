"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { db } from "@/lib/db";
import { useAuth } from "@/lib/auth";
import { cadastrarFotografo, listarFotografos } from "@/lib/marketplace";
import { criarPerfil } from "@/lib/social";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Input } from "@/components/Input";
import { Alert } from "@/components/Alert";

type Modo = "atleta" | "fotografo";
type Passo = 1 | 2;

const ESPORTES = ["Corrida", "Surf", "Skate", "Futebol", "Natação", "Ciclismo", "Musculação", "Outro"];

export default function CadastroPage() {
  const router = useRouter();
  const { login, sessao } = useAuth();

  // Passo 1
  const [modo, setModo] = useState<Modo>("atleta");
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");

  // Passo 2
  const [passo, setPasso] = useState<Passo>(1);
  const [contaCriada, setContaCriada] = useState<{ id: number; nome: string; email: string; role: Modo } | null>(null);
  const [nomeExibicao, setNomeExibicao] = useState("");
  const [bio, setBio] = useState("");
  const [esporte, setEsporte] = useState("");
  const [localidade, setLocalidade] = useState("");
  const [visibilidade, setVisibilidade] = useState<"PUBLICA" | "PRIVADA">("PUBLICA");

  const [erro, setErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (sessao) {
      router.replace(sessao.role === "atleta" ? "/atletas" : "/lotes");
    }
  }, [sessao, router]);

  async function handleCriarConta(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    const n = nome.trim();
    const em = email.trim();
    if (!n || !em) { setErro("Preencha nome e email."); return; }

    setSalvando(true);
    try {
      if (modo === "atleta") {
        const a = db.add("atletas", { nome: n, email: em });
        setContaCriada({ id: a.id, nome: a.nome, email: a.email, role: "atleta" });
        setNomeExibicao(a.nome);
        setPasso(2);
      } else {
        await cadastrarFotografo(n, em);
        const lista = await listarFotografos();
        const criado = lista.find(f => f.email === em);
        if (!criado) { setErro("Conta criada, mas não foi possível identificar. Tente fazer login."); return; }
        setContaCriada({ id: criado.id, nome: criado.nome, email: criado.email, role: "fotografo" });
        setNomeExibicao(criado.nome);
        setPasso(2);
      }
    } catch {
      setErro("Erro ao criar conta. Verifique se o servidor está ativo.");
    } finally {
      setSalvando(false);
    }
  }

  async function handleCriarPerfil(e: React.FormEvent) {
    e.preventDefault();
    if (!contaCriada) return;
    setErro(null);
    setSalvando(true);
    try {
      await criarPerfil(
        contaCriada.id,
        nomeExibicao.trim() || contaCriada.nome,
        contaCriada.role === "atleta" ? "ATLETA" : "FOTOGRAFO"
      );
      // Edita bio/esporte/localidade/visibilidade se preenchidos — lazy: o perfil social já
      // foi criado; o edit pode ser feito em /social/perfil depois. Aqui só garantimos criação.
    } catch {
      // Perfil social é opcional — se o serviço estiver offline, segue sem ele.
    } finally {
      login({ role: contaCriada.role, id: contaCriada.id, nome: contaCriada.nome, email: contaCriada.email });
      router.push(contaCriada.role === "atleta" ? "/atletas" : "/lotes");
    }
  }

  // --- Passo 1: dados da conta ---
  if (passo === 1) {
    return (
      <div className="mx-auto max-w-md fade-up">
        <div className="mb-10 text-center">
          <h1 className="text-5xl font-black tracking-tighter text-ink-900">SportSnap</h1>
          <p className="mt-2 text-lg text-ink-500 font-medium">Crie sua conta e comece agora.</p>
        </div>

        {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}

        <Card>
          <div className="mb-8 grid grid-cols-2 gap-2 rounded-[1.5rem] bg-ink-50 p-1.5 border border-ink-100">
            {(["atleta", "fotografo"] as Modo[]).map(m => (
              <button
                key={m}
                onClick={() => { setModo(m); setErro(null); }}
                className={`rounded-2xl py-3 text-sm font-bold transition-all ${modo === m ? "bg-white text-ink-900 shadow-md ring-1 ring-black/5" : "text-ink-400 hover:text-ink-600"}`}
              >
                {m === "atleta" ? "Sou Atleta" : "Sou Fotógrafo"}
              </button>
            ))}
          </div>

          <form onSubmit={handleCriarConta} className="space-y-4">
            <Input
              label="Nome Completo"
              value={nome}
              onChange={e => setNome(e.target.value)}
              placeholder="Ex: Gabriel Medina"
              required
            />
            <Input
              label="Email"
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="seu@email.com"
              required
            />
            <Button type="submit" variant="accent" className="w-full" size="lg" disabled={salvando}>
              {salvando ? "Criando conta..." : "Continuar"}
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-ink-400">
            Já tem conta?{" "}
            <Link href="/login" className="font-bold text-accent hover:underline">
              Entrar
            </Link>
          </p>
        </Card>
      </div>
    );
  }

  // --- Passo 2: perfil social ---
  const tipoConta = contaCriada?.role === "atleta" ? "Atleta" : "Fotógrafo";

  return (
    <div className="fade-up">
      {/* Header escuro igual ao /social/perfil */}
      <div className="rounded-3xl bg-ink-900 px-8 py-10 mb-10">
        <p className="text-[11px] font-black uppercase tracking-[0.25em] text-accent mb-2">Social</p>
        <h1 className="text-4xl font-black text-white">Criar perfil</h1>
        <p className="mt-2 text-sm text-white/60">Configure como você aparece na plataforma</p>
      </div>

      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}

      <div className="mx-auto max-w-xl">
        <Card>
          {/* Preview avatar */}
          <div className="flex items-center gap-4 mb-6">
            <div className="grid h-12 w-12 shrink-0 place-items-center rounded-full bg-ink-900 text-xl font-black text-white">
              {(nomeExibicao || contaCriada?.nome || "?").charAt(0).toUpperCase()}
            </div>
            <div>
              <p className="font-bold text-ink-900">{nomeExibicao || contaCriada?.nome}</p>
              <p className="text-sm text-ink-500">{tipoConta} · Perfil {visibilidade === "PUBLICA" ? "público" : "privado"}</p>
            </div>
          </div>

          <form onSubmit={handleCriarPerfil} className="space-y-5">
            {/* Nome de exibição */}
            <div>
              <label className="mb-2 block text-sm font-semibold text-ink-700">
                Nome de exibição <span className="text-accent">*</span>
              </label>
              <input
                className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-ink-900 placeholder-ink-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                value={nomeExibicao}
                onChange={e => setNomeExibicao(e.target.value)}
                placeholder={contaCriada?.nome}
                required
              />
            </div>

            {/* Bio */}
            <div>
              <div className="mb-2 flex items-center justify-between">
                <label className="text-sm font-semibold text-ink-700">Bio</label>
                <span className={`text-xs ${bio.length > 280 ? "text-red-500" : "text-ink-400"}`}>
                  {bio.length}/300
                </span>
              </div>
              <textarea
                className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-ink-900 placeholder-ink-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20 resize-none"
                placeholder="Conte um pouco sobre você..."
                rows={3}
                value={bio}
                onChange={e => setBio(e.target.value)}
                maxLength={300}
              />
            </div>

            {/* Esporte */}
            <div>
              <label className="mb-2 block text-sm font-semibold text-ink-700">Esporte principal</label>
              <select
                className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-ink-900 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                value={esporte}
                onChange={e => setEsporte(e.target.value)}
              >
                <option value="">Selecione um esporte</option>
                {ESPORTES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>

            {/* Cidade / Estado */}
            <div>
              <label className="mb-2 block text-sm font-semibold text-ink-700">Cidade / Estado</label>
              <input
                className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-ink-900 placeholder-ink-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                placeholder="Ex: Recife, PE"
                value={localidade}
                onChange={e => setLocalidade(e.target.value)}
                maxLength={100}
              />
            </div>

            {/* Visibilidade */}
            <div>
              <label className="mb-3 block text-sm font-semibold text-ink-700">Visibilidade do perfil</label>
              <div className="grid grid-cols-2 gap-3">
                {(["PUBLICA", "PRIVADA"] as const).map(v => (
                  <button
                    key={v}
                    type="button"
                    onClick={() => setVisibilidade(v)}
                    className={`rounded-2xl border-2 p-4 text-left transition-all ${
                      visibilidade === v
                        ? "border-accent bg-accent-50"
                        : "border-ink-200 bg-white hover:border-ink-300"
                    }`}
                  >
                    <p className="font-bold text-ink-900">{v === "PUBLICA" ? "🌐 Público" : "🔒 Privado"}</p>
                    <p className="mt-1 text-xs text-ink-500">
                      {v === "PUBLICA" ? "Qualquer pessoa pode te seguir" : "Você aprova quem te segue"}
                    </p>
                  </button>
                ))}
              </div>
            </div>

            <Button type="submit" variant="accent" className="w-full" size="lg" disabled={salvando}>
              {salvando ? "Criando perfil..." : "Criar perfil"}
            </Button>
          </form>
        </Card>

        <p className="mt-6 text-center text-xs text-ink-400">
          SportSnap · Ecossistema de performance esportiva e fotografia profissional
        </p>
      </div>
    </div>
  );
}
