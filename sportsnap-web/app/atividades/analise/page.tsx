"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { db } from "@/lib/db";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";
import {
  obterAnalise,
  listarAtividades,
  AnaliseEvolucaoDto,
  PontoEvolucaoDto
} from "@/lib/atividades";

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
  { id: "90d", label: "Últimos 90 dias" }
];

// Helpers to extract and resolve metrics dynamically
const extrairMetricasDisponiveis = (atividades: any[]) => {
  const lista: { id: string; label: string; unit: string }[] = [
    { id: "duracao", label: "⏱️ Duração", unit: "min" },
    { id: "esforcoPercebido", label: "🧠 Esforço Percebido", unit: "/10" }
  ];

  const temDistancia = atividades.some(a => a.distancia > 0);
  if (temDistancia) {
    lista.unshift(
      { id: "distancia", label: "🏃 Distância", unit: "km" },
      { id: "pace", label: "⚡ Pace Médio", unit: "min/km" }
    );
  }

  atividades.forEach(a => {
    if (!a.metricas) return;
    
    if (typeof a.metricas === 'object') {
      if (a.metricas.ondas !== undefined && !lista.some(m => m.id === "ondas")) {
        lista.push({ id: "ondas", label: "🌊 Ondas Surfadas", unit: "ondas" });
      }
      if (a.metricas.manobras !== undefined && !lista.some(m => m.id === "manobras")) {
        lista.push({ id: "manobras", label: "🛹 Manobras Acertadas", unit: "manobras" });
      }
      // Gols e assistências detectados separadamente
      if (a.metricas.gols !== undefined && !lista.some(m => m.id === "gols")) {
        lista.push({ id: "gols", label: "⚽ Gols", unit: "gols" });
      }
      if (a.metricas.assistencias !== undefined && !lista.some(m => m.id === "assistencias")) {
        lista.push({ id: "assistencias", label: "💟 Assistências", unit: "assistências" });
      }
      if (a.metricas.velocidadeMax !== undefined && !lista.some(m => m.id === "velocidadeMax")) {
        lista.push({ id: "velocidadeMax", label: "🚀 Velocidade Máxima", unit: "km/h" });
      }
    }

    let customArray: any[] = [];
    if (Array.isArray(a.metricas.custom)) {
      customArray = a.metricas.custom;
    } else if (typeof a.metricas === 'string') {
      try {
        customArray = JSON.parse(a.metricas);
      } catch {}
    } else if (typeof a.metricas.custom === 'string') {
      try {
        customArray = JSON.parse(a.metricas.custom);
      } catch {}
    }

    if (Array.isArray(customArray)) {
      customArray.forEach((m: any) => {
        if (!m.label) return;
        // Map known football metrics to their canonical IDs
        const lbl = m.label.toLowerCase().trim();
        if (lbl === "gols" && !lista.some(item => item.id === "gols")) {
          lista.push({ id: "gols", label: "⚽ Gols", unit: "gols" });
          return;
        }
        if ((lbl === "assistências" || lbl === "assistencias") && !lista.some(item => item.id === "assistencias")) {
          lista.push({ id: "assistencias", label: "💟 Assistências", unit: "assistências" });
          return;
        }
        const normalizedId = "custom_" + m.label.toLowerCase().trim().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/\s+/g, "_");
        if (!lista.some(item => item.id === normalizedId)) {
          lista.push({ id: normalizedId, label: `📈 ${m.label}`, unit: m.unit || "" });
        }
      });
    }
  });

  return lista;
};

const getMetricValue = (a: any, metricId: string): number | null => {
  if (metricId === "distancia") {
    return a.distancia !== undefined ? Number(a.distancia) : (a.metricas?.distancia || 0);
  }
  if (metricId === "pace") {
    const dist = a.distancia !== undefined ? Number(a.distancia) : (a.metricas?.distancia || 0);
    const dur = a.duracao || (a.duracaoSegundos ? a.duracaoSegundos / 60 : 0);
    return dist > 0 && dur > 0 ? dur / dist : null;
  }
  if (metricId === "duracao") {
    return a.duracao || (a.duracaoSegundos ? a.duracaoSegundos / 60 : 0);
  }
  if (metricId === "esforcoPercebido") {
    return a.esforcoPercebido !== undefined && a.esforcoPercebido !== null ? Number(a.esforcoPercebido) : null;
  }
  // Handle legacy root-level football metrics
  if (metricId === "gols") {
    if (a.metricas?.gols !== undefined) return Number(a.metricas.gols);
  }
  if (metricId === "assistencias") {
    if (a.metricas?.assistencias !== undefined) return Number(a.metricas.assistencias);
  }

  if (a.metricas) {
    if (typeof a.metricas === 'object' && a.metricas[metricId] !== undefined) {
      return Number(a.metricas[metricId]);
    }

    let customArray: any[] = [];
    if (Array.isArray(a.metricas.custom)) {
      customArray = a.metricas.custom;
    } else if (typeof a.metricas === 'string') {
      try {
        customArray = JSON.parse(a.metricas);
      } catch {}
    } else if (typeof a.metricas.custom === 'string') {
      try {
        customArray = JSON.parse(a.metricas.custom);
      } catch {}
    }

    if (Array.isArray(customArray)) {
      // Check for canonical football metrics stored in custom array
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
        const normalizedId = "custom_" + m.label.toLowerCase().trim().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/\s+/g, "_");
        return normalizedId === metricId;
      });
      if (match) return Number(match.value);
    }
  }

  return 0;
};

// Custom Premium SVG Line Chart Component
function PremiumLineChart({
  points,
  colorFrom = "#FF4F00",
  colorTo = "#8B5CF6",
  unit = "",
  label = ""
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
      <div className="flex h-48 items-center justify-center rounded-2xl bg-ink-50/50 border border-ink-100 text-sm text-ink-400 font-bold">
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

  // Map to SVG coordinates
  const svgPoints = points.map((p, i) => {
    const x = paddingX + (i / Math.max(1, points.length - 1)) * (width - 2 * paddingX);
    // Inverse Y because SVG 0 is at the top
    const yVal = valRange === 0 ? 0.5 : (p.valor - minVal) / valRange;
    const y = height - paddingY - yVal * (height - 2 * paddingY);
    return { x, y, ...p };
  });

  // Generate smooth cubic bezier spline path
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

  const areaD = svgPoints.length > 0 
    ? `${pathD} L ${svgPoints[svgPoints.length - 1].x} ${height - paddingY} L ${svgPoints[0].x} ${height - paddingY} Z`
    : "";

  // Format date labels (first, middle, last)
  const xLabelsIndices = points.length <= 3 
    ? points.map((_, i) => i) 
    : [0, Math.floor(points.length / 2), points.length - 1];

  return (
    <div className="w-full space-y-2 relative">
      <div className="flex items-center justify-between text-[11px] font-black uppercase tracking-wider text-ink-400">
        <span>{label}</span>
        <span className="text-accent">{unit}</span>
      </div>
      <div className="relative rounded-[2rem] border border-ink-100 bg-white p-6 shadow-sm group">
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

          {/* Grid lines */}
          <line x1={paddingX} y1={paddingY} x2={width - paddingX} y2={paddingY} stroke="#f1f1f5" strokeWidth="1" strokeDasharray="4 4" />
          <line x1={paddingX} y1={height - paddingY} x2={width - paddingX} y2={height - paddingY} stroke="#e2e2e9" strokeWidth="1.5" />

          {/* Vertical Crosshair tracking hovered point */}
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

          {/* Area Fill */}
          <path d={areaD} fill={`url(#gradArea-${label})`} className="transition-all duration-300" />

          {/* Glowing Baseline */}
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

          {/* Stroke Line */}
          <path d={pathD} fill="none" stroke={`url(#gradLine-${label})`} strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round" />

          {/* Markers */}
          {svgPoints.map((p, i) => {
            const isHovered = hoveredPoint?.data === p.data;
            return (
              <g key={i} className="cursor-pointer">
                {/* Visual marker point */}
                <circle
                  cx={p.x}
                  cy={p.y}
                  r={isHovered ? "7" : "4.5"}
                  fill="white"
                  stroke={isHovered ? colorTo : colorFrom}
                  strokeWidth="3.5"
                  className="transition-all duration-200"
                />
                {/* Larger invisible hover helper zone */}
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

          {/* Labels */}
          {/* Y Axis Max & Min */}
          <text x={paddingX - 10} y={paddingY + 4} textAnchor="end" className="fill-ink-400 font-mono text-[9px] font-bold">
            {maxVal.toFixed(1)}
          </text>
          <text x={paddingX - 10} y={height - paddingY + 4} textAnchor="end" className="fill-ink-400 font-mono text-[9px] font-bold">
            {minVal.toFixed(1)}
          </text>

          {/* X Axis Dates */}
          {xLabelsIndices.map((idx) => {
            const p = svgPoints[idx];
            if (!p) return null;
            const dateFormatted = new Date(p.data).toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit" });
            return (
              <text key={idx} x={p.x} y={height - paddingY + 16} textAnchor="middle" className="fill-ink-400 font-bold text-[9px]">
                {dateFormatted}
              </text>
            );
          })}
        </svg>

        {/* Floating Custom HTML Tooltip */}
        {hoveredPoint && (
          <div 
            className="absolute p-3 rounded-2xl shadow-2xl border pointer-events-none transition-all duration-150 backdrop-blur-md z-30 flex flex-col gap-1 text-[11px]"
            style={{ 
              left: `${(hoveredPoint.x / width) * 100}%`, 
              top: `${(hoveredPoint.y / height) * 100}%`,
              transform: 'translate(-50%, -125%)',
              backgroundColor: 'rgba(15, 23, 42, 0.95)',
              borderColor: 'rgba(255, 255, 255, 0.1)',
              color: '#ffffff'
            }}
          >
            <span className="font-mono text-[9px] uppercase tracking-wider whitespace-nowrap" style={{ color: '#94a3b8' }}>
              {new Date(hoveredPoint.data).toLocaleDateString("pt-BR", { day: '2-digit', month: 'short', year: '2-digit' })}
            </span>
            <span className="font-black text-xs whitespace-nowrap" style={{ color: '#ffffff' }}>
              {hoveredPoint.valor.toFixed(2)} <span className="text-[10px] font-medium" style={{ color: '#94a3b8' }}>{unit}</span>
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
      const checkin = db.find("checkins", c => c.id === t.checkInId);
      if (checkin) {
        const sessaoObj = db.find("sessoes", s => s.id === checkin.sessaoId);
        if (sessaoObj) {
          const spot = db.find("spots", sp => sp.id === sessaoObj.spotId);
          if (spot) return spot.nome;
        }
      }
      return "Check-in";
    }
    return "Sem Spot Associado";
  };

  // Params state
  const [esporte, setEsporte] = useState("CORRIDA");
  const [periodo, setPeriodo] = useState("30d");
  const [esportes, setEsportes] = useState(ESPORTES_BASE);

  // Metric states
  const [metricaChart1, setMetricaChart1] = useState<string>("");
  const [metricaChart2, setMetricaChart2] = useState<string>("");
  const [metricasDisponiveis, setMetricasDisponiveis] = useState<{ id: string; label: string; unit: string }[]>([]);

  // Data state
  const [analise, setAnalise] = useState<AnaliseEvolucaoDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!authLoading && !sessao) router.replace("/login");
  }, [sessao, authLoading, router]);

  // Load custom sports from localStorage on mount
  useEffect(() => {
    const custom = db.getCustomSports();
    if (custom.length > 0) {
      setEsportes([
        ...ESPORTES_BASE,
        ...custom.map(s => ({ id: s.id.toUpperCase(), label: s.label }))
      ]);
    }
  }, []);

  // Handle auto-selecting metric defaults when esporte changes
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
    } else if (!ESPORTES_BASE.some(e => e.id === esporte)) {
      // custom sport - use generic defaults
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
      // 1. Sempre carregar do DB local primeiro (fonte primária para o protótipo)
      let atividadesList: any[] = db.get("atividades") || [];

      // 2. Tentar complementar com dados do servidor (opcional)
      try {
        const serverData = await listarAtividades(sessao.id, esporte);
        if (serverData && serverData.length > 0) {
          // Merge: adiciona atividades do servidor que não existem localmente
          const localIds = new Set(atividadesList.map((a: any) => a.id));
          const novas = serverData.filter((a: any) => !localIds.has(a.id));
          atividadesList = [...atividadesList, ...novas];
        }
      } catch {
        // Servidor offline — continua com dados locais
      }

      // 2. Filtrar e Normalizar

      const agora = new Date();
      const dias = periodo === "7d" ? 7 : periodo === "90d" ? 90 : 30;
      const limiteInicio = new Date();
      limiteInicio.setDate(agora.getDate() - dias);

      const filtered = atividadesList.filter(a => {
        // Resolver o atletaId do usuário logado pelo email (robusto contra reseed)
        const atletaLogado = db.find("atletas", (x: any) => x.email === sessao.email);
        const atletaIdLogado = atletaLogado ? atletaLogado.id : sessao.id;

        const aAtletaId = a.atletaId
          || (a.checkInId ? db.find("checkins", (c: any) => c.id === a.checkInId)?.atletaId : null);
        if (aAtletaId !== atletaIdLogado) return false;

        const aSportStr = (a.esporte || a.sport || "").toString().toUpperCase();
        const matchSport = aSportStr === esporte.toUpperCase();
        if (!matchSport) return false;

        const aDataRaw = a.data || (a.checkInId ? db.find("checkins", (c: any) => c.id === a.checkInId)?.horario : null);
        if (!aDataRaw) return false;
        const aData = new Date(aDataRaw);
        if (isNaN(aData.getTime())) return false;

        return aData >= limiteInicio && aData <= agora;
      });


      const normalized = filtered.map(a => {
        const duracaoMin = a.duracao !== undefined ? a.duracao : (a.duracaoSegundos ? a.duracaoSegundos / 60 : 0);
        const dataIso = a.data || (a.checkInId ? db.find("checkins", c => c.id === a.checkInId)?.horario : null) || new Date().toISOString();
        const dist = a.distancia !== undefined ? Number(a.distancia) : (a.metricas?.distancia || 0);
        return {
          id: a.id,
          checkInId: a.checkInId,
          esporte: (a.esporte || a.sport || "").toUpperCase(),
          data: dataIso,
          distancia: dist,
          duracao: duracaoMin,
          esforcoPercebido: a.esforcoPercebido !== undefined ? a.esforcoPercebido : null,
          intensidade: a.intensidade,
          xpGanho: a.xpGanho !== undefined ? a.xpGanho : (a.xpCalculado || 0),
          metricas: a.metricas,
          origemRegistro: a.origemRegistro || (a.checkInId ? "CHECKIN" : "MANUAL")
        };
      });

      // Extrair métricas disponíveis com base nas atividades encontradas
      const avail = extrairMetricasDisponiveis(normalized);
      setMetricasDisponiveis(avail);

      // Ordenar
      const sortedAsc = [...normalized].sort((a, b) => new Date(a.data).getTime() - new Date(b.data).getTime());
      const sortedDesc = [...normalized].sort((a, b) => new Date(b.data).getTime() - new Date(a.data).getTime());

      // Calcular métricas de resumo
      const totalAtividades = normalized.length;
      const distanciaTotal = normalized.reduce((acc, a) => acc + a.distancia, 0);
      const tempoTotalSegundos = normalized.reduce((acc, a) => acc + a.duracao * 60, 0);

      let ritmoMedioGeral = 0;
      if (distanciaTotal > 0) {
        ritmoMedioGeral = (tempoTotalSegundos / 60) / distanciaTotal;
      }

      const getPace = (a: any) => {
        if (a.distancia > 0 && a.duracao > 0) {
          return a.duracao / a.distancia;
        }
        return null;
      };

      const paces = normalized.map(getPace).filter((p): p is number => p !== null && p > 0);
      const melhorRitmo = paces.length > 0 ? Math.min(...paces) : 0;

      const distances = normalized.map(a => a.distancia);
      const maiorDistancia = distances.length > 0 ? Math.max(...distances) : 0;

      const semanas = dias / 7;
      const frequenciaSemanal = totalAtividades / semanas;

      // Determinar quais métricas usar para os gráficos
      let currentMetrica1 = metricaChart1;
      let currentMetrica2 = metricaChart2;

      const default1 = esporte === "SURF" ? "ondas" : esporte === "SKATE" ? "manobras" : esporte === "FUTEBOL" ? "gols" : !ESPORTES_BASE.some(e => e.id === esporte) ? "duracao" : "distancia";
      const default2 = esporte === "SURF" ? "velocidadeMax" : esporte === "SKATE" ? "velocidadeMax" : esporte === "FUTEBOL" ? "assistencias" : !ESPORTES_BASE.some(e => e.id === esporte) ? "esforcoPercebido" : "pace";

      if (!currentMetrica1 || !avail.some(m => m.id === currentMetrica1)) {
        currentMetrica1 = default1;
      }
      if (!currentMetrica2 || !avail.some(m => m.id === currentMetrica2)) {
        currentMetrica2 = default2;
      }

      const evolucaoDistancia = sortedAsc
        .map(a => {
          const val = getMetricValue(a, currentMetrica1);
          return val !== null ? { data: a.data, valor: val } : null;
        })
        .filter((p): p is { data: string; valor: number } => p !== null);

      const evolucaoRitmo = sortedAsc
        .map(a => {
          const val = getMetricValue(a, currentMetrica2);
          return val !== null ? { data: a.data, valor: val } : null;
        })
        .filter((p): p is { data: string; valor: number } => p !== null);

      const ultimosTreinos = sortedDesc.slice(0, 10).map(a => {
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
          origemRegistro: a.origemRegistro
        };
      });

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
        ultimosTreinos
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

  // Formatting helpers
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

  function formatarDataCurta(isoString: string): string {
    const d = new Date(isoString);
    return d.toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit" });
  }

  if (authLoading || !sessao) {
    return <div className="text-center py-20 text-ink-400">Verificando sessão...</div>;
  }

  const esporteSelecionadoLabel = esportes.find((e: { id: string; label: string }) => e.id === esporte)?.label || esporte;

  return (
    <div className="fade-up space-y-8">
      <PageHeader
        eyebrow="Evolução de Performance Real"
        title="Progresso por Esporte"
        subtitle="Observe métricas puras, médias históricas e gráficos de evolução temporal. Uma análise honesta da sua performance sem o ruído da gamificação."
      />

      {/* Selectors Bar */}
      <div className="surface flex flex-col gap-4 rounded-[2rem] p-6">
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className="mb-1 block text-[11px] font-black uppercase tracking-wider text-ink-400">Modalidade Esportiva</label>
            <select
              value={esporte}
              onChange={(e) => setEsporte(e.target.value)}
              className="w-full rounded-xl border border-ink-100 bg-white px-4 py-2.5 text-[13px] font-bold text-ink-700 focus:outline-none"
            >
              {esportes.map((e) => (
                <option key={e.id} value={e.id}>{e.label}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-[11px] font-black uppercase tracking-wider text-ink-400">Janela de Análise</label>
            <select
              value={periodo}
              onChange={(e) => setPeriodo(e.target.value)}
              className="w-full rounded-xl border border-ink-100 bg-white px-4 py-2.5 text-[13px] font-bold text-ink-700 focus:outline-none"
            >
              {PERIODOS.map((p) => (
                <option key={p.id} value={p.id}>{p.label}</option>
              ))}
            </select>
          </div>
        </div>

        {/* Dynamic Metric Selectors */}
        {metricasDisponiveis.length > 0 && (
          <div className="grid gap-4 border-t border-ink-50 pt-4 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-[11px] font-black uppercase tracking-wider text-ink-400">Gráfico da Esquerda</label>
              <select
                value={metricaChart1}
                onChange={(e) => setMetricaChart1(e.target.value)}
                className="w-full rounded-xl border border-ink-100 bg-white px-4 py-2.5 text-[13px] font-bold text-ink-700 focus:outline-none"
              >
                {metricasDisponiveis.map((m) => (
                  <option key={m.id} value={m.id}>{m.label} ({m.unit})</option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-[11px] font-black uppercase tracking-wider text-ink-400">Gráfico da Direita</label>
              <select
                value={metricaChart2}
                onChange={(e) => setMetricaChart2(e.target.value)}
                className="w-full rounded-xl border border-ink-100 bg-white px-4 py-2.5 text-[13px] font-bold text-ink-700 focus:outline-none"
              >
                {metricasDisponiveis.map((m) => (
                  <option key={m.id} value={m.id}>{m.label} ({m.unit})</option>
                ))}
              </select>
            </div>
          </div>
        )}
      </div>

      {erro && <Alert tone="danger">{erro}</Alert>}

      {loading ? (
        <div className="surface rounded-[2rem] p-16 text-center text-ink-400 font-bold">
          Calculando evolução e processando treinos...
        </div>
      ) : !analise || analise.totalAtividades === 0 ? (
        <div className="surface rounded-[2rem] p-16 text-center text-ink-400 font-bold">
          Nenhum treino de {esporteSelecionadoLabel} registrado no período de {PERIODOS.find(p => p.id === periodo)?.label.toLowerCase()}. Vá até a aba "Treinos" para cadastrar atividades!
        </div>
      ) : (
        <div className="space-y-8">
          {/* Metrics Dashboard */}
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-6">
            <div className="surface rounded-[2rem] p-6 shadow-sm">
              <p className="text-[10px] font-black uppercase tracking-widest text-ink-400">Treinos</p>
              <p className="mt-1 text-3xl font-black text-ink-900">{analise.totalAtividades}</p>
              <p className="text-[11px] font-bold text-ink-500 mt-1">Sessões totais</p>
            </div>
            <div className="surface rounded-[2rem] p-6 shadow-sm">
              <p className="text-[10px] font-black uppercase tracking-widest text-ink-400">Distância</p>
              <p className="mt-1 text-3xl font-black text-ink-900">{analise.distanciaTotal.toFixed(1)} <span className="text-sm">km</span></p>
              <p className="text-[11px] font-bold text-ink-500 mt-1">Distância total</p>
            </div>
            <div className="surface rounded-[2rem] p-6 shadow-sm">
              <p className="text-[10px] font-black uppercase tracking-widest text-ink-400">Duração</p>
              <p className="mt-1 text-3xl font-black text-ink-900">{formatarDuracao(analise.tempoTotalSegundos)}</p>
              <p className="text-[11px] font-bold text-ink-500 mt-1">Tempo total</p>
            </div>
            <div className="surface rounded-[2rem] p-6 shadow-sm">
              <p className="text-[10px] font-black uppercase tracking-widest text-ink-400">Pace Médio</p>
              <p className="mt-1 text-3xl font-black text-ink-900">{formatarPace(analise.ritmoMedioGeral)}</p>
              <p className="text-[11px] font-bold text-ink-500 mt-1">Pace geral</p>
            </div>
            <div className="surface rounded-[2rem] p-6 shadow-sm">
              <p className="text-[10px] font-black uppercase tracking-widest text-ink-400">Melhor Pace</p>
              <p className="mt-1 text-3xl font-black text-accent">{formatarPace(analise.melhorRitmo)}</p>
              <p className="text-[11px] font-bold text-ink-500 mt-1">Melhor pace</p>
            </div>
            <div className="surface rounded-[2rem] p-6 shadow-sm">
              <p className="text-[10px] font-black uppercase tracking-widest text-ink-400">Maior Dist.</p>
              <p className="mt-1 text-3xl font-black text-ink-900">{analise.maiorDistancia.toFixed(1)} <span className="text-sm">km</span></p>
              <p className="text-[11px] font-bold text-ink-500 mt-1">Recorde de distância</p>
            </div>
          </div>

          {/* Consistency and Frequency banner */}
          <div className="surface rounded-[2rem] p-6 flex items-center justify-between shadow-sm bg-gradient-to-r from-ink-900 to-ink-800 text-white">
            <div>
              <h4 className="text-lg font-black">Consistência de Treinos</h4>
              <p className="text-[13px] text-ink-300 font-medium">Sua frequência semanal nos {PERIODOS.find(p => p.id === periodo)?.label.toLowerCase()}.</p>
            </div>
            <div className="text-right">
              <span className="text-4xl font-black text-accent">{analise.frequenciaSemanal.toFixed(1)}</span>
              <span className="text-xs font-black block text-ink-400">treinos / semana</span>
            </div>
          </div>

          {/* Evolution Charts */}
          {(() => {
            // Helper to get labels/units/colors for the selected metrics
            const getChartConfigs = () => {
              const m1 = metricasDisponiveis.find(m => m.id === metricaChart1) || { label: "Métrica 1", unit: "" };
              const m2 = metricasDisponiveis.find(m => m.id === metricaChart2) || { label: "Métrica 2", unit: "" };

              let colorFrom1 = "#FF4F00";
              let colorTo1 = "#FF8700";
              if (metricaChart1 === "ondas") { colorFrom1 = "#0EA5E9"; colorTo1 = "#2563EB"; }
              else if (metricaChart1 === "manobras") { colorFrom1 = "#10B981"; colorTo1 = "#059669"; }
              else if (metricaChart1 === "gols") { colorFrom1 = "#84CC16"; colorTo1 = "#65A30D"; }
              else if (metricaChart1 === "assistencias") { colorFrom1 = "#38BDF8"; colorTo1 = "#0284C7"; }
              else if (metricaChart1 === "duracao") { colorFrom1 = "#6B7280"; colorTo1 = "#4B5563"; }

              let colorFrom2 = "#8B5CF6";
              let colorTo2 = "#EC4899";
              if (metricaChart2 === "velocidadeMax") { colorFrom2 = "#F59E0B"; colorTo2 = "#D97706"; }
              else if (metricaChart2 === "distancia") { colorFrom2 = "#06B6D4"; colorTo2 = "#0891B2"; }
              else if (metricaChart2 === "esforcoPercebido") { colorFrom2 = "#F43F5E"; colorTo2 = "#E11D48"; }
              else if (metricaChart2 === "assistencias") { colorFrom2 = "#38BDF8"; colorTo2 = "#0284C7"; }

              return {
                label1: m1.label,
                unit1: m1.unit,
                colorFrom1,
                colorTo1,
                label2: m2.label,
                unit2: m2.unit,
                colorFrom2,
                colorTo2
              };
            };

            const conf = getChartConfigs();

            return (
              <div className="grid gap-8 lg:grid-cols-2">
                <PremiumLineChart
                  points={analise.evolucaoDistancia}
                  label={conf.label1}
                  unit={conf.unit1}
                  colorFrom={conf.colorFrom1}
                  colorTo={conf.colorTo1}
                />
                <PremiumLineChart
                  points={analise.evolucaoRitmo}
                  label={conf.label2}
                  unit={conf.unit2}
                  colorFrom={conf.colorFrom2}
                  colorTo={conf.colorTo2}
                />
              </div>
            );
          })()}

          {/* Activities list considered */}
          <Card
            title="🏁 Treinos analisados"
            description="Últimos treinos considerados no cálculo de evolução de desempenho."
          >
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-ink-500">
                <thead className="bg-ink-50 text-[11px] font-black uppercase tracking-wider text-ink-400">
                  <tr>
                    <th className="px-6 py-4">Data</th>
                    <th className="px-6 py-4">Distância</th>
                    <th className="px-6 py-4">Duração</th>
                    <th className="px-6 py-4">Pace</th>
                    <th className="px-6 py-4">Esforço</th>
                    <th className="px-6 py-4">Local</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-ink-100 font-bold">
                  {analise.ultimosTreinos.map((t) => (
                    <tr key={t.id} className="hover:bg-ink-50/50">
                      <td className="px-6 py-4 text-ink-900">{t.data ? formatarDataCurta(t.data) : ""}</td>
                      <td className="px-6 py-4 text-ink-800">{t.distancia > 0 ? `${t.distancia.toFixed(2)} km` : "—"}</td>
                      <td className="px-6 py-4 text-ink-800">{formatarDuracao(t.duracaoSegundos)}</td>
                      <td className="px-6 py-4 text-ink-900">{formatarPace(t.ritmoMedio)}</td>
                      <td className="px-6 py-4 text-ink-800">{t.esforcoPercebido ? `${t.esforcoPercebido}/10` : "—"}</td>
                      <td className="px-6 py-4">
                        <Badge tone={t.checkInId ? "accent" : "neutral"}>
                          {obterLocalLabel(t)}
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}
