"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { listarFavoritos, desfavoritar, comprarLicenca, listarTodasFotos, type FavoritoFotoDto } from "@/lib/marketplace";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";
import { WatermarkedImage } from "@/components/WatermarkedImage";

export default function FavoritosPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();

  const [favoritos, setFavoritos] = useState<FavoritoFotoDto[]>([]);
  const [aviso, setAviso] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [comprandoId, setComprandoId] = useState<number | null>(null);

  useEffect(() => {
    if (carregando) return;
    if (!sessao) { router.replace("/login"); return; }
    if (sessao.role !== "atleta") { router.replace("/perfil"); return; }
    carregar();
  }, [sessao, carregando]);

  async function carregar() {
    if (!sessao) return;
    try {
      const favs = await listarFavoritos(sessao.id);
      setFavoritos(favs);
    } catch {
      setErro("Não foi possível carregar os favoritos. O backend está rodando?");
    }
  }

  async function removerFavorito(fotoId: number) {
    if (!sessao) return;
    try {
      await desfavoritar(sessao.id, fotoId);
      setFavoritos(prev => prev.filter(f => f.id !== fotoId));
      setAviso("Foto removida dos favoritos.");
    } catch {
      setErro("Erro ao remover favorito.");
    }
  }

  async function comprar(fotoId: number) {
    if (!sessao) return;
    setComprandoId(fotoId);
    setAviso(null);
    try {
      await comprarLicenca(sessao.id, fotoId);
      setAviso("Licença adquirida! Foto desbloqueada.");
      carregar();
    } catch {
      setErro("Erro ao comprar licença.");
    } finally {
      setComprandoId(null);
    }
  }

  if (!sessao && !carregando) return null;

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Atleta"
        title="Minhas Fotos Favoritas"
        subtitle="Fotos que você salvou para comprar depois."
      />

      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}
      {aviso && <Alert tone="success" className="mb-6">{aviso}</Alert>}

      {favoritos.length === 0 ? (
        <Card>
          <div className="py-20 text-center">
            <div className="mx-auto mb-6 grid h-20 w-20 place-items-center rounded-full bg-ink-50">
              <span className="text-3xl">🤍</span>
            </div>
            <h3 className="text-xl font-bold text-ink-900">Nenhum favorito ainda</h3>
            <p className="mt-2 text-ink-500 max-w-xs mx-auto">
              Vá à loja e clique no coração para salvar fotos que te interessam.
            </p>
            <Button className="mt-6" onClick={() => router.push("/loja")}>
              Explorar Marketplace
            </Button>
          </div>
        </Card>
      ) : (
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
          {favoritos.map(f => (
            <article key={f.id} className="surface-elev overflow-hidden rounded-[2.5rem]">
              <div className="relative aspect-[4/5] overflow-hidden bg-ink-100">
                {f.urlPreview ? (
                  f.licenciada ? (
                    <img src={f.urlPreview} alt={`Foto #${f.id}`} className="w-full h-full object-cover" draggable={false} />
                  ) : (
                    <WatermarkedImage src={f.urlPreview} alt={`Foto #${f.id}`} className="w-full h-full object-cover" />
                  )
                ) : (
                  <div className="w-full h-full bg-gradient-to-br from-ink-200 to-ink-300 flex items-center justify-center">
                    <span className="text-4xl opacity-30">📸</span>
                  </div>
                )}
                <div className="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/70 to-transparent text-white">
                  <p className="text-[10px] font-bold uppercase tracking-widest opacity-70">EXIF</p>
                  <p className="text-sm font-medium">{new Date(f.exifTimestamp).toLocaleString("pt-BR")}</p>
                </div>
              </div>
              <div className="p-6 space-y-3">
                <div className="flex items-center justify-between">
                  <h3 className="font-bold text-ink-900">Foto #{f.id}</h3>
                  <span className="text-2xl font-black text-ink-900">R$ 29,90</span>
                </div>
                <div className="flex gap-2">
                  {!f.licenciada && (
                    <Button
                      className="flex-1"
                      onClick={() => comprar(f.id)}
                      disabled={comprandoId === f.id}
                    >
                      {comprandoId === f.id ? "Comprando..." : "Comprar Licença"}
                    </Button>
                  )}
                  <Button
                    variant="ghost"
                    className="text-rose-500"
                    onClick={() => removerFavorito(f.id)}
                  >
                    ❤️ Remover
                  </Button>
                </div>
                {f.licenciada && (
                  <p className="text-center text-sm font-medium text-emerald-600">✓ Já licenciada</p>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
