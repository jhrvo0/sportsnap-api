"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/lib/auth";
import {
  getDashboard, listarFotos, listarLotes, removerFoto, listarTodasFotos,
  listarLicencas, cancelarLicenca as cancelarLicencaApi,
  type DashboardDto, type FotoDto, type LicencaDto,
} from "@/lib/marketplace";
import {
  obterPerfilPorUsuario, editarPerfil, listarBloqueados, desbloquear, listarPerfis,
  bloquear, listarPostsPorAutor, type PerfilSocial, type Bloqueio, type PerfilResumo,
  type PostEsportivo,
} from "@/lib/social";
import { Card } from "@/components/Card";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";

type Aba = "publicacoes" | "posts" | "configuracoes";
type SecaoConfig = "perfil" | "privacidade" | "bloqueados" | "foto";

const ESPORTES = ["Corrida", "Surf", "Skate", "Futebol", "Natação", "Ciclismo", "Musculação", "Outro"];

export default function PerfilPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const fileRef = useRef<HTMLInputElement>(null);

  // --- Dados ---
  const [licencas, setLicencas]               = useState<LicencaDto[]>([]);
  const [fotosMap, setFotosMap]               = useState<Record<number, string>>({});
  const [dashboard, setDashboard]             = useState<DashboardDto | null>(null);
  const [fotosDoFotografo, setFotosDoFotografo] = useState<FotoDto[]>([]);
  const [perfilSocial, setPerfilSocial]       = useState<PerfilSocial | null>(null);
  const [bloqueados, setBloqueados]           = useState<Bloqueio[]>([]);
  const [todosPerfis, setTodosPerfis]         = useState<PerfilResumo[]>([]);
  const [meusPosts, setMeusPosts]             = useState<PostEsportivo[]>([]);

  // --- UI ---
  const [aba, setAba]                     = useState<Aba>("publicacoes");
  const [secaoConfig, setSecaoConfig]     = useState<SecaoConfig>("perfil");
  const [erro, setErro]                   = useState<string | null>(null);
  const [aviso, setAviso]                 = useState<string | null>(null);
  const [salvando, setSalvando]           = useState(false);

  // --- Editar perfil ---
  const [nome, setNome]           = useState("");
  const [bio, setBio]             = useState("");
  const [esporte, setEsporte]     = useState("");
  const [localidade, setLocalidade] = useState("");

  // --- Busca bloqueados ---
  const [buscaBloqueio, setBuscaBloqueio] = useState("");

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
  }, [sessao, carregando, router]);

  useEffect(() => {
    if (!sessao) return;
    setErro(null);

    obterPerfilPorUsuario(sessao.id).then(p => {
      setPerfilSocial(p);
      if (p) {
        setNome(p.nomeExibicao);
        setBio(p.bio ?? "");
        setEsporte(p.esporte ?? "");
        setLocalidade(p.localidade ?? "");
        if (p.id) {
          listarBloqueados(p.id.id).then(setBloqueados).catch(() => {});
          listarPostsPorAutor(p.id.id).then(setMeusPosts).catch(() => {});
        }
      }
    }).catch(() => {});

    listarPerfis().then(setTodosPerfis).catch(() => {});

    if (sessao.role === "atleta") carregarLicencas();
    else carregarDadosFotografo();
  }, [sessao]);

  async function carregarLicencas() {
    if (!sessao) return;
    try {
      const [lista, todasFotos] = await Promise.all([
        listarLicencas(sessao.id),
        listarTodasFotos().catch(() => [] as FotoDto[]),
      ]);
      setLicencas(lista);
      const mapa: Record<number, string> = {};
      todasFotos.forEach(f => { if (f.urlPreview) mapa[f.id] = f.urlPreview; });
      setFotosMap(mapa);
    } catch { setErro("Erro ao carregar licenças."); }
  }

  async function carregarDadosFotografo() {
    if (!sessao) return;
    try {
      const dash = await getDashboard(sessao.id);
      setDashboard(dash);
      const lotes = await listarLotes(sessao.id);
      const fp = await Promise.all(lotes.map(l => listarFotos(l.id)));
      setFotosDoFotografo(fp.flat());
    } catch { setErro("Erro ao carregar dados do fotógrafo."); }
  }

  async function salvarPerfil() {
    if (!perfilSocial?.id || !sessao) return;
    setSalvando(true); setErro(null);
    try {
      const p = await editarPerfil(perfilSocial.id.id, sessao.id, {
        nomeExibicao: nome,
        bio:       bio       || undefined,
        esporte:   esporte   || undefined,
        localidade: localidade || undefined,
      });
      setPerfilSocial(p);
      setAviso("Perfil atualizado!"); setTimeout(() => setAviso(null), 3000);
    } catch { setErro("Erro ao salvar perfil."); }
    finally { setSalvando(false); }
  }

  async function alternarVisibilidade() {
    if (!perfilSocial?.id || !sessao) return;
    const nova = perfilSocial.visibilidade === "PUBLICA" ? "PRIVADA" : "PUBLICA";
    try {
      const p = await editarPerfil(perfilSocial.id.id, sessao.id, {
        nomeExibicao: perfilSocial.nomeExibicao,
        visibilidade: nova,
      });
      setPerfilSocial(p);
    } catch { setErro("Erro ao alterar privacidade."); }
  }

  async function handleFotoUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file || !perfilSocial?.id || !sessao) return;
    const reader = new FileReader();
    reader.onload = async (ev) => {
      const base64 = ev.target?.result as string;
      try {
        const p = await editarPerfil(perfilSocial.id!.id, sessao.id, {
          nomeExibicao: perfilSocial.nomeExibicao,
          fotoPerfil: base64,
        });
        setPerfilSocial(p);
        setAviso("Foto de perfil atualizada!"); setTimeout(() => setAviso(null), 3000);
      } catch { setErro("Erro ao salvar foto de perfil."); }
    };
    reader.readAsDataURL(file);
  }

  async function handleDesbloquear(bloqueadoId: number) {
    if (!perfilSocial?.id) return;
    try {
      await desbloquear(perfilSocial.id.id, bloqueadoId);
      setBloqueados(prev => prev.filter(b => b.bloqueadoId.id !== bloqueadoId));
    } catch { setErro("Erro ao desbloquear."); }
  }

  async function handleBloquearUsuario(bloqueadoId: number) {
    if (!perfilSocial?.id) return;
    try {
      await bloquear(perfilSocial.id.id, bloqueadoId);
      const updated = await listarBloqueados(perfilSocial.id.id);
      setBloqueados(updated);
    } catch { setErro("Erro ao bloquear."); }
  }

  if (!sessao) return null;

  const ativas    = licencas.filter(l => !l.cancelada);
  const totalGasto = ativas.reduce((s, l) => s + Number(l.preco), 0);
  const numPosts  = sessao.role === "atleta" ? ativas.length : fotosDoFotografo.length;

  const bloqueadosIds = new Set(bloqueados.map(b => b.bloqueadoId.id));
  const candidatosBloqueio = todosPerfis.filter(p =>
    p.id !== perfilSocial?.id?.id &&
    p.nomeExibicao.toLowerCase().includes(buscaBloqueio.toLowerCase())
  );

  return (
    <div className="fade-up max-w-3xl mx-auto">
      {/* ── Cabeçalho Instagram ── */}
      <div className="flex items-start gap-8 py-8">
        {/* Foto de perfil */}
        <div className="relative shrink-0">
          <div className="h-24 w-24 rounded-full overflow-hidden ring-2 ring-ink-200 bg-gradient-to-br from-accent to-violet-600 flex items-center justify-center">
            {perfilSocial?.fotoPerfil ? (
              <img src={perfilSocial.fotoPerfil} alt="foto" className="w-full h-full object-cover" />
            ) : (
              <span className="text-4xl font-black text-white">{sessao.nome.charAt(0).toUpperCase()}</span>
            )}
          </div>
          <button
            onClick={() => { setAba("configuracoes"); setSecaoConfig("foto"); }}
            className="absolute -bottom-1 -right-1 h-7 w-7 rounded-full bg-ink-900 text-white text-xs flex items-center justify-center shadow hover:bg-ink-700 transition-colors"
            title="Alterar foto"
          >
            📷
          </button>
        </div>

        {/* Info */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-3 flex-wrap mb-3">
            <h1 className="text-xl font-bold text-ink-900">{sessao.nome}</h1>
            <Button variant="secondary" size="sm" onClick={() => { setAba("configuracoes"); setSecaoConfig("perfil"); }}>
              Editar perfil
            </Button>
            <button
              onClick={() => { setAba("configuracoes"); setSecaoConfig("perfil"); }}
              className="h-8 w-8 rounded-lg bg-ink-100 flex items-center justify-center text-ink-600 hover:bg-ink-200 transition-colors text-base"
              title="Configurações"
            >
              ⚙️
            </button>
          </div>

          {/* Stats */}
          <div className="flex gap-6 mb-3 text-sm">
            <span><strong className="text-ink-900">{numPosts}</strong> <span className="text-ink-500">{sessao.role === "atleta" ? "licenças" : "fotos"}</span></span>
            <Link href="/social/conexoes" className="hover:text-accent">
              <strong className="text-ink-900">{perfilSocial?.totalSeguidores ?? 0}</strong>{" "}
              <span className="text-ink-500">seguidores</span>
            </Link>
            <Link href="/social/conexoes" className="hover:text-accent">
              <strong className="text-ink-900">{perfilSocial?.totalSeguindo ?? 0}</strong>{" "}
              <span className="text-ink-500">seguindo</span>
            </Link>
          </div>

          {/* Bio */}
          <div className="text-sm space-y-0.5">
            <p className="font-semibold text-ink-900">{perfilSocial?.nomeExibicao || sessao.nome}</p>
            {perfilSocial?.bio        && <p className="text-ink-700">{perfilSocial.bio}</p>}
            {perfilSocial?.esporte    && <p className="text-ink-500">🏅 {perfilSocial.esporte}</p>}
            {perfilSocial?.localidade && <p className="text-ink-500">📍 {perfilSocial.localidade}</p>}
            {perfilSocial && (
              <Badge tone={perfilSocial.visibilidade === "PUBLICA" ? "neutral" : "warning"} className="mt-1">
                {perfilSocial.visibilidade === "PUBLICA" ? "🌐 Público" : "🔒 Privado"}
              </Badge>
            )}
          </div>
        </div>
      </div>

      {erro  && <Alert tone="danger"  className="mb-4">{erro}</Alert>}
      {aviso && <Alert tone="success" className="mb-4">{aviso}</Alert>}

      {/* ── Abas ── */}
      <div className="border-t border-ink-200 flex">
        {([
          { id: "publicacoes",   label: sessao.role === "atleta" ? "LICENÇAS" : "FOTOS", icon: "⊞" },
          { id: "posts",         label: "POSTS", icon: "✍️" },
          { id: "configuracoes", label: "CONFIGURAÇÕES", icon: "⚙️" },
        ] as { id: Aba; label: string; icon: string }[]).map(t => (
          <button
            key={t.id}
            onClick={() => setAba(t.id as Aba)}
            className={`flex-1 flex items-center justify-center gap-2 py-3 text-xs font-bold tracking-widest transition-colors border-t-2 -mt-px ${
              aba === t.id
                ? "border-ink-900 text-ink-900"
                : "border-transparent text-ink-400 hover:text-ink-700"
            }`}
          >
            <span>{t.icon}</span> {t.label}
          </button>
        ))}
      </div>

      {/* ── Aba: Publicações ── */}
      {aba === "publicacoes" && (
        <div className="py-6">
          {sessao.role === "atleta" ? (
            ativas.length === 0 ? (
              <div className="py-20 text-center">
                <span className="text-5xl opacity-20 block mb-4">🖼️</span>
                <p className="font-semibold text-ink-600">Nenhuma licença ainda</p>
                <Button variant="secondary" className="mt-4" onClick={() => router.push("/loja")}>Explorar loja</Button>
              </div>
            ) : (
              <div className="grid grid-cols-3 gap-1">
                {ativas.map(l => (
                  <div key={l.id} className="aspect-square rounded-lg overflow-hidden bg-ink-900 relative group cursor-pointer">
                    {fotosMap[l.fotoId]
                      ? <img src={fotosMap[l.fotoId]} alt="" className="w-full h-full object-cover transition-transform group-hover:scale-105" />
                      : <div className="w-full h-full bg-gradient-to-br from-violet-100 to-rose-100 flex items-center justify-center"><span className="text-3xl opacity-30">📸</span></div>}
                    <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center gap-1">
                      <p className="text-white text-xs font-bold">FOTO #{l.fotoId}</p>
                      <p className="text-white/80 text-xs">R$ {Number(l.preco).toFixed(2)}</p>
                    </div>
                  </div>
                ))}
              </div>
            )
          ) : (
            fotosDoFotografo.length === 0 ? (
              <div className="py-20 text-center">
                <span className="text-5xl opacity-20 block mb-4">📷</span>
                <p className="font-semibold text-ink-600">Nenhuma foto publicada</p>
                <Button variant="secondary" className="mt-4" onClick={() => router.push("/upload")}>Subir fotos</Button>
              </div>
            ) : (
              <div className="grid grid-cols-3 gap-1">
                {fotosDoFotografo.map(f => (
                  <div key={f.id} className="aspect-square bg-ink-900 rounded-lg overflow-hidden relative group">
                    {f.urlPreview
                      ? <img src={f.urlPreview} alt="" className="w-full h-full object-cover transition-transform group-hover:scale-105" />
                      : <div className="w-full h-full flex items-center justify-center"><span className="text-white/20 text-3xl">📸</span></div>}
                    <div className="absolute top-1 right-1">
                      {f.licenciada
                        ? <span className="text-[10px] font-bold bg-emerald-500 text-white px-1.5 py-0.5 rounded-full">Vendida</span>
                        : <span className="text-[10px] font-bold bg-white/80 text-ink-700 px-1.5 py-0.5 rounded-full">R$ {f.preco}</span>}
                    </div>
                  </div>
                ))}
              </div>
            )
          )}
        </div>
      )}

      {/* ── Aba: Posts ── */}
      {aba === "posts" && (
        <div className="py-6 space-y-4">
          {meusPosts.length === 0 ? (
            <div className="py-16 text-center">
              <span className="text-4xl opacity-20 block mb-3">✍️</span>
              <p className="font-semibold text-ink-600">Nenhum post ainda</p>
              <p className="text-sm text-ink-400 mt-1">
                Publique no <Link href="/social/feed" className="text-accent hover:underline">Feed</Link> para seus posts aparecerem aqui
              </p>
            </div>
          ) : (
            meusPosts.map(post => (
              <div key={post.id.id} className="rounded-3xl border border-ink-100 bg-white p-5 shadow-sm">
                <div className="flex items-center justify-between mb-3">
                  {post.esporte && <Badge tone="accent" className="text-xs">#{post.esporte}</Badge>}
                  <span className="text-xs text-ink-400 ml-auto">
                    {new Date(post.criadoEm).toLocaleDateString("pt-BR", { day: "2-digit", month: "short", year: "numeric" })}
                  </span>
                </div>
                <p className="text-sm text-ink-900 leading-relaxed whitespace-pre-wrap">{post.conteudo}</p>
              </div>
            ))
          )}
        </div>
      )}

      {/* ── Aba: Configurações ── */}
      {aba === "configuracoes" && (
        <div className="py-6 grid gap-4 sm:grid-cols-4">
          {/* Menu lateral */}
          <div className="sm:col-span-1 space-y-1">
            {([
              { id: "perfil",      icon: "✏️", label: "Editar perfil" },
              { id: "foto",        icon: "📷", label: "Foto de perfil" },
              { id: "privacidade", icon: "🔒", label: "Privacidade" },
              { id: "bloqueados",  icon: "🚫", label: "Usuários bloqueados" },
            ] as { id: SecaoConfig; icon: string; label: string }[]).map(s => (
              <button
                key={s.id}
                onClick={() => setSecaoConfig(s.id)}
                className={`w-full text-left px-4 py-2.5 rounded-xl text-sm font-medium flex items-center gap-3 transition-colors ${
                  secaoConfig === s.id ? "bg-ink-900 text-white" : "text-ink-600 hover:bg-ink-100"
                }`}
              >
                <span>{s.icon}</span> {s.label}
              </button>
            ))}
          </div>

          {/* Conteúdo da seção */}
          <div className="sm:col-span-3">
            {/* Editar perfil */}
            {secaoConfig === "perfil" && (
              <Card title="Editar perfil">
                <div className="space-y-4">
                  <div>
                    <label className="mb-1 block text-sm font-semibold text-ink-700">Nome de exibição</label>
                    <input className="w-full rounded-2xl border border-ink-200 px-4 py-2.5 text-sm focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                      value={nome} onChange={e => setNome(e.target.value)} />
                  </div>
                  <div>
                    <div className="mb-1 flex justify-between">
                      <label className="text-sm font-semibold text-ink-700">Bio</label>
                      <span className={`text-xs ${bio.length > 280 ? "text-red-500" : "text-ink-400"}`}>{bio.length}/300</span>
                    </div>
                    <textarea className="w-full rounded-2xl border border-ink-200 px-4 py-2.5 text-sm resize-none focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                      rows={3} value={bio} onChange={e => setBio(e.target.value)} maxLength={300} />
                  </div>
                  <div>
                    <label className="mb-1 block text-sm font-semibold text-ink-700">Esporte principal</label>
                    <select className="w-full rounded-2xl border border-ink-200 px-4 py-2.5 text-sm focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                      value={esporte} onChange={e => setEsporte(e.target.value)}>
                      <option value="">Selecione...</option>
                      {ESPORTES.map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="mb-1 block text-sm font-semibold text-ink-700">Cidade / Estado</label>
                    <input className="w-full rounded-2xl border border-ink-200 px-4 py-2.5 text-sm focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                      value={localidade} onChange={e => setLocalidade(e.target.value)} placeholder="Ex: Recife, PE" />
                  </div>
                  <Button variant="accent" className="w-full" onClick={salvarPerfil} disabled={salvando}>
                    {salvando ? "Salvando..." : "Salvar alterações"}
                  </Button>
                </div>
              </Card>
            )}

            {/* Foto de perfil */}
            {secaoConfig === "foto" && (
              <Card title="Foto de perfil">
                <div className="flex flex-col items-center gap-6 py-4">
                  <div className="h-28 w-28 rounded-full overflow-hidden ring-2 ring-ink-200 bg-gradient-to-br from-accent to-violet-600 flex items-center justify-center">
                    {perfilSocial?.fotoPerfil
                      ? <img src={perfilSocial.fotoPerfil} alt="foto" className="w-full h-full object-cover" />
                      : <span className="text-5xl font-black text-white">{sessao.nome.charAt(0).toUpperCase()}</span>}
                  </div>
                  <input ref={fileRef} type="file" accept="image/*" className="hidden" onChange={handleFotoUpload} />
                  <Button variant="accent" onClick={() => fileRef.current?.click()}>
                    Escolher nova foto
                  </Button>
                  {perfilSocial?.fotoPerfil && (
                    <button
                      onClick={async () => {
                        if (!perfilSocial.id || !sessao) return;
                        const p = await editarPerfil(perfilSocial.id.id, sessao.id, {
                          nomeExibicao: perfilSocial.nomeExibicao, fotoPerfil: "",
                        });
                        setPerfilSocial(p);
                      }}
                      className="text-sm text-red-500 hover:underline"
                    >
                      Remover foto
                    </button>
                  )}
                  <p className="text-xs text-ink-400 text-center">JPG, PNG ou GIF · Máx. 2 MB recomendado</p>
                </div>
              </Card>
            )}

            {/* Privacidade */}
            {secaoConfig === "privacidade" && (
              <Card title="Privacidade da conta">
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-4 rounded-2xl bg-ink-50">
                    <div>
                      <p className="font-semibold text-ink-900">Conta privada</p>
                      <p className="text-sm text-ink-500 mt-0.5">
                        {perfilSocial?.visibilidade === "PRIVADA"
                          ? "Somente seus seguidores veem seu conteúdo"
                          : "Qualquer pessoa pode te seguir"}
                      </p>
                    </div>
                    <button
                      onClick={alternarVisibilidade}
                      className={`relative h-7 w-12 rounded-full transition-colors ${
                        perfilSocial?.visibilidade === "PRIVADA" ? "bg-accent" : "bg-ink-300"
                      }`}
                    >
                      <span className={`absolute top-1 h-5 w-5 rounded-full bg-white shadow transition-transform ${
                        perfilSocial?.visibilidade === "PRIVADA" ? "translate-x-6" : "translate-x-1"
                      }`} />
                    </button>
                  </div>
                  <p className="text-xs text-ink-400 px-1">
                    Ao tornar a conta privada, os pedidos de seguimento precisarão da sua aprovação.
                  </p>
                </div>
              </Card>
            )}

            {/* Bloqueados */}
            {secaoConfig === "bloqueados" && (
              <Card title="Usuários bloqueados">
                <div className="space-y-4">
                  {/* Buscar para bloquear */}
                  <div>
                    <label className="mb-1.5 block text-sm font-semibold text-ink-700">Bloquear usuário</label>
                    <input
                      className="w-full rounded-2xl border border-ink-200 px-4 py-2.5 text-sm focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                      placeholder="Buscar por nome..."
                      value={buscaBloqueio}
                      onChange={e => setBuscaBloqueio(e.target.value)}
                    />
                    {buscaBloqueio.length >= 2 && (
                      <div className="mt-2 rounded-2xl border border-ink-100 overflow-hidden">
                        {candidatosBloqueio.slice(0, 5).map(p => (
                          <div key={p.id} className="flex items-center justify-between px-4 py-3 hover:bg-ink-50 border-b border-ink-50 last:border-0">
                            <div className="flex items-center gap-3">
                              <div className="h-8 w-8 rounded-full bg-ink-200 flex items-center justify-center font-bold text-ink-700 text-sm">
                                {p.nomeExibicao.charAt(0).toUpperCase()}
                              </div>
                              <span className="text-sm font-medium text-ink-900">{p.nomeExibicao}</span>
                            </div>
                            {bloqueadosIds.has(p.id) ? (
                              <button onClick={() => handleDesbloquear(p.id)} className="text-xs font-bold text-accent hover:underline">
                                Desbloquear
                              </button>
                            ) : (
                              <button onClick={() => handleBloquearUsuario(p.id)} className="text-xs font-bold text-red-500 hover:underline">
                                Bloquear
                              </button>
                            )}
                          </div>
                        ))}
                        {candidatosBloqueio.length === 0 && (
                          <p className="px-4 py-3 text-sm text-ink-400">Nenhum usuário encontrado</p>
                        )}
                      </div>
                    )}
                  </div>

                  {/* Lista de bloqueados */}
                  <div className="pt-2 border-t border-ink-100">
                    <p className="text-sm font-semibold text-ink-700 mb-3">
                      Bloqueados ({bloqueados.length})
                    </p>
                    {bloqueados.length === 0 ? (
                      <p className="text-sm text-ink-400 text-center py-6">Nenhum usuário bloqueado</p>
                    ) : (
                      <div className="space-y-2">
                        {bloqueados.map(b => {
                          const perfil = todosPerfis.find(p => p.id === b.bloqueadoId.id);
                          return (
                            <div key={b.id.id} className="flex items-center justify-between rounded-2xl bg-ink-50 px-4 py-3">
                              <div className="flex items-center gap-3">
                                <div className="h-8 w-8 rounded-full bg-ink-200 flex items-center justify-center font-bold text-ink-700 text-sm">
                                  {(perfil?.nomeExibicao ?? "?").charAt(0).toUpperCase()}
                                </div>
                                <span className="text-sm font-medium text-ink-900">
                                  {perfil?.nomeExibicao ?? `Usuário #${b.bloqueadoId.id}`}
                                </span>
                              </div>
                              <button
                                onClick={() => handleDesbloquear(b.bloqueadoId.id)}
                                className="text-xs font-bold text-accent hover:underline"
                              >
                                Desbloquear
                              </button>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </div>
                </div>
              </Card>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
