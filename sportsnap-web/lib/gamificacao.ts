// Cliente HTTP do sportsnap-gamification-service (features do Galileu: Carta/Reveal,
// Competicao/Ranking, Temporadas, Analise e Desafios). Diferente de lib/db.ts (mock no
// navegador), este modulo fala com o backend Spring real.

import { db } from "./db";

export const GAMIFICACAO_URL =
  process.env.NEXT_PUBLIC_GAMIFICACAO_URL ?? "http://localhost:8081";

const MOCK_EVOLUCOES: Record<number, Evolucao[]> = {
  1: [
    { overallAnterior: 78.0, overallNovo: 79.5, delta: 1.5, ocorridoEm: "2026-05-15T10:00:00Z" },
    { overallAnterior: 79.5, overallNovo: 82.0, delta: 2.5, ocorridoEm: "2026-06-01T10:00:00Z" }
  ],
  2: [
    { overallAnterior: 73.0, overallNovo: 75.5, delta: 2.5, ocorridoEm: "2026-06-01T10:00:00Z" }
  ]
};

const EVOLUCOES_STORAGE_KEY = "sportsnap_evolucao_v1";

function obterEvolucoesLocais(atletaId: number): Evolucao[] {
  if (typeof window === "undefined") return MOCK_EVOLUCOES[atletaId] || [];
  try {
    const raw = localStorage.getItem(EVOLUCOES_STORAGE_KEY);
    const data = raw ? JSON.parse(raw) : {};
    return data[atletaId] || MOCK_EVOLUCOES[atletaId] || [];
  } catch {
    return MOCK_EVOLUCOES[atletaId] || [];
  }
}

function salvarEvolucaoLocal(atletaId: number, evo: Evolucao) {
  if (typeof window === "undefined") return;
  try {
    const raw = localStorage.getItem(EVOLUCOES_STORAGE_KEY);
    const data = raw ? JSON.parse(raw) : {};
    if (!data[atletaId]) data[atletaId] = MOCK_EVOLUCOES[atletaId] || [];
    data[atletaId].push(evo);
    localStorage.setItem(EVOLUCOES_STORAGE_KEY, JSON.stringify(data));
  } catch {}
}

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
  listarAtletas: async () => {
    try {
      return await get<AtletaApi[]>("/api/atletas");
    } catch (e) {
      console.warn("Usando DB local para listarAtletas:", e);
      return db.get("atletas") as AtletaApi[];
    }
  },
  
  carta: async (atletaId: number) => {
    try {
      return await get<CartaDetalhe>(`/api/atletas/${atletaId}/carta`);
    } catch (e) {
      console.warn("Usando DB local para obter carta:", e);
      const dbCarta = db.find("cartas", c => c.atletaId === atletaId);
      if (!dbCarta) throw new Error("Carta não encontrada no DB local");
      
      const stats = db.getShadowStats(atletaId);
      const sincronizada = dbCarta.ultimaSincronizacao !== null;
      
      return {
        atletaId: dbCarta.atletaId,
        overall: dbCarta.overall,
        tier: dbCarta.overall >= 96 ? "LENDARIA" : dbCarta.overall >= 90 ? "OURO" : dbCarta.overall >= 80 ? "PRATA" : "BRONZE",
        saldoPontos: Math.max(0, Math.floor(stats.xpAcumulado / 10)),
        arquivada: false,
        sincronizada: sincronizada,
        ultimaSincronizacao: dbCarta.ultimaSincronizacao,
        atributos: [
          { nome: "Resistência", valor: dbCarta.resistencia, peso: 1, tipoEsporte: atletaId === 1 ? "surf" : "futebol" },
          { nome: "Velocidade", valor: dbCarta.velocidade, peso: 1, tipoEsporte: atletaId === 1 ? "surf" : "futebol" },
          { nome: "Técnica", valor: dbCarta.tecnica, peso: 1, tipoEsporte: atletaId === 1 ? "surf" : "futebol" },
          { nome: "Explosão", valor: dbCarta.explosao, peso: 1, tipoEsporte: atletaId === 1 ? "surf" : "futebol" }
        ]
      } as CartaDetalhe;
    }
  },

  evolucao: async (atletaId: number) => {
    try {
      return await get<Evolucao[]>(`/api/atletas/${atletaId}/evolucao`);
    } catch (e) {
      console.warn("Usando local storage para evolucao:", e);
      return obterEvolucoesLocais(atletaId);
    }
  },

  sincronizacoes: async (atletaId: number) => {
    try {
      return await get<RegistroSincronizacao[]>(`/api/atletas/${atletaId}/sincronizacoes`);
    } catch (e) {
      console.warn("Sincronizacoes da API indisponiveis localmente:", e);
      return [];
    }
  },

  // Reveal (F1)
  orcamento: async (atletaId: number) => {
    try {
      return await get<Orcamento>(`/api/atletas/${atletaId}/reveal/orcamento`);
    } catch (e) {
      console.warn("Usando DB local para orcamento:", e);
      const dbCarta = db.find("cartas", c => c.atletaId === atletaId);
      const stats = db.getShadowStats(atletaId);
      const licencas = db.filter("licencas", l => l.atletaId === atletaId && !l.cancelada);
      
      if (licencas.length === 0) {
        throw new Error("Você precisa adquirir ao menos 1 licença de foto no Marketplace para desbloquear seu Reveal.");
      }
      if (stats.xpAcumulado <= 0) {
        throw new Error("Você não possui XP Latente acumulado de treinos recentes.");
      }
      
      const pontosDisponiveis = Math.floor(stats.xpAcumulado / 10);
      if (pontosDisponiveis <= 0) {
        throw new Error("Seu XP Latente atual é muito baixo para gerar pontos de Reveal (mínimo de 10 XP).");
      }
      
      return {
        pontosDisponiveis,
        tier: dbCarta ? (dbCarta.overall >= 96 ? "LENDARIA" : dbCarta.overall >= 90 ? "OURO" : dbCarta.overall >= 80 ? "PRATA" : "BRONZE") : "BRONZE",
        xpLatente: stats.xpAcumulado
      };
    }
  },

  simular: async (atletaId: number, alocacao: Record<string, number>) => {
    try {
      return await post<Simulacao>(`/api/atletas/${atletaId}/reveal/simular`, { alocacao: inteiros(alocacao) });
    } catch (e) {
      console.warn("Usando DB local para simular reveal:", e);
      const dbCarta = db.find("cartas", c => c.atletaId === atletaId);
      if (!dbCarta) throw new Error("Carta não encontrada");
      const overallAnterior = dbCarta.overall;
      
      const res = dbCarta.resistencia + (alocacao["Resistência"] || 0);
      const vel = dbCarta.velocidade + (alocacao["Velocidade"] || 0);
      const tec = dbCarta.tecnica + (alocacao["Técnica"] || 0);
      const exp = dbCarta.explosao + (alocacao["Explosão"] || 0);
      const overallResultante = (res + vel + tec + exp) / 4;
      
      const tierAnterior = overallAnterior >= 96 ? "LENDARIA" : overallAnterior >= 90 ? "OURO" : overallAnterior >= 80 ? "PRATA" : "BRONZE";
      const tierResultante = overallResultante >= 96 ? "LENDARIA" : overallResultante >= 90 ? "OURO" : overallResultante >= 80 ? "PRATA" : "BRONZE";
      
      return {
        overallAnterior,
        overallResultante,
        tierAnterior,
        tierResultante,
        haveriaPromocao: tierResultante !== tierAnterior,
        custoTotal: Object.values(alocacao).reduce((a, b) => a + b, 0),
        saldoRestante: 0
      };
    }
  },

  confirmarReveal: async (atletaId: number, alocacao: Record<string, number>) => {
    try {
      return await post<RegistroSincronizacao>(`/api/atletas/${atletaId}/reveal/confirmar`, { alocacao: inteiros(alocacao) });
    } catch (e) {
      console.warn("Erro ao confirmar reveal na API, usando DB local:", e);
      const dbCarta = db.find("cartas", c => c.atletaId === atletaId);
      if (!dbCarta) throw new Error("Carta não encontrada no DB local");

      const overallAnterior = dbCarta.overall;
      
      if (alocacao["Resistência"]) dbCarta.resistencia += alocacao["Resistência"];
      if (alocacao["Velocidade"]) dbCarta.velocidade += alocacao["Velocidade"];
      if (alocacao["Técnica"]) dbCarta.tecnica += alocacao["Técnica"];
      if (alocacao["Explosão"]) dbCarta.explosao += alocacao["Explosão"];
      
      dbCarta.overall = (dbCarta.resistencia + dbCarta.velocidade + dbCarta.tecnica + dbCarta.explosao) / 4;
      dbCarta.ultimaSincronizacao = new Date().toISOString();
      db.update("cartas", atletaId, dbCarta);
      
      const stats = db.getShadowStats(atletaId);
      stats.xpAcumulado = 0;
      stats.streak += 1;
      db.update("shadowStats", atletaId, stats);
      
      const delta = dbCarta.overall - overallAnterior;
      const evo: Evolucao = {
        overallAnterior,
        overallNovo: dbCarta.overall,
        delta,
        ocorridoEm: new Date().toISOString()
      };
      salvarEvolucaoLocal(atletaId, evo);
      
      return {
        orcamentoPontos: Object.values(alocacao).reduce((a, b) => a + b, 0),
        custoTotal: Object.values(alocacao).reduce((a, b) => a + b, 0),
        overallAnterior,
        overallNovo: dbCarta.overall,
        variacaoOverall: delta
      };
    }
  },

  // Competicao (F1)
  classificacao: async () => {
    try {
      return await get<Pontuacao[]>("/api/competicao/classificacao");
    } catch (e) {
      console.warn("Erro ao listar classificacao da API, usando DB local:", e);
      return db.getRankedCartas().map((c, i) => ({
        atletaId: c.atletaId,
        pr: 1200 - i * 100,
        liga: c.overall >= 90 ? "OURO" : c.overall >= 80 ? "PRATA" : "BRONZE"
      }));
    }
  },

  registrarElegivel: (atletaId: number) => post<Pontuacao>(`/api/competicao/elegiveis/${atletaId}`),
  resolverConfronto: (atletaA: number, atletaB: number, modalidade: string) =>
    post<Confronto>("/api/competicao/confrontos", { atletaA, atletaB, modalidade }),
    
  posicao: async (atletaId: number) => {
    try {
      return await get<Posicao>(`/api/competicao/posicao/${atletaId}`);
    } catch (e) {
      console.warn("Erro ao buscar posicao da API, usando DB local:", e);
      const ranked = db.getRankedCartas();
      const index = ranked.findIndex(r => r.atletaId === atletaId);
      const pos = index !== -1 ? index + 1 : 99;
      return {
        posicao: pos,
        liga: atletaId === 1 ? "Prata II" : "Bronze I",
        pr: atletaId === 1 ? 1200 : 950,
        classificado: false
      };
    }
  },

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
    
  progressos: async (atletaId: number) => {
    try {
      return await get<Progresso[]>(`/api/desafios/progressos?atletaId=${atletaId}`);
    } catch (e) {
      console.warn("Erro ao buscar progressos da API, usando DB local:", e);
      return [
        { id: 1, atletaId, desafioId: 1, titulo: "Treino de Sábado", status: "EM_ANDAMENTO", percentual: 50, insigniaConcedida: false, insigniaCodigo: null },
        { id: 2, atletaId, desafioId: 2, titulo: "Mestre das Ondas", status: "CONCLUIDO", percentual: 100, insigniaConcedida: true, insigniaCodigo: "surf_master" }
      ];
    }
  },

  insignias: async (atletaId: number) => {
    try {
      return await get<Insignia[]>(`/api/desafios/insignias?atletaId=${atletaId}`);
    } catch (e) {
      console.warn("Erro ao buscar insignias da API, usando DB local:", e);
      return atletaId === 1 ? [
        { codigo: "surf_master", desafioId: 2, concedidaEm: "2026-06-10T12:00:00Z" }
      ] : [];
    }
  },

  sugestao: (atletaId: number, modalidade: string) =>
    get<Desafio | null>(`/api/desafios/sugestao?atletaId=${atletaId}&modalidade=${encodeURIComponent(modalidade)}`),

  registrarTreino: (atletaId: number) => post<void>(`/api/atletas/${atletaId}/registrar-treino`),
};

export const TIERS_TOM: Record<string, "neutral" | "info" | "warning" | "accent" | "success"> = {
  BRONZE: "warning",
  PRATA: "neutral",
  OURO: "info",
  LENDARIA: "accent",
};
