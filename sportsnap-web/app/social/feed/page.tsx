"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/lib/auth";
import {
  obterPerfilPorUsuario, consultarFeed, curtir, descurtir,
  listarNotificacoes, contarNaoLidas, marcarTodasComoLidas,
  listarPerfis, sugerirConexoes, seguir, criarPost, obterPost,
  listarComentarios, criarComentario, removerComentario,
  type PerfilSocial, type ItemFeed, type Notificacao,
  type PerfilResumo, type SugestaoConexao, type PostEsportivo, type Comentario,
} from "@/lib/social";
import { listarTodasFotos, type FotoDto } from "@/lib/marketplace";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Badge } from "@/components/Badge";
import { Alert } from "@/components/Alert";

const ESPORTES_TAGS = ["Corrida", "Surf", "Skate", "Futebol", "Natação", "Ciclismo", "Musculação"];

const TIPO_ICONE: Record<string, string> = {
  FOTO: "📷",
  NOVA_CONEXAO: "🤝",
  POST_ESPORTIVO: "✍️",
  LICENCA_ADQUIRIDA: "🖼️",
};

function dataRelativa(iso: string) {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1)  return "agora";
  if (mins < 60) return `${mins}m`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)  return `${hrs}h`;
  return `${Math.floor(hrs / 24)}d`;
}

export default function FeedPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();

  const [perfil, setPerfil]         = useState<PerfilSocial | null>(null);
  const [feed, setFeed]             = useState<ItemFeed[]>([]);
  const [posts, setPosts]           = useState<Record<number, PostEsportivo>>({});
  const [todos, setTodos]           = useState<PerfilResumo[]>([]);
  const [notificacoes, setNotifs]   = useState<Notificacao[]>([]);
  const [naoLidas, setNaoLidas]     = useState(0);
  const [sugestoes, setSugestoes]   = useState<SugestaoConexao[]>([]);
  const [curtidas, setCurtidas]     = useState<Set<number>>(new Set());
  const [contagens, setContagens]   = useState<Record<number, number>>({});
  const [mostrarNotifs, setMostrarNotifs] = useState(false);
  const [erro, setErro]             = useState<string | null>(null);
  const [curtidando, setCurtidando] = useState<number | null>(null);
  const [busca, setBusca]           = useState("");
  const [fotosMap, setFotosMap]     = useState<Record<number, string>>({});

  // Comentários por item
  const [comentariosPorItem, setComentariosPorItem] = useState<Record<number, Comentario[]>>({});
  const [itemExpandido, setItemExpandido]           = useState<number | null>(null);
  const [textoComentario, setTextoComentario]       = useState<Record<number, string>>({});
  const [respostaParent, setRespostaParent]         = useState<{ itemId: number; parentId: number; nomeParent: string } | null>(null);

  // Novo post
  const [novoConteudo, setNovoConteudo] = useState("");
  const [novoEsporte, setNovoEsporte]   = useState("");
  const [publicando, setPublicando]     = useState(false);

  useEffect(() => {
    if (carregando) return;
    if (!sessao) { router.replace("/login"); return; }

    obterPerfilPorUsuario(sessao.id).then(async p => {
      if (!p) { router.replace("/perfil"); return; }
      setPerfil(p);
      const pid = p.id!.id;

      const [feedData, todosPerfis, notifs, count] = await Promise.all([
        consultarFeed(pid),
        listarPerfis(),
        listarNotificacoes(pid),
        contarNaoLidas(pid),
      ]);

      setFeed(feedData);
      setTodos(todosPerfis);
      setNotifs(notifs);
      setNaoLidas(count);

      // Mapa de fotos do marketplace (para cards de licença/foto)
      listarTodasFotos().then(fotos => {
        const mapa: Record<number, string> = {};
        fotos.forEach(f => { if (f.urlPreview) mapa[f.id] = f.urlPreview; });
        setFotosMap(mapa);
      }).catch(() => {});

      // Busca conteúdo dos posts esportivos
      const postIds = feedData
        .filter(f => f.tipo === "POST_ESPORTIVO")
        .map(f => f.referenciaId);
      const postsMap: Record<number, PostEsportivo> = {};
      await Promise.all(postIds.map(async id => {
        const p = await obterPost(id);
        if (p) postsMap[id] = p;
      }));
      setPosts(postsMap);

      if (feedData.length === 0) {
        sugerirConexoes(pid).then(setSugestoes).catch(() => {});
      }
    }).catch(() => setErro("Erro ao carregar feed"));
  }, [sessao, carregando, router]);

  function nomeDoId(id: number) {
    return todos.find(p => p.id === id)?.nomeExibicao ?? `Usuário #${id}`;
  }

  function fotoDoId(id: number) {
    return null; // sem foto pública nos resumos ainda
  }

  async function handleCurtir(itemId: number) {
    if (!perfil?.id) return;
    setCurtidando(itemId);
    const uid = perfil.id.id;
    try {
      if (curtidas.has(itemId)) {
        await descurtir(itemId, uid);
        setCurtidas(prev => { const s = new Set(prev); s.delete(itemId); return s; });
        setContagens(prev => ({ ...prev, [itemId]: Math.max(0, (prev[itemId] ?? 1) - 1) }));
      } else {
        await curtir(itemId, uid);
        setCurtidas(prev => new Set([...prev, itemId]));
        setContagens(prev => ({ ...prev, [itemId]: (prev[itemId] ?? 0) + 1 }));
      }
    } catch { setErro("Erro ao curtir"); }
    finally { setCurtidando(null); }
  }

  async function handlePublicar() {
    if (!perfil?.id || !novoConteudo.trim()) return;
    setPublicando(true);
    try {
      const post = await criarPost(perfil.id.id, novoConteudo.trim(), novoEsporte || undefined);
      // Recarrega o feed para incluir o novo post
      const novoFeed = await consultarFeed(perfil.id.id);
      setFeed(novoFeed);
      // Adiciona o post ao mapa
      setPosts(prev => ({ ...prev, [post.id.id]: post }));
      setNovoConteudo("");
      setNovoEsporte("");
    } catch { setErro("Erro ao publicar"); }
    finally { setPublicando(false); }
  }

  async function handleMarcarNotifs() {
    if (!perfil?.id) return;
    await marcarTodasComoLidas(perfil.id.id);
    setNaoLidas(0);
    setNotifs(prev => prev.map(n => ({ ...n, lida: true })));
  }

  async function handleSeguirSugestao(seguidoId: number) {
    if (!perfil?.id) return;
    try {
      await seguir(perfil.id.id, seguidoId);
      setSugestoes(prev => prev.filter(s => s.perfil.id?.id !== seguidoId));
      const novoFeed = await consultarFeed(perfil.id.id);
      setFeed(novoFeed);
    } catch { setErro("Não foi possível seguir"); }
  }

  async function handleExpandirComentarios(itemId: number) {
    if (itemExpandido === itemId) { setItemExpandido(null); return; }
    setItemExpandido(itemId);
    if (!comentariosPorItem[itemId]) {
      const lista = await listarComentarios(itemId);
      setComentariosPorItem(prev => ({ ...prev, [itemId]: lista }));
    }
  }

  async function handleComentar(itemId: number) {
    if (!perfil?.id) return;
    const texto = textoComentario[itemId]?.trim();
    if (!texto) return;
    try {
      const parentId = respostaParent?.itemId === itemId ? respostaParent.parentId : undefined;
      const novo = await criarComentario(itemId, perfil.id.id, texto, parentId);
      setComentariosPorItem(prev => ({
        ...prev,
        [itemId]: [...(prev[itemId] ?? []), novo],
      }));
      setTextoComentario(prev => ({ ...prev, [itemId]: "" }));
      setRespostaParent(null);
    } catch { setErro("Erro ao comentar"); }
  }

  async function handleRemoverComentario(itemId: number, comentarioId: number) {
    if (!perfil?.id) return;
    try {
      await removerComentario(comentarioId, perfil.id.id);
      setComentariosPorItem(prev => ({
        ...prev,
        [itemId]: prev[itemId]?.filter(c => c.id.id !== comentarioId) ?? [],
      }));
    } catch { setErro("Erro ao remover comentário"); }
  }

  if (carregando || !sessao) return null;

  const NOTIF_LABEL: Record<string, string> = {
    CURTIDA: "curtiu seu post",
    NOVO_SEGUIDOR: "começou a te seguir",
    PEDIDO_CONEXAO: "enviou pedido de conexão",
  };

  const resultadosBusca = busca.length >= 2
    ? todos.filter(p => p.nomeExibicao.toLowerCase().includes(busca.toLowerCase()))
    : [];

  return (
    <div className="fade-up max-w-xl mx-auto space-y-4">

      {/* Barra de busca de pessoas */}
      <div className="relative">
        <input
          className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-2.5 pl-10 text-sm text-ink-900 placeholder-ink-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
          placeholder="🔍  Buscar pessoas..."
          value={busca}
          onChange={e => setBusca(e.target.value)}
        />
        {resultadosBusca.length > 0 && (
          <div className="absolute top-full left-0 right-0 mt-1 rounded-2xl border border-ink-100 bg-white shadow-lg z-20 overflow-hidden">
            {resultadosBusca.slice(0, 6).map(p => (
              <Link
                key={p.id}
                href={`/social/usuario/${p.id}`}
                onClick={() => setBusca("")}
                className="flex items-center gap-3 px-4 py-3 hover:bg-ink-50 transition-colors border-b border-ink-50 last:border-0"
              >
                <div className="h-9 w-9 shrink-0 rounded-full bg-gradient-to-br from-accent to-violet-600 flex items-center justify-center font-bold text-white text-sm">
                  {p.nomeExibicao.charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="text-sm font-semibold text-ink-900">{p.nomeExibicao}</p>
                  <p className="text-xs text-ink-400">{p.tipoConta === "ATLETA" ? "Atleta" : "Fotógrafo"}</p>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>

      {/* Sino de notificações */}
      {perfil && (
        <div className="flex items-center justify-between py-2">
          <h2 className="text-xl font-black text-ink-900">Feed</h2>
          <button
            onClick={() => setMostrarNotifs(!mostrarNotifs)}
            className="relative rounded-full p-2 hover:bg-ink-100 transition-colors"
          >
            <span className="text-xl">🔔</span>
            {naoLidas > 0 && (
              <span className="absolute -right-0.5 -top-0.5 grid h-5 w-5 place-items-center rounded-full bg-accent text-[10px] font-black text-white">
                {naoLidas > 9 ? "9+" : naoLidas}
              </span>
            )}
          </button>
        </div>
      )}

      {erro && <Alert tone="danger">{erro}</Alert>}

      {/* Painel de notificações */}
      {mostrarNotifs && perfil && (
        <Card>
          <div className="flex items-center justify-between mb-3">
            <p className="font-bold text-ink-900">Notificações</p>
            {naoLidas > 0 && (
              <Button variant="ghost" size="sm" onClick={handleMarcarNotifs}>Marcar lidas</Button>
            )}
          </div>
          {notificacoes.length === 0 ? (
            <p className="py-4 text-center text-sm text-ink-400">Nenhuma notificação</p>
          ) : (
            <div className="space-y-2">
              {notificacoes.map(n => (
                <div key={n.id.id} className={`flex items-center gap-3 rounded-2xl px-3 py-2.5 ${n.lida ? "bg-ink-50" : "bg-accent-50"}`}>
                  <span>{n.tipo === "CURTIDA" ? "❤️" : n.tipo === "NOVO_SEGUIDOR" ? "👋" : "🔔"}</span>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-ink-900">
                      {n.numAtores > 1 ? `${n.numAtores} pessoas` : "Alguém"} {NOTIF_LABEL[n.tipo]}
                    </p>
                    <p className="text-xs text-ink-400">{dataRelativa(n.criadaEm)}</p>
                  </div>
                  {!n.lida && <div className="h-2 w-2 rounded-full bg-accent shrink-0" />}
                </div>
              ))}
            </div>
          )}
        </Card>
      )}

      {/* Caixa de novo post */}
      {perfil && (
        <Card>
          <div className="flex gap-3">
            <div className="h-9 w-9 shrink-0 rounded-full overflow-hidden bg-gradient-to-br from-accent to-violet-600 flex items-center justify-center">
              {perfil.fotoPerfil
                ? <img src={perfil.fotoPerfil} alt="" className="w-full h-full object-cover" />
                : <span className="text-sm font-black text-white">{sessao.nome.charAt(0).toUpperCase()}</span>}
            </div>
            <div className="flex-1 space-y-3">
              <textarea
                className="w-full rounded-2xl border border-ink-200 bg-ink-50 px-4 py-2.5 text-sm text-ink-900 placeholder-ink-400 focus:border-accent focus:bg-white focus:outline-none focus:ring-2 focus:ring-accent/20 resize-none"
                rows={3}
                placeholder="Compartilhe algo sobre seus treinos, conquistas ou dicas esportivas..."
                value={novoConteudo}
                onChange={e => setNovoConteudo(e.target.value)}
                maxLength={500}
              />
              <div className="flex items-center gap-2 flex-wrap">
                <div className="flex gap-1.5 flex-wrap flex-1">
                  {ESPORTES_TAGS.slice(0, 4).map(s => (
                    <button
                      key={s}
                      onClick={() => setNovoEsporte(novoEsporte === s ? "" : s)}
                      className={`rounded-full px-3 py-1 text-xs font-bold transition-all ${
                        novoEsporte === s ? "bg-accent text-white" : "bg-ink-100 text-ink-500 hover:bg-ink-200"
                      }`}
                    >
                      {s}
                    </button>
                  ))}
                </div>
                <Button
                  variant="accent"
                  size="sm"
                  onClick={handlePublicar}
                  disabled={publicando || !novoConteudo.trim()}
                >
                  {publicando ? "Publicando..." : "Publicar"}
                </Button>
              </div>
            </div>
          </div>
        </Card>
      )}

      {/* Feed vazio → sugestões */}
      {feed.length === 0 && !mostrarNotifs && (
        <Card>
          <div className="py-10 text-center">
            <div className="mx-auto mb-4 grid h-16 w-16 place-items-center rounded-full bg-ink-50">
              <span className="text-2xl">📰</span>
            </div>
            <h3 className="font-bold text-ink-900">Feed vazio</h3>
            <p className="mt-1 text-sm text-ink-500">Siga pessoas para ver as atividades delas</p>
          </div>

          {sugestoes.length > 0 && (
            <div className="mt-4 space-y-2 border-t border-ink-100 pt-4">
              <p className="text-xs font-bold uppercase tracking-widest text-ink-400">Sugestões para você</p>
              {sugestoes.slice(0, 4).map(s => (
                <div key={s.perfil.id?.id} className="flex items-center gap-3 rounded-2xl bg-ink-50 px-3 py-2.5">
                  <div className="h-9 w-9 shrink-0 rounded-full overflow-hidden bg-ink-200 flex items-center justify-center font-bold text-ink-700 text-sm">
                    {s.perfil.fotoPerfil
                      ? <img src={s.perfil.fotoPerfil} alt="" className="w-full h-full object-cover" />
                      : s.perfil.nomeExibicao.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-ink-900 truncate">{s.perfil.nomeExibicao}</p>
                    {s.perfil.esporte && <p className="text-xs text-ink-400">{s.perfil.esporte}</p>}
                  </div>
                  <Button variant="accent" size="sm" onClick={() => handleSeguirSugestao(s.perfil.id!.id)}>
                    Seguir
                  </Button>
                </div>
              ))}
            </div>
          )}
        </Card>
      )}

      {/* Itens do feed */}
      {feed.map(item => {
        const isCurtido  = curtidas.has(item.id.id);
        const numCurtidas = contagens[item.id.id] ?? 0;
        const post = posts[item.referenciaId];
        const autorNome  = nomeDoId(item.autorId.id);
        const autorFoto  = todos.find(p => p.id === item.autorId.id);

        return (
          <Card key={item.id.id}>
            {/* Cabeçalho do post — clicável para ver perfil */}
            <Link href={`/social/usuario/${item.autorId.id}`} className="flex items-center gap-3 mb-3 group">
              <div className="h-9 w-9 shrink-0 rounded-full bg-gradient-to-br from-accent to-violet-600 flex items-center justify-center font-bold text-white text-sm overflow-hidden">
                {autorNome.charAt(0).toUpperCase()}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-ink-900 truncate group-hover:text-accent transition-colors">{autorNome}</p>
                <p className="text-xs text-ink-400">{dataRelativa(item.publicadoEm)}</p>
              </div>
              <span className="text-lg">{TIPO_ICONE[item.tipo]}</span>
            </Link>

            {/* Conteúdo por tipo */}
            {item.tipo === "POST_ESPORTIVO" && post && (
              <div className="mb-3">
                {post.esporte && (
                  <Badge tone="accent" className="mb-2 text-xs">#{post.esporte}</Badge>
                )}
                <p className="text-sm text-ink-900 leading-relaxed whitespace-pre-wrap">{post.conteudo}</p>
              </div>
            )}

            {item.tipo === "FOTO" && (
              <div className="mb-3 rounded-2xl bg-gradient-to-br from-violet-50 to-rose-50 p-4 flex items-center gap-3">
                <span className="text-2xl">📸</span>
                <p className="text-sm text-ink-700 font-medium">Publicou uma nova foto no marketplace</p>
              </div>
            )}

            {item.tipo === "NOVA_CONEXAO" && (
              <div className="mb-3 rounded-2xl bg-ink-50 p-4 flex items-center gap-3">
                <span className="text-2xl">🤝</span>
                <p className="text-sm text-ink-700 font-medium">Fez uma nova conexão na plataforma</p>
              </div>
            )}

            {item.tipo === "LICENCA_ADQUIRIDA" && (
              <div className="mb-3 rounded-2xl overflow-hidden border border-ink-100">
                {fotosMap[item.referenciaId]
                  ? <img src={fotosMap[item.referenciaId]} alt="" className="w-full aspect-[16/9] object-cover" />
                  : <div className="aspect-[16/9] bg-gradient-to-br from-violet-100 to-rose-100 flex items-center justify-center"><span className="text-4xl opacity-40">🖼️</span></div>}
                <div className="px-4 py-3 bg-ink-50">
                  <p className="text-sm font-semibold text-ink-900">Adquiriu uma licença de foto</p>
                  <p className="text-xs text-ink-500 mt-0.5">Foto #{item.referenciaId} desbloqueada em alta resolução</p>
                </div>
              </div>
            )}

            {/* Ações */}
            <div className="flex items-center gap-3 pt-2 border-t border-ink-100">
              <button
                onClick={() => handleCurtir(item.id.id)}
                disabled={curtidando === item.id.id}
                className={`flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm font-semibold transition-all ${
                  isCurtido ? "bg-red-50 text-red-500 hover:bg-red-100" : "text-ink-400 hover:bg-ink-100 hover:text-ink-700"
                }`}
              >
                <span>{isCurtido ? "❤️" : "🤍"}</span>
                {numCurtidas > 0 && <span>{numCurtidas}</span>}
              </button>

              {/* Botão comentar */}
              <button
                onClick={() => handleExpandirComentarios(item.id.id)}
                className="flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm font-semibold text-ink-400 hover:bg-ink-100 hover:text-ink-700 transition-all"
              >
                <span>💬</span>
                {(comentariosPorItem[item.id.id]?.length ?? 0) > 0 && (
                  <span>{comentariosPorItem[item.id.id].length}</span>
                )}
              </button>
            </div>

            {/* Seção de comentários expandida */}
            {itemExpandido === item.id.id && (
              <div className="mt-3 border-t border-ink-100 pt-3 space-y-3">
                {/* Lista de comentários */}
                {(comentariosPorItem[item.id.id] ?? [])
                  .filter(c => !c.resposta)
                  .map(c => {
                    const respostas = (comentariosPorItem[item.id.id] ?? [])
                      .filter(r => r.parentId?.id === c.id.id);
                    return (
                      <div key={c.id.id} className="space-y-2">
                        {/* Comentário raiz */}
                        <div className="flex gap-2 group">
                          <div className="h-7 w-7 shrink-0 rounded-full bg-ink-200 flex items-center justify-center text-xs font-bold text-ink-600">
                            {nomeDoId(c.autorId.id).charAt(0).toUpperCase()}
                          </div>
                          <div className="flex-1 min-w-0 bg-ink-50 rounded-2xl px-3 py-2">
                            <p className="text-xs font-bold text-ink-700">{nomeDoId(c.autorId.id)}</p>
                            <p className="text-sm text-ink-900">{c.conteudo}</p>
                          </div>
                          <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                            <button
                              onClick={() => setRespostaParent({ itemId: item.id.id, parentId: c.id.id, nomeParent: nomeDoId(c.autorId.id) })}
                              className="text-xs text-ink-400 hover:text-accent"
                            >
                              Responder
                            </button>
                            {perfil?.id?.id === c.autorId.id && (
                              <button onClick={() => handleRemoverComentario(item.id.id, c.id.id)} className="text-xs text-red-400 hover:text-red-600">
                                Excluir
                              </button>
                            )}
                          </div>
                        </div>

                        {/* Respostas */}
                        {respostas.map(r => (
                          <div key={r.id.id} className="flex gap-2 ml-9 group">
                            <div className="h-6 w-6 shrink-0 rounded-full bg-accent-100 flex items-center justify-center text-xs font-bold text-accent">
                              {nomeDoId(r.autorId.id).charAt(0).toUpperCase()}
                            </div>
                            <div className="flex-1 min-w-0 bg-accent-50 rounded-2xl px-3 py-2">
                              <p className="text-xs font-bold text-ink-700">{nomeDoId(r.autorId.id)}</p>
                              <p className="text-sm text-ink-900">{r.conteudo}</p>
                            </div>
                            {perfil?.id?.id === r.autorId.id && (
                              <button onClick={() => handleRemoverComentario(item.id.id, r.id.id)} className="text-xs text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100">
                                Excluir
                              </button>
                            )}
                          </div>
                        ))}
                      </div>
                    );
                  })}

                {/* Input de comentário */}
                <div className="flex gap-2">
                  <div className="h-7 w-7 shrink-0 rounded-full bg-gradient-to-br from-accent to-violet-600 flex items-center justify-center text-xs font-bold text-white">
                    {sessao.nome.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 space-y-1">
                    {respostaParent?.itemId === item.id.id && (
                      <div className="flex items-center gap-2 text-xs text-accent bg-accent-50 rounded-lg px-2 py-1">
                        <span>↩ Respondendo {respostaParent.nomeParent}</span>
                        <button onClick={() => setRespostaParent(null)} className="text-ink-400 hover:text-ink-600">✕</button>
                      </div>
                    )}
                    <div className="flex gap-2">
                      <input
                        className="flex-1 rounded-2xl border border-ink-200 bg-ink-50 px-3 py-1.5 text-sm focus:border-accent focus:bg-white focus:outline-none focus:ring-2 focus:ring-accent/20"
                        placeholder={respostaParent?.itemId === item.id.id ? `Respondendo ${respostaParent.nomeParent}...` : "Adicionar comentário..."}
                        value={textoComentario[item.id.id] ?? ""}
                        onChange={e => setTextoComentario(prev => ({ ...prev, [item.id.id]: e.target.value }))}
                        onKeyDown={e => { if (e.key === "Enter") handleComentar(item.id.id); }}
                        maxLength={300}
                      />
                      <button
                        onClick={() => handleComentar(item.id.id)}
                        disabled={!(textoComentario[item.id.id]?.trim())}
                        className="rounded-full bg-accent px-3 py-1.5 text-xs font-bold text-white disabled:opacity-40 hover:bg-accent-600 transition-colors"
                      >
                        Enviar
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </Card>
        );
      })}
    </div>
  );
}
