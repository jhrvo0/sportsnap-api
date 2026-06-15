const BASE = "http://localhost:8082/api";

// ─── tipos espelho dos DTOs Java ─────────────────────────────────────────────

export type LoteDto = {
  id: number;
  fotografoId: number;
  sessaoId: number;
  spotId: number;
  descricao: string;
  criadoEm: string;
  arquivado: boolean;
};

export type FotoDto = {
  id: number;
  loteId: number;
  urlPreview: string;
  urlOriginal: string;
  exifTimestamp: string;
  exifDetalhes: string;
  licenciada: boolean;
  removida: boolean;
};

export type DashboardDto = {
  fotografoId: number;
  totalLotes: number;
  totalFotos: number;
  totalVendas: number;
  receitaBruta: number;
  saldoDisponivel: number;
};

export type FotografoDto = {
  id: number;
  nome: string;
  email: string;
};

// ─── Lotes ───────────────────────────────────────────────────────────────────

export async function listarLotes(fotografoId: number): Promise<LoteDto[]> {
  const r = await fetch(`${BASE}/lotes?fotografoId=${fotografoId}`);
  if (!r.ok) throw new Error("Erro ao listar lotes");
  return r.json();
}

export async function criarLote(body: {
  fotografoId: number;
  sessaoId: number;
  spotId: number;
  descricao: string;
}): Promise<LoteDto> {
  const r = await fetch(`${BASE}/lotes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!r.ok) throw new Error("Erro ao criar lote");
  return r.json();
}

export async function editarLote(id: number, descricao: string): Promise<LoteDto> {
  const r = await fetch(`${BASE}/lotes/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ descricao }),
  });
  if (!r.ok) throw new Error("Erro ao editar lote");
  return r.json();
}

export async function arquivarLote(id: number): Promise<void> {
  const r = await fetch(`${BASE}/lotes/${id}/arquivar`, { method: "POST" });
  if (!r.ok) throw new Error("Erro ao arquivar lote");
}

export async function desarquivarLote(id: number): Promise<void> {
  const r = await fetch(`${BASE}/lotes/${id}/desarquivar`, { method: "POST" });
  if (!r.ok) throw new Error("Erro ao desarquivar lote");
}

export async function excluirLote(id: number): Promise<void> {
  const r = await fetch(`${BASE}/lotes/${id}`, { method: "DELETE" });
  if (!r.ok) throw new Error("Erro ao excluir lote");
}

// ─── Fotos ───────────────────────────────────────────────────────────────────

export async function listarTodasFotos(): Promise<FotoDto[]> {
  const r = await fetch(`${BASE}/fotos`);
  if (!r.ok) throw new Error("Erro ao listar fotos");
  const fotos: FotoDto[] = await r.json();
  return fotos.filter((f) => !f.removida);
}

export async function listarFotos(loteId: number): Promise<FotoDto[]> {
  const r = await fetch(`${BASE}/fotos?loteId=${loteId}`);
  if (!r.ok) throw new Error("Erro ao listar fotos");
  const fotos: FotoDto[] = await r.json();
  return fotos.filter((f) => !f.removida);
}

export async function uploadFotos(loteId: number, fotos: { nome: string; urlPreview: string }[]): Promise<FotoDto[]> {
  const r = await fetch(`${BASE}/fotos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ loteId, fotos }),
  });
  if (!r.ok) throw new Error("Erro ao fazer upload");
  return r.json();
}

export async function removerFoto(id: number): Promise<void> {
  const r = await fetch(`${BASE}/fotos/${id}/remover`, { method: "POST" });
  if (!r.ok) throw new Error("Erro ao remover foto");
}

// ─── Dashboard ───────────────────────────────────────────────────────────────

export async function getDashboard(fotografoId: number): Promise<DashboardDto> {
  const r = await fetch(`${BASE}/fotografos/${fotografoId}/dashboard`);
  if (!r.ok) throw new Error("Erro ao carregar dashboard");
  return r.json();
}

// ─── Favoritos ────────────────────────────────────────────────────────────────

export type FavoritoFotoDto = {
  id: number;
  loteId: number;
  urlPreview: string;
  exifTimestamp: string;
  licenciada: boolean;
};

export async function favoritar(atletaId: number, fotoId: number): Promise<void> {
  const r = await fetch(`${BASE}/favoritos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ atletaId, fotoId }),
  });
  if (!r.ok) throw new Error("Erro ao favoritar foto");
}

export async function desfavoritar(atletaId: number, fotoId: number): Promise<void> {
  const r = await fetch(`${BASE}/favoritos`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ atletaId, fotoId }),
  });
  if (!r.ok) throw new Error("Erro ao desfavoritar foto");
}

export async function listarFavoritos(atletaId: number): Promise<FavoritoFotoDto[]> {
  const r = await fetch(`${BASE}/favoritos/${atletaId}`);
  if (!r.ok) throw new Error("Erro ao listar favoritos");
  return r.json();
}

// ─── Licenças ────────────────────────────────────────────────────────────────

export type LicencaDto = {
  id: number;
  atletaId: number;
  fotoId: number;
  preco: number;
  adquiridaEm: string;
  cancelada: boolean;
};

export async function comprarLicenca(atletaId: number, fotoId: number): Promise<LicencaDto> {
  const r = await fetch(`${BASE}/licencas`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ atletaId, fotoId }),
  });
  if (!r.ok) throw new Error("Erro ao comprar licença");
  return r.json();
}

// ─── Fotógrafos ──────────────────────────────────────────────────────────────

export async function listarFotografos(): Promise<FotografoDto[]> {
  const r = await fetch(`${BASE}/fotografos`);
  if (!r.ok) throw new Error("Erro ao listar fotógrafos");
  return r.json();
}

export async function cadastrarFotografo(nome: string, email: string): Promise<void> {
  const r = await fetch(`${BASE}/fotografos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nome, email }),
  });
  if (!r.ok) throw new Error("Erro ao cadastrar fotógrafo");
}
