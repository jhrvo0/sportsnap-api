const BASE_URLS = {
  gamification: process.env.NEXT_PUBLIC_GAMIFICATION_URL || "http://localhost:8081",
  marketplace: process.env.NEXT_PUBLIC_MARKETPLACE_URL || "http://localhost:8082",
  session: process.env.NEXT_PUBLIC_SESSION_URL || "http://localhost:8083",
};

async function request(base: keyof typeof BASE_URLS, path: string, options?: RequestInit) {
  const res = await fetch(`${BASE_URLS[base]}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...options?.headers },
  });
  if (!res.ok) throw new Error(`API Error: ${res.status}`);
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  criarAtleta: (nome: string, email: string) =>
    request("gamification", "/api/gamificacao/atletas", {
      method: "POST",
      body: JSON.stringify({ nome, email }),
    }),
  buscarAtleta: (id: number) =>
    request("gamification", `/api/gamificacao/atletas/${id}`),
  listarAtletas: () =>
    request("gamification", "/api/gamificacao/atletas"),
  sincronizarCarta: (id: number) =>
    request("gamification", `/api/gamificacao/atletas/${id}/sincronizar`, { method: "POST" }),
  buscarCarta: (id: number) =>
    request("gamification", `/api/gamificacao/atletas/${id}/carta`),
  buscarStatusPotencial: (id: number) =>
    request("gamification", `/api/gamificacao/atletas/${id}/status-potencial`),
  buscarRanking: () =>
    request("gamification", "/api/gamificacao/ranking"),

  criarFotografo: (nome: string, email: string) =>
    request("marketplace", "/api/marketplace/fotografos", {
      method: "POST",
      body: JSON.stringify({ nome, email }),
    }),
  listarLotes: (fotografoId: number) =>
    request("marketplace", `/api/marketplace/fotografos/${fotografoId}/lotes`),
  criarLote: (fotografoId: number, sessionId: number, spotId: number) =>
    request("marketplace", `/api/marketplace/fotografos/${fotografoId}/lotes`, {
      method: "POST",
      body: JSON.stringify({ sessionId, spotId }),
    }),
  uploadFotos: (loteId: number, caminhos: string[]) =>
    request("marketplace", `/api/marketplace/lotes/${loteId}/upload`, {
      method: "POST",
      body: JSON.stringify({ caminhos }),
    }),
  listarFotos: (loteId: number) =>
    request("marketplace", `/api/marketplace/lotes/${loteId}/fotos`),
  comprarLicenca: (fotoId: number, atletaId: number) =>
    request("marketplace", `/api/marketplace/fotos/${fotoId}/comprar`, {
      method: "POST",
      body: JSON.stringify({ atletaId }),
    }),
  listarLicencas: (atletaId: number) =>
    request("marketplace", `/api/marketplace/atletas/${atletaId}/licencas`),

  listarSpots: () =>
    request("session", "/api/sessoes/spots"),
  criarSpot: (nome: string, latitude: number, longitude: number, descricao: string) =>
    request("session", "/api/sessoes/spots", {
      method: "POST",
      body: JSON.stringify({ nome, latitude, longitude, descricao }),
    }),
  listarSessions: (spotId: number) =>
    request("session", `/api/sessoes/spots/${spotId}/sessions`),
  realizarCheckIn: (sessionId: number, atletaId: number, latitude: number, longitude: number) =>
    request("session", `/api/sessoes/sessions/${sessionId}/checkin`, {
      method: "POST",
      body: JSON.stringify({ atletaId, latitude, longitude }),
    }),
  listarCheckIns: (sessionId: number) =>
    request("session", `/api/sessoes/sessions/${sessionId}/checkins`),
};
