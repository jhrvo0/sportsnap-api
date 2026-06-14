"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { db } from "@/lib/db";
import { useAuth } from "@/lib/auth";
import { type Foto, type Licenca } from "@/lib/api";
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

  function carregar() {
    if (!sessao) return;
    setErro(null);
    if (sessao.role === "atleta") {
      setLicencas(db.filter("licencas", l => l.atletaId === sessao.id));
      setFotos(db.get("fotos"));
    }
  }

  useEffect(() => {
    if (sessao) carregar();
  }, [sessao]);

  function cancelarLicenca(id: number) {
    if (confirm("Tem certeza que deseja cancelar esta licença? O valor será reembolsado.")) {
      db.update("licencas", id, { cancelada: true });
      setAviso(`Licença #${id} cancelada com sucesso.`);
      carregar();
    }
  }

  if (!sessao) return null;

  const ativas = licencas.filter((l) => !l.cancelada);
  const totalGasto = ativas.reduce((acc, l) => acc + Number(l.preco), 0);
  const fotosMap = new Map(fotos.map((f) => [f.id, f]));

  return (
    <div className="fade-up">
      <section
        className="relative mb-10 overflow-hidden rounded-[2.5rem] px-8 py-12 sm:px-12 shadow-2xl"
        style={{
          background: "linear-gradient(135deg, #0a0a0c 0%, #1d1d1f 50%, #0a0a0c 100%)",
          colorScheme: "light",
        }}
      >
        <div className="absolute -right-24 -top-24 h-64 w-64 rounded-full bg-accent/30 blur-[80px]" />
        <div className="absolute -bottom-32 -left-20 h-64 w-64 rounded-full bg-violet-500/20 blur-[80px]" />
        <div className="relative">
          <p className="mb-3 text-[11px] font-bold uppercase tracking-[0.2em] text-accent">
            {sessao.role === "atleta" ? "Perfil do Atleta" : "Perfil do Fotógrafo"}
          </p>
          <div className="flex flex-wrap items-center gap-6">
            <div className="grid h-20 w-20 place-items-center rounded-[1.5rem] bg-gradient-to-br from-accent to-violet-600 text-3xl font-black text-white shadow-inner">
              {sessao.nome.charAt(0).toUpperCase()}
            </div>
            <div>
              <h1 className="text-4xl font-bold tracking-tight text-white mb-1">
                {sessao.nome}
              </h1>
              <p className="text-ink-400 font-medium">{sessao.email}</p>
            </div>
            <div className="ml-auto rounded-full px-4 py-1.5 text-[12px] font-bold tracking-widest uppercase bg-white/10 text-white/80 border border-white/5 backdrop-blur-sm">
              ID #{sessao.id}
            </div>
          </div>
        </div>
      </section>

      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}
      {aviso && <Alert tone="success" className="mb-6">{aviso}</Alert>}

      {sessao.role === "atleta" ? (
        <div className="grid gap-6 md:grid-cols-3">
          <Card className="bg-ink-900 text-white border-none shadow-xl relative overflow-hidden group cursor-pointer" onClick={() => router.push("/atletas")}>
             <div className="absolute inset-0 bg-gradient-to-br from-accent/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
             <div className="relative">
                <p className="text-[10px] font-bold uppercase tracking-widest text-accent mb-2">Visão Geral</p>
                <h3 className="text-2xl font-bold mb-1">Dashboard</h3>
                <p className="text-ink-400 text-sm mb-6">Acesse sua Carta Oficial e sincronize seu XP.</p>
                <div className="text-accent font-semibold text-sm">Ir para Dashboard →</div>
             </div>
          </Card>

          <Card title="Financeiro">
            <p className="text-[11px] font-bold uppercase tracking-widest text-ink-400 mb-1">Total Investido</p>
            <p className="text-4xl font-black text-ink-900">
              R$ {totalGasto.toFixed(2).replace('.', ',')}
            </p>
            <div className="mt-4 flex items-center justify-between pt-4 border-t border-ink-100">
               <span className="text-sm font-medium text-ink-600">Licenças Ativas</span>
               <Badge tone="success">{ativas.length}</Badge>
            </div>
          </Card>

          <div className="md:col-span-3 mt-4">
            <Card title="Minhas Licenças" description="Fotos de alta resolução adquiridas no Marketplace.">
              {licencas.length === 0 ? (
                <div className="py-20 text-center bg-ink-50 rounded-[2rem] border border-dashed border-ink-200">
                   <span className="text-4xl opacity-20 block mb-4">🖼️</span>
                   <p className="text-sm font-medium text-ink-600">Sua galeria está vazia.</p>
                   <Button variant="secondary" className="mt-4" onClick={() => router.push("/loja")}>Explorar Marketplace</Button>
                </div>
              ) : (
                <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                  {licencas.map((l) => {
                    const foto = fotosMap.get(l.fotoId);
                    return (
                      <div key={l.id} className="overflow-hidden rounded-[2rem] border border-ink-100 bg-white shadow-sm transition-all hover:shadow-md">
                        <div className="aspect-[4/3] relative bg-ink-100 p-4 flex flex-col justify-between">
                           {/* Placeholder for real image */}
                           <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-violet-100 to-rose-100 opacity-50">
                              <span className="text-2xl opacity-50">📸</span>
                           </div>
                           
                           <div className="relative flex justify-between items-start">
                              <Badge tone="info" className="bg-white/80 backdrop-blur border-none">Alta Resolução</Badge>
                              {l.cancelada ? <Badge tone="danger">Cancelada</Badge> : <Badge tone="success">Licenciada</Badge>}
                           </div>
                           
                           <div className="relative text-ink-900 font-mono text-[10px] font-bold bg-white/60 w-fit px-2 py-1 rounded-md backdrop-blur">
                              FOTO #{l.fotoId}
                           </div>
                        </div>
                        
                        <div className="p-5">
                          <div className="flex items-center justify-between mb-4">
                            <span className="text-xl font-bold text-ink-900">
                              R$ {Number(l.preco).toFixed(2).replace('.', ',')}
                            </span>
                            <span className="text-[11px] font-medium text-ink-400">
                              {new Date(l.adquiridaEm).toLocaleDateString("pt-BR")}
                            </span>
                          </div>
                          
                          <div className="flex gap-2">
                             {!l.cancelada && (
                               <Button variant="secondary" size="sm" className="flex-1">Baixar Original</Button>
                             )}
                             {!l.cancelada && (
                               <Button variant="ghost" size="sm" className="text-rose-500" onClick={() => cancelarLicenca(l.id)}>Devolver</Button>
                             )}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </Card>
          </div>
        </div>
      ) : (
        <Card title="Painel do Fotógrafo" className="bg-ink-50 border-none">
          <div className="grid gap-4 sm:grid-cols-2">
            <div
              onClick={() => router.push("/lotes")}
              className="rounded-[2rem] bg-white p-6 shadow-sm transition-all hover:shadow-md hover:scale-[1.02] cursor-pointer border border-ink-100"
            >
              <div className="h-12 w-12 rounded-full bg-accent/10 flex items-center justify-center text-xl mb-4">📁</div>
              <p className="text-[11px] font-bold uppercase tracking-widest text-ink-400 mb-1">Gestão</p>
              <h3 className="text-xl font-bold text-ink-900">Meus Lotes</h3>
              <p className="text-sm text-ink-500 mt-2">Crie e organize álbuns por sessão e local.</p>
            </div>
            
            <div
              onClick={() => router.push("/upload")}
              className="rounded-[2rem] bg-white p-6 shadow-sm transition-all hover:shadow-md hover:scale-[1.02] cursor-pointer border border-ink-100"
            >
              <div className="h-12 w-12 rounded-full bg-violet-500/10 flex items-center justify-center text-xl mb-4">☁️</div>
              <p className="text-[11px] font-bold uppercase tracking-widest text-ink-400 mb-1">Publicação</p>
              <h3 className="text-xl font-bold text-ink-900">Upload de Fotos</h3>
              <p className="text-sm text-ink-500 mt-2">Suba imagens em alta e extraia metadados EXIF.</p>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
}
