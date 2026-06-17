"use client";

import { useEffect, useRef, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import {
  listarFotos, uploadFotos, removerFoto,
  definirPreco, disponibilizar, indisponibilizar,
  type FotoDto, type LoteDto,
} from "@/lib/marketplace";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";

const BASE = "http://localhost:8082/api";

type Preview = { nome: string; dataUrl: string; thumbBase64: string; tamanho: string };

export default function AlbumPage() {
  const { id } = useParams<{ id: string }>();
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [lote, setLote] = useState<LoteDto | null>(null);
  const [fotos, setFotos] = useState<FotoDto[]>([]);
  const [previews, setPreviews] = useState<Preview[]>([]);
  const [uploading, setUploading] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (carregando) return;
    if (!sessao) { router.replace("/login"); return; }
    if (sessao.role !== "fotografo") { router.replace("/perfil"); return; }
    carregar();
  }, [sessao, carregando]);

  async function carregar() {
    try {
      const r = await fetch(`${BASE}/lotes?fotografoId=${sessao?.id}`);
      if (r.ok) {
        const lotes: LoteDto[] = await r.json();
        const encontrado = lotes.find(l => l.id === parseInt(id));
        if (encontrado) setLote(encontrado);
      }
      setFotos(await listarFotos(parseInt(id)));
    } catch { setErro("Erro ao carregar álbum."); }
  }

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
    if (!previews.length) { setErro("Selecione ao menos uma imagem."); return; }
    setUploading(true); setErro(null); setAviso(null);
    try {
      await uploadFotos(parseInt(id), previews.map(p => ({ nome: p.nome, urlPreview: p.thumbBase64 })));
      setAviso(`${previews.length} foto(s) enviadas.`);
      setPreviews([]);
      if (fileInputRef.current) fileInputRef.current.value = "";
      setFotos(await listarFotos(parseInt(id)));
    } catch { setErro("Erro ao enviar fotos. O backend está rodando?"); }
    finally { setUploading(false); }
  }

  async function remover(fotoId: number) {
    if (!confirm("Remover esta foto do marketplace?")) return;
    try { await removerFoto(fotoId); setAviso(`Foto #${fotoId} removida.`); setFotos(await listarFotos(parseInt(id))); }
    catch { setErro("Erro ao remover foto."); }
  }

  async function alterarPreco(fotoId: number, novoPreco: string) {
    const valor = parseFloat(novoPreco.replace(",", "."));
    if (isNaN(valor) || valor <= 0) { setErro("Preço inválido."); return; }
    try { await definirPreco(fotoId, valor); setAviso(`Preço atualizado.`); setFotos(await listarFotos(parseInt(id))); }
    catch { setErro("Erro ao atualizar preço."); }
  }

  async function toggleDisponivel(foto: FotoDto) {
    try {
      if (foto.disponivel) { await indisponibilizar(foto.id); setAviso(`Foto #${foto.id} retirada do marketplace.`); }
      else { await disponibilizar(foto.id); setAviso(`Foto #${foto.id} disponibilizada.`); }
      setFotos(await listarFotos(parseInt(id)));
    } catch { setErro("Erro ao alterar disponibilidade."); }
  }

  if (!sessao) return null;

  const arquivado = lote?.arquivado ?? false;

  return (
    <div className="fade-up">
      <div className="mb-6 flex items-center gap-3">
        <button onClick={() => router.push("/lotes")} className="text-sm text-ink-400 hover:text-ink-700 font-medium">
          ← Meus Álbuns
        </button>
      </div>

      <PageHeader
        eyebrow={arquivado ? "Álbum Arquivado" : "Fotógrafo"}
        title={lote?.descricao ?? `Álbum #${id}`}
        subtitle={`Spot #${lote?.spotId} · Criado em ${lote ? new Date(lote.criadoEm).toLocaleDateString("pt-BR") : "—"}`}
      />

      {aviso && <Alert tone="success" className="mb-6">{aviso}</Alert>}
      {erro && <Alert tone="danger" className="mb-6">{erro}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_2fr]">
        {/* Upload — só disponível se não arquivado */}
        <Card title={arquivado ? "Álbum Arquivado" : "Adicionar Fotos"}>
          {arquivado ? (
            <div className="py-10 text-center text-ink-400 text-sm">
              Este álbum está arquivado.<br />Reative-o em Meus Álbuns para adicionar fotos.
            </div>
          ) : (
            <form onSubmit={enviar} className="space-y-4">
              <div
                className="flex flex-col items-center justify-center gap-3 rounded-[1.5rem] border-2 border-dashed border-ink-200 bg-ink-50 p-8 cursor-pointer hover:border-accent/50 hover:bg-accent/5 transition-colors"
                onClick={() => fileInputRef.current?.click()}
              >
                <div className="text-4xl opacity-40">🖼️</div>
                <p className="font-semibold text-ink-700 text-sm">Clique para selecionar</p>
                <p className="text-xs text-ink-400">JPG, PNG, WEBP — múltiplos arquivos</p>
                <input ref={fileInputRef} type="file" accept="image/*" multiple className="hidden" onChange={handleFileChange} />
              </div>

              {previews.length > 0 && (
                <div className="grid grid-cols-2 gap-2">
                  {previews.map((p, i) => (
                    <div key={i} className="relative group rounded-xl overflow-hidden border border-ink-100">
                      <img src={p.dataUrl} alt={p.nome} className="w-full aspect-square object-cover" />
                      <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center gap-1 p-2">
                        <p className="text-white text-[10px] truncate w-full text-center">{p.nome}</p>
                        <button type="button" onClick={(e) => { e.stopPropagation(); setPreviews(prev => prev.filter((_, j) => j !== i)); }} className="text-rose-400 text-[11px] font-bold">Remover</button>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              <Button type="submit" className="w-full" disabled={uploading || !previews.length}>
                {uploading ? "Enviando..." : previews.length > 0 ? `Publicar ${previews.length} foto(s)` : "Publicar"}
              </Button>
            </form>
          )}
        </Card>

        {/* Galeria de fotos do álbum */}
        <Card title={`Fotos do Álbum (${fotos.length})`}>
          {fotos.length === 0 ? (
            <div className="py-20 text-center bg-ink-50 rounded-[2rem] border border-dashed border-ink-200">
              <span className="text-4xl opacity-20 block mb-4">📷</span>
              <p className="text-sm text-ink-500">Este álbum ainda está vazio.</p>
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {fotos.map(f => (
                <div key={f.id} className="overflow-hidden rounded-[2rem] border border-ink-100 bg-white shadow-sm">
                  {/* Imagem */}
                  <div className="aspect-[4/3] relative overflow-hidden bg-ink-900">
                    {f.urlPreview && <img src={f.urlPreview} alt={`Foto #${f.id}`} className="w-full h-full object-cover" draggable={false} />}
                    <span className="absolute top-2 right-2">
                      {f.licenciada
                        ? <Badge tone="success">Vendida</Badge>
                        : f.disponivel
                          ? <Badge tone="info">À venda</Badge>
                          : <Badge tone="warning">Indisponível</Badge>}
                    </span>
                  </div>

                  {/* Controles */}
                  <div className="p-4 space-y-3">
                    <div className="flex items-center justify-between">
                      <p className="text-xs text-ink-400 font-mono">#{f.id} · {new Date(f.exifTimestamp).toLocaleDateString("pt-BR")}</p>
                      {!f.licenciada && (
                        <div className="flex gap-1">
                          <button
                            onClick={() => toggleDisponivel(f)}
                            className={`text-[11px] font-bold px-2 py-0.5 rounded-full transition-colors ${f.disponivel ? "bg-emerald-100 text-emerald-700 hover:bg-emerald-200" : "bg-ink-100 text-ink-500 hover:bg-ink-200"}`}
                          >
                            {f.disponivel ? "Disponível" : "Indisponível"}
                          </button>
                          <button onClick={() => remover(f.id)} className="text-[11px] font-bold text-rose-500 hover:text-rose-700 px-2 py-0.5 rounded-full hover:bg-rose-50 transition-colors">
                            Apagar
                          </button>
                        </div>
                      )}
                      {f.licenciada && <span className="text-[11px] text-emerald-600 font-medium">Vendida</span>}
                    </div>

                    {!f.licenciada && (
                      <div className="flex items-center gap-2">
                        <span className="text-[11px] text-ink-500 font-medium whitespace-nowrap">Preço R$</span>
                        <input
                          type="number"
                          defaultValue={Number(f.preco ?? 29.90).toFixed(2)}
                          step="0.01"
                          min="0.01"
                          className="flex-1 text-sm font-bold border border-ink-200 rounded-lg px-2 py-1 focus:outline-none focus:ring-2 focus:ring-accent/30"
                          onBlur={(e) => {
                            if (parseFloat(e.target.value) !== Number(f.preco)) {
                              alterarPreco(f.id, e.target.value);
                            }
                          }}
                        />
                      </div>
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
