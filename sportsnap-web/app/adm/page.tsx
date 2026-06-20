"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/lib/auth";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Select } from "@/components/Input";
import { Alert } from "@/components/Alert";

export default function AdminLoginPage() {
  const router = useRouter();
  const { login, sessao } = useAuth();
  
  const [selecionadoId, setSelecionadoId] = useState("");
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (sessao?.role === "adm") {
      router.replace("/adm/dashboard");
    } else if (sessao) {
      // Se tiver sessão de outro tipo, desloga para forçar login de admin ou apenas redireciona
      // Vamos apenas não fazer nada ou forçar deslogar se for outra role? Deixaremos ele na tela para poder entrar como admin e sobrescrever a sessão.
    }
  }, [sessao, router]);

  const admins = [
    { id: "1", nome: "Administrador Master", email: "admin@sportsnap.com" },
  ];

  function entrar() {
    setErro(null);
    if (!selecionadoId) { setErro("Selecione uma conta de administrador."); return; }

    const a = admins.find(x => x.id === selecionadoId);
    if (!a) { setErro("Administrador não encontrado."); return; }

    login({ role: "adm", id: parseInt(a.id, 10), nome: a.nome, email: a.email });
    router.push("/adm/dashboard");
  }

  return (
    <div className="mx-auto max-w-md fade-up mt-10">
      <div className="mb-10 text-center">
        <h1 className="text-5xl font-black tracking-tighter text-ink-900">SportSnap</h1>
        <p className="mt-2 text-lg font-bold text-accent">Acesso Restrito - Admin</p>
      </div>

      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}

      <Card>
        <Select
          label="Escolha sua conta de administrador"
          value={selecionadoId}
          onChange={e => setSelecionadoId(e.target.value)}
        >
          <option value="">Selecione...</option>
          {admins.map(p => (
            <option key={p.id} value={p.id}>{p.nome}</option>
          ))}
        </Select>

        <Button onClick={entrar} className="mt-6 w-full" size="lg" disabled={!selecionadoId}>
          Entrar como Administrador
        </Button>

        <p className="mt-6 text-center text-sm text-ink-400">
          Não é administrador?{" "}
          <Link href="/login" className="font-bold text-ink-600 hover:underline">
            Voltar ao login normal
          </Link>
        </p>
      </Card>
    </div>
  );
}
