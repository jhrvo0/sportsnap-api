"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/lib/auth";
import {
  obterPerfilPorUsuario, listarPostsPorAutor, listarPerfis, seguir, deixarDeSeguir,
  listarItensFeedPorAutor,
  type PerfilSocial, type PostEsportivo, type PerfilResumo, type ItemFeed,
} from "@/lib/social";
import { listarAtividades, type RegistroAtividadeDto } from "@/lib/atividades";
import { listarTodasFotos, type FotoDto } from "@/lib/marketplace";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Badge } from "@/components/Badge";
import { Alert } from "@/components/Alert";

const BASE = process.env.NEXT_PUBLIC_SOCIAL_URL || "http://localhost:8081";

async function obterPerfilPorId(perfilId: number): Promise<PerfilSocial | null> {
  try {
    const r = await fetch(`${BASE}/api/perfis/${perfilId}`, { cache: "no-store" });
    if (!r.ok) return null;
    return r.json();
  } catch { return null; }
}

type Aba = "posts" | "fotos" | "atividades";

function formatDuracao(segundos: number) {
  const h = Math.floor(segundos / 3600);
  const m = Math.floor((segundos % 3600) / 60);
  return h > 0 ? `${h}h ${m}min` : `${m}min`;
}

function dataRelativa(iso: string) {
  const diff = Date.now() - new Date(iso).getTime();
  const dias = Math.floor(diff / 86400000);
  if (dias === 0) return "hoje";
  if (dias === 1) return "ontem";
  return `há ${dias} dias`;
}

const INTENSIDADE_COR: Record<string, string> = {
  ALTA:  "bg-rose-100 text-rose-700",
  MEDIA: "bg-amber-100 text-amber-700",
  BAIXA: "bg-emerald-100 text-emerald-700",
};

export default function PerfilUsuarioPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const params = useParams();
  const perfilId = parseInt(params.id as string, 10);

  const [perfil, setPerfil]           = useState<PerfilSocial | null>(null);
  const [meuPerfil, setMeuPerfil]     = useState<PerfilSocial | null>(null);
  const [posts, setPosts]             = useState<PostEsportivo[]>([]);
  const [atividades, setAtividades]   = useState<RegistroAtividadeDto[]>([]);
  const [feedItems, setFeedItems]     = useState<ItemFeed[]>([]);
  const [fotosMap, setFotosMap]       = useState<Record<number, string>>({});
  const [aba, setAba]                 = useState<Aba>("posts");
  const [estouSeguindo, setEstouSeguindo] = useState(false);
  const [erro, setErro]               = useState<string | null>(null);
  const [acao, setAcao]               = useState(false);

  useEffect(() => {
    if (carregando) return;
    if (!sessao) { router.replace("/login"); return; }

    // Carrega perfil do alvo
    obterPerfilPorId(perfilId).then(async p => {
      if (!p) { setErro("Perfil não encontrado."); return; }
      setPerfil(p);

      // Carrega posts
      listarPostsPorAutor(perfilId).then(setPosts).catch(() => {});

      // Carrega atividades, licenças e fotos usando o usuarioId do perfil
      if (p.usuarioId?.id) {
        listarAtividades(p.usuarioId.id).then(setAtividades).catch(() => {});
      }
      // Carrega feed items e fotos do autor
      Promise.all([
        listarItensFeedPorAutor(perfilId),
        listarTodasFotos().catch(() => [] as FotoDto[]),
      ]).then(([itens, fotos]) => {
        setFeedItems(itens);
        const mapa: Record<number, string> = {};
        fotos.forEach((f: FotoDto) => { if (f.urlPreview) mapa[f.id] = f.urlPreview; });
        setFotosMap(mapa);
      });
    });

    // Verifica se já estou seguindo
    obterPerfilPorUsuario(sessao.id).then(async mp => {
      if (!mp?.id) return;
      setMeuPerfil(mp);
      // Checa se já segue via lista de seguidos
      const r = await fetch(`${BASE}/api/conexoes/${mp.id.id}/seguindo`, { cache: "no-store" });
      if (r.ok) {
        const lista = await r.json();
        setEstouSeguindo(lista.some((c: any) => c.seguidoId?.id === perfilId));
      }
    }).catch(() => {});
  }, [sessao, carregando, perfilId, router]);

  async function handleSeguir() {
    if (!meuPerfil?.id) return;
    setAcao(true);
    try {
      if (estouSeguindo) {
        await deixarDeSeguir(meuPerfil.id.id, perfilId);
        setEstouSeguindo(false);
        setPerfil(prev => prev ? { ...prev, totalSeguidores: prev.totalSeguidores - 1 } : prev);
      } else {
        await seguir(meuPerfil.id.id, perfilId);
        setEstouSeguindo(true);
        setPerfil(prev => prev ? { ...prev, totalSeguidores: prev.totalSeguidores + 1 } : prev);
      }
    } catch { setErro("Erro ao atualizar conexão."); }
    finally { setAcao(false); }
  }

  const isOwn = meuPerfil?.id?.id === perfilId;

  if (carregando || !sessao) return null;

  return (
    <div className="fade-up max-w-2xl mx-auto">
      {erro && <Alert tone="danger" className="mb-4">{erro}</Alert>}

      {perfil && (
        <>
          {/* Header do perfil */}
          <div className="flex items-start gap-6 py-8">
            <div className="h-20 w-20 shrink-0 rounded-full overflow-hidden bg-gradient-to-br from-accent to-violet-600 flex items-center justify-center ring-2 ring-ink-200">
              {perfil.fotoPerfil
                ? <img src={perfil.fotoPerfil} alt="" className="w-full h-full object-cover" />
                : <span className="text-3xl font-black text-white">{perfil.nomeExibicao.charAt(0).toUpperCase()}</span>}
            </div>

            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-3 flex-wrap mb-2">
                <h1 className="text-xl font-bold text-ink-900">{perfil.nomeExibicao}</h1>
                {!isOwn && meuPerfil && (
                  <Button
                    variant={estouSeguindo ? "secondary" : "accent"}
                    size="sm"
                    onClick={handleSeguir}
                    disabled={acao}
                  >
                    {acao ? "..." : estouSeguindo ? "Seguindo" : "Seguir"}
                  </Button>
                )}
                {isOwn && (
                  <Link href="/perfil">
                    <Button variant="secondary" size="sm">Editar perfil</Button>
                  </Link>
                )}
              </div>

              <div className="flex gap-5 text-sm mb-2">
                <span><strong className="text-ink-900">{posts.length}</strong> <span className="text-ink-500">posts</span></span>
                <span><strong className="text-ink-900">{perfil.totalSeguidores}</strong> <span className="text-ink-500">seguidores</span></span>
                <span><strong className="text-ink-900">{perfil.totalSeguindo}</strong> <span className="text-ink-500">seguindo</span></span>
              </div>

              <div className="text-sm space-y-0.5">
                {perfil.bio && <p className="text-ink-700">{perfil.bio}</p>}
                <div className="flex gap-3 text-ink-500 flex-wrap">
                  {perfil.tipoConta && <Badge tone={perfil.tipoConta === "ATLETA" ? "accent" : "info"}>{perfil.tipoConta === "ATLETA" ? "Atleta" : "Fotógrafo"}</Badge>}
                  {perfil.esporte    && <span>🏅 {perfil.esporte}</span>}
                  {perfil.localidade && <span>📍 {perfil.localidade}</span>}
                </div>
              </div>
            </div>
          </div>

          {/* Abas */}
          <div className="border-t border-ink-200 flex mb-6">
            {([
              { id: "posts",      label: "POSTS",      icon: "✍️" },
              { id: "fotos",      label: "FOTOS",       icon: "📷" },
              { id: "atividades", label: "ATIVIDADES",  icon: "🏃" },
            ] as { id: Aba; label: string; icon: string }[]).map(t => (
              <button
                key={t.id}
                onClick={() => setAba(t.id)}
                className={`flex-1 flex items-center justify-center gap-2 py-3 text-xs font-bold tracking-widest transition-colors border-t-2 -mt-px ${
                  aba === t.id ? "border-ink-900 text-ink-900" : "border-transparent text-ink-400 hover:text-ink-700"
                }`}
              >
                <span>{t.icon}</span> {t.label}
              </button>
            ))}
          </div>

          {/* Posts */}
          {aba === "posts" && (
            <div className="space-y-4">
              {posts.length === 0 ? (
                <div className="py-16 text-center">
                  <span className="text-4xl opacity-20 block mb-3">✍️</span>
                  <p className="text-ink-500">Nenhum post ainda</p>
                </div>
              ) : posts.map(post => (
                <Card key={post.id.id}>
                  <div className="flex items-center justify-between mb-2">
                    {post.esporte && <Badge tone="accent" className="text-xs">#{post.esporte}</Badge>}
                    <span className="text-xs text-ink-400 ml-auto">{dataRelativa(post.criadoEm)}</span>
                  </div>
                  <p className="text-sm text-ink-900 leading-relaxed whitespace-pre-wrap">{post.conteudo}</p>
                </Card>
              ))}
            </div>
          )}

          {/* Fotos */}
          {aba === "fotos" && (
            <div>
              {feedItems.filter(i => i.tipo === "LICENCA_ADQUIRIDA").length === 0 ? (
                <div className="py-16 text-center">
                  <span className="text-4xl opacity-20 block mb-3">📷</span>
                  <p className="text-ink-500">Nenhuma foto adquirida</p>
                </div>
              ) : (
                <div className="grid grid-cols-3 gap-1">
                  {feedItems
                    .filter(i => i.tipo === "LICENCA_ADQUIRIDA")
                    .map(item => (
                    <div key={item.id.id} className="aspect-square rounded-lg overflow-hidden bg-ink-900 relative group">
                      {fotosMap[item.referenciaId]
                        ? <img src={fotosMap[item.referenciaId]} alt="" className="w-full h-full object-cover transition-transform group-hover:scale-105" />
                        : <div className="w-full h-full bg-gradient-to-br from-violet-100 to-rose-100 flex items-center justify-center"><span className="text-2xl opacity-30">📸</span></div>}
                      <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                        <p className="text-white text-xs font-bold">#{item.referenciaId}</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Atividades */}
          {aba === "atividades" && (
            <div className="space-y-3">
              {atividades.length === 0 ? (
                <div className="py-16 text-center">
                  <span className="text-4xl opacity-20 block mb-3">🏃</span>
                  <p className="text-ink-500">Nenhuma atividade registrada</p>
                </div>
              ) : atividades.map(a => (
                <div key={a.id} className="rounded-2xl border border-ink-100 bg-white p-4 shadow-sm">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-ink-900">{a.esporte}</span>
                      {a.intensidade && (
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${INTENSIDADE_COR[a.intensidade] ?? "bg-ink-100 text-ink-600"}`}>
                          {a.intensidade}
                        </span>
                      )}
                    </div>
                    <span className="text-xs text-ink-400">{dataRelativa(a.data || a.criadoEm || "")}</span>
                  </div>
                  <div className="flex gap-4 text-sm text-ink-600">
                    {a.distancia > 0 && <span>📍 {a.distancia.toFixed(1)} km</span>}
                    <span>⏱️ {formatDuracao(a.duracaoSegundos)}</span>
                    {a.esforcoPercebido && <span>💪 Esforço {a.esforcoPercebido}/10</span>}
                  </div>
                  {a.observacoes && (
                    <p className="mt-2 text-xs text-ink-500 italic">"{a.observacoes}"</p>
                  )}
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}
