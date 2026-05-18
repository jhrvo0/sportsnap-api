"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api, MARKETPLACE_BASE, type Foto, type Licenca } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { PageHeader } from "@/components/PageHeader";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";
import { Card } from "@/components/Card";

export default function LojaPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const [fotos, setFotos] = useState<Foto[]>([]);
  const [licencas, setLicencas] = useState<Licenca[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [comprandoId, setComprandoId] = useState<number | null>(null);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
    if (sessao && sessao.role !== "atleta") router.replace("/perfil");
  }, [sessao, carregando, router]);

  async function carregar() {
    if (!sessao) return;
    setErro(null);
    try {
      const [fs, ls] = await Promise.all([
        api.get<Foto[]>(`${MARKETPLACE_BASE}/api/fotos`),
        api.get<Licenca[]>(`${MARKETPLACE_BASE}/api/licencas?atletaId=${sessao.id}`),
      ]);
      setFotos((fs ?? []).filter((f) => !f.removida));
      setLicencas(ls ?? []);
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  useEffect(() => {
    if (sessao) carregar();
  }, [sessao]);

  async function comprar(fotoId: number) {
    if (!sessao || sessao.role !== "atleta") return;
    setErro(null);
    setAviso(null);
    setComprandoId(fotoId);
    try {
      await api.post(`${MARKETPLACE_BASE}/api/licencas`, { atletaId: sessao.id, fotoId });
      setAviso(`Licença adquirida! Foto #${fotoId} agora é sua.`);
      await carregar();
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setComprandoId(null);
    }
  }

  if (!sessao) return null;

  const minhasFotosIds = new Set(licencas.filter((l) => !l.cancelada).map((l) => l.fotoId));
  const disponiveis = fotos.filter((f) => !minhasFotosIds.has(f.id));

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Marketplace"
        title="Encontre suas fotos"
        subtitle="Adquira licenças de imagens capturadas durante suas sessões e desbloqueie a sincronização da carta."
      />

      {erro && <Alert tone="danger">{erro}</Alert>}
      {aviso && <Alert tone="success">{aviso}</Alert>}

      {disponiveis.length === 0 ? (
        <Card>
          <div className="py-10 text-center">
            <div className="mx-auto mb-4 grid h-16 w-16 place-items-center rounded-full bg-ink-100">
              <span className="text-2xl">📷</span>
            </div>
            <h3 className="text-lg font-semibold text-ink-900">Nenhuma foto disponível ainda</h3>
            <p className="mt-1 text-ink-500">
              Aguarde fotógrafos publicarem novos lotes ou peça que façam upload.
            </p>
          </div>
        </Card>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {disponiveis.map((f) => (
            <article
              key={f.id}
              className="surface-elev overflow-hidden rounded-3xl transition hover:-translate-y-1"
            >
              <div className="relative aspect-[4/3] overflow-hidden bg-gradient-to-br from-violet-300 via-fuchsia-400 to-rose-300">
                <span className="absolute right-3 top-3 rounded-full bg-black/40 px-2 py-0.5 text-[10px] font-medium uppercase tracking-wider text-white backdrop-blur">
                  Preview · marca d'água
                </span>
                <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-3 text-[11px] font-mono text-white/80">
                  © SportSnap · ID #{f.id}
                </div>
              </div>
              <div className="p-5">
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold text-ink-900">Foto #{f.id}</h3>
                  <Badge tone="accent">Lote #{f.loteId}</Badge>
                </div>
                <p className="mt-1 text-[12px] text-ink-500">
                  {new Date(f.exifTimestamp).toLocaleString("pt-BR")}
                </p>
                <div className="mt-4 flex items-center justify-between">
                  <div>
                    <p className="text-[11px] uppercase tracking-wider text-ink-400">Preço</p>
                    <p className="text-xl font-bold text-ink-900">R$ 29,90</p>
                  </div>
                  <Button
                    onClick={() => comprar(f.id)}
                    disabled={comprandoId === f.id}
                    size="sm"
                  >
                    {comprandoId === f.id ? "Processando..." : "Comprar"}
                  </Button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
