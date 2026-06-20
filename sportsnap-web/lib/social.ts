"use client";

const BASE = process.env.NEXT_PUBLIC_SOCIAL_URL || "http://localhost:8081";

// --- Tipos ---

export type PerfilSocial = {
  id: { id: number } | null;
  usuarioId: { id: number };
  nomeExibicao: string;
  tipoConta: "ATLETA" | "FOTOGRAFO";
  bio: string | null;
  esporte: string | null;
  localidade: string | null;
  visibilidade: "PUBLICA" | "PRIVADA";
  publico: boolean;
  totalSeguidores: number;
  totalSeguindo: number;
  fotoPerfil: string | null;
};

export type PerfilResumo = {
  id: number;
  nomeExibicao: string;
  tipoConta: string;
  visibilidade: string;
  totalSeguidores: number;
  totalSeguindo: number;
};

export type Conexao = {
  id: { id: number };
  seguidorId: { id: number };
  seguidoId: { id: number };
  criadaEm: string;
};

export type PedidoConexao = {
  id: { id: number };
  solicitanteId: { id: number };
  alvoId: { id: number };
  criadoEm: string;
  status: "PENDENTE" | "APROVADO" | "RECUSADO" | "CANCELADO";
};

export type SugestaoConexao = {
  perfil: PerfilSocial;
  score: number;
};

export type ItemFeed = {
  id: { id: number };
  autorId: { id: number };
  tipo: "FOTO" | "NOVA_CONEXAO";
  referenciaId: number;
  publicadoEm: string;
};

export type Notificacao = {
  id: { id: number };
  destinatarioId: { id: number };
  tipo: "CURTIDA" | "NOVO_SEGUIDOR" | "PEDIDO_CONEXAO";
  referenciaId: number;
  lida: boolean;
  criadaEm: string;
  numAtores: number;
};

// --- Perfil ---

export async function obterPerfilPorUsuario(usuarioId: number): Promise<PerfilSocial | null> {
  const r = await fetch(`${BASE}/api/perfis/usuario/${usuarioId}`, { cache: "no-store" });
  if (r.status === 404 || r.status === 400 || r.status === 500) return null;
  if (!r.ok) return null;
  return r.json();
}

export async function criarPerfil(
  usuarioId: number,
  nomeExibicao: string,
  tipoConta: "ATLETA" | "FOTOGRAFO"
): Promise<PerfilSocial> {
  const r = await fetch(`${BASE}/api/perfis`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ usuarioId, nomeExibicao, tipoConta }),
  });
  if (!r.ok) throw new Error("Erro ao criar perfil");
  return r.json();
}

export async function editarPerfil(
  id: number,
  solicitanteId: number,
  dados: { nomeExibicao: string; bio?: string; esporte?: string; localidade?: string; visibilidade?: string; fotoPerfil?: string }
): Promise<PerfilSocial> {
  const r = await fetch(`${BASE}/api/perfis/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ solicitanteId, ...dados }),
  });
  if (!r.ok) throw new Error("Erro ao editar perfil");
  return r.json();
}

export async function listarPerfis(): Promise<PerfilResumo[]> {
  const r = await fetch(`${BASE}/api/perfis`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

export async function sugerirConexoes(perfilId: number): Promise<SugestaoConexao[]> {
  const r = await fetch(`${BASE}/api/perfis/${perfilId}/sugestoes`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

// --- Conexoes ---

export async function seguir(seguidorId: number, seguidoId: number): Promise<void> {
  const r = await fetch(`${BASE}/api/conexoes/seguir`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ seguidorId, seguidoId }),
  });
  if (!r.ok) throw new Error("Erro ao seguir");
}

export async function deixarDeSeguir(seguidorId: number, seguidoId: number): Promise<void> {
  const r = await fetch(`${BASE}/api/conexoes/deixar-de-seguir`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ seguidorId, seguidoId }),
  });
  if (!r.ok) throw new Error("Erro ao deixar de seguir");
}

export async function aprovarPedido(pedidoId: number): Promise<Conexao> {
  const r = await fetch(`${BASE}/api/conexoes/pedidos/${pedidoId}/aprovar`, { method: "POST" });
  if (!r.ok) throw new Error("Erro ao aprovar pedido");
  return r.json();
}

export async function recusarPedido(pedidoId: number): Promise<void> {
  const r = await fetch(`${BASE}/api/conexoes/pedidos/${pedidoId}/recusar`, { method: "POST" });
  if (!r.ok) throw new Error("Erro ao recusar pedido");
}

export async function cancelarPedido(pedidoId: number, canceladorId: number): Promise<void> {
  const r = await fetch(`${BASE}/api/conexoes/pedidos/${pedidoId}`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ canceladorId }),
  });
  if (!r.ok) throw new Error("Erro ao cancelar pedido");
}

export async function bloquear(bloqueadorId: number, bloqueadoId: number): Promise<void> {
  const r = await fetch(`${BASE}/api/conexoes/bloquear`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ seguidorId: bloqueadorId, seguidoId: bloqueadoId }),
  });
  if (!r.ok) throw new Error("Erro ao bloquear");
}

export async function desbloquear(bloqueadorId: number, bloqueadoId: number): Promise<void> {
  const r = await fetch(`${BASE}/api/conexoes/desbloquear`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ seguidorId: bloqueadorId, seguidoId: bloqueadoId }),
  });
  if (!r.ok) throw new Error("Erro ao desbloquear");
}

export type Bloqueio = {
  id: { id: number };
  bloqueadorId: { id: number };
  bloqueadoId: { id: number };
};

export async function listarBloqueados(perfilId: number): Promise<Bloqueio[]> {
  const r = await fetch(`${BASE}/api/conexoes/${perfilId}/bloqueados`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

export async function listarSeguindo(perfilId: number): Promise<Conexao[]> {
  const r = await fetch(`${BASE}/api/conexoes/${perfilId}/seguindo`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

export async function listarSeguidores(perfilId: number): Promise<Conexao[]> {
  const r = await fetch(`${BASE}/api/conexoes/${perfilId}/seguidores`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

export async function listarPedidosPendentes(perfilId: number): Promise<PedidoConexao[]> {
  const r = await fetch(`${BASE}/api/conexoes/${perfilId}/pedidos-pendentes`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

// --- Feed ---

export async function consultarFeed(perfilId: number, pagina = 0): Promise<ItemFeed[]> {
  const r = await fetch(`${BASE}/api/feed/${perfilId}?pagina=${pagina}`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

export async function curtir(itemId: number, usuarioId: number): Promise<void> {
  const r = await fetch(`${BASE}/api/feed/${itemId}/curtir`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ usuarioId }),
  });
  if (!r.ok) throw new Error("Erro ao curtir");
}

export async function descurtir(itemId: number, usuarioId: number): Promise<void> {
  const r = await fetch(`${BASE}/api/feed/${itemId}/curtir`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ usuarioId }),
  });
  if (!r.ok) throw new Error("Erro ao descurtir");
}

// --- Post Esportivo ---

export type PostEsportivo = {
  id: { id: number };
  autorId: { id: number };
  conteudo: string;
  esporte: string | null;
  criadoEm: string;
};

export async function criarPost(autorId: number, conteudo: string, esporte?: string): Promise<PostEsportivo> {
  const r = await fetch(`${BASE}/api/posts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ autorId, conteudo, esporte: esporte || null }),
  });
  if (!r.ok) throw new Error("Erro ao criar post");
  return r.json();
}

export async function obterPost(id: number): Promise<PostEsportivo | null> {
  const r = await fetch(`${BASE}/api/posts/${id}`, { cache: "no-store" });
  if (!r.ok) return null;
  return r.json();
}

export async function listarItensFeedPorAutor(perfilId: number): Promise<ItemFeed[]> {
  const r = await fetch(`${BASE}/api/feed/autor/${perfilId}`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

export async function listarPostsPorAutor(autorId: number): Promise<PostEsportivo[]> {
  const r = await fetch(`${BASE}/api/posts?autorId=${autorId}`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

// --- Comentários ---

export type Comentario = {
  id: { id: number };
  itemFeedId: { id: number };
  autorId: { id: number };
  conteudo: string;
  parentId: { id: number } | null;
  criadoEm: string;
  resposta: boolean;
};

export async function listarComentarios(itemId: number): Promise<Comentario[]> {
  const r = await fetch(`${BASE}/api/comentarios?itemId=${itemId}`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

export async function criarComentario(
  itemFeedId: number, autorId: number, conteudo: string, parentId?: number
): Promise<Comentario> {
  const r = await fetch(`${BASE}/api/comentarios`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ itemFeedId, autorId, conteudo, parentId: parentId ?? null }),
  });
  if (!r.ok) throw new Error("Erro ao comentar");
  return r.json();
}

export async function removerComentario(comentarioId: number, autorId: number): Promise<void> {
  await fetch(`${BASE}/api/comentarios/${comentarioId}?autorId=${autorId}`, { method: "DELETE" });
}

export async function publicarNoFeed(autorId: number, tipo: string, referenciaId: number): Promise<void> {
  await fetch(`${BASE}/api/feed/publicar`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ autorId, tipo, referenciaId }),
  });
}

// --- Notificações ---

export async function listarNotificacoes(perfilId: number): Promise<Notificacao[]> {
  const r = await fetch(`${BASE}/api/feed/${perfilId}/notificacoes`, { cache: "no-store" });
  if (!r.ok) return [];
  return r.json();
}

export async function contarNaoLidas(perfilId: number): Promise<number> {
  const r = await fetch(`${BASE}/api/feed/${perfilId}/notificacoes/nao-lidas`, { cache: "no-store" });
  if (!r.ok) return 0;
  return r.json();
}

export async function marcarTodasComoLidas(perfilId: number): Promise<void> {
  await fetch(`${BASE}/api/feed/${perfilId}/notificacoes/marcar-lidas`, { method: "POST" });
}
