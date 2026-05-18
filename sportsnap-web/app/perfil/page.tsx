"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api, GAMIFICATION_BASE, MARKETPLACE_BASE, type Foto, type Licenca } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { Card } from "@/components/Card";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";

export default function PerfilPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const [licencas, setLicencas] = useState<Licenca[]>([]);
  const [fotos, setFotos] = useState<Foto[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
  }, [sessao, carregando, router]);

  async function carregar() {
    if (!sessao) return;
    setErro(null);
    try {
      if (sessao.role === "atleta") {
        const [licData, fotosData] = await Promise.all([
          api.get<Licenca[]>(`${MARKETPLACE_BASE}/api/licencas?atletaId=${sessao.id}`),
          api.get<Foto[]>(`${MARKETPLACE_BASE}/api/fotos`),
        ]);
        setLicencas(licData ?? []);
        setFotos(fotosData ?? []);
      }
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  useEffect(() => {
    if (sessao) carregar();
  }, [sessao]);

  async function sincronizar() {
    if (!sessao || sessao.role !== "atleta") return;
    setErro(null);
    setAviso(null);
    try {
      await api.post(`${GAMIFICATION_BASE}/api/atletas/${sessao.id}/sincronizar`, {});
      setAviso("Sincronização (Reveal) disparada com sucesso. Confira o ranking.");
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  async function cancelarLicenca(id: number) {
    setErro(null);
    setAviso(null);
    try {
      await api.post(`${MARKETPLACE_BASE}/api/licencas/${id}/cancelar`, {});
      setAviso("Licença cancelada.");
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  if (!sessao) return null;

  const ativas = licencas.filter((l) => !l.cancelada);
  const totalGasto = ativas.reduce((acc, l) => acc + Number(l.preco), 0);
  const fotosMap = new Map(fotos.map((f) => [f.id, f]));

  return (
    <div className="fade-up">
      <section
        className="relative mb-10 overflow-hidden rounded-3xl px-8 py-12 sm:px-12 fade-up"
        style={{
          background: "linear-gradient(135deg, #0a0a0c 0%, #1d1d1f 50%, #0a0a0c 100%)",
          colorScheme: "light",
        }}
      >
        <div className="absolute -right-24 -top-24 h-64 w-64 rounded-full bg-accent/30 blur-3xl" />
        <div className="absolute -bottom-32 -left-20 h-64 w-64 rounded-full bg-violet-500/20 blur-3xl" />
        <div className="relative">
          <p className="mb-2 text-[13px] font-medium uppercase tracking-[0.16em]" style={{ color: "#0a84ff" }}>
            {sessao.role === "atleta" ? "Atleta" : "Fotógrafo"}
          </p>
          <div className="flex flex-wrap items-center gap-4">
            <span className="grid h-16 w-16 place-items-center rounded-full bg-gradient-to-br from-accent to-violet-500 text-2xl font-bold text-white">
              {sessao.nome.charAt(0).toUpperCase()}
            </span>
            <div>
              <h1 className="text-3xl font-bold tracking-tight" style={{ color: "#ffffff" }}>
                {sessao.nome}
              </h1>
              <p style={{ color: "#a8a8ad" }}>{sessao.email}</p>
            </div>
            <div
              className="ml-auto rounded-full px-3 py-1 text-[12px] font-medium"
              style={{ background: "rgba(255,255,255,0.08)", color: "#d2d2d7" }}
            >
              ID #{sessao.id}
            </div>
          </div>
        </div>
      </section>

      {erro && <Alert tone="danger">{erro}</Alert>}
      {aviso && <Alert tone="success">{aviso}</Alert>}

      {sessao.role === "atleta" ? (
        <div className="grid gap-6 md:grid-cols-3">
          <div
            className="rounded-3xl p-7 md:col-span-2"
            style={{
              background: "linear-gradient(135deg, #0a0a0c 0%, #1d1d1f 100%)",
              colorScheme: "light",
            }}
          >
            <p className="text-[13px] uppercase tracking-[0.12em]" style={{ color: "#a8a8ad" }}>
              Sua Carta
            </p>
            <h2 className="mt-2 text-3xl font-bold" style={{ color: "#ffffff" }}>
              Sincronização (Reveal)
            </h2>
            <p className="mt-2 max-w-md" style={{ color: "#a8a8ad" }}>
              Após adquirir uma licença, dispare a Sincronização para transferir seu Shadow XP
              para a Carta Oficial e atualizar o ranking.
            </p>
            <Button
              onClick={sincronizar}
              className="mt-5 border border-white/20"
              style={{ background: "#000000", color: "#ffffff" }}
            >
              Sincronizar agora
            </Button>
          </div>

          <Card>
            <p className="text-[13px] uppercase tracking-[0.12em] text-ink-400">Total investido</p>
            <p className="mt-2 text-3xl font-bold text-ink-900">
              R$ {totalGasto.toFixed(2)}
            </p>
            <p className="mt-1 text-sm text-ink-500">{ativas.length} licença(s) ativa(s)</p>
          </Card>

          <div className="md:col-span-3">
            <Card title="Minhas licenças" description="Fotos que você adquiriu.">
              {licencas.length === 0 ? (
                <p className="text-sm text-ink-500">
                  Você ainda não comprou fotos.{" "}
                  <a href="/loja" className="font-medium text-accent">
                    Visite a loja →
                  </a>
                </p>
              ) : (
                <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  {licencas.map((l) => {
                    const foto = fotosMap.get(l.fotoId);
                    return (
                      <li
                        key={l.id}
                        className="overflow-hidden rounded-2xl border border-ink-100"
                      >
                        <div className="aspect-video bg-gradient-to-br from-purple-300 to-pink-400 p-3 text-[10px] font-mono text-white">
                          foto #{l.fotoId}
                        </div>
                        <div className="p-4">
                          <div className="flex items-center justify-between">
                            <span className="text-sm font-semibold text-ink-900">
                              R$ {Number(l.preco).toFixed(2)}
                            </span>
                            {l.cancelada ? (
                              <Badge tone="warning">Cancelada</Badge>
                            ) : (
                              <Badge tone="success">Ativa</Badge>
                            )}
                          </div>
                          <p className="mt-1 text-[12px] text-ink-500">
                            {new Date(l.adquiridaEm).toLocaleString("pt-BR")}
                          </p>
                          {!l.cancelada && (
                            <button
                              onClick={() => cancelarLicenca(l.id)}
                              className="mt-3 text-[12px] font-medium text-rose-500 hover:text-rose-600"
                            >
                              Cancelar (7 dias)
                            </button>
                          )}
                        </div>
                      </li>
                    );
                  })}
                </ul>
              )}
            </Card>
          </div>
        </div>
      ) : (
        <Card title="Painel do Fotógrafo">
          <div className="grid gap-3 sm:grid-cols-2">
            <a
              href="/lotes"
              className="rounded-2xl bg-ink-50 p-5 transition hover:bg-ink-100"
            >
              <p className="text-[13px] uppercase tracking-wider text-ink-400">Lotes</p>
              <p className="mt-1 text-lg font-semibold text-ink-900">Gerenciar meus Lotes →</p>
            </a>
            <a
              href="/upload"
              className="rounded-2xl bg-ink-50 p-5 transition hover:bg-ink-100"
            >
              <p className="text-[13px] uppercase tracking-wider text-ink-400">Upload</p>
              <p className="mt-1 text-lg font-semibold text-ink-900">Subir novas fotos →</p>
            </a>
          </div>
        </Card>
      )}
    </div>
  );
}
