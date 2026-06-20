"use client";

import { Spot, Sessao, Lote, Foto, Licenca, Atleta, Fotografo, CartaResumo } from "./api";

// --- Tipos usados apenas no estado local ---

export type CheckIn = {
  id: number;
  atletaId: number;
  sessaoId: number;
  horario: string;
  checkoutHorario?: string | null;
  cancelado: boolean;
  temAtividade: boolean;
};

// The prototype supports built-in sports plus custom sports created at runtime.
export type SportType = string;

export type CustomMetricaValue = {
  label: string;
  value: number;
  unit: string;
};

export type RegistroAtividade = {
  id: number;
  atletaId?: number;
  checkInId?: number | null;
  esporte?: string;
  data?: string;
  distancia?: number;
  duracaoSegundos?: number;
  origemRegistro?: "MANUAL" | "CHECKIN" | "IMPORTADO" | string;
  sport: SportType;
  duracao: number; // em minutos
  intensidade: "baixa" | "media" | "alta";
  xpGanho: number;
  metricas: {
    distancia?: number; // em km
    ondas?: number; // surfe
    manobras?: number; // skate
    gols?: number; // futebol
    assistencias?: number; // futebol
    velocidadeMax?: number; // surfe/skate/corrida
    custom?: CustomMetricaValue[];
  };
};

export type MetricaDefinition = {
  id: string;
  label: string;
  sport: SportType;
  unit: string;
};

export type ShadowStats = {
  atletaId: number;
  xpAcumulado: number;
  streak: number;
  customMetricas: MetricaDefinition[];
};

export type CartaOficial = {
  atletaId: number;
  nome: string;
  overall: number;
  resistencia: number;
  velocidade: number;
  tecnica: number;
  explosao: number;
  imagemUrl?: string;
  ultimaSincronizacao: string | null;
};

// --- Implementação do banco local ---

const STORAGE_KEY = "sportsnap_db";

export type CustomSport = { id: string; label: string };

type DBData = {
  spots: Spot[];
  sessoes: Sessao[];
  checkins: CheckIn[];
  atividades: RegistroAtividade[];
  lotes: Lote[];
  fotos: Foto[];
  licencas: Licenca[];
  atletas: Atleta[];
  fotografos: Fotografo[];
  cartas: CartaOficial[];
  shadowStats: ShadowStats[];
  customSports: CustomSport[];
};

const INITIAL_DATA: DBData = {
  spots: [
    { id: 1, nome: "Praia de Stella Maris", latitude: -12.9463, longitude: -38.3308, descricao: "Excelente pico de surf em Salvador. Cuidado com as pedras na maré seca." },
    { id: 2, nome: "Pista da Costeira", latitude: -27.6083, longitude: -48.5492, descricao: "Pista de skate clássica de Floripa. Banks e mini ramp excelentes." },
    { id: 3, nome: "Aterro do Flamengo", latitude: -22.9367, longitude: -43.1729, descricao: "Ótimo para corrida e treinos funcionais ao ar livre." }
  ],
  sessoes: [
    // Sessão passada
    { id: 1, spotId: 1, periodoInicio: "2026-06-05T08:00:00Z", periodoFim: "2026-06-05T12:00:00Z", descricao: "Swell de Inverno - Stella" },
    // Sessão ativa com check-in
    { id: 2, spotId: 2, periodoInicio: "2026-06-08T06:00:00Z", periodoFim: "2026-06-08T22:00:00Z", descricao: "Sessão Livre - Costeira" },
    // Sessão futura
    { id: 3, spotId: 3, periodoInicio: "2026-06-10T18:00:00Z", periodoFim: "2026-06-10T21:00:00Z", descricao: "Treino Noturno 10k" },
    // Sessão ativa sem check-in
    { id: 4, spotId: 3, periodoInicio: "2026-06-08T00:00:00Z", periodoFim: "2026-06-08T23:59:00Z", descricao: "Corrida Matinal - Aterro" }
  ],
  checkins: [
    // Check-in passado com atividades
    { id: 1, atletaId: 1, sessaoId: 1, horario: "2026-06-05T08:30:00Z", checkoutHorario: "2026-06-05T11:45:00Z", cancelado: false, temAtividade: true },
    // Check-in ativo com atividades
    { id: 2, atletaId: 1, sessaoId: 2, horario: "2026-06-08T14:00:00Z", cancelado: false, temAtividade: true }
  ],
  atividades: [
    // Atividades do check-in passado (surfe)
    { id: 1, checkInId: 1, sport: "surf", duracao: 45, intensidade: "media", xpGanho: 70, metricas: { ondas: 8, velocidadeMax: 22 } },
    { id: 2, checkInId: 1, sport: "surf", duracao: 60, intensidade: "alta", xpGanho: 135, metricas: { ondas: 12, velocidadeMax: 28 } },
    { id: 3, checkInId: 1, sport: "surf", duracao: 30, intensidade: "baixa", xpGanho: 30, metricas: { ondas: 4, velocidadeMax: 18 } },
    // Atividades do check-in ativo (skate)
    { id: 4, checkInId: 2, sport: "skate", duracao: 40, intensidade: "media", xpGanho: 80, metricas: { manobras: 15, velocidadeMax: 15 } },
    { id: 5, checkInId: 2, sport: "skate", duracao: 50, intensidade: "alta", xpGanho: 150, metricas: { manobras: 22, velocidadeMax: 18 } }
  ],
  lotes: [],
  fotos: [],
  licencas: [],
  atletas: [
    { id: 1, nome: "João Henrique", email: "joao@atleta.com" }
  ],
  fotografos: [
    { id: 2, nome: "Antônio Paes", email: "antonio@foto.com" }
  ],
  customSports: [],
  cartas: [
    { 
      atletaId: 1, 
      nome: "João Henrique", 
      overall: 78.5, 
      resistencia: 80, 
      velocidade: 75, 
      tecnica: 82, 
      explosao: 77, 
      ultimaSincronizacao: "2026-06-01T10:00:00Z" 
    }
  ],
  shadowStats: [
    { atletaId: 1, xpAcumulado: 45.5, streak: 3, customMetricas: [
      { id: "custom_def", label: "Defesas Difíceis", unit: "defesas", sport: "futebol" }
    ]}
  ]
};

const COLECOES_COM_ID: Array<keyof DBData> = [
  "spots",
  "sessoes",
  "checkins",
  "atividades",
  "lotes",
  "fotos",
  "licencas",
  "atletas",
  "fotografos",
];

type ItemDaColecao<K extends keyof DBData> = DBData[K][number];
type NovoItem<K extends keyof DBData> = ItemDaColecao<K> extends { id: number }
  ? Omit<ItemDaColecao<K>, "id">
  : ItemDaColecao<K>;

function clonarDadosIniciais(): DBData {
  const dados = JSON.parse(JSON.stringify(INITIAL_DATA)) as DBData;
  const dataRelativa = (horas: number) => new Date(Date.now() + horas * 60 * 60 * 1000).toISOString();

  dados.sessoes = [
    { id: 1, spotId: 1, periodoInicio: dataRelativa(-120), periodoFim: dataRelativa(-116), descricao: "Swell de Inverno - Stella" },
    { id: 2, spotId: 2, periodoInicio: dataRelativa(-2), periodoFim: dataRelativa(4), descricao: "Sessão Livre - Costeira" },
    { id: 3, spotId: 3, periodoInicio: dataRelativa(24), periodoFim: dataRelativa(27), descricao: "Treino Noturno 10k" },
    { id: 4, spotId: 3, periodoInicio: dataRelativa(-1), periodoFim: dataRelativa(3), descricao: "Corrida Matinal - Aterro" },
  ];

  dados.checkins = [
    { id: 1, atletaId: 1, sessaoId: 1, horario: dataRelativa(-119), checkoutHorario: dataRelativa(-117), cancelado: false, temAtividade: true },
    { id: 2, atletaId: 1, sessaoId: 2, horario: dataRelativa(-1), cancelado: false, temAtividade: true },
  ];

  const mockAtividades: RegistroAtividade[] = [
    // --- CORRIDA (Running) ---
    {
      id: 10, atletaId: 1, sport: "corrida", esporte: "CORRIDA", duracao: 25, duracaoSegundos: 25 * 60,
      intensidade: "baixa", xpGanho: 50, data: dataRelativa(-24 * 28), distancia: 4.2, origemRegistro: "MANUAL",
      metricas: { distancia: 4.2, velocidadeMax: 12.0 }
    },
    {
      id: 11, atletaId: 1, sport: "corrida", esporte: "CORRIDA", duracao: 30, duracaoSegundos: 30 * 60,
      intensidade: "media", xpGanho: 70, data: dataRelativa(-24 * 25), distancia: 5.0, origemRegistro: "MANUAL",
      metricas: { distancia: 5.0, velocidadeMax: 13.0 }
    },
    {
      id: 12, atletaId: 1, sport: "corrida", esporte: "CORRIDA", duracao: 28, duracaoSegundos: 28 * 60,
      intensidade: "media", xpGanho: 65, data: dataRelativa(-24 * 22), distancia: 5.2, origemRegistro: "MANUAL",
      metricas: { distancia: 5.2, velocidadeMax: 12.5 }
    },
    {
      id: 13, atletaId: 1, sport: "corrida", esporte: "CORRIDA", duracao: 35, duracaoSegundos: 35 * 60,
      intensidade: "media", xpGanho: 80, data: dataRelativa(-24 * 19), distancia: 6.5, origemRegistro: "MANUAL",
      metricas: { distancia: 6.5, velocidadeMax: 14.0 }
    },
    {
      id: 14, atletaId: 1, sport: "corrida", esporte: "CORRIDA", duracao: 42, duracaoSegundos: 42 * 60,
      intensidade: "alta", xpGanho: 110, data: dataRelativa(-24 * 16), distancia: 8.0, origemRegistro: "MANUAL",
      metricas: { distancia: 8.0, velocidadeMax: 15.0 }
    },
    {
      id: 15, atletaId: 1, sport: "corrida", esporte: "CORRIDA", duracao: 38, duracaoSegundos: 38 * 60,
      intensidade: "media", xpGanho: 90, data: dataRelativa(-24 * 13), distancia: 7.2, origemRegistro: "MANUAL",
      metricas: { distancia: 7.2, velocidadeMax: 14.5 }
    },
    {
      id: 16, atletaId: 1, sport: "corrida", esporte: "CORRIDA", duracao: 50, duracaoSegundos: 50 * 60,
      intensidade: "alta", xpGanho: 140, data: dataRelativa(-24 * 10), distancia: 10.0, origemRegistro: "MANUAL",
      metricas: { distancia: 10.0, velocidadeMax: 16.0 }
    },
    {
      id: 17, atletaId: 1, sport: "corrida", esporte: "CORRIDA", duracao: 45, duracaoSegundos: 45 * 60,
      intensidade: "media", xpGanho: 115, data: dataRelativa(-24 * 7), distancia: 9.0, origemRegistro: "MANUAL",
      metricas: { distancia: 9.0, velocidadeMax: 15.2 }
    },
    {
      id: 18, atletaId: 1, sport: "corrida", esporte: "CORRIDA", duracao: 55, duracaoSegundos: 55 * 60,
      intensidade: "alta", xpGanho: 160, data: dataRelativa(-24 * 4), distancia: 11.5, origemRegistro: "MANUAL",
      metricas: { distancia: 11.5, velocidadeMax: 16.8 }
    },
    {
      id: 19, atletaId: 1, sport: "corrida", esporte: "CORRIDA", duracao: 58, duracaoSegundos: 58 * 60,
      intensidade: "alta", xpGanho: 175, data: dataRelativa(-24 * 2), distancia: 12.0, origemRegistro: "MANUAL",
      metricas: { distancia: 12.0, velocidadeMax: 17.2 }
    },

    // --- SURF (Surfing) ---
    {
      id: 20, atletaId: 1, sport: "surf", esporte: "SURF", duracao: 60, duracaoSegundos: 60 * 60,
      intensidade: "media", xpGanho: 80, data: dataRelativa(-24 * 25), origemRegistro: "MANUAL",
      metricas: { ondas: 8, velocidadeMax: 21.0 }
    },
    {
      id: 21, atletaId: 1, sport: "surf", esporte: "SURF", duracao: 80, duracaoSegundos: 80 * 60,
      intensidade: "alta", xpGanho: 120, data: dataRelativa(-24 * 20), origemRegistro: "MANUAL",
      metricas: { ondas: 12, velocidadeMax: 26.0 }
    },
    {
      id: 22, atletaId: 1, sport: "surf", esporte: "SURF", duracao: 50, duracaoSegundos: 50 * 60,
      intensidade: "baixa", xpGanho: 50, data: dataRelativa(-24 * 15), origemRegistro: "MANUAL",
      metricas: { ondas: 5, velocidadeMax: 19.0 }
    },
    {
      id: 23, atletaId: 1, sport: "surf", esporte: "SURF", duracao: 90, duracaoSegundos: 90 * 60,
      intensidade: "alta", xpGanho: 150, data: dataRelativa(-24 * 10), origemRegistro: "MANUAL",
      metricas: { ondas: 15, velocidadeMax: 28.0 }
    },
    {
      id: 24, atletaId: 1, sport: "surf", esporte: "SURF", duracao: 75, duracaoSegundos: 75 * 60,
      intensidade: "media", xpGanho: 100, data: dataRelativa(-24 * 5), origemRegistro: "MANUAL",
      metricas: { ondas: 10, velocidadeMax: 24.0 }
    },
    {
      id: 25, atletaId: 1, sport: "surf", esporte: "SURF", duracao: 100, duracaoSegundos: 100 * 60,
      intensidade: "alta", xpGanho: 180, data: dataRelativa(-24 * 2), origemRegistro: "MANUAL",
      metricas: { ondas: 18, velocidadeMax: 32.0 }
    },

    // --- SKATE (Skateboarding) ---
    {
      id: 30, atletaId: 1, sport: "skate", esporte: "SKATE", duracao: 40, duracaoSegundos: 40 * 60,
      intensidade: "media", xpGanho: 60, data: dataRelativa(-24 * 24), origemRegistro: "MANUAL",
      metricas: { manobras: 10, velocidadeMax: 12.0 }
    },
    {
      id: 31, atletaId: 1, sport: "skate", esporte: "SKATE", duracao: 50, duracaoSegundos: 50 * 60,
      intensidade: "media", xpGanho: 75, data: dataRelativa(-24 * 18), origemRegistro: "MANUAL",
      metricas: { manobras: 15, velocidadeMax: 14.0 }
    },
    {
      id: 32, atletaId: 1, sport: "skate", esporte: "SKATE", duracao: 60, duracaoSegundos: 60 * 60,
      intensidade: "alta", xpGanho: 100, data: dataRelativa(-24 * 12), origemRegistro: "MANUAL",
      metricas: { manobras: 20, velocidadeMax: 16.0 }
    },
    {
      id: 33, atletaId: 1, sport: "skate", esporte: "SKATE", duracao: 45, duracaoSegundos: 45 * 60,
      intensidade: "media", xpGanho: 70, data: dataRelativa(-24 * 6), origemRegistro: "MANUAL",
      metricas: { manobras: 12, velocidadeMax: 13.0 }
    },
    {
      id: 34, atletaId: 1, sport: "skate", esporte: "SKATE", duracao: 70, duracaoSegundos: 70 * 60,
      intensidade: "alta", xpGanho: 130, data: dataRelativa(-24 * 3), origemRegistro: "MANUAL",
      metricas: { manobras: 25, velocidadeMax: 17.0 }
    }
  ];

  dados.atividades = [...dados.atividades, ...mockAtividades];

  return dados;
}

function normalizarDados(data: Partial<DBData>): DBData {
  const base = clonarDadosIniciais();
  try {
    if (!data || typeof data !== "object") return base;

    const normalizado = { ...base, ...data };
    
    normalizado.shadowStats = (normalizado.shadowStats || []).map((stats) => {
      if (!stats) return { atletaId: 1, xpAcumulado: 0, streak: 0, customMetricas: [] };
      return {
        ...stats,
        customMetricas: stats.customMetricas || [],
      };
    });

    if (data.atividades && Array.isArray(data.atividades)) {
      const localIds = new Set(data.atividades.filter(a => a && a.id).map((a: any) => a.id));
      const novas = base.atividades.filter((a) => !localIds.has(a.id));
      normalizado.atividades = [...data.atividades.filter(a => a), ...novas];
    } else {
      normalizado.atividades = base.atividades;
    }
    
    return normalizado;
  } catch (e) {
    console.error("Erro na normalizacao:", e);
    return base;
  }
}

function gerarMockAtividades(atletaId: number, startId: number): RegistroAtividade[] {
  const dataRelativa = (horas: number) => new Date(Date.now() + horas * 60 * 60 * 1000).toISOString();
  let actId = startId;
  return [
    // --- CORRIDA (Running) ---
    {
      id: actId++, atletaId, sport: "corrida", esporte: "CORRIDA", duracao: 25, duracaoSegundos: 25 * 60,
      intensidade: "baixa", xpGanho: 50, data: dataRelativa(-24 * 28), distancia: 4.2, origemRegistro: "MANUAL",
      metricas: { distancia: 4.2, velocidadeMax: 12.0 }
    },
    {
      id: actId++, atletaId, sport: "corrida", esporte: "CORRIDA", duracao: 30, duracaoSegundos: 30 * 60,
      intensidade: "media", xpGanho: 70, data: dataRelativa(-24 * 25), distancia: 5.0, origemRegistro: "MANUAL",
      metricas: { distancia: 5.0, velocidadeMax: 13.0 }
    },
    {
      id: actId++, atletaId, sport: "corrida", esporte: "CORRIDA", duracao: 28, duracaoSegundos: 28 * 60,
      intensidade: "media", xpGanho: 65, data: dataRelativa(-24 * 22), distancia: 5.2, origemRegistro: "MANUAL",
      metricas: { distancia: 5.2, velocidadeMax: 12.5 }
    },
    {
      id: actId++, atletaId, sport: "corrida", esporte: "CORRIDA", duracao: 35, duracaoSegundos: 35 * 60,
      intensidade: "media", xpGanho: 80, data: dataRelativa(-24 * 19), distancia: 6.5, origemRegistro: "MANUAL",
      metricas: { distancia: 6.5, velocidadeMax: 14.0 }
    },
    {
      id: actId++, atletaId, sport: "corrida", esporte: "CORRIDA", duracao: 42, duracaoSegundos: 42 * 60,
      intensidade: "alta", xpGanho: 110, data: dataRelativa(-24 * 16), distancia: 8.0, origemRegistro: "MANUAL",
      metricas: { distancia: 8.0, velocidadeMax: 15.0 }
    },
    {
      id: actId++, atletaId, sport: "corrida", esporte: "CORRIDA", duracao: 38, duracaoSegundos: 38 * 60,
      intensidade: "media", xpGanho: 90, data: dataRelativa(-24 * 13), distancia: 7.2, origemRegistro: "MANUAL",
      metricas: { distancia: 7.2, velocidadeMax: 14.5 }
    },
    {
      id: actId++, atletaId, sport: "corrida", esporte: "CORRIDA", duracao: 50, duracaoSegundos: 50 * 60,
      intensidade: "alta", xpGanho: 140, data: dataRelativa(-24 * 10), distancia: 10.0, origemRegistro: "MANUAL",
      metricas: { distancia: 10.0, velocidadeMax: 16.0 }
    },
    {
      id: actId++, atletaId, sport: "corrida", esporte: "CORRIDA", duracao: 45, duracaoSegundos: 45 * 60,
      intensidade: "media", xpGanho: 115, data: dataRelativa(-24 * 7), distancia: 9.0, origemRegistro: "MANUAL",
      metricas: { distancia: 9.0, velocidadeMax: 15.2 }
    },
    {
      id: actId++, atletaId, sport: "corrida", esporte: "CORRIDA", duracao: 55, duracaoSegundos: 55 * 60,
      intensidade: "alta", xpGanho: 160, data: dataRelativa(-24 * 4), distancia: 11.5, origemRegistro: "MANUAL",
      metricas: { distancia: 11.5, velocidadeMax: 16.8 }
    },
    {
      id: actId++, atletaId, sport: "corrida", esporte: "CORRIDA", duracao: 58, duracaoSegundos: 58 * 60,
      intensidade: "alta", xpGanho: 175, data: dataRelativa(-24 * 2), distancia: 12.0, origemRegistro: "MANUAL",
      metricas: { distancia: 12.0, velocidadeMax: 17.2 }
    },

    // --- SURF (Surfing) ---
    {
      id: actId++, atletaId, sport: "surf", esporte: "SURF", duracao: 60, duracaoSegundos: 60 * 60,
      intensidade: "media", xpGanho: 80, data: dataRelativa(-24 * 25), origemRegistro: "MANUAL",
      metricas: { ondas: 8, velocidadeMax: 21.0 }
    },
    {
      id: actId++, atletaId, sport: "surf", esporte: "SURF", duracao: 80, duracaoSegundos: 80 * 60,
      intensidade: "alta", xpGanho: 120, data: dataRelativa(-24 * 20), origemRegistro: "MANUAL",
      metricas: { ondas: 12, velocidadeMax: 26.0 }
    },
    {
      id: actId++, atletaId, sport: "surf", esporte: "SURF", duracao: 50, duracaoSegundos: 50 * 60,
      intensidade: "baixa", xpGanho: 50, data: dataRelativa(-24 * 15), origemRegistro: "MANUAL",
      metricas: { ondas: 5, velocidadeMax: 19.0 }
    },
    {
      id: actId++, atletaId, sport: "surf", esporte: "SURF", duracao: 90, duracaoSegundos: 90 * 60,
      intensidade: "alta", xpGanho: 150, data: dataRelativa(-24 * 10), origemRegistro: "MANUAL",
      metricas: { ondas: 15, velocidadeMax: 28.0 }
    },
    {
      id: actId++, atletaId, sport: "surf", esporte: "SURF", duracao: 75, duracaoSegundos: 75 * 60,
      intensidade: "media", xpGanho: 100, data: dataRelativa(-24 * 5), origemRegistro: "MANUAL",
      metricas: { ondas: 10, velocidadeMax: 24.0 }
    },
    {
      id: actId++, atletaId, sport: "surf", esporte: "SURF", duracao: 100, duracaoSegundos: 100 * 60,
      intensidade: "alta", xpGanho: 180, data: dataRelativa(-24 * 2), origemRegistro: "MANUAL",
      metricas: { ondas: 18, velocidadeMax: 32.0 }
    },

    // --- SKATE (Skateboarding) ---
    {
      id: actId++, atletaId, sport: "skate", esporte: "SKATE", duracao: 40, duracaoSegundos: 40 * 60,
      intensidade: "media", xpGanho: 60, data: dataRelativa(-24 * 24), origemRegistro: "MANUAL",
      metricas: { manobras: 10, velocidadeMax: 12.0 }
    },
    {
      id: actId++, atletaId, sport: "skate", esporte: "SKATE", duracao: 50, duracaoSegundos: 50 * 60,
      intensidade: "media", xpGanho: 75, data: dataRelativa(-24 * 18), origemRegistro: "MANUAL",
      metricas: { manobras: 15, velocidadeMax: 14.0 }
    },
    {
      id: actId++, atletaId, sport: "skate", esporte: "SKATE", duracao: 60, duracaoSegundos: 60 * 60,
      intensidade: "alta", xpGanho: 100, data: dataRelativa(-24 * 12), origemRegistro: "MANUAL",
      metricas: { manobras: 20, velocidadeMax: 16.0 }
    },
    {
      id: actId++, atletaId, sport: "skate", esporte: "SKATE", duracao: 45, duracaoSegundos: 45 * 60,
      intensidade: "media", xpGanho: 70, data: dataRelativa(-24 * 6), origemRegistro: "MANUAL",
      metricas: { manobras: 12, velocidadeMax: 13.0 }
    },
    {
      id: actId++, atletaId, sport: "skate", esporte: "SKATE", duracao: 70, duracaoSegundos: 70 * 60,
      intensidade: "alta", xpGanho: 130, data: dataRelativa(-24 * 3), origemRegistro: "MANUAL",
      metricas: { manobras: 25, velocidadeMax: 17.0 }
    }
  ];
}

class SportSnapDB {
  private data: DBData;

  constructor() {
    try {
      if (typeof window !== "undefined") {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (raw) {
          try {
            const parsed = JSON.parse(raw);
            if (parsed && typeof parsed === "object") {
              this.data = normalizarDados(parsed);
            } else {
              this.data = clonarDadosIniciais();
            }
          } catch {
            this.data = clonarDadosIniciais();
          }
        } else {
          this.data = clonarDadosIniciais();
        }
        this.checkSessionMock();
        try {
          this.save();
        } catch {}
      } else {
        this.data = clonarDadosIniciais();
      }
    } catch {
      this.data = clonarDadosIniciais();
    }
  }

  private save() {
    try {
      if (typeof window !== "undefined") {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.data));
      }
    } catch {}
  }

  public ensureMockData(atletaId: number) {
    try {
      const hasManualMock = (this.data.atividades || []).some(
        (a) => a && a.atletaId === atletaId && a.origemRegistro === "MANUAL"
      );
      
      if (!hasManualMock) {
        const maxId = (this.data.atividades || []).reduce((max, a) => Math.max(max, (a && a.id) || 0), 0);
        const novasMock = gerarMockAtividades(atletaId, maxId + 1);
        this.data.atividades = [...(this.data.atividades || []), ...novasMock];
        
        const hasCheckin = (this.data.checkins || []).some((c) => c && c.atletaId === atletaId);
        if (!hasCheckin) {
          const dataRelativa = (horas: number) => new Date(Date.now() + horas * 60 * 60 * 1000).toISOString();
          const maxCiId = (this.data.checkins || []).reduce((max, c) => Math.max(max, (c && c.id) || 0), 0);
          this.data.checkins = this.data.checkins || [];
          this.data.checkins.push(
            { id: maxCiId + 1, atletaId, sessaoId: 1, horario: dataRelativa(-119), checkoutHorario: dataRelativa(-117), cancelado: false, temAtividade: true },
            { id: maxCiId + 2, atletaId, sessaoId: 2, horario: dataRelativa(-1), cancelado: false, temAtividade: true }
          );
        }
        
        this.save();
      }
    } catch (err) {
      console.error("Erro ao injetar dados mockados:", err);
    }
  }

  private checkSessionMock() {
    if (typeof window !== "undefined") {
      try {
        const sessionRaw = localStorage.getItem("sportsnap.sessao");
        if (sessionRaw) {
          const sessaoObj = JSON.parse(sessionRaw);
          if (sessaoObj && sessaoObj.role === "atleta" && typeof sessaoObj.id === "number") {
            this.ensureMockData(sessaoObj.id);
          }
        }
      } catch {}
    }
  }

  // Consultas genéricas
  get<K extends keyof DBData>(key: K): DBData[K] {
    this.checkSessionMock();
    return this.data[key];
  }

  set<K extends keyof DBData>(key: K, value: DBData[K]): void {
    this.data[key] = value;
    this.save();
  }

  // Buscas genéricas
  find<K extends keyof DBData>(key: K, predicate: (item: DBData[K][number]) => boolean): DBData[K][number] | undefined {
    this.checkSessionMock();
    return (this.data[key] as any[]).find(predicate);
  }

  filter<K extends keyof DBData>(key: K, predicate: (item: DBData[K][number]) => boolean): DBData[K] {
    this.checkSessionMock();
    return (this.data[key] as any[]).filter(predicate) as any;
  }

  // Mutações genéricas
  add<K extends keyof DBData>(key: K, item: NovoItem<K>): DBData[K][number] {
    this.checkSessionMock();
    const list = this.data[key] as any[];
    const precisaId = COLECOES_COM_ID.includes(key);
    const nextId = list.length > 0 ? Math.max(...list.map(i => i.id || 0)) + 1 : 1;
    const newItem = precisaId ? { id: nextId, ...item } : item;
    list.push(newItem);
    this.save();
    return newItem as DBData[K][number];
  }

  update<K extends keyof DBData>(key: K, id: number, updates: Partial<DBData[K][number]>): DBData[K][number] | undefined {
    this.checkSessionMock();
    const list = this.data[key] as any[];
    const index = list.findIndex(i => i.id === id || (key === 'cartas' && i.atletaId === id) || (key === 'shadowStats' && i.atletaId === id));
    if (index === -1) return undefined;
    
    list[index] = { ...list[index], ...updates };
    this.save();
    return list[index];
  }

  comprarLicenca(atletaId: number, fotoId: number): Licenca {
    const foto = this.find("fotos", (f) => f.id === fotoId);
    if (!foto) {
      throw new Error("Foto nao encontrada");
    }
    if (foto.removida) {
      throw new Error("Foto removida nao pode ser licenciada");
    }
    if (foto.licenciada) {
      throw new Error("Foto ja licenciada");
    }

    const now = new Date().toISOString();
    const licenca = this.add("licencas", {
      atletaId,
      fotoId,
      preco: foto.preco ?? 29.9,
      adquiridaEm: now,
      cancelada: false,
    });

    foto.licenciada = true;
    foto.disponivel = false;
    this.save();

    return licenca;
  }

  delete<K extends keyof DBData>(key: K, id: number) {
    const list = this.data[key] as any[];
    this.data[key] = list.filter(i => i.id !== id) as any;
    this.save();
  }

  // Regras específicas do protótipo
  
  getShadowStats(atletaId: number): ShadowStats {
    let stats = this.find("shadowStats", s => s.atletaId === atletaId);
    if (!stats) {
      stats = { atletaId, xpAcumulado: 0, streak: 0, customMetricas: [] };
      this.data.shadowStats.push(stats);
      this.save();
    }
    // Compatibilidade com dados locais antigos.
    if (!stats.customMetricas) {
      stats.customMetricas = [];
      this.save();
    }
    return stats;
  }

  addXP(atletaId: number, xp: number) {
    const stats = this.getShadowStats(atletaId);
    stats.xpAcumulado += xp;
    this.save();
  }

  syncCarta(atletaId: number) {
    const stats = this.getShadowStats(atletaId);
    const carta = this.find("cartas", c => c.atletaId === atletaId);
    if (!carta || stats.xpAcumulado <= 0) return false;

    // Distribui o XP entre os atributos de forma simples para o protótipo.
    const bonus = stats.xpAcumulado / 4;
    carta.resistencia += bonus;
    carta.velocidade += bonus;
    carta.tecnica += bonus;
    carta.explosao += bonus;
    
    // Recalcula o Overall.
    carta.overall = (carta.resistencia + carta.velocidade + carta.tecnica + carta.explosao) / 4;
    carta.ultimaSincronizacao = new Date().toISOString();
    
    // Zera os Shadow Stats.
    stats.xpAcumulado = 0;
    stats.streak += 1;
    
    this.save();
    return true;
  }

  getRankedCartas(): CartaResumo[] {
    return [...this.data.cartas]
      .sort((a, b) => b.overall - a.overall)
      .map(c => ({
        atletaId: c.atletaId,
        overall: c.overall,
        ultimaSincronizacao: c.ultimaSincronizacao,
        sincronizada: true
      }));
  }

  getDefaultSpots(): Spot[] {
    return [
      { id: 1, nome: "Praia de Stella Maris", latitude: -12.9463, longitude: -38.3308, descricao: "Excelente pico de surf em Salvador." },
      { id: 2, nome: "Pista da Costeira",     latitude: -27.6083, longitude: -48.5492, descricao: "Pista de skate clássica de Floripa." },
      { id: 3, nome: "Aterro do Flamengo",    latitude: -22.9367, longitude: -43.1729, descricao: "Ótimo para corrida ao ar livre." },
      { id: 4, nome: "Campo do Retiro",       latitude: -23.5505, longitude: -46.6333, descricao: "Campo society com gramado sintético." },
      { id: 5, nome: "Ciclovia da Orla",      latitude: -22.9110, longitude: -43.1726, descricao: "Ciclovia ao longo da orla carioca." },
    ];
  }

  getCustomSports(): CustomSport[] {
    return [...(this.data.customSports || [])];
  }

  addCustomSport(nome: string, emoji: string): string {
    const id = `custom_${nome.toLowerCase().replace(/\s+/g, "_")}_${Date.now()}`;
    const label = `${emoji} ${nome}`;
    this.data.customSports = this.data.customSports || [];
    this.data.customSports.push({ id, label });
    this.save();
    return id;
  }
}

export const db = new SportSnapDB();
