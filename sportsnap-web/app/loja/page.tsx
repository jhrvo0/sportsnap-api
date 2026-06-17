"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { useCart } from "@/lib/cart";
// Merged the duplicated imports into one clean line
import { listarTodasFotos, comprarLicenca, favoritar, desfavoritar, listarFavoritos, type FotoDto } from "@/lib/marketplace";
import { PageHeader } from "@/components/PageHeader";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";
import { Card } from "@/components/Card";
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
    if (!sessao) { router.replace("/login"); return; }
    if (sessao.role !== "atleta") { router.replace("/perfil"); return; }
    
    listarTodasFotos()
      .then(setFotos)
      .catch(() => setErro("Não foi possível carregar as fotos. O backend está rodando?"));
      
    listarFavoritos(sessao.id)
      .then(favs => setFavoritosIds(new Set(favs.map(f => f.id))))
      .catch(() => {});
  }, [sessao, carregando, router]);

  // Kept from main: You need this for the heart button
  async function toggleFavorito(fotoId: number) {
    if (!sessao) return;
    try {
      if (favoritosIds.has(fotoId)) {
        await desfavoritar(sessao.id, fotoId);
        setFavoritosIds(prev => { const n = new Set(prev); n.delete(fotoId); return n; });
      } else {
        await favoritar(sessao.id, fotoId);
        setFavoritosIds(prev => new Set(prev).add(fotoId));
      }
    } catch { 
        setErro("Erro ao atualizar favorito."); 
    }
  }

  // Removed `comprar` function here to prevent state errors and favor your new Cart system

  if (!sessao && !carregando) return null;

  const fotosNoCarrinhoIds = new Set(cart.map((item) => item.id));
  const sugestoes = fotos.filter(f => !f.licenciada && f.disponivel && !fotosNoCarrinhoIds.has(f.id));

  return (
    <div className="fade-up">
      <div className="flex justify-between items-start mb-6">
        <PageHeader
          eyebrow="Marketplace"
          title="Fotos Sugeridas"
          subtitle="O motor de busca encontrou fotos suas baseadas nos seus check-ins."
        />
        {cart.length > 0 && (
          <Button onClick={() => router.push("/carrinho")} className="shrink-0 bg-violet-600 hover:bg-violet-700 text-white shadow-lg shadow-violet-500/30">
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
              <span className="text-3xl">🔍</span>
            </div>
            <h3 className="text-xl font-bold text-ink-900">Sem fotos no momento</h3>
            <p className="mt-2 text-ink-500 max-w-xs mx-auto">
              Aguarde os fotógrafos subirem as fotos desta sessão ou verifique se você já adicionou tudo ao carrinho.
            </p>
          </div>
        </Card>
      ) : (
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
          {sugestoes.map((f) => (
            <article key={f.id} className="surface-elev group overflow-hidden rounded-[2.5rem] transition-all hover:-translate-y-2">
              <div className="relative aspect-[4/5] overflow-hidden bg-ink-100">
                {f.urlPreview ? (
                  <WatermarkedImage src={f.urlPreview} alt={`Foto #${f.id}`} className="w-full h-full object-cover" />
                ) : (
                  <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-ink-200 to-ink-300">
                    <div className="rotate-45 text-4xl font-black text-white/20 select-none uppercase tracking-[1em]">SportSnap</div>
                  </div>
                )}
                <div className="absolute right-4 top-4">
                  <Badge tone="accent" className="glass border-white/20 text-white">Prévia</Badge>
                </div>
                <div className="absolute bottom-0 left-0 right-0 p-6 bg-gradient-to-t from-black/80 via-black/40 to-transparent text-white">
                  <p className="text-[10px] font-bold uppercase tracking-widest opacity-70">Timestamp EXIF</p>
                  <p className="font-medium text-sm">{new Date(f.exifTimestamp).toLocaleString("pt-BR")}</p>
                </div>
              </div>
              <div className="p-7">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-bold text-ink-900">Foto #{f.id}</h3>
                  <div className="flex items-center gap-3">
                    <button
                      onClick={() => toggleFavorito(f.id)}
                      className="text-xl transition-transform hover:scale-125"
                      title={favoritosIds.has(f.id) ? "Desfavoritar" : "Favoritar"}
                    >
                      {favoritosIds.has(f.id) ? "❤️" : "🤍"}
                    </button>
                    <div className="text-right">
                      <p className="text-[10px] font-bold uppercase tracking-widest text-ink-400">Preço</p>
                      <p className="text-2xl font-black text-ink-900">R$ {Number(f.preco ?? 29.90).toFixed(2).replace(".", ",")}</p>
                    </div>
                  </div>
                </div>
                <Button className="w-full" size="lg" onClick={() => addToCart(f)}>
                  Adicionar ao Carrinho
                </Button>
                <p className="mt-4 text-center text-[11px] text-ink-400">
                  Inclui Split Financeiro (70% Fotógrafo / 30% Plataforma)
                </p>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}