"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { useCart } from "@/lib/cart";
import { comprarLicenca, obterAssinatura, AssinaturaDto } from "@/lib/marketplace";
import { PageHeader } from "@/components/PageHeader";
import { useEffect } from "react";
import { Button } from "@/components/Button";
import { Card } from "@/components/Card";
import { WatermarkedImage } from "@/components/WatermarkedImage";
import { Alert } from "@/components/Alert";

export default function CarrinhoPage() {
  const { sessao, carregando } = useAuth();
  const { cart, removeFromCart, clearCart } = useCart();
  const router = useRouter();

  const [finalizando, setFinalizando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [assinatura, setAssinatura] = useState<AssinaturaDto | null>(null);

  useEffect(() => {
    if (sessao) {
      obterAssinatura(sessao.id).then(setAssinatura).catch(console.error);
    }
  }, [sessao]);

  if (carregando) return null;
  if (!sessao) { router.replace("/login"); return null; }

  const totalCotas = assinatura && assinatura.status !== "INATIVA" ? assinatura.saldoCotas : 0;
  const numCotasUsadas = Math.min(totalCotas, cart.length);
  const numPagas = cart.length - numCotasUsadas;
  const total = numPagas * 29.90;

  async function checkout() {
    if (!sessao) return;
    setFinalizando(true);
    setErro(null);
    try {
      // Compra cada foto no carrinho
      await Promise.all(cart.map((foto) => comprarLicenca(sessao.id, foto.id)));
      clearCart();
      router.push("/perfil?checkout=sucesso");
    } catch {
      setErro("Houve um problema ao processar o pagamento. Nenhuma licença foi emitida.");
      setFinalizando(false);
    }
  }

  return (
    <div className="fade-up">
      <div className="flex justify-between items-start mb-6">
        <PageHeader eyebrow="Checkout" title="Seu Carrinho" subtitle="Revise as fotos selecionadas antes de finalizar." />
      </div>

      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}

      {cart.length === 0 ? (
        <Card>
          <div className="py-20 text-center">
            <div className="mx-auto mb-6 grid h-20 w-20 place-items-center rounded-full bg-ink-50">
              <span className="text-3xl">🛒</span>
            </div>
            <h3 className="text-xl font-bold text-ink-900">Seu carrinho está vazio</h3>
            <p className="mt-2 text-ink-500 max-w-xs mx-auto mb-6">
              Volte para a loja para encontrar suas melhores fotos.
            </p>
            <Button onClick={() => router.push("/loja")}>Explorar Loja</Button>
          </div>
        </Card>
      ) : (
        <div className="grid gap-8 lg:grid-cols-3">
          <div className="lg:col-span-2 space-y-4">
            {cart.map((foto) => (
              <div key={foto.id} className="flex items-center gap-4 bg-white p-4 rounded-[1.5rem] border border-ink-100 shadow-sm">
                <div className="h-24 w-24 rounded-[1rem] overflow-hidden bg-ink-100 shrink-0">
                  {foto.urlPreview ? (
                    <WatermarkedImage src={foto.urlPreview} alt={`Foto #${foto.id}`} className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-ink-200 to-ink-300">
                      <span className="text-white/20">📷</span>
                    </div>
                  )}
                </div>
                <div className="flex-1">
                  <h4 className="font-bold text-ink-900">Licença de Imagem #{foto.id}</h4>
                  <p className="text-sm text-ink-500">Uso pessoal em redes sociais</p>
                </div>
                <div className="text-right">
                  <p className="font-black text-lg text-ink-900">R$ 29,90</p>
                  <button onClick={() => removeFromCart(foto.id)} className="text-xs font-bold text-rose-500 hover:text-rose-600">
                    Remover
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="lg:col-span-1">
            <Card title="Resumo do Pedido" className="sticky top-6">
              <div className="space-y-4 mb-6">
                <div className="flex justify-between text-ink-600">
                  <span>Itens ({cart.length})</span>
                  <span>R$ {(cart.length * 29.9).toFixed(2).replace(".", ",")}</span>
                </div>
                {numCotasUsadas > 0 && (
                  <div className="flex justify-between text-indigo-600 font-bold bg-indigo-50 p-2 rounded-lg -mx-2">
                    <span>Cotas do SportSnap Pass ({numCotasUsadas})</span>
                    <span>- R$ {(numCotasUsadas * 29.9).toFixed(2).replace(".", ",")}</span>
                  </div>
                )}
                <div className="flex justify-between text-ink-600">
                  <span>Taxa de serviço</span>
                  <span>R$ 0,00</span>
                </div>
                <div className="border-t border-ink-100 pt-4 flex justify-between font-black text-xl text-ink-900">
                  <span>Total</span>
                  <span>R$ {total.toFixed(2).replace(".", ",")}</span>
                </div>
              </div>
              <Button size="lg" className="w-full bg-emerald-600 hover:bg-emerald-700 shadow-emerald-500/30" onClick={checkout} disabled={finalizando}>
                {finalizando ? "Processando..." : (numCotasUsadas === cart.length ? "Finalizar usando Cotas" : "Finalizar Compra Segura")}
              </Button>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
}
