"use client";

import { Spot, Sessao, Lote, Foto, Licenca, Atleta, Fotografo, CartaResumo } from "./api";

// --- Tipos usados apenas no estado local ---

export type CheckIn = {
  id: number;
  atletaId: number;
  sessaoId: number;
  horario: string;
  checkoutHorario?: string;
  cancelado: boolean;
  temAtividade: boolean;
};

export type SportType = "surf" | "skate" | "futebol" | "corrida" | "custom";

export type CustomMetricaValue = {
  label: string;
  value: number;
  unit: string;
};

export type RegistroAtividade = {
  id: number;
  checkInId: number;
  sport: SportType;
  duracao: number; // em minutos
  intensidade: "baixa" | "media" | "alta";
  xpGanho: number;
  metricas: {
    distancia?: number; // em km
    ondas?: number; // surfe
    manobras?: number; // skate
    gols?: number; // futebol
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
    { id: 1, nome: "Maria Atleta", email: "maria@email.com" },
    { id: 2, nome: "Joao Silva", email: "joao@email.com" }
  ],
  fotografos: [
    { id: 2, nome: "Antônio Paes", email: "antonio@foto.com" }
  ],
  cartas: [
    {
      atletaId: 1,
      nome: "Maria Atleta",
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

  return dados;
}

function normalizarDados(data: Partial<DBData>): DBData {
  const base = clonarDadosIniciais();
  const normalizado = { ...base, ...data };
  normalizado.shadowStats = normalizado.shadowStats.map((stats) => ({
    ...stats,
    customMetricas: stats.customMetricas || [],
  }));
  return normalizado;
}

class SportSnapDB {
  private data: DBData;

  constructor() {
    if (typeof window !== "undefined") {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        try {
          this.data = normalizarDados(JSON.parse(raw) as Partial<DBData>);
        } catch {
          this.data = clonarDadosIniciais();
        }
      } else {
        this.data = clonarDadosIniciais();
      }
      this.save();
    } else {
      this.data = clonarDadosIniciais();
    }
  }

  private save() {
    if (typeof window !== "undefined") {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.data));
    }
  }

  // Consultas genéricas
  get<K extends keyof DBData>(key: K): DBData[K] {
    return this.data[key];
  }

  // Buscas genéricas
  find<K extends keyof DBData>(key: K, predicate: (item: DBData[K][number]) => boolean): DBData[K][number] | undefined {
    return (this.data[key] as any[]).find(predicate);
  }

  filter<K extends keyof DBData>(key: K, predicate: (item: DBData[K][number]) => boolean): DBData[K] {
    return (this.data[key] as any[]).filter(predicate) as any;
  }

  // Mutações genéricas
  add<K extends keyof DBData>(key: K, item: NovoItem<K>): DBData[K][number] {
    const list = this.data[key] as any[];
    const precisaId = COLECOES_COM_ID.includes(key);
    const nextId = list.length > 0 ? Math.max(...list.map(i => i.id || 0)) + 1 : 1;
    const newItem = precisaId ? { id: nextId, ...item } : item;
    list.push(newItem);
    this.save();
    return newItem as DBData[K][number];
  }

  update<K extends keyof DBData>(key: K, id: number, updates: Partial<DBData[K][number]>): DBData[K][number] | undefined {
    const list = this.data[key] as any[];
    const index = list.findIndex(i => i.id === id || (key === 'cartas' && i.atletaId === id) || (key === 'shadowStats' && i.atletaId === id));
    if (index === -1) return undefined;
    
    list[index] = { ...list[index], ...updates };
    this.save();
    return list[index];
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
}

export const db = new SportSnapDB();
