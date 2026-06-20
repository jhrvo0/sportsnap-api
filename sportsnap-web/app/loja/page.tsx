"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { useCart } from "@/lib/cart";
import {
  desfavoritar,
  favoritar,
  listarFavoritos,
  listarTodasFotos,
  type FotoDto,
} from "@/lib/marketplace";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { WatermarkedImage } from "@/components/WatermarkedImage";

export default function LojaPage() {
  const { sessao, carregando } = useAuth();
  const { cart, addToCart } = useCart();
  const router = useRouter();

  const [fotos, setFotos] = useState<FotoDto[]>([]);
  const [favoritosIds, setFavoritosIds] = useState<Set<number>>(new Set());
  const [aviso, setAviso] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (carregando) return;
    if (!sessao) {
      router.replace("/login");
      return;
    }
    if (sessao.role !== "atleta") {
      router.replace("/perfil");
      return;
    }

    listarTodasFotos()
      .then(setFotos)
      .catch(() => setErro("Nao foi possivel carregar as fotos. O backend esta rodando?"));

    listarFavoritos(sessao.id)
      .then((favs) => setFavoritosIds(new Set(favs.map((f) => f.id))))
      .catch(() => {});
  }, [sessao, carregando, router]);

  async function toggleFavorito(fotoId: number) {
    if (!sessao) return;
    try {
      if (favoritosIds.has(fotoId)) {
        await desfavoritar(sessao.id, fotoId);
        setFavoritosIds((prev) => {
          const next = new Set(prev);
          next.delete(fotoId);
          return next;
        });
      } else {
        await favoritar(sessao.id, fotoId);
        setFavoritosIds((prev) => new Set(prev).add(fotoId));
      }
    } catch {
      setErro("Erro ao atualizar favorito.");
    }
  }

  if (!sessao && !carregando) return null;

  const fotosNoCarrinhoIds = new Set(cart.map((item) => item.id));
  const sugestoes = fotos.filter((f) => !f.licenciada && f.disponivel && !fotosNoCarrinhoIds.has(f.id));

  return (
    <div className="fade-up">
      <div className="mb-6 flex items-start justify-between">
        <PageHeader
          eyebrow="Marketplace"
          title="Fotos Sugeridas"
          subtitle="O motor de busca encontrou fotos suas baseadas nos seus check-ins."
        />
        {cart.length > 0 && (
          <Button onClick={() => router.push("/carrinho")} className="shrink-0 bg-violet-600 text-white shadow-lg shadow-violet-500/30 hover:bg-violet-700">
            Ver Carrinho ({cart.length})
          </Button>
        )}
      </div>

      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}
      {aviso && <Alert tone="success" className="mb-6">{aviso}</Alert>}

      {sugestoes.length === 0 ? (
        <Card>
          <div className="py-20 text-center">
            <div className="mx-auto mb-6 grid h-20 w-20 place-items-center rounded-full bg-ink-50">
              <span className="text-3xl">Buscar</span>
            </div>
            <h3 className="text-xl font-bold text-ink-900">Sem fotos no momento</h3>
            <p className="mx-auto mt-2 max-w-xs text-ink-500">
              Aguarde os fotografos subirem as fotos desta sessao ou verifique se voce ja adicionou tudo ao carrinho.
            </p>
          </div>
        </Card>
      ) : (
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
          {sugestoes.map((foto) => (
            <article key={foto.id} className="surface-elev group overflow-hidden rounded-[2.5rem] transition-all hover:-translate-y-2">
              <div className="relative aspect-[4/5] overflow-hidden bg-ink-100">
                {foto.urlPreview ? (
                  <WatermarkedImage src={foto.urlPreview} alt={`Foto #${foto.id}`} className="h-full w-full object-cover" />
                ) : (
                  <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-ink-200 to-ink-300">
                    <div className="rotate-45 select-none text-4xl font-black uppercase tracking-[1em] text-white/20">SportSnap</div>
                  </div>
                )}
                <div className="absolute right-4 top-4">
                  <Badge tone="accent" className="glass border-white/20 text-white">Previa</Badge>
                </div>
                <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/80 via-black/40 to-transparent p-6 text-white">
                  <p className="text-[10px] font-bold uppercase tracking-widest opacity-70">Timestamp EXIF</p>
                  <p className="text-sm font-medium">{new Date(foto.exifTimestamp).toLocaleString("pt-BR")}</p>
                </div>
              </div>
              <div className="p-7">
                <div className="mb-4 flex items-center justify-between">
                  <h3 className="text-lg font-bold text-ink-900">Foto #{foto.id}</h3>
                  <div className="flex items-center gap-3">
                    <button
                      type="button"
                      onClick={() => toggleFavorito(foto.id)}
                      className="text-xl transition-transform hover:scale-125"
                      title={favoritosIds.has(foto.id) ? "Desfavoritar" : "Favoritar"}
                    >
                      {favoritosIds.has(foto.id) ? "Favorita" : "Favoritar"}
                    </button>
                    <div className="text-right">
                      <p className="text-[10px] font-bold uppercase tracking-widest text-ink-400">Preco</p>
                      <p className="text-2xl font-black text-ink-900">R$ {Number(foto.preco ?? 29.9).toFixed(2).replace(".", ",")}</p>
                    </div>
                  </div>
                </div>
                <Button className="w-full" size="lg" onClick={() => addToCart(foto)}>
                  Adicionar ao Carrinho
                </Button>
                <p className="mt-4 text-center text-[11px] text-ink-400">
                  Inclui Split Financeiro (70% Fotografo / 30% Plataforma)
                </p>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
