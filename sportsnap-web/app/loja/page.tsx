"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { db } from "@/lib/db";
import { useAuth } from "@/lib/auth";
import { PageHeader } from "@/components/PageHeader";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";
import { Card } from "@/components/Card";

export default function LojaPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  
  const [fotos, setFotos] = useState(db.get("fotos"));
  const [licencas, setLicencas] = useState(db.get("licencas"));
  const [checkins, setCheckins] = useState(db.get("checkins"));
  
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [comprandoId, setComprandoId] = useState<number | null>(null);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
    if (sessao && sessao.role !== "atleta") router.replace("/perfil");
  }, [sessao, carregando, router]);

  async function comprar(fotoId: number) {
    if (!sessao) return;
    setComprandoId(fotoId);
    setAviso(null);
    
    await new Promise(resolve => setTimeout(resolve, 1500));

    db.add("licencas", {
      atletaId: sessao.id,
      fotoId,
      preco: 29.9,
      adquiridaEm: new Date().toISOString(),
      cancelada: false
    });

    setLicencas([...db.get("licencas")]);
    setAviso(`Licença adquirida! Esta foto validou seu treino e liberou a Sincronização.`);
    setComprandoId(null);
  }

  if (!sessao) return null;

  const minhasFotosIds = new Set(licencas.filter(l => l.atletaId === sessao.id && !l.cancelada).map(l => l.fotoId));
  
  // Lógica simplificada do motor de sugestão para o protótipo de alta fidelidade.
  // Exibe fotos compatíveis com a janela de check-in do atleta.
  const meusCheckins = checkins.filter(c => c.atletaId === sessao.id && !c.cancelado);
  const sugestoes = fotos.filter(f => {
    if (minhasFotosIds.has(f.id)) return false;
    // No protótipo, qualquer check-in ativo habilita fotos correspondentes.
    return meusCheckins.length > 0; 
  });

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Marketplace"
        title="Fotos Sugeridas"
        subtitle="O motor de busca encontrou fotos suas baseadas nos seus check-ins."
      />

      {aviso && (
        <div className="mb-8 rounded-[2rem] bg-ink-900 p-6 text-white shadow-xl animate-fade-in flex items-center justify-between">
           <div className="flex items-center gap-4">
              <span className="text-2xl">💎</span>
              <p className="font-medium">{aviso}</p>
           </div>
           <Button size="sm" variant="secondary" onClick={() => router.push("/atletas")}>Ir para Dashboard</Button>
        </div>
      )}

      {sugestoes.length === 0 ? (
        <Card>
          <div className="py-20 text-center">
            <div className="mx-auto mb-6 grid h-20 w-20 place-items-center rounded-full bg-ink-50">
              <span className="text-3xl">🔍</span>
            </div>
            <h3 className="text-xl font-bold text-ink-900">Sem fotos no momento</h3>
            <p className="mt-2 text-ink-500 max-w-xs mx-auto">
              {meusCheckins.length === 0 
                ? "Você precisa fazer um check-in para que possamos sugerir fotos." 
                : "Aguarde os fotógrafos subirem as fotos desta sessão."}
            </p>
            {meusCheckins.length === 0 && (
               <Button className="mt-6" onClick={() => router.push("/checkin")}>📍 Fazer Check-in agora</Button>
            )}
          </div>
        </Card>
      ) : (
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
          {sugestoes.map((f) => (
            <article
              key={f.id}
              className="surface-elev group overflow-hidden rounded-[2.5rem] transition-all hover:-translate-y-2"
            >
              <div className="relative aspect-[4/5] overflow-hidden bg-ink-100">
                {/* Prévia com marca d'água */}
                <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-ink-200 to-ink-300">
                   <div className="rotate-45 text-4xl font-black text-white/20 select-none uppercase tracking-[1em]">SportSnap</div>
                </div>
                
                <div className="absolute right-4 top-4 flex gap-2">
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
                  <div className="text-right">
                    <p className="text-[10px] font-bold uppercase tracking-widest text-ink-400">Preço</p>
                    <p className="text-2xl font-black text-ink-900">R$ 29,90</p>
                  </div>
                </div>
                
                <Button
                  className="w-full"
                  size="lg"
                  onClick={() => comprar(f.id)}
                  disabled={comprandoId === f.id}
                >
                  {comprandoId === f.id ? "Validando..." : "Comprar Licença"}
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
