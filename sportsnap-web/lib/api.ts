export const GAMIFICATION_BASE = "http://localhost:8081";
export const MARKETPLACE_BASE = "http://localhost:8082";
export const SESSION_BASE = "http://localhost:8083";

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    cache: "no-store",
    headers: { "Content-Type": "application/json" },
    ...init,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(text || `${res.status} ${res.statusText}`);
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}

export const api = {
  get: <T>(url: string) => request<T>(url),
  post: <T>(url: string, body: unknown) =>
    request<T>(url, { method: "POST", body: JSON.stringify(body) }),
};

export type Atleta = { id: number; nome: string; email: string };
export type CartaResumo = {
  atletaId: number;
  overall: number;
  ultimaSincronizacao: string | null;
  sincronizada: boolean;
};
export type Spot = { id: number; nome: string; latitude: number; longitude: number; descricao?: string };
export type Sessao = {
  id: number;
  spotId: number;
  periodoInicio: string;
  periodoFim: string;
  descricao: string;
};
export type Fotografo = { id: number; nome: string; email: string };
export type Lote = {
  id: number;
  fotografoId: number;
  sessaoId: number;
  spotId: number;
  descricao: string;
  criadoEm: string;
  arquivado: boolean;
};
export type Foto = {
  id: number;
  loteId: number;
  urlPreview: string;
  urlOriginal: string;
  exifTimestamp: string;
  exifDetalhes: string;
  licenciada: boolean;
  removida: boolean;
};
export type Licenca = {
  id: number;
  atletaId: number;
  fotoId: number;
  preco: number;
  adquiridaEm: string;
  cancelada: boolean;
};
