// Cliente HTTP do sportsnap-gamification-service (features do Galileu: Carta/Reveal,
// Competicao/Ranking, Temporadas, Analise e Desafios). Diferente de lib/db.ts (mock no
// navegador), este modulo fala com o backend Spring real.

export const GAMIFICACAO_URL =
  process.env.NEXT_PUBLIC_GAMIFICACAO_URL ?? "http://localhost:8081";

// ---- Tipos ----

export type AtletaApi = { id: number; nome: string; email: string };

export type AtributoCarta = { nome: string; valor: number; peso: number; tipoEsporte: string };
export type CartaDetalhe = {
  atletaId: number;
  overall: number;
  tier: string;
  saldoPontos: number;
  arquivada: boolean;
  sincronizada: boolean;
  ultimaSincronizacao: string | null;
  atributos: AtributoCarta[];
};

export type Orcamento = { pontosDisponiveis: number; tier: string; xpLatente: number };
export type Simulacao = {
  overallAnterior: number;
  overallResultante: number;
  tierAnterior: string;
  tierResultante: string;
  haveriaPromocao: boolean;
  custoTotal: number;
  saldoRestante: number;
};
export type RegistroSincronizacao = {
  orcamentoPontos: number;
  custoTotal: number;
  overallAnterior: number;
  overallNovo: number;
  variacaoOverall: number;
};
export type Evolucao = {
  overallAnterior: number;
  overallNovo: number;
  delta: number;
  ocorridoEm: string;
};

export type Pontuacao = { atletaId: number; pr: number; liga: string };
export type Confronto = { vencedorId: number; perdedorId: number; prTransferida: number; temporadaId: number };
export type Posicao = { posicao: number; liga: string | null; pr: number; classificado: boolean };

export type Temporada = {
  id: number;
  modalidade: string;
  inicio: string;
  fim: string;
  status: string;
  tamanhoSnapshot: number;
};

export type Percentil = { atributo: string; valor: number; percentil: number };
export type Similar = { atletaId: number; distancia: number };
export type ForcaFraqueza = { atributo: string; valor: number; media: number; classificacao: string };
export type DadosRadar = { valoresNormalizados: Record<string, number> };
export type Projecao = { overallAtual: number; tendencia: number; overallProjetado: number };

export type Desafio = {
  id: number;
  titulo: string;
  insigniaCodigo: string;
  permanente: boolean;
  cadencia: string;
  numeroCriterios: number;
};
export type Progresso = {
  id: number;
  atletaId: number;
  desafioId: number;
  titulo: string;
  status: string;
  percentual: number;
  insigniaConcedida: boolean;
  insigniaCodigo: string | null;
};
export type Insignia = { codigo: string; desafioId: number; concedidaEm: string };

export type CriterioInput = { tipo: string; meta: number; alvoAtributo?: string | null };
export type DesafioInput = {
  titulo: string;
  criterios: CriterioInput[];
  inicio?: string | null;
  fim?: string | null;
  permanente: boolean;
  insigniaCodigo: string;
  prerequisitos: number[];
  cadencia: string;
  repetivel: boolean;
};

// ---- Helper de requisicao ----

async function req<T>(caminho: string, init?: RequestInit): Promise<T> {
  let resp: Response;
  try {
    resp = await fetch(`${GAMIFICACAO_URL}${caminho}`, {
      headers: { "Content-Type": "application/json" },
      cache: "no-store",
      ...init,
    });
  } catch {
    throw new Error(
      `Nao foi possivel conectar ao servico de gamificacao em ${GAMIFICACAO_URL}. Ele esta rodando?`,
    );
  }
  if (!resp.ok) {
    let mensagem = `Erro ${resp.status}`;
    try {
      const corpo = await resp.json();
      if (corpo?.message) mensagem = corpo.message;
    } catch {}
    throw new Error(mensagem);
  }
  if (resp.status === 204) return undefined as T;
  const texto = await resp.text();
  return (texto ? JSON.parse(texto) : undefined) as T;
}

const get = <T>(c: string) => req<T>(c);
const post = <T>(c: string, body?: unknown) =>
  req<T>(c, { method: "POST", body: body === undefined ? undefined : JSON.stringify(body) });

/** Garante inteiros na alocacao (o backend espera Map<String,Integer>). */
function inteiros(alocacao: Record<string, number>): Record<string, number> {
  return Object.fromEntries(Object.entries(alocacao).map(([k, v]) => [k, Math.round(v)]));
}

// ---- API ----

export const gamificacao = {
  // Atletas / Carta
  listarAtletas: () => get<AtletaApi[]>("/api/atletas"),
  carta: (atletaId: number) => get<CartaDetalhe>(`/api/atletas/${atletaId}/carta`),
  evolucao: (atletaId: number) => get<Evolucao[]>(`/api/atletas/${atletaId}/evolucao`),
  sincronizacoes: (atletaId: number) =>
    get<RegistroSincronizacao[]>(`/api/atletas/${atletaId}/sincronizacoes`),

  // Reveal (F1)
  orcamento: (atletaId: number) => get<Orcamento>(`/api/atletas/${atletaId}/reveal/orcamento`),
  simular: (atletaId: number, alocacao: Record<string, number>) =>
    post<Simulacao>(`/api/atletas/${atletaId}/reveal/simular`, { alocacao: inteiros(alocacao) }),
  confirmarReveal: (atletaId: number, alocacao: Record<string, number>) =>
    post<RegistroSincronizacao>(`/api/atletas/${atletaId}/reveal/confirmar`, { alocacao: inteiros(alocacao) }),

  // Competicao (F1)
  classificacao: () => get<Pontuacao[]>("/api/competicao/classificacao"),
  registrarElegivel: (atletaId: number) => post<Pontuacao>(`/api/competicao/elegiveis/${atletaId}`),
  resolverConfronto: (atletaA: number, atletaB: number, modalidade: string) =>
    post<Confronto>("/api/competicao/confrontos", { atletaA, atletaB, modalidade }),
  posicao: (atletaId: number) => get<Posicao>(`/api/competicao/posicao/${atletaId}`),
  oponentes: (atletaId: number, limite = 5) =>
    get<Pontuacao[]>(`/api/competicao/${atletaId}/oponentes?limite=${limite}`),

  // Temporadas (F1)
  listarTemporadas: () => get<Temporada[]>("/api/temporadas"),
  criarTemporada: (modalidade: string, inicio: string, fim: string) =>
    post<Temporada>("/api/temporadas", { modalidade, inicio, fim }),
  cancelarTemporada: (id: number) => post<Temporada>(`/api/temporadas/${id}/cancelar`),
  encerrarTemporada: (id: number) => post<Temporada>(`/api/temporadas/${id}/encerrar`),

  // Analise (F2)
  percentil: (atletaId: number, atributo: string) =>
    get<Percentil>(`/api/atletas/${atletaId}/analise/percentil?atributo=${encodeURIComponent(atributo)}`),
  similares: (atletaId: number, modalidade: string, n = 5) =>
    get<Similar[]>(`/api/atletas/${atletaId}/analise/similares?modalidade=${encodeURIComponent(modalidade)}&n=${n}`),
  forcaFraqueza: (atletaId: number, modalidade: string) =>
    get<ForcaFraqueza[]>(`/api/atletas/${atletaId}/analise/forca-fraqueza?modalidade=${encodeURIComponent(modalidade)}`),
  radar: (atletaId: number) => get<DadosRadar>(`/api/atletas/${atletaId}/analise/radar`),
  projecao: (atletaId: number) => get<Projecao>(`/api/atletas/${atletaId}/analise/projecao`),

  // Desafios (F2)
  listarDesafios: () => get<Desafio[]>("/api/desafios"),
  desafiosDisponiveis: (atletaId: number) =>
    get<Desafio[]>(`/api/desafios/disponiveis?atletaId=${atletaId}`),
  definirDesafio: (input: DesafioInput) => post<Desafio>("/api/desafios", input),
  aceitarDesafio: (desafioId: number, atletaId: number) =>
    post<Progresso>(`/api/desafios/${desafioId}/aceitar?atletaId=${atletaId}`),
  cancelarProgresso: (progressoId: number) =>
    post<Progresso>(`/api/desafios/progressos/${progressoId}/cancelar`),
  progressos: (atletaId: number) => get<Progresso[]>(`/api/desafios/progressos?atletaId=${atletaId}`),
  insignias: (atletaId: number) => get<Insignia[]>(`/api/desafios/insignias?atletaId=${atletaId}`),
  sugestao: (atletaId: number, modalidade: string) =>
    get<Desafio | null>(`/api/desafios/sugestao?atletaId=${atletaId}&modalidade=${encodeURIComponent(modalidade)}`),

  // Registra um "treino" que avanca os desafios na demo (sem consumir XP)
  registrarTreino: (atletaId: number) => post<void>(`/api/atletas/${atletaId}/registrar-treino`),
};

export const TIERS_TOM: Record<string, "neutral" | "info" | "warning" | "accent" | "success"> = {
  BRONZE: "warning",
  PRATA: "neutral",
  OURO: "info",
  LENDARIA: "accent",
};
