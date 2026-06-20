"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { db } from "@/lib/db";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";
import { Select } from "@/components/Input";
import { StatCard } from "@/components/StatCard";
import { SectionHeading } from "@/components/SectionHeading";
import { Loading, ErrorState } from "@/components/StateView";
import { EmptyState } from "@/components/EmptyState";
import {
  obterAnalise,
  listarAtividades,
  AnaliseEvolucaoDto,
  PontoEvolucaoDto,
} from "@/lib/atividades";

// Helpers para extrair e resolver métricas dinamicamente
const ESPORTES_BASE = [
  { id: "CORRIDA", label: "🏃 Corrida" },
  { id: "SURF", label: "🏄 Surfe" },
  { id: "SKATE", label: "🛹 Skate" },
  { id: "FUTEBOL", label: "⚽ Futebol" },
  { id: "BICICLETA", label: "🚴 Bicicleta" },
  { id: "CAMINHADA", label: "🥾 Caminhada" },
  { id: "NATACAO", label: "🏊 Natação" },
];

const PERIODOS = [
  { id: "7d", label: "Últimos 7 dias" },
  { id: "30d", label: "Últimos 30 dias" },
  { id: "90d", label: "Últimos 90 dias" },
];

const extrairMetricasDisponiveis = (atividades: any[], esporte: string) => {
  const lista: { id: string; label: string; unit: string }[] = [
    { id: "duracao", label: "⏱️ Duração", unit: "min" },
    { id: "esforcoPercebido", label: "🧠 Esforço Percebido", unit: "/10" },
  ];

  const temDistancia = atividades.some((a) => a.distancia > 0);
  if (temDistancia) {
    if (esporte === "BICICLETA") {
      lista.unshift(
        { id: "distancia", label: "🚴 Distância", unit: "km" },
        { id: "velocidadeMedia", label: "⚡ Velocidade Média", unit: "km/h" },
      );
    } else {
      lista.unshift(
        { id: "distancia", label: "🏃 Distância", unit: "km" },
        { id: "pace", label: "⚡ Pace Médio", unit: "min/km" },
      );
    }
  }

  atividades.forEach((a) => {
    if (!a.metricas) return;

    if (typeof a.metricas === "object") {
      if (a.metricas.ondas !== undefined && !lista.some((m) => m.id === "ondas")) {
        lista.push({ id: "ondas", label: "🌊 Ondas Surfadas", unit: "ondas" });
      }
      if (a.metricas.manobras !== undefined && !lista.some((m) => m.id === "manobras")) {
        lista.push({ id: "manobras", label: "🛹 Manobras Acertadas", unit: "manobras" });
      }
      if (a.metricas.gols !== undefined && !lista.some((m) => m.id === "gols")) {
        lista.push({ id: "gols", label: "⚽ Gols", unit: "gols" });
      }
      if (a.metricas.assistencias !== undefined && !lista.some((m) => m.id === "assistencias")) {
        lista.push({ id: "assistencias", label: "💟 Assistências", unit: "assistências" });
      }
      if (a.metricas.velocidadeMax !== undefined && !lista.some((m) => m.id === "velocidadeMax")) {
        lista.push({ id: "velocidadeMax", label: "🚀 Velocidade Máxima", unit: "km/h" });
      }
    }

    let customArray: any[] = [];
    if (Array.isArray(a.metricas.custom)) {
      customArray = a.metricas.custom;
    } else if (typeof a.metricas === "string") {
      try {
        customArray = JSON.parse(a.metricas);
      } catch {}
    } else if (typeof a.metricas.custom === "string") {
      try {
        customArray = JSON.parse(a.metricas.custom);
      } catch {}
    }

    if (Array.isArray(customArray)) {
      customArray.forEach((m: any) => {
        if (!m.label) return;
        const lbl = m.label.toLowerCase().trim();
        if (lbl === "gols" && !lista.some((item) => item.id === "gols")) {
          lista.push({ id: "gols", label: "⚽ Gols", unit: "gols" });
          return;
        }
        if ((lbl === "assistências" || lbl === "assistencias") && !lista.some((item) => item.id === "assistencias")) {
          lista.push({ id: "assistencias", label: "💟 Assistências", unit: "assistências" });
          return;
        }
        const normalizedId =
          "custom_" +
          m.label
            .toLowerCase()
            .trim()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .replace(/\s+/g, "_");
        if (!lista.some((item) => item.id === normalizedId)) {
          lista.push({ id: normalizedId, label: `📈 ${m.label}`, unit: m.unit || "" });
        }
      });
    }
  });

  return lista;
};

const getMetricValue = (a: any, metricId: string): number | null => {
  if (metricId === "distancia") {
    return a.distancia !== undefined ? Number(a.distancia) : a.metricas?.distancia || 0;
  }
  if (metricId === "pace") {
    const dist = a.distancia !== undefined ? Number(a.distancia) : a.metricas?.distancia || 0;
    const dur = a.duracao || (a.duracaoSegundos ? a.duracaoSegundos / 60 : 0);
    return dist > 0 && dur > 0 ? dur / dist : null;
  }
  if (metricId === "velocidadeMedia") {
    const dist = a.distancia !== undefined ? Number(a.distancia) : a.metricas?.distancia || 0;
    const dur = a.duracao || (a.duracaoSegundos ? a.duracaoSegundos / 60 : 0);
    return dist > 0 && dur > 0 ? (dist / (dur / 60)) : 0;
  }
  if (metricId === "duracao") {
    return a.duracao || (a.duracaoSegundos ? a.duracaoSegundos / 60 : 0);
  }
  if (metricId === "esforcoPercebido") {
    return a.esforcoPercebido !== undefined && a.esforcoPercebido !== null ? Number(a.esforcoPercebido) : null;
  }
  if (metricId === "gols") {
    if (a.metricas?.gols !== undefined) return Number(a.metricas.gols);
  }
  if (metricId === "assistencias") {
    if (a.metricas?.assistencias !== undefined) return Number(a.metricas.assistencias);
  }

  if (a.metricas) {
    if (typeof a.metricas === "object" && a.metricas[metricId] !== undefined) {
      return Number(a.metricas[metricId]);
    }

    let customArray: any[] = [];
    if (Array.isArray(a.metricas.custom)) {
      customArray = a.metricas.custom;
    } else if (typeof a.metricas === "string") {
      try {
        customArray = JSON.parse(a.metricas);
      } catch {}
    } else if (typeof a.metricas.custom === "string") {
      try {
        customArray = JSON.parse(a.metricas.custom);
      } catch {}
    }

    if (Array.isArray(customArray)) {
      if (metricId === "gols") {
        const match = customArray.find((m: any) => m.label?.toLowerCase().trim() === "gols");
        if (match) return Number(match.value);
      }
      if (metricId === "assistencias") {
        const match = customArray.find((m: any) => {
          const lbl = m.label?.toLowerCase().trim();
          return lbl === "assistências" || lbl === "assistencias";
        });
        if (match) return Number(match.value);
      }
      const match = customArray.find((m: any) => {
        if (!m.label) return false;
        const normalizedId =
          "custom_" +
          m.label
            .toLowerCase()
            .trim()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .replace(/\s+/g, "_");
        return normalizedId === metricId;
      });
      if (match) return Number(match.value);
    }
  }

  return 0;
};

// Gráfico de linha SVG com interação de hover.
function PremiumLineChart({
  points,
  colorFrom = "#FF4F00",
  colorTo = "#8B5CF6",
  unit = "",
  label = "",
}: {
  points: PontoEvolucaoDto[];
  colorFrom?: string;
  colorTo?: string;
  unit?: string;
  label: string;
}) {
  const [hoveredPoint, setHoveredPoint] = useState<{ x: number; y: number; data: string; valor: number } | null>(null);

  if (!points || points.length === 0) {
    return (
      <div className="flex h-48 items-center justify-center rounded-3xl border border-ink-100 bg-ink-50/50 text-center text-[13px] font-bold text-ink-400">
        Ainda não há dados suficientes para exibir o gráfico de {label}.
      </div>
    );
  }

  const width = 600;
  const height = 220;
  const paddingX = 40;
  const paddingY = 30;

  const values = points.map((p) => p.valor);
  const maxVal = Math.max(...values);
  const minVal = Math.min(...values);
  const valRange = maxVal - minVal;

  const svgPoints = points.map((p, i) => {
    const x = paddingX + (i / Math.max(1, points.length - 1)) * (width - 2 * paddingX);
    const yVal = valRange === 0 ? 0.5 : (p.valor - minVal) / valRange;
    const y = height - paddingY - yVal * (height - 2 * paddingY);
    return { x, y, ...p };
  });

  let pathD = "";
  if (svgPoints.length > 0) {
    pathD = `M ${svgPoints[0].x} ${svgPoints[0].y}`;
    for (let i = 1; i < svgPoints.length; i++) {
      const p0 = svgPoints[i - 1];
      const p1 = svgPoints[i];
      const cp1x = p0.x + (p1.x - p0.x) / 2;
      const cp1y = p0.y;
      const cp2x = p0.x + (p1.x - p0.x) / 2;
      const cp2y = p1.y;
      pathD += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p1.x} ${p1.y}`;
    }
  }

  const areaD =
    svgPoints.length > 0
      ? `${pathD} L ${svgPoints[svgPoints.length - 1].x} ${height - paddingY} L ${svgPoints[0].x} ${height - paddingY} Z`
      : "";

  const xLabelsIndices =
    points.length <= 3 ? points.map((_, i) => i) : [0, Math.floor(points.length / 2), points.length - 1];

  return (
    <div className="relative w-full space-y-2">
      <div className="flex items-center justify-between text-[11px] font-black uppercase tracking-wider text-ink-400">
        <span>{label}</span>
        <span className="text-accent">{unit}</span>
      </div>
      <div className="group relative rounded-3xl border border-ink-100 bg-white p-6 shadow-soft">
        <svg viewBox={`0 0 ${width} ${height}`} className="w-full overflow-visible">
          <defs>
            <linearGradient id={`gradLine-${label}`} x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stopColor={colorFrom} />
              <stop offset="100%" stopColor={colorTo} />
            </linearGradient>
            <linearGradient id={`gradArea-${label}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={colorFrom} stopOpacity="0.18" />
              <stop offset="100%" stopColor={colorTo} stopOpacity="0.0" />
            </linearGradient>
          </defs>

          <line x1={paddingX} y1={paddingY} x2={width - paddingX} y2={paddingY} stroke="#f1f1f5" strokeWidth="1" strokeDasharray="4 4" />
          <line x1={paddingX} y1={height - paddingY} x2={width - paddingX} y2={height - paddingY} stroke="#e2e2e9" strokeWidth="1.5" />

          {hoveredPoint && (
            <line
              x1={hoveredPoint.x}
              y1={paddingY}
              x2={hoveredPoint.x}
              y2={height - paddingY}
              stroke={colorFrom}
              strokeWidth="1.5"
              strokeDasharray="4 4"
              className="opacity-30"
            />
          )}

          <path d={areaD} fill={`url(#gradArea-${label})`} className="transition-all duration-300" />

          <path
            d={pathD}
            fill="none"
            stroke={`url(#gradLine-${label})`}
            strokeWidth="8"
            strokeLinecap="round"
            strokeLinejoin="round"
            opacity="0.25"
            style={{ filter: "blur(4px)" }}
          />

          <path d={pathD} fill="none" stroke={`url(#gradLine-${label})`} strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round" />

          {svgPoints.map((p, i) => {
            const isHovered = hoveredPoint?.data === p.data;
            return (
              <g key={i} className="cursor-pointer">
                <circle
                  cx={p.x}
                  cy={p.y}
                  r={isHovered ? "7" : "4.5"}
                  fill="white"
                  stroke={isHovered ? colorTo : colorFrom}
                  strokeWidth="3.5"
                  className="transition-all duration-200"
                />
                <circle
                  cx={p.x}
                  cy={p.y}
                  r="20"
                  fill="transparent"
                  onMouseEnter={() => setHoveredPoint(p)}
                  onMouseLeave={() => setHoveredPoint(null)}
                />
              </g>
            );
          })}

          <text x={paddingX - 10} y={paddingY + 4} textAnchor="end" className="fill-ink-400 font-mono text-[9px] font-bold">
            {maxVal.toFixed(1)}
          </text>
          <text x={paddingX - 10} y={height - paddingY + 4} textAnchor="end" className="fill-ink-400 font-mono text-[9px] font-bold">
            {minVal.toFixed(1)}
          </text>

          {xLabelsIndices.map((idx) => {
            const p = svgPoints[idx];
            if (!p) return null;
            const dateFormatted = new Date(p.data).toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit" });
            return (
              <text key={idx} x={p.x} y={height - paddingY + 16} textAnchor="middle" className="fill-ink-400 text-[9px] font-bold">
                {dateFormatted}
              </text>
            );
          })}
        </svg>

        {hoveredPoint && (
          <div
            className="pointer-events-none absolute z-30 flex flex-col gap-1 rounded-2xl border p-3 text-[11px] shadow-2xl backdrop-blur-md transition-all duration-150"
            style={{
              left: `${(hoveredPoint.x / width) * 100}%`,
              top: `${(hoveredPoint.y / height) * 100}%`,
              transform: "translate(-50%, -125%)",
              backgroundColor: "rgba(15, 23, 42, 0.95)",
              borderColor: "rgba(255, 255, 255, 0.1)",
              color: "#ffffff",
            }}
          >
            <span className="font-mono text-[9px] uppercase tracking-wider whitespace-nowrap" style={{ color: "#94a3b8" }}>
              {new Date(hoveredPoint.data).toLocaleDateString("pt-BR", { day: "2-digit", month: "short", year: "2-digit" })}
            </span>
            <span className="text-xs font-black whitespace-nowrap" style={{ color: "#ffffff" }}>
              {hoveredPoint.valor.toFixed(2)} <span className="text-[10px] font-medium" style={{ color: "#94a3b8" }}>{unit}</span>
            </span>
          </div>
        )}
      </div>
    </div>
  );
}

export default function AnaliseAtividadesPage() {
  const { sessao, carregando: authLoading } = useAuth();
  const router = useRouter();

  const obterLocalLabel = (t: any) => {
    if (t.checkInId) {
      const checkin = db.find("checkins", (c) => c.id === t.checkInId);
      if (checkin) {
        const sessaoObj = db.find("sessoes", (s) => s.id === checkin.sessaoId);
        if (sessaoObj) {
          const spot = db.find("spots", (sp) => sp.id === sessaoObj.spotId);
          if (spot) return spot.nome;
        }
      }
      return "Check-in";
    }
    return "Sem Spot Associado";
  };

  const [esporte, setEsporte] = useState("CORRIDA");
  const [periodo, setPeriodo] = useState("30d");
  const [esportes, setEsportes] = useState(ESPORTES_BASE);

  const [metricaChart1, setMetricaChart1] = useState<string>("");
  const [metricaChart2, setMetricaChart2] = useState<string>("");
  const [metricasDisponiveis, setMetricasDisponiveis] = useState<{ id: string; label: string; unit: string }[]>([]);

  const [analise, setAnalise] = useState<(AnaliseEvolucaoDto & { velocidadeMediaGeral?: number; melhorVelocidade?: number }) | null>(null);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!authLoading && !sessao) router.replace("/login");
  }, [sessao, authLoading, router]);

  useEffect(() => {
    const custom = db.getCustomSports();
    if (custom.length > 0) {
      setEsportes([
        ...ESPORTES_BASE,
        ...custom.map((s) => ({ id: s.id.toUpperCase(), label: s.label })),
      ]);
    }
  }, []);

  useEffect(() => {
    let default1 = "distancia";
    let default2 = "pace";

    if (esporte === "SURF") {
      default1 = "ondas";
      default2 = "velocidadeMax";
    } else if (esporte === "SKATE") {
      default1 = "manobras";
      default2 = "velocidadeMax";
    } else if (esporte === "FUTEBOL") {
      default1 = "gols";
      default2 = "assistencias";
    } else if (!ESPORTES_BASE.some((e) => e.id === esporte)) {
      default1 = "duracao";
      default2 = "esforcoPercebido";
    }

    setMetricaChart1(default1);
    setMetricaChart2(default2);
  }, [esporte]);

  const carregarAnalise = useCallback(async () => {
    if (!sessao) return;
    setLoading(true);
    setErro(null);
    try {
      let atividadesList: any[] = db.get("atividades") || [];

      try {
        const serverData = await listarAtividades(sessao.id, esporte);
        if (serverData && serverData.length > 0) {
          const localIds = new Set(atividadesList.map((a: any) => a.id));
          const novas = serverData.filter((a: any) => !localIds.has(a.id));
          atividadesList = [...atividadesList, ...novas];
        }
      } catch {
        // Servidor offline — continua com dados locais
      }

      const agora = new Date();
      const dias = periodo === "7d" ? 7 : periodo === "90d" ? 90 : 30;
      const limiteInicio = new Date();
      limiteInicio.setDate(agora.getDate() - dias);

      const filtered = atividadesList.filter((a) => {
        const atletaLogado = db.find("atletas", (x: any) => x.email === sessao.email);
        const atletaIdLogado = atletaLogado ? atletaLogado.id : sessao.id;

        const aAtletaId =
          a.atletaId || (a.checkInId ? db.find("checkins", (c: any) => c.id === a.checkInId)?.atletaId : null);
        if (aAtletaId !== atletaIdLogado) return false;

        const aSportStr = (a.esporte || a.sport || "").toString().toUpperCase();
        const matchSport = aSportStr === esporte.toUpperCase();
        if (!matchSport) return false;

        const aDataRaw = a.data || (a.checkInId ? db.find("checkins", (c: any) => c.id === a.checkInId)?.horario : null);
        if (!aDataRaw) return false;
        const aData = new Date(aDataRaw);
        if (isNaN(aData.getTime())) return false;

        return aData >= limiteInicio;
      });

      const normalized = filtered.map((a) => {
        const duracaoMin = a.duracao !== undefined ? a.duracao : a.duracaoSegundos ? a.duracaoSegundos / 60 : 0;
        const dataIso =
          a.data || (a.checkInId ? db.find("checkins", (c) => c.id === a.checkInId)?.horario : null) || new Date().toISOString();
        
        let dist = a.distancia !== undefined ? Number(a.distancia) : a.metricas?.distancia || 0;
        
        let customArray: any[] = [];
        if (a.metricas) {
          if (Array.isArray(a.metricas.custom)) {
            customArray = a.metricas.custom;
          } else if (typeof a.metricas === "string") {
            try {
              customArray = JSON.parse(a.metricas);
            } catch {}
          } else if (typeof a.metricas.custom === "string") {
            try {
              customArray = JSON.parse(a.metricas.custom);
            } catch {}
          }
        }

        // CORREÇÃO RETROATIVA DE DISTÂNCIA
        if (dist === 0 && Array.isArray(customArray)) {
          const match = customArray.find((m: any) => 
            m.label?.toLowerCase().includes("distân") || 
            m.label?.toLowerCase().includes("distan")
          );
          if (match) dist = Number(match.value);
        }

        // CONVERSÃO DE METROS PARA KM PARA NATAÇÃO
        const esporteNormalizado = (a.esporte || a.sport || "").toUpperCase();
        if (esporteNormalizado === "NATACAO" && dist > 50) {
          dist = dist / 1000;
        }

        // EXTRAÇÃO DE VELOCIDADE E PACE
        let velocidadeMedia = 0;
        let velocidadeMax = 0;
        let pace = 0;
        if (Array.isArray(customArray)) {
          const matchSpeed = customArray.find((m: any) => 
            m.label?.toLowerCase().includes("velocidade média") || 
            m.label?.toLowerCase().includes("velocidade media")
          );
          if (matchSpeed) velocidadeMedia = Number(matchSpeed.value);
          
          const matchMax = customArray.find((m: any) => 
            m.label?.toLowerCase().includes("velocidade máxima") || 
            m.label?.toLowerCase().includes("velocidade maxima") ||
            m.label?.toLowerCase().includes("vel. máx") ||
            m.label?.toLowerCase().includes("vel. max")
          );
          if (matchMax) velocidadeMax = Number(matchMax.value);

          const matchPace = customArray.find((m: any) => 
            m.label?.toLowerCase().includes("pace")
          );
          if (matchPace) pace = Number(matchPace.value);
        }
        if (velocidadeMax === 0) {
          velocidadeMax = velocidadeMedia;
        }

        return {
          id: a.id,
          checkInId: a.checkInId,
          esporte: esporteNormalizado,
          data: dataIso,
          distancia: dist,
          duracao: duracaoMin,
          esforcoPercebido: a.esforcoPercebido !== undefined ? a.esforcoPercebido : null,
          intensidade: a.intensidade,
          xpGanho: a.xpGanho !== undefined ? a.xpGanho : a.xpCalculado || 0,
          metricas: a.metricas,
          origemRegistro: a.origemRegistro || (a.checkInId ? "CHECKIN" : "MANUAL"),
          velocidadeMedia,
          velocidadeMax,
          pace,
        };
      });

      const avail = extrairMetricasDisponiveis(normalized, esporte);
      setMetricasDisponiveis(avail);

      const sortedAsc = [...normalized].sort((a, b) => new Date(a.data).getTime() - new Date(b.data).getTime());
      const sortedDesc = [...normalized].sort((a, b) => new Date(b.data).getTime() - new Date(a.data).getTime());

      const totalAtividades = normalized.length;
      const distanciaTotal = normalized.reduce((acc, a) => acc + a.distancia, 0);
      const tempoTotalSegundos = normalized.reduce((acc, a) => acc + a.duracao * 60, 0);

      // Prioriza pace registrado nas métricas customizadas de Corrida
      const pacesRegistrados = normalized.map(a => a.pace).filter((p): p is number => p !== undefined && p > 0);

      let ritmoMedioGeral = 0;
      if (pacesRegistrados.length > 0) {
        ritmoMedioGeral = pacesRegistrados.reduce((acc, p) => acc + p, 0) / pacesRegistrados.length;
      } else if (distanciaTotal > 0) {
        ritmoMedioGeral = tempoTotalSegundos / 60 / distanciaTotal;
      }

      const getPace = (a: any) => {
        if (a.distancia > 0 && a.duracao > 0) {
          return a.duracao / a.distancia;
        }
        return null;
      };

      const pacesCalculados = normalized.map(getPace).filter((p): p is number => p !== null && p > 0);
      const melhorRitmo = pacesRegistrados.length > 0
        ? Math.min(...pacesRegistrados)
        : (pacesCalculados.length > 0 ? Math.min(...pacesCalculados) : 0);

      const distances = normalized.map((a) => a.distancia);
      const maiorDistancia = distances.length > 0 ? Math.max(...distances) : 0;

      const semanas = dias / 7;
      const frequenciaSemanal = totalAtividades / semanas;

      let currentMetrica1 = metricaChart1;
      let currentMetrica2 = metricaChart2;

      const default1 =
        esporte === "SURF"
          ? "ondas"
          : esporte === "SKATE"
            ? "manobras"
            : esporte === "FUTEBOL"
              ? "gols"
              : !ESPORTES_BASE.some((e) => e.id === esporte)
                ? "duracao"
                : "distancia";
      const default2 =
        esporte === "SURF"
          ? "velocidadeMax"
          : esporte === "SKATE"
            ? "velocidadeMax"
            : esporte === "FUTEBOL"
              ? "assistencias"
              : esporte === "BICICLETA"
                ? "velocidadeMedia"
                : !ESPORTES_BASE.some((e) => e.id === esporte)
                  ? "esforcoPercebido"
                  : "pace";

      if (!currentMetrica1 || !avail.some((m) => m.id === currentMetrica1)) {
        currentMetrica1 = default1;
      }
      if (!currentMetrica2 || !avail.some((m) => m.id === currentMetrica2)) {
        currentMetrica2 = default2;
      }

      const evolucaoDistancia = sortedAsc
        .map((a) => {
          const val = getMetricValue(a, currentMetrica1);
          return val !== null ? { data: a.data, valor: val } : null;
        })
        .filter((p): p is { data: string; valor: number } => p !== null);

      const evolucaoRitmo = sortedAsc
        .map((a) => {
          const val = getMetricValue(a, currentMetrica2);
          return val !== null ? { data: a.data, valor: val } : null;
        })
        .filter((p): p is { data: string; valor: number } => p !== null);

      const ultimosTreinos = sortedDesc.slice(0, 10).map((a) => {
        return {
          id: a.id,
          atletaId: sessao.id,
          checkInId: a.checkInId,
          esporte: a.esporte,
          data: a.data,
          distancia: a.distancia,
          duracaoSegundos: a.duracao * 60,
          intensidade: a.intensidade,
          xpCalculado: a.xpGanho,
          ritmoMedio: getPace(a),
          esforcoPercebido: a.esforcoPercebido,
          origemRegistro: a.origemRegistro,
        };
      });
      // CALCULA VELOCIDADE REAL PARA BICICLETA
      const velocidadesMedias = normalized.map(a => a.velocidadeMedia).filter((v): v is number => v !== undefined && v > 0);
      const velocidadeMediaGeral = velocidadesMedias.length > 0
        ? velocidadesMedias.reduce((acc, v) => acc + v, 0) / velocidadesMedias.length
        : 0;

      const velocidadesMax = normalized.map(a => a.velocidadeMax).filter((v): v is number => v !== undefined && v > 0);
      const melhorVelocidade = velocidadesMax.length > 0
        ? Math.max(...velocidadesMax)
        : 0;

      setAnalise({
        totalAtividades,
        distanciaTotal,
        tempoTotalSegundos,
        ritmoMedioGeral,
        melhorRitmo,
        maiorDistancia,
        frequenciaSemanal,
        evolucaoDistancia,
        evolucaoRitmo,
        ultimosTreinos,
        velocidadeMediaGeral,
        melhorVelocidade,
      });
    } catch (err) {
      console.error("Falha ao calcular dados offline:", err);
      setErro("Não foi possível carregar a análise de treinos.");
    } finally {
      setLoading(false);
    }
  }, [sessao, esporte, periodo, metricaChart1, metricaChart2]);

  useEffect(() => {
    if (sessao) carregarAnalise();
  }, [sessao, carregarAnalise]);

  function formatarDuracao(segundos: number): string {
    const hrs = Math.floor(segundos / 3600);
    const mins = Math.floor((segundos % 3600) / 60);
    if (hrs > 0) return `${hrs}h ${mins}m`;
    return `${mins}m`;
  }

  function formatarPace(pace: number | null | undefined): string {
    if (!pace || pace === 0) return "—";
    const mins = Math.floor(pace);
    const segs = Math.round((pace - mins) * 60);
    return `${mins}:${segs.toString().padStart(2, "0")} /km`;
  }

  function formatarValorRitmo(ritmo: number | null | undefined, isBicicleta: boolean): string {
    if (!ritmo || ritmo === 0) return "—";
    if (isBicicleta) {
      const vel = 60 / ritmo;
      return `${vel.toFixed(1)} km/h`;
    }
    return formatarPace(ritmo);
  }

  function formatarDataCurta(isoString: string): string {
    const d = new Date(isoString);
    return d.toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit" });
  }

  if (authLoading || !sessao) {
    return <Loading message="Verificando sessão..." className="py-20" />;
  }

  const esporteSelecionadoLabel = esportes.find((e: { id: string; label: string }) => e.id === esporte)?.label || esporte;
  const periodoLabel = PERIODOS.find((p) => p.id === periodo)?.label.toLowerCase();

  return (
    <div className="fade-up space-y-8">
      <PageHeader
        eyebrow="Evolução de Performance Real"
        title="Progresso por Esporte"
        subtitle="Métricas concretas de treino, médias históricas e gráficos de evolução temporal. Uma análise honesta da sua performance — sem o ruído da gamificação."
      />

      {/* Barra de seleção */}
      <Card className="space-y-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <Select label="Modalidade Esportiva" value={esporte} onChange={(e) => setEsporte(e.target.value)}>
            {esportes.map((e) => (
              <option key={e.id} value={e.id}>
                {e.label}
              </option>
            ))}
          </Select>
          <Select label="Janela de Análise" value={periodo} onChange={(e) => setPeriodo(e.target.value)}>
            {PERIODOS.map((p) => (
              <option key={p.id} value={p.id}>
                {p.label}
              </option>
            ))}
          </Select>
        </div>

        {metricasDisponiveis.length > 0 && (
          <div className="grid gap-4 border-t border-ink-50 pt-4 sm:grid-cols-2">
            <Select label="Gráfico da Esquerda" value={metricaChart1} onChange={(e) => setMetricaChart1(e.target.value)}>
              {metricasDisponiveis.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.label} ({m.unit})
                </option>
              ))}
            </Select>
            <Select label="Gráfico da Direita" value={metricaChart2} onChange={(e) => setMetricaChart2(e.target.value)}>
              {metricasDisponiveis.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.label} ({m.unit})
                </option>
              ))}
            </Select>
          </div>
        )}
      </Card>

      {erro && (
        <ErrorState
          title="Não foi possível carregar a análise"
          description={erro}
          tone="danger"
        />
      )}

      {loading ? (
        <Loading message="Calculando evolução e processando treinos..." />
      ) : !analise || analise.totalAtividades === 0 ? (
        <EmptyState
          icon="📊"
          title="Nenhum treino registrado"
          description={`Não encontramos treinos de ${esporteSelecionadoLabel} no período de ${periodoLabel}. Registre atividades na aba "Treinos" para acompanhar sua evolução.`}
        />
      ) : (
        <div className="space-y-8">
          {/* Resumo de métricas reais */}
          {(() => {
            const isBicicleta = esporte === "BICICLETA";
            
            let ritmoMedioValue = formatarPace(analise.ritmoMedioGeral);
            let ritmoMedioLabel = "Pace Médio";
            let ritmoMedioHint = "Ritmo geral";
            let ritmoMedioIcon = "⚡";
            
            let melhorRitmoValue = formatarPace(analise.melhorRitmo);
            let melhorRitmoLabel = "Melhor Pace";
            let melhorRitmoHint = "Recorde de ritmo";
            
            if (isBicicleta) {
              const tempoHoras = analise.tempoTotalSegundos / 3600;
              const velMediaVal = (analise.velocidadeMediaGeral && analise.velocidadeMediaGeral > 0)
                ? analise.velocidadeMediaGeral 
                : (tempoHoras > 0 ? analise.distanciaTotal / tempoHoras : 0);
              
              ritmoMedioValue = `${velMediaVal.toFixed(1)} km/h`;
              ritmoMedioLabel = "Velocidade Média";
              ritmoMedioHint = "Velocidade geral";
              ritmoMedioIcon = "🚀";
              
              const velMaxVal = (analise.melhorVelocidade && analise.melhorVelocidade > 0)
                ? analise.melhorVelocidade 
                : (analise.melhorRitmo > 0 ? 60 / analise.melhorRitmo : 0);
                
              melhorRitmoValue = velMaxVal > 0 ? `${velMaxVal.toFixed(1)} km/h` : "—";
              melhorRitmoLabel = "Melhor Velocidade";
              melhorRitmoHint = "Recorde de velocidade";
            }

            return (
              <div>
                <SectionHeading
                  title="Resumo do período"
                  description={`Métricas consolidadas de ${esporteSelecionadoLabel} · ${periodoLabel}`}
                  badge={{ tone: "accent", label: `${analise.totalAtividades || 0} treinos` }}
                />
                <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
                  <StatCard label="Treinos" value={`${analise.totalAtividades || 0}`} hint="Sessões totais" icon="🏋️" />
                  <StatCard label="Distância" value={(analise.distanciaTotal || 0).toFixed(1)} unit="km" hint="Distância total" icon={isBicicleta ? "🚴" : "🏃"} />
                  <StatCard label="Duração" value={formatarDuracao(analise.tempoTotalSegundos || 0)} hint="Tempo total" icon="⏱️" />
                  <StatCard label={ritmoMedioLabel} value={ritmoMedioValue} hint={ritmoMedioHint} icon={ritmoMedioIcon} />
                  <StatCard
                    label={melhorRitmoLabel}
                    value={melhorRitmoValue}
                    hint={melhorRitmoHint}
                    icon="🏅"
                    highlight
                  />
                  <StatCard
                    label="Maior Dist."
                    value={(analise.maiorDistancia || 0).toFixed(1)}
                    unit="km"
                    hint="Recorde de distância"
                    icon="📈"
                  />
                </div>
              </div>
            );
          })()}

          {/* Consistência / frequência */}
          <div className="surface flex flex-col items-start justify-between gap-4 rounded-3xl bg-gradient-to-r from-ink-900 to-ink-800 p-6 text-white sm:flex-row sm:items-center">
            <div>
              <h4 className="text-lg font-black">Consistência de Treinos</h4>
              <p className="text-[13px] font-medium text-ink-300">Sua frequência semanal nos {periodoLabel}.</p>
            </div>
            <div className="text-left sm:text-right">
              <span className="text-4xl font-black text-accent">{(analise.frequenciaSemanal || 0).toFixed(1)}</span>
              <span className="block text-xs font-black text-ink-400">treinos / semana</span>
            </div>
          </div>

          {/* Gráficos de evolução */}
          {(() => {
            const getChartConfigs = () => {
              const m1 = metricasDisponiveis.find((m) => m.id === metricaChart1) || { label: "Métrica 1", unit: "" };
              const m2 = metricasDisponiveis.find((m) => m.id === metricaChart2) || { label: "Métrica 2", unit: "" };

              let colorFrom1 = "#FF4F00";
              let colorTo1 = "#FF8700";
              if (metricaChart1 === "ondas") {
                colorFrom1 = "#0EA5E9";
                colorTo1 = "#2563EB";
              } else if (metricaChart1 === "manobras") {
                colorFrom1 = "#10B981";
                colorTo1 = "#059669";
              } else if (metricaChart1 === "gols") {
                colorFrom1 = "#84CC16";
                colorTo1 = "#65A30D";
              } else if (metricaChart1 === "assistencias") {
                colorFrom1 = "#38BDF8";
                colorTo1 = "#0284C7";
              } else if (metricaChart1 === "duracao") {
                colorFrom1 = "#6B7280";
                colorTo1 = "#4B5563";
              }

              let colorFrom2 = "#8B5CF6";
              let colorTo2 = "#EC4899";
              if (metricaChart2 === "velocidadeMax" || metricaChart2 === "velocidadeMedia") {
                colorFrom2 = "#F59E0B";
                colorTo2 = "#D97706";
              } else if (metricaChart2 === "distancia") {
                colorFrom2 = "#06B6D4";
                colorTo2 = "#0891B2";
              } else if (metricaChart2 === "esforcoPercebido") {
                colorFrom2 = "#F43F5E";
                colorTo2 = "#E11D48";
              } else if (metricaChart2 === "assistencias") {
                colorFrom2 = "#38BDF8";
                colorTo2 = "#0284C7";
              }

              return {
                label1: m1.label,
                unit1: m1.unit,
                colorFrom1,
                colorTo1,
                label2: m2.label,
                unit2: m2.unit,
                colorFrom2,
                colorTo2,
              };
            };

            const conf = getChartConfigs();

            return (
              <div>
                <SectionHeading title="Evolução temporal" description="Acompanhe a variação das suas métricas ao longo do tempo." />
                <div className="mt-4 grid gap-6 lg:grid-cols-2">
                  <PremiumLineChart
                    points={analise.evolucaoDistancia || []}
                    label={conf.label1}
                    unit={conf.unit1}
                    colorFrom={conf.colorFrom1}
                    colorTo={conf.colorTo1}
                  />
                  <PremiumLineChart
                    points={analise.evolucaoRitmo || []}
                    label={conf.label2}
                    unit={conf.unit2}
                    colorFrom={conf.colorFrom2}
                    colorTo={conf.colorTo2}
                  />
                </div>
              </div>
            );
          })()}

          {/* Histórico de treinos */}
          <Card
            title="🏁 Treinos analisados"
            description="Últimos treinos considerados no cálculo de evolução de desempenho."
          >
            {/* Tabela em desktop */}
            <div className="hidden overflow-x-auto sm:block">
              <table className="w-full text-left text-sm text-ink-500">
                <thead className="bg-ink-50 text-[11px] font-black uppercase tracking-wider text-ink-400">
                  <tr>
                    <th className="px-6 py-4">Data</th>
                    <th className="px-6 py-4">Distância</th>
                    <th className="px-6 py-4">Duração</th>
                    <th className="px-6 py-4">{esporte === "BICICLETA" ? "Velocidade" : "Pace"}</th>
                    <th className="px-6 py-4">Esforço</th>
                    <th className="px-6 py-4">Local</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-ink-100 font-bold">
                  {(analise.ultimosTreinos || []).map((t) => (
                    <tr key={t.id} className="transition-colors hover:bg-ink-50/60">
                      <td className="px-6 py-4 text-ink-900">{t.data ? formatarDataCurta(t.data) : ""}</td>
                      <td className="px-6 py-4 text-ink-800">{t.distancia > 0 ? `${t.distancia.toFixed(2)} km` : "—"}</td>
                      <td className="px-6 py-4 text-ink-800">{formatarDuracao(t.duracaoSegundos)}</td>
                      <td className="px-6 py-4 text-ink-900">{formatarValorRitmo(t.ritmoMedio, esporte === "BICICLETA")}</td>
                      <td className="px-6 py-4 text-ink-800">{t.esforcoPercebido ? `${t.esforcoPercebido}/10` : "—"}</td>
                      <td className="px-6 py-4">
                        <Badge tone={t.checkInId ? "accent" : "neutral"}>{obterLocalLabel(t)}</Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Cards em mobile */}
            <div className="space-y-3 sm:hidden">
              {(analise.ultimosTreinos || []).map((t) => (
                <div key={t.id} className="rounded-2xl border border-ink-100 p-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-black text-ink-900">{t.data ? formatarDataCurta(t.data) : "—"}</span>
                    <Badge tone={t.checkInId ? "accent" : "neutral"}>{obterLocalLabel(t)}</Badge>
                  </div>
                  <div className="mt-3 grid grid-cols-2 gap-2 text-[12px]">
                    <div>
                      <p className="font-black uppercase text-ink-400">Distância</p>
                      <p className="font-bold text-ink-800">{t.distancia > 0 ? `${t.distancia.toFixed(2)} km` : "—"}</p>
                    </div>
                    <div>
                      <p className="font-black uppercase text-ink-400">Duração</p>
                      <p className="font-bold text-ink-800">{formatarDuracao(t.duracaoSegundos)}</p>
                    </div>
                    <div>
                      <p className="font-black uppercase text-ink-400">{esporte === "BICICLETA" ? "Velocidade" : "Pace"}</p>
                      <p className="font-bold text-ink-900">{formatarValorRitmo(t.ritmoMedio, esporte === "BICICLETA")}</p>
                    </div>
                    <div>
                      <p className="font-black uppercase text-ink-400">Esforço</p>
                      <p className="font-bold text-ink-800">{t.esforcoPercebido ? `${t.esforcoPercebido}/10` : "—"}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}
