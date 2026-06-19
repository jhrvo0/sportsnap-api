"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/lib/auth";
import { obterPerfilPorUsuario, contarNaoLidas, listarNotificacoes, marcarTodasComoLidas, type PerfilSocial, type Notificacao } from "@/lib/social";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Badge } from "@/components/Badge";
import { Alert } from "@/components/Alert";

const TIPO_LABEL: Record<string, string> = {
  CURTIDA: "curtiu seu item",
  NOVO_SEGUIDOR: "começou a te seguir",
  PEDIDO_CONEXAO: "enviou pedido de conexão",
};

export default function SocialPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();

  const [perfil, setPerfil] = useState<PerfilSocial | null>(null);
  const [notificacoes, setNotificacoes] = useState<Notificacao[]>([]);
  const [naoLidas, setNaoLidas] = useState(0);
  const [erro, setErro] = useState<string | null>(null);
  const [marcando, setMarcando] = useState(false);

  useEffect(() => {
    if (carregando) return;
    if (!sessao) { router.replace("/login"); return; }

    obterPerfilPorUsuario(sessao.id)
      .then(setPerfil)
      .catch(() => setErro("Erro ao conectar com o serviço social"));
  }, [sessao, carregando, router]);

  useEffect(() => {
    if (!perfil?.id) return;
    const pid = perfil.id!.id;
    Promise.all([
      listarNotificacoes(pid),
      contarNaoLidas(pid),
    ]).then(([notifs, count]) => {
      setNotificacoes(notifs.slice(0, 5));
      setNaoLidas(count);
    });
  }, [perfil]);

  async function handleMarcarLidas() {
    if (!perfil?.id) return;
    setMarcando(true);
    await marcarTodasComoLidas(perfil.id.id);
    setNaoLidas(0);
    setNotificacoes(prev => prev.map(n => ({ ...n, lida: true })));
    setMarcando(false);
  }

  if (carregando) return null;
  if (!sessao) return null;

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Social"
        title="Sua rede"
        subtitle="Perfil, conexões e atividades da plataforma"
      />

      <div className="mx-auto max-w-7xl px-6 py-10 space-y-8">
        {erro && <Alert tone="danger">{erro}</Alert>}

        {/* Sem perfil */}
        {!perfil && !erro && (
          <Card>
            <div className="py-20 text-center">
              <div className="mx-auto mb-6 grid h-20 w-20 place-items-center rounded-full bg-accent-50">
                <span className="text-3xl">👤</span>
              </div>
              <h3 className="text-xl font-bold text-ink-900">Você ainda não tem perfil social</h3>
              <p className="mt-2 text-ink-500">Crie seu perfil para começar a se conectar com outros atletas e fotógrafos</p>
              <div className="mt-8">
                <Link href="/social/perfil">
                  <Button variant="accent" size="lg">Criar meu perfil</Button>
                </Link>
              </div>
            </div>
          </Card>
        )}

        {/* Com perfil */}
        {perfil && (
          <>
            {/* Card do perfil */}
            <Card>
              <div className="flex items-center gap-6">
                <div className="grid h-16 w-16 shrink-0 place-items-center rounded-full bg-ink-900 text-2xl font-black text-white">
                  {perfil.nomeExibicao.charAt(0).toUpperCase()}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3 flex-wrap">
                    <h2 className="text-xl font-bold text-ink-900 truncate">{perfil.nomeExibicao}</h2>
                    <Badge tone={perfil.tipoConta === "ATLETA" ? "accent" : "info"}>
                      {perfil.tipoConta === "ATLETA" ? "Atleta" : "Fotógrafo"}
                    </Badge>
                    <Badge tone="neutral">{perfil.visibilidade === "PUBLICA" ? "Público" : "Privado"}</Badge>
                  </div>
                  {perfil.bio && <p className="mt-1 text-sm text-ink-500 truncate">{perfil.bio}</p>}
                  <div className="mt-2 flex gap-4 text-sm text-ink-500">
                    {perfil.esporte && <span>🏅 {perfil.esporte}</span>}
                    {perfil.localidade && <span>📍 {perfil.localidade}</span>}
                  </div>
                </div>
                <Link href="/social/perfil">
                  <Button variant="secondary" size="sm">Editar</Button>
                </Link>
              </div>

              <div className="mt-6 grid grid-cols-2 divide-x divide-ink-100 rounded-2xl bg-ink-50 py-4">
                <div className="text-center">
                  <p className="text-2xl font-black text-ink-900">{perfil.totalSeguidores}</p>
                  <p className="text-sm text-ink-500">seguidores</p>
                </div>
                <div className="text-center">
                  <p className="text-2xl font-black text-ink-900">{perfil.totalSeguindo}</p>
                  <p className="text-sm text-ink-500">seguindo</p>
                </div>
              </div>
            </Card>

            {/* Navegação */}
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Link href="/social/feed">
                <Card className="cursor-pointer transition-transform hover:-translate-y-1">
                  <div className="flex items-center gap-4">
                    <div className="grid h-12 w-12 shrink-0 place-items-center rounded-2xl bg-accent-50 text-2xl">📰</div>
                    <div>
                      <p className="font-bold text-ink-900">Feed</p>
                      <p className="text-sm text-ink-500">Atividades de quem você segue</p>
                    </div>
                  </div>
                </Card>
              </Link>

              <Link href="/social/conexoes">
                <Card className="cursor-pointer transition-transform hover:-translate-y-1">
                  <div className="flex items-center gap-4">
                    <div className="grid h-12 w-12 shrink-0 place-items-center rounded-2xl bg-accent-50 text-2xl">🤝</div>
                    <div>
                      <p className="font-bold text-ink-900">Conexões</p>
                      <p className="text-sm text-ink-500">Seguindo, seguidores e pedidos</p>
                    </div>
                  </div>
                </Card>
              </Link>

              <Link href="/social/perfil">
                <Card className="cursor-pointer transition-transform hover:-translate-y-1">
                  <div className="flex items-center gap-4">
                    <div className="grid h-12 w-12 shrink-0 place-items-center rounded-2xl bg-accent-50 text-2xl">✏️</div>
                    <div>
                      <p className="font-bold text-ink-900">Editar perfil</p>
                      <p className="text-sm text-ink-500">Bio, esporte, visibilidade</p>
                    </div>
                  </div>
                </Card>
              </Link>
            </div>

            {/* Notificações recentes */}
            <Card title="Notificações recentes">
              {notificacoes.length === 0 ? (
                <p className="py-6 text-center text-sm text-ink-500">Nenhuma notificação</p>
              ) : (
                <div className="space-y-3">
                  {naoLidas > 0 && (
                    <div className="flex items-center justify-between">
                      <Badge tone="accent">{naoLidas} não lidas</Badge>
                      <Button variant="ghost" size="sm" onClick={handleMarcarLidas} disabled={marcando}>
                        Marcar todas como lidas
                      </Button>
                    </div>
                  )}
                  {notificacoes.map(n => (
                    <div
                      key={n.id.id}
                      className={`flex items-center gap-3 rounded-2xl px-4 py-3 ${n.lida ? "bg-ink-50" : "bg-accent-50"}`}
                    >
                      <span className="text-lg">
                        {n.tipo === "CURTIDA" ? "❤️" : n.tipo === "NOVO_SEGUIDOR" ? "👋" : "🔔"}
                      </span>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-ink-900">
                          {n.numAtores > 1 ? `${n.numAtores} pessoas` : "Alguém"} {TIPO_LABEL[n.tipo]}
                        </p>
                        <p className="text-xs text-ink-500">{new Date(n.criadaEm).toLocaleDateString("pt-BR")}</p>
                      </div>
                      {!n.lida && <div className="h-2 w-2 rounded-full bg-accent shrink-0" />}
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </>
        )}
      </div>
    </div>
  );
}
