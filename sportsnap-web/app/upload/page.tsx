"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { db } from "@/lib/db";
import { useAuth } from "@/lib/auth";
import { type Foto, type Lote } from "@/lib/api";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Select, Textarea } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";

export default function UploadPage() {
  return (
    <Suspense fallback={<div className="py-20 text-center text-sm font-medium text-ink-500">Carregando upload...</div>}>
      <UploadPageContent />
    </Suspense>
  );
}

function UploadPageContent() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();
  const initialLoteId = searchParams.get('loteId') || "";
  
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [loteId, setLoteId] = useState(initialLoteId);
  const [caminhos, setCaminhos] = useState("");
  const [fotos, setFotos] = useState<Foto[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
    if (sessao && sessao.role !== "fotografo") router.replace("/perfil");
  }, [sessao, carregando, router]);

  useEffect(() => {
    if (sessao) {
      setLotes(db.filter("lotes", l => l.fotografoId === sessao.id && !l.arquivado));
    }
  }, [sessao]);

  useEffect(() => {
    if (loteId) {
       setFotos(db.filter("fotos", f => f.loteId === parseInt(loteId, 10)));
    } else {
       setFotos([]);
    }
  }, [loteId, aviso]); // Re-run when aviso changes (after upload/delete)

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setAviso(null);
    
    const lista = caminhos.split("\n").map((s) => s.trim()).filter(Boolean);
    if (!loteId || lista.length === 0) {
      setErro("Escolha um lote e informe ao menos um caminho.");
      return;
    }

    setUploading(true);
    // Simulate upload delay for EXIF extraction
    await new Promise(resolve => setTimeout(resolve, 1500));

    lista.forEach((_, index) => {
       db.add("fotos", {
         loteId: parseInt(loteId, 10),
         urlPreview: "",
         urlOriginal: "",
         exifTimestamp: new Date(Date.now() - (index * 60000)).toISOString(),
         exifDetalhes: "Câmera: Sony A7III | Lente: 85mm f/1.8",
         licenciada: false,
         removida: false
       });
    });

    setAviso(`${lista.length} foto(s) enviadas. Metadados EXIF extraídos automaticamente.`);
    setCaminhos("");
    setUploading(false);
  }

  function remover(id: number) {
    if (confirm("Deseja realmente excluir esta foto do marketplace?")) {
      db.update("fotos", id, { removida: true });
      setAviso(`Foto #${id} removida do marketplace.`);
    }
  }

  if (!sessao) return null;

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Fotógrafo"
        title="Upload de Fotos"
        subtitle="Envie fotos para um lote ativo. O motor extrai EXIF automaticamente para matching com atletas."
      />

      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}
      {aviso && <Alert tone="success" className="mb-6">{aviso}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_2fr]">
        <Card title="Upload em Massa">
          <form onSubmit={enviar} className="space-y-5">
            <Select
              label="Álbum de Destino"
              value={loteId}
              onChange={(e) => setLoteId(e.target.value)}
              required
            >
              <option value="">Selecione um lote ativo...</option>
              {lotes.map((l) => (
                <option key={l.id} value={l.id}>{l.descricao}</option>
              ))}
            </Select>
            <Textarea
              label="Arquivos (Simulação)"
              value={caminhos}
              onChange={(e) => setCaminhos(e.target.value)}
              placeholder={"/DCIM/100/IMG_001.jpg\n/DCIM/100/IMG_002.jpg"}
              hint="Cole os caminhos ou nomes dos arquivos (um por linha) para simular o upload."
              rows={6}
              required
            />
            <Button type="submit" className="w-full" size="lg" disabled={uploading}>
              {uploading ? "Processando EXIF..." : "Iniciar Upload"}
            </Button>
          </form>
        </Card>

        <Card title={`Fotos do Álbum Selecionado (${fotos.filter(f => !f.removida).length})`}>
          {!loteId ? (
            <div className="py-20 text-center bg-ink-50 rounded-[2rem] border border-dashed border-ink-200">
               <p className="text-sm text-ink-500">Selecione um lote na barra lateral para ver ou subir fotos.</p>
            </div>
          ) : fotos.filter(f => !f.removida).length === 0 ? (
            <div className="py-20 text-center bg-ink-50 rounded-[2rem] border border-dashed border-ink-200">
               <span className="text-4xl opacity-20 mb-4 block">📸</span>
               <p className="text-sm text-ink-500">Este lote ainda está vazio.</p>
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {fotos.filter(f => !f.removida).map((f) => (
                <div key={f.id} className="group overflow-hidden rounded-[2rem] border border-ink-100 bg-white shadow-sm transition-all hover:shadow-md">
                  <div className="aspect-[4/3] bg-gradient-to-br from-ink-800 to-ink-900 relative p-4 flex flex-col justify-end">
                     <span className="absolute top-4 right-4"><Badge tone="accent">Prévia</Badge></span>
                     <div className="text-white">
                        <p className="text-[10px] font-mono text-ink-400">ID #{f.id}</p>
                        <p className="text-sm font-medium">{new Date(f.exifTimestamp).toLocaleString("pt-BR")}</p>
                     </div>
                  </div>
                  <div className="p-4 flex items-center justify-between">
                    <div>
                      {f.licenciada ? (
                        <Badge tone="success">Vendida</Badge>
                      ) : (
                        <Badge tone="info">Disponível no Marketplace</Badge>
                      )}
                    </div>
                    {!f.licenciada && (
                      <Button size="sm" variant="ghost" className="text-rose-500" onClick={() => remover(f.id)}>
                        Remover
                      </Button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
