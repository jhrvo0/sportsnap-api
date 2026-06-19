"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/lib/auth";
import {
  obterPerfilPorUsuario, consultarFeed, curtir, descurtir,
  listarNotificacoes, contarNaoLidas, marcarTodasComoLidas,
  listarPerfis, sugerirConexoes, seguir,
  type PerfilSocial, type ItemFeed, type Notificacao, type PerfilResumo, type SugestaoConexao,
} from "@/lib/social";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Badge } from "@/components/Badge";
import { Alert } from "@/components/Alert";

const TIPO_ICONE: Record<string, string> = {
  FOTO: "📷",
  NOVA_CONEXAO: "🤝",
};

const TIPO_LABEL: Record<string, string> = {
  FOTO: "publicou uma foto",
  NOVA_CONEXAO: "fez uma nova conexão",
};

const NOTIF_ICONE: Record<string, string> = {
  CURTIDA: "❤️",
  NOVO_SEGUIDOR: "👋",
  PEDIDO_CONEXAO: "🔔",
};

const NOTIF_LABEL: Record<string, string> = {
  CURTIDA: "curtiu seu item",
  NOVO_SEGUIDOR: "começou a te seguir",
  PEDIDO_CONEXAO: "enviou pedido de conexão",
};

export default function FeedPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();

  const [perfil, setPerfil] = useState<PerfilSocial | null>(null);
  const [feed, setFeed] = useState<ItemFeed[]>([]);
  const [todos, setTodos] = useState<PerfilResumo[]>([]);
  const [notificacoes, setNotificacoes] = useState<Notificacao[]>([]);
  const [naoLidas, setNaoLidas] = useState(0);
  const [sugestoes, setSugestoes] = useState<SugestaoConexao[]>([]);
  const [curtidas, setCurtidas] = useState<Set<number>>(new Set());
  const [contagemCurtidas, setContagemCurtidas] = useState<Record<number, number>>({});
  const [mostrarNotifs, setMostrarNotifs] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [curtidando, setCurtidando] = useState<number | null>(null);

  useEffect(() => {
    if (carregando) return;
    if (!sessao) { router.replace("/login"); return; }

    obterPerfilPorUsuario(sessao.id).then(async p => {
      if (!p) { router.replace("/social/perfil"); return; }
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
      setNotificacoes(notifs);
      setNaoLidas(count);

      if (feedData.length === 0) {
        sugerirConexoes(pid).then(setSugestoes).catch(() => {});
      }
    }).catch(() => setErro("Erro ao carregar feed"));
  }, [sessao, carregando, router]);

  function nomeDoId(id: number) {
    return todos.find(p => p.id === id)?.nomeExibicao ?? `Usuário ${id}`;
  }

  function dataRelativa(iso: string) {
    const diff = Date.now() - new Date(iso).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return "agora";
    if (mins < 60) return `${mins}m`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h`;
    return `${Math.floor(hrs / 24)}d`;
  }

  async function handleCurtir(itemId: number) {
    if (!perfil?.id) return;
    setCurtidando(itemId);
    const uid = perfil.id.id;
    try {
      if (curtidas.has(itemId)) {
        await descurtir(itemId, uid);
        setCurtidas(prev => { const s = new Set(prev); s.delete(itemId); return s; });
        setContagemCurtidas(prev => ({ ...prev, [itemId]: Math.max(0, (prev[itemId] ?? 1) - 1) }));
      } else {
        await curtir(itemId, uid);
        setCurtidas(prev => new Set([...prev, itemId]));
        setContagemCurtidas(prev => ({ ...prev, [itemId]: (prev[itemId] ?? 0) + 1 }));
      }
    } catch {
      setErro("Erro ao curtir item");
    } finally {
      setCurtidando(null);
    }
  }

  async function handleMarcarNotifs() {
    if (!perfil?.id) return;
    await marcarTodasComoLidas(perfil.id.id);
    setNaoLidas(0);
    setNotificacoes(prev => prev.map(n => ({ ...n, lida: true })));
  }

  async function handleSeguirSugestao(seguidoId: number) {
    if (!perfil?.id) return;
    try {
      await seguir(perfil.id.id, seguidoId);
      setSugestoes(prev => prev.filter(s => s.perfil.id?.id !== seguidoId));
      if (sugestoes.length <= 1) {
        const feedData = await consultarFeed(perfil.id.id);
        setFeed(feedData);
      }
    } catch {
      setErro("Não foi possível seguir");
    }
  }

  if (carregando) return null;
  if (!sessao) return null;

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Social"
        title="Feed"
        subtitle="Atividades de quem você segue"
      >
        {/* Sino de notificações */}
        {perfil && (
          <button
            onClick={() => setMostrarNotifs(!mostrarNotifs)}
            className="relative rounded-full p-2 hover:bg-white/20 transition-colors"
          >
            <span className="text-2xl">🔔</span>
            {naoLidas > 0 && (
              <span className="absolute -right-1 -top-1 grid h-5 w-5 place-items-center rounded-full bg-accent text-[10px] font-black text-white">
                {naoLidas > 9 ? "9+" : naoLidas}
              </span>
            )}
          </button>
        )}
      </PageHeader>

      <div className="mx-auto max-w-2xl px-6 py-10 space-y-6">
        {erro && <Alert tone="danger">{erro}</Alert>}

        {/* Painel de notificações */}
        {mostrarNotifs && perfil && (
          <Card>
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-bold text-ink-900">Notificações</h3>
              {naoLidas > 0 && (
                <Button variant="ghost" size="sm" onClick={handleMarcarNotifs}>
                  Marcar todas lidas
                </Button>
              )}
            </div>
            {notificacoes.length === 0 ? (
              <p className="py-4 text-center text-sm text-ink-500">Nenhuma notificação</p>
            ) : (
              <div className="space-y-2">
                {notificacoes.map(n => (
                  <div
                    key={n.id.id}
                    className={`flex items-center gap-3 rounded-2xl px-4 py-3 ${n.lida ? "bg-ink-50" : "bg-accent-50"}`}
                  >
                    <span className="text-lg">{NOTIF_ICONE[n.tipo]}</span>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-ink-900">
                        {n.numAtores > 1 ? `${n.numAtores} pessoas` : "Alguém"} {NOTIF_LABEL[n.tipo]}
                      </p>
                      <p className="text-xs text-ink-500">{dataRelativa(n.criadaEm)}</p>
                    </div>
                    {!n.lida && <div className="h-2 w-2 rounded-full bg-accent shrink-0" />}
                  </div>
                ))}
              </div>
            )}
          </Card>
        )}

        {/* Feed vazio — sugestões */}
        {feed.length === 0 && !mostrarNotifs && (
          <Card>
            <div className="py-12 text-center">
              <div className="mx-auto mb-6 grid h-20 w-20 place-items-center rounded-full bg-ink-50">
                <span className="text-3xl">📰</span>
              </div>
              <h3 className="text-xl font-bold text-ink-900">Seu feed está vazio</h3>
              <p className="mt-2 text-sm text-ink-500">Siga pessoas para ver as atividades delas aqui</p>
            </div>

            {sugestoes.length > 0 && (
              <div className="mt-6 space-y-3">
                <p className="text-sm font-semibold text-ink-700">Quem você pode conhecer</p>
                {sugestoes.slice(0, 4).map(s => (
                  <div key={s.perfil.id?.id} className="flex items-center gap-3 rounded-2xl bg-ink-50 px-4 py-3">
                    <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-ink-200 font-bold text-ink-700">
                      {s.perfil.nomeExibicao.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-ink-900 truncate">{s.perfil.nomeExibicao}</p>
                      {s.perfil.esporte && <p className="text-xs text-ink-500">{s.perfil.esporte}</p>}
                    </div>
                    <Button
                      variant="accent"
                      size="sm"
                      onClick={() => handleSeguirSugestao(s.perfil.id!.id)}
                    >
                      Seguir
                    </Button>
                  </div>
                ))}
                <div className="pt-2 text-center">
                  <Link href="/social/conexoes">
                    <Button variant="secondary" size="sm">Ver mais sugestões</Button>
                  </Link>
                </div>
              </div>
            )}
          </Card>
        )}

        {/* Itens do feed */}
        {feed.map(item => {
          const isCurtido = curtidas.has(item.id.id);
          const numCurtidas = contagemCurtidas[item.id.id] ?? 0;
          return (
            <Card key={item.id.id}>
              {/* Cabeçalho */}
              <div className="flex items-center gap-3 mb-4">
                <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-ink-900 font-bold text-white text-sm">
                  {nomeDoId(item.autorId.id).charAt(0).toUpperCase()}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-ink-900 truncate">{nomeDoId(item.autorId.id)}</p>
                  <p className="text-xs text-ink-500">
                    {TIPO_LABEL[item.tipo]} · {dataRelativa(item.publicadoEm)}
                  </p>
                </div>
                <span className="text-2xl">{TIPO_ICONE[item.tipo]}</span>
              </div>

              {/* Tipo */}
              <div className="rounded-2xl bg-ink-50 px-4 py-3 mb-4">
                <Badge tone={item.tipo === "FOTO" ? "info" : "success"} className="mb-2">
                  {item.tipo === "FOTO" ? "Nova foto publicada" : "Nova conexão"}
                </Badge>
                <p className="text-sm text-ink-600">
                  {item.tipo === "FOTO"
                    ? `Foto #${item.referenciaId} adicionada ao catálogo`
                    : `Conectou-se com perfil #${item.referenciaId}`}
                </p>
              </div>

              {/* Curtida */}
              <div className="flex items-center gap-3">
                <button
                  onClick={() => handleCurtir(item.id.id)}
                  disabled={curtidando === item.id.id}
                  className={`flex items-center gap-2 rounded-full px-4 py-2 text-sm font-semibold transition-all ${
                    isCurtido
                      ? "bg-red-50 text-red-500 hover:bg-red-100"
                      : "bg-ink-50 text-ink-500 hover:bg-ink-100"
                  }`}
                >
                  <span>{isCurtido ? "❤️" : "🤍"}</span>
                  <span>{numCurtidas > 0 ? numCurtidas : ""}</span>
                  <span>{isCurtido ? "Curtido" : "Curtir"}</span>
                </button>
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
}
