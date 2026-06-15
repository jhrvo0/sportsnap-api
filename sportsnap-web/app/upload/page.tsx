"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { listarLotes, listarFotos, uploadFotos, removerFoto, type LoteDto, type FotoDto } from "@/lib/marketplace";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Select } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";

export default function UploadPage() {
  return (
    <Suspense fallback={<div className="py-20 text-center text-sm font-medium text-ink-500">Carregando...</div>}>
      <UploadPageContent />
    </Suspense>
  );
}

type Preview = { nome: string; dataUrl: string; thumbBase64: string; tamanho: string };

function UploadPageContent() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [lotes, setLotes] = useState<LoteDto[]>([]);
  const [loteId, setLoteId] = useState(searchParams.get("loteId") || "");
  const [fotos, setFotos] = useState<FotoDto[]>([]);
  const [previews, setPreviews] = useState<Preview[]>([]);
  const [uploading, setUploading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
    if (sessao && sessao.role !== "fotografo") router.replace("/perfil");
  }, [sessao, carregando, router]);

  useEffect(() => {
    if (sessao) listarLotes(sessao.id).then(setLotes).catch(() => setErro("Backend indisponível."));
  }, [sessao]);

  useEffect(() => {
    if (loteId) {
      listarFotos(parseInt(loteId)).then(setFotos).catch(() => setFotos([]));
    } else {
      setFotos([]);
    }
  }, [loteId, aviso]);

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const files = Array.from(e.target.files ?? []);
    if (!files.length) return;
    if (files.some(f => !f.type.startsWith("image/"))) { setErro("Apenas imagens são permitidas."); return; }
    setErro(null);

    let done = 0;
    const loaded: Preview[] = new Array(files.length);
    files.forEach((file, idx) => {
      const reader = new FileReader();
      reader.onload = (ev) => {
        const full = ev.target?.result as string;
        const img = new Image();
        img.onload = () => {
          const MAX = 400;
          const ratio = Math.min(MAX / img.width, MAX / img.height, 1);
          const canvas = document.createElement("canvas");
          canvas.width = Math.round(img.width * ratio);
          canvas.height = Math.round(img.height * ratio);
          canvas.getContext("2d")!.drawImage(img, 0, 0, canvas.width, canvas.height);
          loaded[idx] = { nome: file.name, dataUrl: full, thumbBase64: canvas.toDataURL("image/jpeg", 0.7), tamanho: (file.size / 1024).toFixed(0) + " KB" };
          if (++done === files.length) setPreviews(p => [...p, ...loaded]);
        };
        img.src = full;
      };
      reader.readAsDataURL(file);
    });
  }

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null); setAviso(null);
    if (!loteId) { setErro("Selecione um lote."); return; }
    if (!previews.length) { setErro("Selecione ao menos uma imagem."); return; }
    setUploading(true);
    try {
      // Envia nome + thumbnail base64 para o backend persistir a imagem
      await uploadFotos(parseInt(loteId), previews.map(p => ({ nome: p.nome, urlPreview: p.thumbBase64 })));
      setAviso(`${previews.length} foto(s) enviadas.`);
      setPreviews([]);
      if (fileInputRef.current) fileInputRef.current.value = "";
    } catch { setErro("Erro ao enviar fotos. O backend está rodando?"); }
    finally { setUploading(false); }
  }

  async function remover(id: number) {
    if (!confirm("Remover esta foto do marketplace?")) return;
    try { await removerFoto(id); setAviso(`Foto #${id} removida.`); }
    catch { setErro("Erro ao remover foto."); }
  }

  if (!sessao) return null;

  return (
    <div className="fade-up">
      <PageHeader eyebrow="Fotógrafo" title="Upload de Fotos" subtitle="Selecione imagens do seu computador para publicar no marketplace." />
      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}
      {aviso && <Alert tone="success" className="mb-6">{aviso}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_2fr]">
        <Card title="Enviar Fotos">
          <form onSubmit={enviar} className="space-y-5">
            <Select label="Álbum de Destino" value={loteId} onChange={(e) => { setLoteId(e.target.value); setPreviews([]); }} required>
              <option value="">Selecione um lote ativo...</option>
              {lotes.filter(l => !l.arquivado).map(l => <option key={l.id} value={l.id}>{l.descricao}</option>)}
            </Select>

            <div>
              <label className="block text-sm font-semibold text-ink-700 mb-2">Imagens</label>
              <div
                className="relative flex flex-col items-center justify-center gap-3 rounded-[1.5rem] border-2 border-dashed border-ink-200 bg-ink-50 p-8 text-center cursor-pointer hover:border-accent/50 hover:bg-accent/5 transition-colors"
                onClick={() => fileInputRef.current?.click()}
              >
                <div className="text-4xl opacity-40">🖼️</div>
                <div>
                  <p className="font-semibold text-ink-700 text-sm">Clique para selecionar</p>
                  <p className="text-xs text-ink-400 mt-1">JPG, PNG, WEBP — múltiplos arquivos</p>
                </div>
                <input ref={fileInputRef} type="file" accept="image/*" multiple className="hidden" onChange={handleFileChange} />
              </div>
            </div>

            {previews.length > 0 && (
              <div>
                <p className="text-xs font-semibold text-ink-500 uppercase tracking-widest mb-2">{previews.length} imagem(ns) selecionada(s)</p>
                <div className="grid grid-cols-2 gap-2">
                  {previews.map((p, i) => (
                    <div key={i} className="relative group rounded-xl overflow-hidden border border-ink-100">
                      <img src={p.dataUrl} alt={p.nome} className="w-full aspect-square object-cover" />
                      <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center gap-1 p-2">
                        <p className="text-white text-[10px] font-medium text-center truncate w-full">{p.nome}</p>
                        <p className="text-white/60 text-[10px]">{p.tamanho}</p>
                        <button type="button" onClick={(e) => { e.stopPropagation(); setPreviews(prev => prev.filter((_, j) => j !== i)); }} className="mt-1 text-rose-400 text-[11px] font-bold hover:text-rose-300">Remover</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <Button type="submit" className="w-full" size="lg" disabled={uploading || !previews.length}>
              {uploading ? "Enviando..." : `Publicar ${previews.length > 0 ? previews.length + " foto(s)" : ""}`}
            </Button>
          </form>
        </Card>

        <Card title={`Fotos do Álbum (${fotos.length})`}>
          {!loteId ? (
            <div className="py-20 text-center bg-ink-50 rounded-[2rem] border border-dashed border-ink-200">
              <p className="text-sm text-ink-500">Selecione um lote para ver as fotos.</p>
            </div>
          ) : fotos.length === 0 ? (
            <div className="py-20 text-center bg-ink-50 rounded-[2rem] border border-dashed border-ink-200">
              <span className="text-4xl opacity-20 block mb-4">📷</span>
              <p className="text-sm text-ink-500">Este lote ainda está vazio.</p>
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {fotos.map(f => (
                <div key={f.id} className="group overflow-hidden rounded-[2rem] border border-ink-100 bg-white shadow-sm transition-all hover:shadow-md">
                  <div className="aspect-[4/3] bg-gradient-to-br from-ink-800 to-ink-900 relative overflow-hidden flex items-end p-4">
                    {f.urlPreview && (
                      <img src={f.urlPreview} alt={`Foto #${f.id}`} className="absolute inset-0 w-full h-full object-cover opacity-80" />
                    )}
                    <span className="absolute top-3 right-3">{f.licenciada ? <Badge tone="success">Vendida</Badge> : <Badge tone="info">À venda</Badge>}</span>
                    <div className="relative text-white">
                      <p className="text-[10px] font-mono text-ink-300">ID #{f.id}</p>
                      <p className="text-sm font-medium">{new Date(f.exifTimestamp).toLocaleString("pt-BR")}</p>
                    </div>
                  </div>
                  <div className="p-4 flex items-center justify-between">
                    <p className="text-[11px] text-ink-400 font-mono">{f.exifDetalhes}</p>
                    {!f.licenciada && (
                      <Button size="sm" variant="ghost" className="text-rose-500" onClick={() => remover(f.id)}>Remover</Button>
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
