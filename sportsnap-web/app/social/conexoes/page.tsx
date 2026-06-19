"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import {
  obterPerfilPorUsuario, listarPerfis, listarSeguindo, listarSeguidores,
  listarPedidosPendentes, sugerirConexoes, seguir, deixarDeSeguir,
  aprovarPedido, recusarPedido, bloquear,
  type PerfilSocial, type PerfilResumo, type Conexao, type PedidoConexao, type SugestaoConexao,
} from "@/lib/social";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Badge } from "@/components/Badge";
import { Alert } from "@/components/Alert";

type Aba = "seguindo" | "seguidores" | "pedidos" | "sugestoes";

export default function ConexoesPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();

  const [perfil, setPerfil] = useState<PerfilSocial | null>(null);
  const [aba, setAba] = useState<Aba>("seguindo");
  const [todos, setTodos] = useState<PerfilResumo[]>([]);
  const [seguindo, setSeguindo] = useState<Conexao[]>([]);
  const [seguidores, setSeguidores] = useState<Conexao[]>([]);
  const [pedidos, setPedidos] = useState<PedidoConexao[]>([]);
  const [sugestoes, setSugestoes] = useState<SugestaoConexao[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [carregandoAcao, setCarregandoAcao] = useState<number | null>(null);

  useEffect(() => {
    if (carregando) return;
    if (!sessao) { router.replace("/login"); return; }

    obterPerfilPorUsuario(sessao.id).then(p => {
      if (!p) { router.replace("/social/perfil"); return; }
      setPerfil(p);
      const pid = p.id!.id;
      Promise.all([
        listarPerfis(),
        listarSeguindo(pid),
        listarSeguidores(pid),
        listarPedidosPendentes(pid),
        sugerirConexoes(pid),
      ]).then(([t, snd, seg, ped, sug]) => {
        setTodos(t);
        setSeguindo(snd);
        setSeguidores(seg);
        setPedidos(ped);
        setSugestoes(sug);
      }).catch(() => setErro("Erro ao carregar conexões"));
    });
  }, [sessao, carregando, router]);

  function nomeDoId(id: number) {
    return todos.find(p => p.id === id)?.nomeExibicao ?? `Usuário ${id}`;
  }

  async function handleSeguir(seguidoId: number) {
    if (!perfil?.id) return;
    setCarregandoAcao(seguidoId);
    try {
      await seguir(perfil.id.id, seguidoId);
      setSugestoes(prev => prev.filter(s => s.perfil.id?.id !== seguidoId));
      setPerfil(prev => prev ? { ...prev, totalSeguindo: prev.totalSeguindo + 1 } : prev);
      setAviso("Seguindo!");
      setTimeout(() => setAviso(null), 3000);
    } catch {
      setErro("Não foi possível seguir. Tente novamente.");
    } finally {
      setCarregandoAcao(null);
    }
  }

  async function handleDeixarDeSeguir(seguidoId: number) {
    if (!perfil?.id) return;
    setCarregandoAcao(seguidoId);
    try {
      await deixarDeSeguir(perfil.id.id, seguidoId);
      setSeguindo(prev => prev.filter(c => c.seguidoId.id !== seguidoId));
      setPerfil(prev => prev ? { ...prev, totalSeguindo: prev.totalSeguindo - 1 } : prev);
    } catch {
      setErro("Erro ao deixar de seguir.");
    } finally {
      setCarregandoAcao(null);
    }
  }

  async function handleAprovar(pedidoId: number, solicitanteId: number) {
    setCarregandoAcao(pedidoId);
    try {
      await aprovarPedido(pedidoId);
      setPedidos(prev => prev.filter(p => p.id.id !== pedidoId));
      setPerfil(prev => prev ? { ...prev, totalSeguidores: prev.totalSeguidores + 1 } : prev);
      setAviso("Pedido aprovado!");
      setTimeout(() => setAviso(null), 3000);
    } catch {
      setErro("Erro ao aprovar pedido.");
    } finally {
      setCarregandoAcao(null);
    }
  }

  async function handleRecusar(pedidoId: number) {
    setCarregandoAcao(pedidoId);
    try {
      await recusarPedido(pedidoId);
      setPedidos(prev => prev.filter(p => p.id.id !== pedidoId));
    } catch {
      setErro("Erro ao recusar pedido.");
    } finally {
      setCarregandoAcao(null);
    }
  }

  async function handleBloquear(bloqueadoId: number) {
    if (!perfil?.id) return;
    setCarregandoAcao(bloqueadoId);
    try {
      await bloquear(perfil.id.id, bloqueadoId);
      setSeguindo(prev => prev.filter(c => c.seguidoId.id !== bloqueadoId));
      setSeguidores(prev => prev.filter(c => c.seguidorId.id !== bloqueadoId));
      setSugestoes(prev => prev.filter(s => s.perfil.id?.id !== bloqueadoId));
    } catch {
      setErro("Erro ao bloquear.");
    } finally {
      setCarregandoAcao(null);
    }
  }

  if (carregando) return null;
  if (!sessao) return null;

  const ABAS: { id: Aba; label: string; count: number }[] = [
    { id: "seguindo",   label: "Seguindo",   count: seguindo.length },
    { id: "seguidores", label: "Seguidores",  count: seguidores.length },
    { id: "pedidos",    label: "Pedidos",     count: pedidos.length },
    { id: "sugestoes",  label: "Sugestões",   count: sugestoes.length },
  ];

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Social"
        title="Conexões"
        subtitle="Gerencie quem você segue e suas sugestões"
      />

      <div className="mx-auto max-w-3xl px-6 py-10 space-y-6">
        {erro && <Alert tone="danger" className="mb-2">{erro}</Alert>}
        {aviso && <Alert tone="success" className="mb-2">{aviso}</Alert>}

        {/* Stats */}
        {perfil && (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            {ABAS.map(a => (
              <button
                key={a.id}
                onClick={() => setAba(a.id)}
                className={`rounded-2xl p-4 text-center transition-all ${
                  aba === a.id
                    ? "bg-ink-900 text-white shadow-md"
                    : "surface hover:bg-ink-100"
                }`}
              >
                <p className="text-2xl font-black">{a.count}</p>
                <p className={`text-xs font-semibold mt-1 ${aba === a.id ? "text-white/70" : "text-ink-500"}`}>
                  {a.label}
                </p>
              </button>
            ))}
          </div>
        )}

        {/* Aba: Seguindo */}
        {aba === "seguindo" && (
          <Card title="Quem você segue">
            {seguindo.length === 0 ? (
              <p className="py-8 text-center text-sm text-ink-500">Você ainda não segue ninguém. Veja as sugestões!</p>
            ) : (
              <div className="space-y-3">
                {seguindo.map(c => (
                  <div key={c.id.id} className="flex items-center gap-3 rounded-2xl bg-ink-50 px-4 py-3">
                    <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-ink-200 font-bold text-ink-700">
                      {nomeDoId(c.seguidoId.id).charAt(0).toUpperCase()}
                    </div>
                    <p className="flex-1 font-medium text-ink-900">{nomeDoId(c.seguidoId.id)}</p>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDeixarDeSeguir(c.seguidoId.id)}
                      disabled={carregandoAcao === c.seguidoId.id}
                    >
                      Deixar de seguir
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => handleBloquear(c.seguidoId.id)}
                      disabled={carregandoAcao === c.seguidoId.id}
                    >
                      Bloquear
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </Card>
        )}

        {/* Aba: Seguidores */}
        {aba === "seguidores" && (
          <Card title="Seus seguidores">
            {seguidores.length === 0 ? (
              <p className="py-8 text-center text-sm text-ink-500">Ainda ninguém te segue. Compartilhe seu perfil!</p>
            ) : (
              <div className="space-y-3">
                {seguidores.map(c => (
                  <div key={c.id.id} className="flex items-center gap-3 rounded-2xl bg-ink-50 px-4 py-3">
                    <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-ink-200 font-bold text-ink-700">
                      {nomeDoId(c.seguidorId.id).charAt(0).toUpperCase()}
                    </div>
                    <p className="flex-1 font-medium text-ink-900">{nomeDoId(c.seguidorId.id)}</p>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => handleBloquear(c.seguidorId.id)}
                      disabled={carregandoAcao === c.seguidorId.id}
                    >
                      Bloquear
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </Card>
        )}

        {/* Aba: Pedidos */}
        {aba === "pedidos" && (
          <Card title="Pedidos de conexão pendentes">
            {pedidos.length === 0 ? (
              <p className="py-8 text-center text-sm text-ink-500">Nenhum pedido pendente</p>
            ) : (
              <div className="space-y-3">
                {pedidos.map(p => (
                  <div key={p.id.id} className="flex items-center gap-3 rounded-2xl bg-accent-50 px-4 py-3">
                    <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-accent-100 font-bold text-accent">
                      {nomeDoId(p.solicitanteId.id).charAt(0).toUpperCase()}
                    </div>
                    <p className="flex-1 font-medium text-ink-900">{nomeDoId(p.solicitanteId.id)}</p>
                    <Button
                      variant="accent"
                      size="sm"
                      onClick={() => handleAprovar(p.id.id, p.solicitanteId.id)}
                      disabled={carregandoAcao === p.id.id}
                    >
                      Aceitar
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleRecusar(p.id.id)}
                      disabled={carregandoAcao === p.id.id}
                    >
                      Recusar
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </Card>
        )}

        {/* Aba: Sugestões */}
        {aba === "sugestoes" && (
          <Card title="Quem você pode conhecer">
            {sugestoes.length === 0 ? (
              <p className="py-8 text-center text-sm text-ink-500">Sem sugestões por agora. Complete seu perfil com esporte e localidade!</p>
            ) : (
              <div className="space-y-3">
                {sugestoes.map(s => (
                  <div key={s.perfil.id?.id} className="flex items-center gap-3 rounded-2xl bg-ink-50 px-4 py-3">
                    <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-ink-200 font-bold text-ink-700">
                      {s.perfil.nomeExibicao.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-ink-900 truncate">{s.perfil.nomeExibicao}</p>
                      <div className="flex gap-2 mt-1">
                        {s.perfil.esporte && <Badge tone="neutral">{s.perfil.esporte}</Badge>}
                        {s.perfil.localidade && <Badge tone="neutral">📍 {s.perfil.localidade}</Badge>}
                        {s.score > 0 && (
                          <Badge tone="accent">
                            {s.score >= 3 ? "⭐⭐⭐" : s.score >= 2 ? "⭐⭐" : "⭐"}
                          </Badge>
                        )}
                      </div>
                    </div>
                    <Button
                      variant="accent"
                      size="sm"
                      onClick={() => handleSeguir(s.perfil.id!.id)}
                      disabled={carregandoAcao === s.perfil.id?.id}
                    >
                      Seguir
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </Card>
        )}
      </div>
    </div>
  );
}
