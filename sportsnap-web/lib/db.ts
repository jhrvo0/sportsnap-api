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

export type SportType = string;

export type CustomMetricaValue = {
  label: string;
  value: number;
  unit: string;
};

export type RegistroAtividade = {
  id: number;
  checkInId: number;
  atletaId: number;
  sport: SportType;
  duracao: number; // em minutos
  intensidade?: "baixa" | "media" | "alta" | null;
  xpGanho: number;
  distancia?: number;
  esforcoPercebido?: number | null;
  observacoes?: string | null;
  origemRegistro?: string;
  data?: string;
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

const STORAGE_KEY = "sportsnap_db_v11";
const CUSTOM_SPORTS_KEY = "sportsnap_custom_sports";

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
    { id: 1, nome: "Praia de Stella Maris",    latitude: -12.9463, longitude: -38.3308, descricao: "Excelente pico de surf em Salvador. Cuidado com as pedras na maré seca." },
    { id: 2, nome: "Pista da Costeira",         latitude: -27.6083, longitude: -48.5492, descricao: "Pista de skate clássica de Floripa. Banks e mini ramp excelentes." },
    { id: 3, nome: "Aterro do Flamengo",        latitude: -22.9367, longitude: -43.1729, descricao: "Ótimo para corrida e treinos funcionais ao ar livre." },
    { id: 4, nome: "Campo do Retiro",           latitude: -23.5505, longitude: -46.6333, descricao: "Campo society com gramado sintético. Ideal para rachões e treinos táticos." },
    { id: 5, nome: "Ciclovia da Orla",          latitude: -22.9110, longitude: -43.1726, descricao: "Ciclovia extensa ao longo da orla carioca. Ótima para pedais longos." }
  ],
  sessoes: [
    { id: 1, spotId: 1, periodoInicio: "2026-06-05T08:00:00Z", periodoFim: "2026-06-05T12:00:00Z", descricao: "Swell de Inverno - Stella" },
    { id: 2, spotId: 2, periodoInicio: "2026-06-08T06:00:00Z", periodoFim: "2026-06-08T22:00:00Z", descricao: "Sessão Livre - Costeira" },
    { id: 3, spotId: 3, periodoInicio: "2026-06-10T18:00:00Z", periodoFim: "2026-06-10T21:00:00Z", descricao: "Treino Noturno 10k" },
    { id: 4, spotId: 3, periodoInicio: "2026-06-08T00:00:00Z", periodoFim: "2026-06-08T23:59:00Z", descricao: "Corrida Matinal - Aterro" },
    { id: 6, spotId: 4, periodoInicio: "2026-06-07T17:00:00Z", periodoFim: "2026-06-07T21:00:00Z", descricao: "Rachão Semanal - Campo do Retiro" },
    { id: 7, spotId: 5, periodoInicio: "2026-06-09T06:00:00Z", periodoFim: "2026-06-09T10:00:00Z", descricao: "Pedal Matinal - Orla" }
  ],
  checkins: [
    // Maria — surf e skate
    { id: 1, atletaId: 1, sessaoId: 1, horario: "2026-06-05T08:30:00Z", checkoutHorario: "2026-06-05T11:45:00Z", cancelado: false, temAtividade: true },
    { id: 2, atletaId: 1, sessaoId: 2, horario: "2026-06-08T14:00:00Z", cancelado: false, temAtividade: true },
    { id: 3, atletaId: 1, sessaoId: 3, horario: "2026-06-10T18:30:00Z", cancelado: false, temAtividade: true },
    { id: 4, atletaId: 1, sessaoId: 4, horario: "2026-06-08T06:30:00Z", cancelado: false, temAtividade: true },
    // João — futebol e corrida
    { id: 5, atletaId: 2, sessaoId: 6, horario: "2026-06-07T17:30:00Z", checkoutHorario: "2026-06-07T20:30:00Z", cancelado: false, temAtividade: true },
    { id: 6, atletaId: 2, sessaoId: 7, horario: "2026-06-09T06:30:00Z", checkoutHorario: "2026-06-09T09:30:00Z", cancelado: false, temAtividade: true },
    { id: 7, atletaId: 2, sessaoId: 3, horario: "2026-06-10T18:15:00Z", cancelado: false, temAtividade: true },
    { id: 8, atletaId: 2, sessaoId: 4, horario: "2026-06-08T06:00:00Z", cancelado: false, temAtividade: true }
  ],
  atividades: [
    // ══════════════════════════════════════════════════════════════════════
    // MARIA (atletaId: 1) — Surfista & Skatista
    // ══════════════════════════════════════════════════════════════════════

    // ── Surfe ──
    { id: 1,  checkInId: 1, atletaId: 1, sport: "surf",      duracao: 45, intensidade: "media", xpGanho: 70,  esforcoPercebido: 6, data: new Date(Date.now() - 28 * 864e5).toISOString().slice(0,19), distancia: 2.5, origemRegistro: "CHECKIN", metricas: { ondas: 6,  velocidadeMax: 20, distancia: 2.5 } },
    { id: 2,  checkInId: 1, atletaId: 1, sport: "surf",      duracao: 60, intensidade: "alta",  xpGanho: 135, esforcoPercebido: 8, data: new Date(Date.now() - 21 * 864e5).toISOString().slice(0,19), distancia: 4.2, origemRegistro: "CHECKIN", metricas: { ondas: 10, velocidadeMax: 26, distancia: 4.2 } },
    { id: 3,  checkInId: 1, atletaId: 1, sport: "surf",      duracao: 30, intensidade: "baixa", xpGanho: 30,  esforcoPercebido: 4, data: new Date(Date.now() - 14 * 864e5).toISOString().slice(0,19), distancia: 1.8, origemRegistro: "CHECKIN", metricas: { ondas: 4,  velocidadeMax: 17, distancia: 1.8 } },
    { id: 11, checkInId: 1, atletaId: 1, sport: "surf",      duracao: 80, intensidade: "media", xpGanho: 100, esforcoPercebido: 7, data: new Date(Date.now() -  7 * 864e5).toISOString().slice(0,19), distancia: 3.5, origemRegistro: "CHECKIN", metricas: { ondas: 12, velocidadeMax: 24, distancia: 3.5 } },
    { id: 12, checkInId: 1, atletaId: 1, sport: "surf",      duracao: 90, intensidade: "alta",  xpGanho: 140, esforcoPercebido: 9, data: new Date(Date.now() -  2 * 864e5).toISOString().slice(0,19), distancia: 5.1, origemRegistro: "CHECKIN", metricas: { ondas: 15, velocidadeMax: 31, distancia: 5.1 } },

    // ── Skate ──
    { id: 4,  checkInId: 2, atletaId: 1, sport: "skate",     duracao: 40, intensidade: "media", xpGanho: 80,  esforcoPercebido: 5, data: new Date(Date.now() - 26 * 864e5).toISOString().slice(0,19), distancia: 3.0, origemRegistro: "CHECKIN", metricas: { manobras: 12, velocidadeMax: 14, distancia: 3.0 } },
    { id: 5,  checkInId: 2, atletaId: 1, sport: "skate",     duracao: 50, intensidade: "alta",  xpGanho: 150, esforcoPercebido: 9, data: new Date(Date.now() - 19 * 864e5).toISOString().slice(0,19), distancia: 5.2, origemRegistro: "CHECKIN", metricas: { manobras: 20, velocidadeMax: 18, distancia: 5.2 } },
    { id: 13, checkInId: 2, atletaId: 1, sport: "skate",     duracao: 45, intensidade: "media", xpGanho: 90,  esforcoPercebido: 6, data: new Date(Date.now() - 12 * 864e5).toISOString().slice(0,19), distancia: 4.0, origemRegistro: "CHECKIN", metricas: { manobras: 17, velocidadeMax: 16, distancia: 4.0 } },
    { id: 14, checkInId: 2, atletaId: 1, sport: "skate",     duracao: 60, intensidade: "alta",  xpGanho: 130, esforcoPercebido: 8, data: new Date(Date.now() -  5 * 864e5).toISOString().slice(0,19), distancia: 6.0, origemRegistro: "CHECKIN", metricas: { manobras: 24, velocidadeMax: 21, distancia: 6.0 } },

    // ── Corrida (Maria — casual, distâncias curtas) ──
    { id: 6,  checkInId: 3, atletaId: 1, sport: "corrida",   duracao: 28, intensidade: "baixa", xpGanho: 40,  distancia: 4.0,  data: new Date(Date.now() - 25 * 864e5).toISOString().slice(0,19), origemRegistro: "CHECKIN", esforcoPercebido: 5, metricas: { distancia: 4.0 } },
    { id: 7,  checkInId: 4, atletaId: 1, sport: "corrida",   duracao: 35, intensidade: "media", xpGanho: 70,  distancia: 5.5,  data: new Date(Date.now() - 18 * 864e5).toISOString().slice(0,19), origemRegistro: "CHECKIN", esforcoPercebido: 6, metricas: { distancia: 5.5 } },
    { id: 8,  checkInId: 3, atletaId: 1, sport: "corrida",   duracao: 30, intensidade: "media", xpGanho: 60,  distancia: 5.0,  data: new Date(Date.now() - 11 * 864e5).toISOString().slice(0,19), origemRegistro: "CHECKIN", esforcoPercebido: 6, metricas: { distancia: 5.0 } },
    { id: 9,  checkInId: 4, atletaId: 1, sport: "corrida",   duracao: 38, intensidade: "media", xpGanho: 75,  distancia: 6.0,  data: new Date(Date.now() -  4 * 864e5).toISOString().slice(0,19), origemRegistro: "CHECKIN", esforcoPercebido: 7, metricas: { distancia: 6.0 } },

    // ── Natação ──
    { id: 27, checkInId: 3, atletaId: 1, sport: "natacao",   duracao: 30, intensidade: "media", xpGanho: 50,  data: new Date(Date.now() - 27 * 864e5).toISOString().slice(0,19), distancia: 1.0, esforcoPercebido: 6, origemRegistro: "CHECKIN", metricas: { distancia: 1.0, velocidadeMax: 3.5 } },
    { id: 28, checkInId: 4, atletaId: 1, sport: "natacao",   duracao: 45, intensidade: "alta",  xpGanho: 90,  data: new Date(Date.now() - 20 * 864e5).toISOString().slice(0,19), distancia: 1.5, esforcoPercebido: 8, origemRegistro: "CHECKIN", metricas: { distancia: 1.5, velocidadeMax: 4.2 } },
    { id: 29, checkInId: 3, atletaId: 1, sport: "natacao",   duracao: 40, intensidade: "media", xpGanho: 70,  data: new Date(Date.now() - 13 * 864e5).toISOString().slice(0,19), distancia: 1.2, esforcoPercebido: 7, origemRegistro: "CHECKIN", metricas: { distancia: 1.2, velocidadeMax: 3.8 } },
    { id: 30, checkInId: 4, atletaId: 1, sport: "natacao",   duracao: 50, intensidade: "alta",  xpGanho: 100, data: new Date(Date.now() -  6 * 864e5).toISOString().slice(0,19), distancia: 1.8, esforcoPercebido: 9, origemRegistro: "CHECKIN", metricas: { distancia: 1.8, velocidadeMax: 4.8 } },
    { id: 38, checkInId: 3, atletaId: 1, sport: "natacao",   duracao: 35, intensidade: "media", xpGanho: 60,  data: new Date(Date.now() -  1 * 864e5).toISOString().slice(0,19), distancia: 1.1, esforcoPercebido: 6, origemRegistro: "CHECKIN", metricas: { distancia: 1.1, velocidadeMax: 3.6 } },

    // ══════════════════════════════════════════════════════════════════════
    // JOÃO (atletaId: 2) — Jogador de Futebol & Corredor de Longa Distância
    // ══════════════════════════════════════════════════════════════════════

    // ── Futebol (ponta-esquerda, finalizador e criador) ──
    { id: 200, checkInId: 5, atletaId: 2, sport: "futebol",   duracao: 90, intensidade: "alta",  xpGanho: 160, data: new Date(Date.now() - 28 * 864e5).toISOString().slice(0,19), distancia: 9.4, esforcoPercebido: 9, origemRegistro: "CHECKIN", metricas: { gols: 2, assistencias: 1, distancia: 9.4 } },
    { id: 201, checkInId: 5, atletaId: 2, sport: "futebol",   duracao: 90, intensidade: "media", xpGanho: 130, data: new Date(Date.now() - 21 * 864e5).toISOString().slice(0,19), distancia: 8.1, esforcoPercebido: 7, origemRegistro: "CHECKIN", metricas: { gols: 1, assistencias: 3, distancia: 8.1 } },
    { id: 202, checkInId: 5, atletaId: 2, sport: "futebol",   duracao: 60, intensidade: "media", xpGanho: 100, data: new Date(Date.now() - 14 * 864e5).toISOString().slice(0,19), distancia: 6.2, esforcoPercebido: 6, origemRegistro: "CHECKIN", metricas: { gols: 0, assistencias: 2, distancia: 6.2 } },
    { id: 203, checkInId: 5, atletaId: 2, sport: "futebol",   duracao: 90, intensidade: "alta",  xpGanho: 175, data: new Date(Date.now() -  7 * 864e5).toISOString().slice(0,19), distancia: 10.2, esforcoPercebido: 9, origemRegistro: "CHECKIN", metricas: { gols: 3, assistencias: 2, distancia: 10.2 } },
    { id: 204, checkInId: 5, atletaId: 2, sport: "futebol",   duracao: 45, intensidade: "media", xpGanho: 85,  data: new Date(Date.now() -  2 * 864e5).toISOString().slice(0,19), distancia: 5.8, esforcoPercebido: 6, origemRegistro: "CHECKIN", metricas: { gols: 1, assistencias: 1, distancia: 5.8 } },

    // ── Corrida (João — distâncias longas, ritmo forte) ──
    { id: 210, checkInId: 7, atletaId: 2, sport: "corrida",   duracao: 55, intensidade: "alta",  xpGanho: 130, distancia: 10.5, data: new Date(Date.now() - 26 * 864e5).toISOString().slice(0,19), origemRegistro: "CHECKIN", esforcoPercebido: 8, metricas: { distancia: 10.5 } },
    { id: 211, checkInId: 8, atletaId: 2, sport: "corrida",   duracao: 62, intensidade: "alta",  xpGanho: 150, distancia: 12.0, data: new Date(Date.now() - 19 * 864e5).toISOString().slice(0,19), origemRegistro: "CHECKIN", esforcoPercebido: 9, metricas: { distancia: 12.0 } },
    { id: 212, checkInId: 7, atletaId: 2, sport: "corrida",   duracao: 48, intensidade: "media", xpGanho: 110, distancia: 9.0,  data: new Date(Date.now() - 12 * 864e5).toISOString().slice(0,19), origemRegistro: "CHECKIN", esforcoPercebido: 7, metricas: { distancia: 9.0 } },
    { id: 213, checkInId: 8, atletaId: 2, sport: "corrida",   duracao: 70, intensidade: "alta",  xpGanho: 170, distancia: 14.0, data: new Date(Date.now() -  5 * 864e5).toISOString().slice(0,19), origemRegistro: "CHECKIN", esforcoPercebido: 9, metricas: { distancia: 14.0 } },
    { id: 214, checkInId: 7, atletaId: 2, sport: "corrida",   duracao: 52, intensidade: "media", xpGanho: 120, distancia: 10.0, data: new Date(Date.now() -  1 * 864e5).toISOString().slice(0,19), origemRegistro: "CHECKIN", esforcoPercebido: 8, metricas: { distancia: 10.0 } },

    // ── Bicicleta (João — pedais longos na ciclovia) ──
    { id: 220, checkInId: 6, atletaId: 2, sport: "bicicleta", duracao: 75, intensidade: "alta",  xpGanho: 150, data: new Date(Date.now() - 27 * 864e5).toISOString().slice(0,19), distancia: 38.0, esforcoPercebido: 8, origemRegistro: "CHECKIN", metricas: { distancia: 38.0, velocidadeMax: 42.0 } },
    { id: 221, checkInId: 6, atletaId: 2, sport: "bicicleta", duracao: 90, intensidade: "alta",  xpGanho: 190, data: new Date(Date.now() - 20 * 864e5).toISOString().slice(0,19), distancia: 48.0, esforcoPercebido: 9, origemRegistro: "CHECKIN", metricas: { distancia: 48.0, velocidadeMax: 46.0 } },
    { id: 222, checkInId: 6, atletaId: 2, sport: "bicicleta", duracao: 65, intensidade: "media", xpGanho: 120, data: new Date(Date.now() - 13 * 864e5).toISOString().slice(0,19), distancia: 32.0, esforcoPercebido: 7, origemRegistro: "CHECKIN", metricas: { distancia: 32.0, velocidadeMax: 38.0 } },
    { id: 223, checkInId: 6, atletaId: 2, sport: "bicicleta", duracao: 80, intensidade: "alta",  xpGanho: 165, data: new Date(Date.now() -  6 * 864e5).toISOString().slice(0,19), distancia: 42.0, esforcoPercebido: 9, origemRegistro: "CHECKIN", metricas: { distancia: 42.0, velocidadeMax: 44.0 } },
    { id: 224, checkInId: 6, atletaId: 2, sport: "bicicleta", duracao: 60, intensidade: "media", xpGanho: 110, data: new Date(Date.now() -  1 * 864e5).toISOString().slice(0,19), distancia: 28.0, esforcoPercebido: 6, origemRegistro: "CHECKIN", metricas: { distancia: 28.0, velocidadeMax: 36.0 } },

    // ── Caminhada (João — recuperação ativa) ──
    { id: 230, checkInId: 7, atletaId: 2, sport: "caminhada", duracao: 40, intensidade: "baixa", xpGanho: 25,  data: new Date(Date.now() - 24 * 864e5).toISOString().slice(0,19), distancia: 3.5, esforcoPercebido: 2, origemRegistro: "CHECKIN", metricas: { distancia: 3.5, velocidadeMax: 80  } },
    { id: 231, checkInId: 8, atletaId: 2, sport: "caminhada", duracao: 45, intensidade: "baixa", xpGanho: 28,  data: new Date(Date.now() - 17 * 864e5).toISOString().slice(0,19), distancia: 3.8, esforcoPercebido: 2, origemRegistro: "CHECKIN", metricas: { distancia: 3.8, velocidadeMax: 90  } },
    { id: 232, checkInId: 7, atletaId: 2, sport: "caminhada", duracao: 50, intensidade: "baixa", xpGanho: 30,  data: new Date(Date.now() - 10 * 864e5).toISOString().slice(0,19), distancia: 4.0, esforcoPercebido: 2, origemRegistro: "CHECKIN", metricas: { distancia: 4.0, velocidadeMax: 95  } },
    { id: 233, checkInId: 8, atletaId: 2, sport: "caminhada", duracao: 35, intensidade: "baixa", xpGanho: 20,  data: new Date(Date.now() -  3 * 864e5).toISOString().slice(0,19), distancia: 3.0, esforcoPercebido: 2, origemRegistro: "CHECKIN", metricas: { distancia: 3.0, velocidadeMax: 75  } }
  ],
  lotes: [],
  fotos: [],
  licencas: [],
  atletas: [
    { id: 1, nome: "Maria Atleta", email: "maria@email.com" },
    { id: 2, nome: "Joao Silva",   email: "joao@email.com"  }
  ],
  fotografos: [
    { id: 2, nome: "Antônio Paes", email: "antonio@foto.com" }
  ],
  cartas: [
    { atletaId: 1, nome: "Maria Atleta", overall: 82.0, resistencia: 78, velocidade: 88, tecnica: 85, explosao: 77, ultimaSincronizacao: "2026-06-01T10:00:00Z" },
    { atletaId: 2, nome: "Joao Silva",   overall: 75.5, resistencia: 82, velocidade: 72, tecnica: 74, explosao: 74, ultimaSincronizacao: "2026-06-01T10:00:00Z" }
  ],
  shadowStats: [
    { atletaId: 1, xpAcumulado: 45.5, streak: 4, customMetricas: [] },
    { atletaId: 2, xpAcumulado: 62.0, streak: 6, customMetricas: [
      { id: "custom_chutes", label: "Chutes ao Gol", unit: "chutes", sport: "futebol" }
    ]}
  ],
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
    { id: 2, spotId: 2, periodoInicio: dataRelativa(-2),   periodoFim: dataRelativa(4),    descricao: "Sessão Livre - Costeira" },
    { id: 3, spotId: 3, periodoInicio: dataRelativa(24),   periodoFim: dataRelativa(27),   descricao: "Treino Noturno 10k" },
    { id: 4, spotId: 3, periodoInicio: dataRelativa(-1),   periodoFim: dataRelativa(3),    descricao: "Corrida Matinal - Aterro" },
    { id: 6, spotId: 4, periodoInicio: dataRelativa(-48),  periodoFim: dataRelativa(-44),  descricao: "Rachão Semanal - Campo do Retiro" },
    { id: 7, spotId: 5, periodoInicio: dataRelativa(-2),   periodoFim: dataRelativa(2),    descricao: "Pedal Matinal - Orla" }
  ];

  dados.checkins = [
    { id: 1, atletaId: 1, sessaoId: 1, horario: dataRelativa(-119), checkoutHorario: dataRelativa(-117), cancelado: false, temAtividade: true },
    { id: 2, atletaId: 1, sessaoId: 2, horario: dataRelativa(-1),   cancelado: false, temAtividade: true },
    { id: 3, atletaId: 1, sessaoId: 3, horario: dataRelativa(24),   cancelado: false, temAtividade: true },
    { id: 4, atletaId: 1, sessaoId: 4, horario: dataRelativa(-1),   cancelado: false, temAtividade: true },
    { id: 5, atletaId: 2, sessaoId: 6, horario: dataRelativa(-47),  checkoutHorario: dataRelativa(-44), cancelado: false, temAtividade: true },
    { id: 6, atletaId: 2, sessaoId: 7, horario: dataRelativa(-1),   checkoutHorario: dataRelativa(2),   cancelado: false, temAtividade: true },
    { id: 7, atletaId: 2, sessaoId: 3, horario: dataRelativa(24),   cancelado: false, temAtividade: true },
    { id: 8, atletaId: 2, sessaoId: 4, horario: dataRelativa(-1),   cancelado: false, temAtividade: true }
  ];

  return dados;
}

function normalizarDados(data: Partial<DBData>): DBData {
  const base = clonarDadosIniciais();

  // Merge inteligente de atividades: mantém as mockadas do seed + adiciona novas salvas pelo usuário
  if (data.atividades && data.atividades.length > 0) {
    const baseIds = new Set(base.atividades.map((a: any) => a.id));
    const novas = data.atividades.filter((a: any) => !baseIds.has(a.id));
    data = { ...data, atividades: [...base.atividades, ...novas] };
  }

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
      // Limpar versões antigas do banco (v1–v10) ao inicializar
      for (let v = 1; v <= 10; v++) {
        localStorage.removeItem(`sportsnap_db_v${v}`);
      }

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

  reset() {
    this.data = clonarDadosIniciais();
    this.save();
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

  set<K extends keyof DBData>(key: K, val: DBData[K]) {
    this.data[key] = val;
    this.save();
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

  // Esportes customizados persistidos no localStorage separadamente
  getCustomSports(): { id: string; label: string }[] {
    if (typeof window === "undefined") return [];
    try {
      return JSON.parse(localStorage.getItem(CUSTOM_SPORTS_KEY) || "[]");
    } catch {
      return [];
    }
  }

  addCustomSport(name: string, emoji: string): string {
    const id = "sport_" + name.toLowerCase().trim().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/\s+/g, "_");
    const label = `${emoji} ${name}`;
    const existing = this.getCustomSports();
    if (!existing.some(s => s.id === id)) {
      existing.push({ id, label });
      if (typeof window !== "undefined") {
        localStorage.setItem(CUSTOM_SPORTS_KEY, JSON.stringify(existing));
      }
    }
    return id;
  }
}

export const db = new SportSnapDB();
