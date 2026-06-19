"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { db, CheckIn, RegistroAtividade, SportType, MetricaDefinition, CustomMetricaValue } from "@/lib/db";
import { Sessao, Spot } from "@/lib/api";
import {
  registrarAtividade as apiRegistrarAtividade,
  listarAtividades as apiListarAtividades,
  atualizarAtividade as apiAtualizarAtividade,
  RegistroAtividadeDto
} from "@/lib/atividades";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { Button } from "@/components/Button";
import { Input, Select } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";
import { DynamicMap } from "@/components/DynamicMap";
import { Modal } from "@/components/Modal";
import { SectionHeading } from "@/components/SectionHeading";
import { Loading } from "@/components/StateView";
import React from "react";
import { 
  listarSpots, 
  listarSessoes, 
  realizarCheckIn as apiRealizarCheckIn, 
  listarCheckIns as apiListarCheckIns, 
  cancelarCheckIn as apiCancelarCheckIn 
} from "@/lib/spots";

// --- Metric Templates Library ---
const METRIC_TEMPLATES: MetricaDefinition[] = [
  { id: "waves", label: "Ondas Surfadas", unit: "ondas", sport: "surf" },
  { id: "tricks", label: "Manobras Acertadas", unit: "manobras", sport: "skate" },
  { id: "goals", label: "Gols", unit: "gols", sport: "futebol" },
  { id: "assists", label: "Assistências", unit: "assistências", sport: "futebol" },
  { id: "distance", label: "Distância Percorrida", unit: "km", sport: "corrida" },
  { id: "pace", label: "Pace Médio", unit: "min/km", sport: "corrida" },
  // Bicicleta
  { id: "distance_cycling", label: "Distância Pedalada", unit: "km", sport: "bicicleta" },
  { id: "speed_cycling", label: "Velocidade Média", unit: "km/h", sport: "bicicleta" },
  // Caminhada
  { id: "distance_walking", label: "Distância Caminhada", unit: "km", sport: "caminhada" },
  { id: "elevation_gain", label: "Ganho de Elevação", unit: "m", sport: "caminhada" },
  // Natação
  { id: "distance_swimming", label: "Distância Nadada", unit: "m", sport: "natacao" },
  { id: "laps_swimming", label: "Voltas", unit: "voltas", sport: "natacao" },
];

export default function CheckinPage() {
  const { sessao, carregando: authLoading } = useAuth();
  const router = useRouter();
  
  // State
  const [spots, setSpots] = useState<Spot[]>([]);
  const [sessoesAtivas, setSessoesAtivas] = useState<Sessao[]>([]);
  const [checkIns, setCheckIns] = useState<CheckIn[]>([]);
  const [atividades, setAtividades] = useState<RegistroAtividade[]>([]);
  const [customMetricasDisponiveis, setCustomMetricasDisponiveis] = useState<MetricaDefinition[]>([]);
  const [customSports, setCustomSports] = useState<{ id: string; label: string }[]>([]);

  // UI States
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Form states
  const [checkInSelecionado, setCheckInSelecionado] = useState<CheckIn | null>(null);
  const [sport, setSport] = useState<SportType>("surf");
  const [duracao, setDuracao] = useState("60");
  const [intensidade, setIntensidade] = useState<"baixa" | "media" | "alta">("media");
  const [xpCalculado, setXpCalculado] = useState(10);

  // Dynamic metrics management
  const [activeMetricIds, setActiveMetricIds] = useState<string[]>([]);
  const [metricValues, setMetricValues] = useState<Record<string, string>>({});
  
  // Custom metric creation form state
  const [novaMetricaLabel, setNovaMetricaLabel] = useState("");
  const [novaMetricaUnit, setNovaMetricaUnit] = useState("");
  const [showAddCustom, setShowAddCustom] = useState(false);
  const [editAtividadeId, setEditAtividadeId] = useState<number | null>(null);

  // New sport creation form state
  const [showAddSport, setShowAddSport] = useState(false);
  const [novoEsporteNome, setNovoEsporteNome] = useState("");
  const [novoEsporteEmoji, setNovoEsporteEmoji] = useState("");

  const [verCheckInId, setVerCheckInId] = useState<number | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  // Load custom sports on mount
  useEffect(() => {
    setCustomSports(db.getCustomSports());
  }, []);

  useEffect(() => {
    if (!authLoading && !sessao) router.replace("/login");
  }, [sessao, authLoading, router]);

  // Combined library of available metrics
  const allAvailableMetrics = [...METRIC_TEMPLATES, ...customMetricasDisponiveis];

  // Auto-set default metrics when sport changes
  useEffect(() => {
    const defaults = METRIC_TEMPLATES.filter(m => m.sport === sport).map(m => m.id);
    setActiveMetricIds(defaults);
  }, [sport]);

  useEffect(() => {
    let baseXP = (parseInt(duracao) || 0) * 0.5; // Base XP for time
    const multiplicador = intensidade === "baixa" ? 1 : intensidade === "media" ? 2 : 3;

    // Sum XP from all active metrics
    Object.entries(metricValues).forEach(([id, val]) => {
      if (!activeMetricIds.includes(id)) return;
      const numVal = parseFloat(val) || 0;
      
      // Simple XP logic: 10 XP per unit of metric, with some sport-specific weights if we wanted
      baseXP += numVal * 10;
    });

    setXpCalculado(baseXP * multiplicador);
  }, [sport, duracao, intensidade, metricValues, activeMetricIds]);

  async function carregar() {
    if (!sessao) return;
    setLoading(true);
    setErro(null);

    try {
      const sps = await listarSpots();
      db.set("spots", sps);
    } catch (e) {
      console.warn("Erro ao buscar spots da API, usando DB local:", e);
    }

    try {
      const sesss = await listarSessoes();
      db.set("sessoes", sesss);
    } catch (e) {
      console.warn("Erro ao buscar sessoes da API, usando DB local:", e);
    }

    try {
      const cks = await apiListarCheckIns(sessao.id);
      const mappedCks = cks.map(c => ({
        id: c.id!,
        atletaId: c.atletaId,
        sessaoId: c.sessaoId,
        horario: c.horario ? c.horario.toString() : "",
        cancelado: c.cancelado || false,
        temAtividade: c.atividadeRegistrada || false
      }));
      db.set("checkins", mappedCks);
    } catch (e) {
      console.warn("Erro ao buscar checkins da API, usando DB local:", e);
    }

    const s = db.get("sessoes");
    const sp = db.get("spots");
    setSpots(sp);

    const now = new Date();
    const active = s.filter(sess => {
      if (sess.cancelada) return false;
      const start = new Date(sess.periodoInicio);
      const end = new Date(sess.periodoFim);
      return start <= now && end >= now;
    });
    setSessoesAtivas(active);

    setCheckIns(db.filter("checkins", c => c.atletaId === sessao.id));
    
    try {
      const apiAtivs = await apiListarAtividades(sessao.id);
      const mappedAtivs = apiAtivs.map(a => {
        let parsedMetrics = [];
        if (a.metricas) {
          try {
            parsedMetrics = JSON.parse(a.metricas);
          } catch {}
        }
        return {
          id: a.id!,
          checkInId: a.checkInId!,
          atletaId: a.atletaId || sessao.id,
          sport: a.esporte.toLowerCase() as any,
          duracao: Math.round(a.duracaoSegundos / 60),
          intensidade: (a.intensidade?.toLowerCase() || "media") as any,
          xpGanho: a.xpCalculado || 0,
          metricas: {
            distancia: a.distancia > 0 ? a.distancia : undefined,
            custom: parsedMetrics
          }
        };
      });
      setAtividades(mappedAtivs);
      db.set("atividades", mappedAtivs);
    } catch (e) {
      setAtividades(db.get("atividades"));
    }
    
    const stats = db.getShadowStats(sessao.id);
    setCustomMetricasDisponiveis(stats.customMetricas || []);
    
    setTimeout(() => setLoading(false), 500);
  }

  useEffect(() => {
    if (sessao) carregar();
  }, [sessao]);

  async function fazerCheckIn(sessaoObj: Sessao) {
    if (!sessao) return;
    setSaving(true);
    setSuccessMessage(null);
    
    await new Promise(resolve => setTimeout(resolve, 800));

    const duplicate = db.find("checkins", c => c.sessaoId === sessaoObj.id && c.atletaId === sessao.id && !c.cancelado);
    if (duplicate) {
      setErro("Você já possui um check-in ativo para esta sessão.");
      setSaving(false);
      return;
    }

    const payload = {
      atletaId: sessao.id,
      sessaoId: sessaoObj.id,
      latitude: -23.5505,
      longitude: -46.6333
    };

    try {
      const checkinSalvoBackend = await apiRealizarCheckIn(payload);
      db.add("checkins", {
        id: checkinSalvoBackend.id!,
        atletaId: sessao.id,
        sessaoId: sessaoObj.id,
        horario: checkinSalvoBackend.horario ? checkinSalvoBackend.horario.toString() : new Date().toISOString(),
        cancelado: false,
        temAtividade: false
      } as any);
      setSuccessMessage(`Check-in confirmado na sessão "${sessaoObj.descricao}"!`);
    } catch (err) {
      db.add("checkins", {
        atletaId: sessao.id,
        sessaoId: sessaoObj.id,
        horario: new Date().toISOString(),
        cancelado: false,
        temAtividade: false
      });
      setSuccessMessage("Check-in confirmado localmente (offline).");
    }

    carregar();
    setSaving(false);
  }

  async function fazerCheckout(checkInId: number) {
    if (!sessao) return;
    setSaving(true);
    setSuccessMessage(null);
    
    await new Promise(resolve => setTimeout(resolve, 800));
    
    db.update("checkins", checkInId, { checkoutHorario: new Date().toISOString() });
    
    setSuccessMessage("Checkout realizado com sucesso! Até o próximo treino.");
    carregar();
    setSaving(false);
  }

  async function cancelarCheckIn(checkInId: number) {
    if (!sessao) return;
    
    const ativs = atividades.filter(a => a.checkInId === checkInId);
    if (ativs.length > 0) {
       setErro("Não é possível cancelar um check-in que possui atividades registradas.");
       return;
    }
    
    if (confirm("Tem certeza que deseja cancelar este check-in?")) {
      try {
        await apiCancelarCheckIn(checkInId);
        db.update("checkins", checkInId, { cancelado: true });
        setSuccessMessage("Check-in cancelado com sucesso globalmente.");
      } catch (err) {
        db.update("checkins", checkInId, { cancelado: true });
        setSuccessMessage("Check-in cancelado com sucesso localmente.");
      }
      carregar();
    }
  }

  function adicionarNovaDefinicaoMetrica() {
    if (!novaMetricaLabel.trim() || !novaMetricaUnit.trim() || !sessao) return;
    
    const newDef: MetricaDefinition = {
      id: "custom_" + Math.random().toString(36).substr(2, 9),
      label: novaMetricaLabel,
      unit: novaMetricaUnit,
      sport: sport // associate with currently selected sport
    };
    
    const stats = db.getShadowStats(sessao.id);
    db.update("shadowStats", sessao.id, {
      customMetricas: [...(stats.customMetricas || []), newDef]
    });
    
    setCustomMetricasDisponiveis(prev => [...prev, newDef]);
    setActiveMetricIds(prev => [...prev, newDef.id]);
    setNovaMetricaLabel("");
    setNovaMetricaUnit("");
    setShowAddCustom(false);
  }

  function adicionarNovoEsporte() {
    if (!novoEsporteNome.trim()) return;
    const emoji = novoEsporteEmoji.trim() || "🏅";
    const newId = db.addCustomSport(novoEsporteNome.trim(), emoji);
    const updated = db.getCustomSports();
    setCustomSports(updated);
    setSport(newId);
    setNovoEsporteNome("");
    setNovoEsporteEmoji("");
    setShowAddSport(false);
  }

  function toggleMetric(id: string) {
    setActiveMetricIds(prev => 
      prev.includes(id) ? prev.filter(mid => mid !== id) : [...prev, id]
    );
  }

  async function registrarAtividade(e: React.FormEvent) {
    e.preventDefault();
    if (!checkInSelecionado || !sessao) return;
    
    // RN 10: Atividade não pode ser registrada em CheckIn cancelado
    if (checkInSelecionado.cancelado) {
      setErro("Erro: Não é possível registrar atividade em um check-in cancelado.");
      setCheckInSelecionado(null);
      setEditAtividadeId(null);
      return;
    }

    const sessaoObj = sessoesAtivas.concat(db.get("sessoes")).find(s => s.id === checkInSelecionado.sessaoId);
    const isSessaoAtiva = sessaoObj && new Date(sessaoObj.periodoFim) > new Date() && !sessaoObj.cancelada;

    // Block only if user checked out AND is NOT editing an activity of an active session
    if (checkInSelecionado.checkoutHorario && (editAtividadeId === null || !isSessaoAtiva)) {
      setErro("Erro: Não é possível registrar performance após realizar o check-out.");
      setCheckInSelecionado(null);
      setEditAtividadeId(null);
      return;
    }

    // RN 12: Duração da atividade deve ser positiva
    const duracaoInt = parseInt(duracao, 10);
    if (isNaN(duracaoInt) || duracaoInt <= 0) {
      setErro("Erro: A duração da atividade deve ser maior que zero.");
      setCheckInSelecionado(null);
      setEditAtividadeId(null);
      return;
    }

    // Validate metrics to ensure they are positive
    for (const id of activeMetricIds) {
      const val = parseFloat(metricValues[id]);
      if (isNaN(val) || val <= 0) {
        setErro("Erro: Todas as métricas registradas devem possuir valores maiores que zero.");
        setCheckInSelecionado(null);
        setEditAtividadeId(null);
        return;
      }
    }

    setSaving(true);
    
    await new Promise(resolve => setTimeout(resolve, 1000));

    const finalMetrics: CustomMetricaValue[] = activeMetricIds.map(id => {
      const def = allAvailableMetrics.find(m => m.id === id);
      return {
        label: def?.label || "Métrica",
        value: parseFloat(metricValues[id]) || 0,
        unit: def?.unit || ""
      };
    });

    const distanceVal = parseFloat(metricValues["distance"] || "0");
    const formattedDate = new Date().toISOString().slice(0, 19);

    const payload: RegistroAtividadeDto = {
      atletaId: sessao.id,
      checkInId: checkInSelecionado.id,
      esporte: sport.toUpperCase(),
      data: formattedDate,
      distancia: distanceVal,
      duracaoSegundos: duracaoInt * 60,
      intensidade: intensidade.toUpperCase(),
      xpCalculado: xpCalculado,
      origemRegistro: "CHECKIN",
      metricas: JSON.stringify(finalMetrics)
    };

    try {
      let apiRes;
      if (editAtividadeId !== null) {
        apiRes = await apiAtualizarAtividade(editAtividadeId, payload);
        db.update("atividades", editAtividadeId, {
          sport,
          duracao: duracaoInt,
          intensidade,
          xpGanho: xpCalculado,
          distancia: distanceVal,
          data: formattedDate,
          metricas: {
            distancia: distanceVal,
            custom: finalMetrics
          }
        } as any);
        setSuccessMessage("Performance atualizada com sucesso no banco de dados!");
      } else {
        apiRes = await apiRegistrarAtividade(payload);
        db.add("atividades", {
          id: apiRes.id,
          checkInId: checkInSelecionado.id,
          atletaId: sessao.id,
          sport,
          duracao: duracaoInt,
          intensidade,
          xpGanho: xpCalculado,
          distancia: distanceVal,
          data: formattedDate,
          origemRegistro: "CHECKIN",
          metricas: {
            distancia: distanceVal,
            custom: finalMetrics
          }
        } as any);
        db.update("checkins", checkInSelecionado.id, { temAtividade: true });
        db.addXP(sessao.id, xpCalculado);
        setSuccessMessage(`Performance registrada! +${xpCalculado.toFixed(1)} XP adicionados ao seu potencial.`);
      }
    } catch (e) {
      if (editAtividadeId !== null) {
        db.update("atividades", editAtividadeId, {
          sport,
          duracao: duracaoInt,
          intensidade,
          xpGanho: xpCalculado,
          distancia: distanceVal,
          metricas: {
            distancia: distanceVal,
            custom: finalMetrics
          }
        } as any);
        setSuccessMessage("Performance atualizada localmente!");
      } else {
        db.add("atividades", {
          checkInId: checkInSelecionado.id,
          atletaId: sessao.id,
          sport,
          duracao: duracaoInt,
          intensidade,
          xpGanho: xpCalculado,
          distancia: distanceVal,
          data: formattedDate,
          origemRegistro: "CHECKIN",
          metricas: {
            distancia: distanceVal,
            custom: finalMetrics
          }
        } as any);
        db.update("checkins", checkInSelecionado.id, { temAtividade: true });
        db.addXP(sessao.id, xpCalculado);
        setSuccessMessage(`Performance registrada localmente! +${xpCalculado.toFixed(1)} XP adicionados.`);
      }
    }

    setCheckInSelecionado(null);
    setEditAtividadeId(null);
    setMetricValues({});
    setDuracao("60");
    setSport("surf");
    setIntensidade("media");
    carregar();
    setSaving(false);
  }

  const checkInAtividades = verCheckInId !== null ? atividades.filter(a => a.checkInId === verCheckInId) : [];
  const verCheckInObj = checkIns.find(c => c.id === verCheckInId);
  const sessaoDoVer = sessoesAtivas.concat(db.get("sessoes")).find(s => s.id === verCheckInObj?.sessaoId);
  const totalXpCheckIn = checkInAtividades.reduce((acc, a) => acc + a.xpGanho, 0);

  const now = new Date();
  const allSessions = db.get("sessoes");
  const historicoCheckIns = checkIns.filter(c => {
    if (c.checkoutHorario || c.cancelado) return true;
    const sObj = allSessions.find(sess => sess.id === c.sessaoId);
    if (!sObj) return true;
    return new Date(sObj.periodoFim) <= now;
  });

  if (loading) {
    return <Loading message="Sincronizando seu universo esportivo..." className="min-h-[60vh]" />;
  }

  return (
    <div className="fade-up space-y-12 pb-20">
      <PageHeader
        eyebrow="Treino & Evolução"
        title="Check-in de Performance"
        subtitle="Valide sua presença nos spots e registre cada treino para acompanhar sua evolução."
      />

      {erro && (
        <Alert tone="danger" className="mb-8">
          {erro}
        </Alert>
      )}
      {successMessage && (
        <div
          className={`mb-8 flex items-center gap-5 rounded-3xl p-6 text-white shadow-soft-lg animate-in fade-in zoom-in slide-in-from-bottom-4 duration-500 ${
            successMessage.toLowerCase().includes("cancelado") ? "bg-amber-500" : "bg-emerald-500"
          }`}
        >
          <span className="text-3xl">{successMessage.toLowerCase().includes("cancelado") ? "ℹ️" : "🔥"}</span>
          <div className="min-w-0">
            <p className="text-lg font-black leading-tight">
              {successMessage.toLowerCase().includes("cancelado") ? "Sessão Atualizada" : "Excelente trabalho!"}
            </p>
            <p className="font-medium text-white/90">{successMessage}</p>
          </div>
        </div>
      )}

      <section className="space-y-6">
        <SectionHeading
          title="Sessões Ativas Agora"
          description="Janelas de treino acontecendo neste momento."
          badge={{ tone: "success", label: "● Ao Vivo" }}
        />

        {sessoesAtivas.length === 0 ? (
          <div className="flex flex-col items-center justify-center rounded-3xl border-2 border-dashed border-ink-200 bg-ink-50/30 px-6 py-16 text-center">
            <span className="mb-4 grid h-16 w-16 place-items-center rounded-2xl bg-white text-4xl shadow-soft grayscale opacity-50">
              🏖️
            </span>
            <p className="text-lg font-black text-ink-900">Nenhuma sessão acontecendo no momento.</p>
            <p className="mt-2 max-w-sm text-[13px] font-medium text-ink-500">
              Fique de olho no calendário para as próximas janelas de treino.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            {sessoesAtivas.map((s) => {
              const spotObj = spots.find((sp) => sp.id === s.spotId) || db.getDefaultSpots().find((sp) => sp.id === s.spotId);
              const checkInExistente = checkIns.find((c) => c.sessaoId === s.id && !c.cancelado);
              return (
                <div key={s.id} className="surface overflow-hidden rounded-3xl transition-shadow hover:shadow-soft-lg">
                  <div className="relative h-44 overflow-hidden bg-gradient-to-br from-indigo-950 via-slate-900 to-black">
                    {spotObj ? (
                      <>
                        <DynamicMap latitude={spotObj.latitude} longitude={spotObj.longitude} readOnly={true} height="100%" />
                        <div className="absolute inset-0 z-10 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />
                        <div className="absolute bottom-4 left-5 z-20">
                          <Badge tone="accent" className="mb-2 border-white/20 bg-white/20 text-white backdrop-blur-md">
                            📍 {spotObj.nome}
                          </Badge>
                          <h3 className="text-xl font-black leading-tight text-white drop-shadow-md">{s.descricao}</h3>
                        </div>
                      </>
                    ) : (
                      <>
                        <div className="absolute inset-0 z-10 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />
                        <div className="absolute bottom-4 left-5 z-20">
                          <Badge tone="accent" className="mb-2 border-white/20 bg-white/20 text-white backdrop-blur-md">
                            📍 Local Desconhecido #{s.spotId}
                          </Badge>
                          <h3 className="text-xl font-black leading-tight text-white drop-shadow-md">{s.descricao}</h3>
                        </div>
                      </>
                    )}
                  </div>
                  {checkInExistente ? (
                    checkInExistente.checkoutHorario ? (
                      // Concluído / check-out realizado
                      <div className="space-y-5 p-6">
                        <div className="flex items-center justify-between border-b border-ink-100 pb-4">
                          <div>
                            <p className="text-[10px] font-black uppercase tracking-wider text-ink-400">Seu Check-in</p>
                            <p className="mt-0.5 text-[11px] font-medium text-ink-500">Concluído</p>
                          </div>
                          <Badge tone="info">Check-out Realizado</Badge>
                        </div>

                        <div className="flex flex-col gap-3">
                          {checkInExistente.temAtividade ? (
                            <>
                              <div className="flex items-center justify-between px-1">
                                <span className="text-sm font-medium text-ink-600">Performance</span>
                                <Badge tone="success">
                                  {atividades.filter((a) => a.checkInId === checkInExistente.id).length} Treino(s)
                                </Badge>
                              </div>
                              <Button variant="primary" size="md" className="w-full" onClick={() => setVerCheckInId(checkInExistente.id)}>
                                Ver Resumo XP
                              </Button>
                            </>
                          ) : (
                            <div className="rounded-2xl border border-ink-100 bg-ink-50 py-4 text-center">
                              <p className="text-sm font-medium text-ink-500">Sem Registro de Performance</p>
                            </div>
                          )}
                        </div>
                      </div>
                    ) : (
                      // Ativo
                      <div className="space-y-5 p-6">
                        <div className="flex items-center justify-between border-b border-ink-100 pb-4">
                          <div>
                            <p className="text-[10px] font-black uppercase tracking-wider text-ink-400">Seu Check-in</p>
                            <p className="mt-0.5 text-[11px] font-medium text-ink-500">
                              Ativo ·{" "}
                              {new Date(checkInExistente.horario).toLocaleTimeString("pt-BR", {
                                hour: "2-digit",
                                minute: "2-digit",
                              })}
                            </p>
                          </div>
                          {checkInExistente.temAtividade ? (
                            <Badge tone="success">
                              {atividades.filter((a) => a.checkInId === checkInExistente.id).length} Treino(s)
                            </Badge>
                          ) : (
                            <Badge tone="warning" className="animate-pulse">
                              Pendente
                            </Badge>
                          )}
                        </div>

                        <div className="flex flex-col gap-3">
                          <Button
                            variant={checkInExistente.temAtividade ? "secondary" : "accent"}
                            size="md"
                            className="w-full"
                            onClick={() => setCheckInSelecionado(checkInExistente)}
                          >
                            {checkInExistente.temAtividade ? "➕ Adicionar Treino" : "Registrar Performance"}
                          </Button>

                          {checkInExistente.temAtividade && (
                            <Button variant="primary" size="md" className="w-full" onClick={() => setVerCheckInId(checkInExistente.id)}>
                              Ver Resumo XP
                            </Button>
                          )}

                          <div className="mt-1 flex gap-2 border-t border-ink-50 pt-3">
                            <Button
                              variant="secondary"
                              size="sm"
                              className="flex-1 text-rose-600 hover:bg-rose-50"
                              onClick={() => fazerCheckout(checkInExistente.id)}
                              disabled={saving}
                            >
                              Fazer checkout
                            </Button>

                            {!checkInExistente.temAtividade && (
                              <Button
                                variant="ghost"
                                size="sm"
                                className="px-3 text-ink-400 hover:bg-rose-50 hover:text-rose-600"
                                onClick={() => cancelarCheckIn(checkInExistente.id)}
                                disabled={saving}
                              >
                                Cancelar
                              </Button>
                            )}
                          </div>
                        </div>
                      </div>
                    )
                  ) : (
                    <div className="p-6">
                      <p className="mb-5 text-[13px] font-medium leading-relaxed text-ink-500 line-clamp-2">
                        Faça seu check-in neste spot para registrar suas atividades e acompanhar sua evolução.
                      </p>
                      <Button variant="accent" size="lg" className="w-full" disabled={saving} onClick={() => fazerCheckIn(s)} loading={saving}>
                        {saving ? "Validando..." : "Fazer Check-in"}
                      </Button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </section>

      <section className="space-y-6 pt-8">
        <SectionHeading
          title="Meu Histórico de Treinos"
          description={`${historicoCheckIns.length} sessões registradas em seu diário.`}
        />

        {historicoCheckIns.length === 0 ? (
          <div className="flex flex-col items-center justify-center rounded-3xl border-2 border-dashed border-ink-200 bg-ink-50/40 px-6 py-16 text-center">
            <span className="mb-4 grid h-16 w-16 place-items-center rounded-2xl bg-white text-4xl shadow-soft opacity-40">
              🏃‍♂️
            </span>
            <p className="text-lg font-black text-ink-900">Sua jornada começa com o primeiro check-in.</p>
            <p className="mt-2 max-w-sm text-[13px] font-medium text-ink-500">
              Quando você registrar treinos, eles aparecerão aqui como histórico.
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {historicoCheckIns.map((c) => {
              const checkInDate = new Date(c.horario).toLocaleString("pt-BR", { dateStyle: "medium", timeStyle: "short" });
              const s = db.get("sessoes").find((sess) => sess.id === c.sessaoId);
              const checkInAtivs = atividades.filter((a) => a.checkInId === c.id);
              const isSessionActive = s && new Date(s.periodoFim) > new Date();

              return (
                <div
                  key={c.id}
                  className="surface flex flex-col justify-between gap-5 rounded-3xl border border-ink-100/50 p-5 transition-shadow hover:shadow-soft-lg sm:p-6 md:flex-row md:items-center"
                >
                  <div className="flex items-start gap-4">
                    <div
                      className={`grid h-14 w-14 shrink-0 place-items-center rounded-2xl text-2xl ${
                        c.temAtividade
                          ? "bg-emerald-100 text-emerald-600"
                          : isSessionActive && !c.checkoutHorario
                            ? "bg-amber-100 text-amber-600"
                            : "bg-ink-100 text-ink-400"
                      }`}
                    >
                      {c.temAtividade ? "✅" : isSessionActive && !c.checkoutHorario ? "⏳" : "✖️"}
                    </div>
                    <div className="pt-0.5">
                      <h4 className="text-xl font-black tracking-tight text-ink-900">{s?.descricao || "Sessão"}</h4>
                      <p className="mb-3 mt-1 text-[11px] font-bold uppercase tracking-widest text-ink-400">
                        {checkInDate} <span className="mx-2 text-ink-200">|</span> ID #{c.id}
                      </p>

                      <div className="flex flex-wrap gap-2">
                        {c.cancelado ? (
                          <Badge tone="danger">Cancelado</Badge>
                        ) : (
                          <>
                            {c.temAtividade ? (
                              <Badge tone="success">{checkInAtivs.length} Treinos Registrados</Badge>
                            ) : c.checkoutHorario ? (
                              <Badge tone="neutral">Sem Registro de Performance</Badge>
                            ) : (
                              <Badge tone={isSessionActive ? "warning" : "neutral"}>
                                {isSessionActive ? "Performance Pendente" : "Sessão Encerrada s/ Treino"}
                              </Badge>
                            )}
                            {c.checkoutHorario && <Badge tone="info">Check-out Realizado</Badge>}
                          </>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="mt-4 flex w-full flex-col gap-2 border-t border-ink-100 pt-4 md:mt-0 md:w-auto md:flex-row md:items-center md:border-none md:pt-0">
                    {!c.cancelado && !c.checkoutHorario && isSessionActive && (
                      <Button
                        variant="secondary"
                        size="md"
                        className="w-full text-rose-600 hover:bg-rose-50 md:w-auto"
                        onClick={() => fazerCheckout(c.id)}
                        disabled={saving}
                      >
                        Fazer Checkout
                      </Button>
                    )}
                    {!c.cancelado && !c.checkoutHorario && !c.temAtividade && isSessionActive && (
                      <Button variant="accent" size="md" className="w-full md:w-auto" onClick={() => setCheckInSelecionado(c)}>
                        Registrar Performance
                      </Button>
                    )}
                    {!c.cancelado && !c.checkoutHorario && !c.temAtividade && (
                      <Button
                        variant="ghost"
                        size="md"
                        className="w-full text-ink-400 hover:bg-rose-50 hover:text-rose-600 md:w-auto"
                        onClick={() => cancelarCheckIn(c.id)}
                        disabled={saving}
                      >
                        Cancelar Check-in
                      </Button>
                    )}
                    {c.temAtividade && (
                      <Button variant="primary" size="md" className="w-full md:w-auto" onClick={() => setVerCheckInId(c.id)}>
                        Ver Resumo XP
                      </Button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      <Modal
        isOpen={!!checkInSelecionado}
        onClose={() => {
          setCheckInSelecionado(null);
          setEditAtividadeId(null);
          setMetricValues({});
          setDuracao("60");
          setSport("surf");
          setIntensidade("media");
        }}
        title={editAtividadeId ? "Editar Performance" : "Registrar Performance"}
      >
        <form onSubmit={registrarAtividade} className="space-y-6">
          <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
            <div className="flex flex-col gap-2">
              <span className="mb-0.5 block text-[13px] font-medium text-ink-700">Esporte</span>
              <div className="flex gap-2">
                <select
                  value={sport}
                  onChange={(e) => setSport(e.target.value as SportType)}
                  required
                  className="h-11 flex-1 rounded-xl border border-ink-200 bg-white px-4 text-[15px] text-ink-900 transition-colors focus:border-accent focus:outline-none focus:ring-4 focus:ring-accent/20"
                >
                  <option value="corrida">🏃 Corrida</option>
                  <option value="surf">🏄 Surfe</option>
                  <option value="skate">🛹 Skate</option>
                  <option value="futebol">⚽ Futebol</option>
                  <option value="bicicleta">🚴 Bicicleta</option>
                  <option value="caminhada">🥾 Caminhada</option>
                  <option value="natacao">🏊 Natação</option>
                  {customSports.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.label}
                    </option>
                  ))}
                </select>
                <button
                  type="button"
                  onClick={() => setShowAddSport(!showAddSport)}
                  title="Adicionar novo esporte"
                  className="shrink-0 rounded-xl border border-accent/40 bg-accent/10 px-3 text-[12px] font-black text-accent outline-none transition-colors hover:bg-accent/20 focus-visible:ring-4 focus-visible:ring-accent/30"
                >
                  {showAddSport ? "✕" : "+ Esporte"}
                </button>
              </div>
              {showAddSport && (
                <div className="flex flex-col gap-3 rounded-2xl border border-accent/20 bg-white p-4 shadow-soft-lg animate-in zoom-in duration-200">
                  <p className="text-[10px] font-black uppercase tracking-widest text-ink-400">Novo Esporte</p>
                  <div className="grid grid-cols-[80px_1fr] gap-3">
                    <div>
                      <label className="mb-1 block text-[10px] font-bold text-ink-400">Emoji</label>
                      <input
                        type="text"
                        placeholder="🏅"
                        value={novoEsporteEmoji}
                        onChange={(e) => setNovoEsporteEmoji(e.target.value)}
                        maxLength={2}
                        className="w-full rounded-xl border border-ink-200 bg-ink-50 px-3 py-2 text-center text-xl focus:outline-none focus:ring-2 focus:ring-accent/30"
                      />
                    </div>
                    <div>
                      <label className="mb-1 block text-[10px] font-bold text-ink-400">Nome do Esporte</label>
                      <input
                        type="text"
                        placeholder="Ex: Basquete"
                        value={novoEsporteNome}
                        onChange={(e) => setNovoEsporteNome(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && adicionarNovoEsporte()}
                        className="w-full rounded-xl border border-ink-200 bg-ink-50 px-3 py-2 text-[13px] font-bold text-ink-700 focus:outline-none focus:ring-2 focus:ring-accent/30"
                      />
                    </div>
                  </div>
                  <Button type="button" size="sm" variant="accent" className="w-full" onClick={adicionarNovoEsporte} disabled={!novoEsporteNome.trim()}>
                    Salvar Esporte
                  </Button>
                </div>
              )}
            </div>

            <Select label="Intensidade do Treino" value={intensidade} onChange={(e) => setIntensidade(e.target.value as any)} required>
              <option value="baixa">Baixa (Recuperação)</option>
              <option value="media">Média (Ritmo)</option>
              <option value="alta">Alta (Intervalado)</option>
            </Select>
          </div>

          <div className="space-y-6 rounded-3xl border border-ink-100 bg-ink-50/50 p-6">
            <Input label="Duração (minutos)" type="number" value={duracao} onChange={(e) => setDuracao(e.target.value)} required />

            <div className="border-t border-ink-200 pt-6">
              <div className="mb-5 flex items-center justify-between">
                <h4 className="text-lg font-black text-ink-900">O que você quer registrar?</h4>
                <button
                  type="button"
                  onClick={() => setShowAddCustom(!showAddCustom)}
                  className="text-[12px] font-black uppercase tracking-widest text-accent hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/30"
                >
                  {showAddCustom ? "Cancelar" : "+ Criar Nova"}
                </button>
              </div>

              {/* Seletor de métricas */}
              <div className="mb-6 flex flex-wrap gap-2 rounded-2xl border border-ink-200 bg-white/60 p-4">
                {allAvailableMetrics.map((m) => {
                  const isActive = activeMetricIds.includes(m.id);
                  return (
                    <button
                      key={m.id}
                      type="button"
                      onClick={() => toggleMetric(m.id)}
                      className={`rounded-xl border px-4 py-2 text-[11px] font-bold transition-all ${
                        isActive
                          ? "scale-105 border-accent bg-accent text-white shadow-md"
                          : "border-ink-200 bg-white text-ink-500 hover:border-ink-400"
                      }`}
                    >
                      {m.label} {isActive && "✓"}
                    </button>
                  );
                })}
              </div>

              {showAddCustom && (
                <div className="mb-6 flex flex-col gap-3 rounded-3xl border border-accent/20 bg-white p-6 shadow-soft-lg animate-in zoom-in duration-300">
                  <p className="mb-2 text-[10px] font-black uppercase tracking-widest text-ink-400">Nova métrica personalizada</p>
                  <div className="grid grid-cols-2 gap-4">
                    <Input label="Nome (ex: Defesas)" placeholder="Ex: Defesas" value={novaMetricaLabel} onChange={(e) => setNovaMetricaLabel(e.target.value)} />
                    <Input label="Unidade (ex: und)" placeholder="Ex: unidades" value={novaMetricaUnit} onChange={(e) => setNovaMetricaUnit(e.target.value)} />
                  </div>
                  <Button type="button" size="md" variant="accent" className="mt-2 w-full" onClick={adicionarNovaDefinicaoMetrica}>
                    Salvar no meu Perfil
                  </Button>
                </div>
              )}

              {/* Inputs dinâmicos das métricas ativas */}
              <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
                {activeMetricIds.map((mid) => {
                  const m = allAvailableMetrics.find((metric) => metric.id === mid);
                  if (!m) return null;
                  return (
                    <div key={mid} className="group relative">
                      <Input
                        label={`${m.label} (${m.unit})`}
                        type="number"
                        placeholder="0"
                        value={metricValues[mid] || ""}
                        onChange={(e) => setMetricValues((prev) => ({ ...prev, [mid]: e.target.value }))}
                      />
                      <button
                        type="button"
                        onClick={() => toggleMetric(mid)}
                        className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] text-white opacity-0 shadow-sm transition-opacity group-hover:opacity-100 focus-visible:opacity-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-rose-300"
                      >
                        ✕
                      </button>
                    </div>
                  );
                })}
              </div>

              {activeMetricIds.length === 0 && !showAddCustom && (
                <div className="rounded-3xl border border-dashed border-ink-200 bg-white/40 py-10 text-center">
                  <p className="text-sm font-medium italic text-ink-400">Selecione métricas acima para começar o registro.</p>
                </div>
              )}
            </div>
          </div>

          <div className="relative overflow-hidden rounded-3xl bg-ink-900 p-6 text-white shadow-soft-lg">
            <div className="pointer-events-none absolute bottom-0 right-0 top-0 w-32 bg-gradient-to-l from-accent/20 to-transparent" />
            <div className="relative flex items-center justify-between">
              <div>
                <p className="mb-1 text-[12px] font-black uppercase tracking-[0.2em] text-accent">Ganhos Estimados</p>
                <p className="text-5xl font-black">
                  {xpCalculado.toFixed(1)} <span className="text-lg text-ink-400">XP</span>
                </p>
              </div>
              <div className="text-right">
                <p className="mb-2 text-[10px] font-bold uppercase tracking-widest text-ink-400">Sincronização Sugerida</p>
                <Badge tone="accent" className="border-white/10 bg-white/10 text-white">
                  Revelar com Fotos
                </Badge>
              </div>
            </div>
          </div>

          <div className="flex flex-col gap-3 pt-2 sm:flex-row-reverse">
            <Button type="submit" variant="accent" className="flex-1" size="lg" disabled={saving || activeMetricIds.length === 0} loading={saving}>
              {saving ? "Salvando Treino..." : "Salvar Performance"}
            </Button>
            <Button
              type="button"
              variant="secondary"
              size="lg"
              onClick={() => {
                setCheckInSelecionado(null);
                setEditAtividadeId(null);
                setMetricValues({});
                setDuracao("60");
                setSport("surf");
                setIntensidade("media");
              }}
            >
              Cancelar
            </Button>
          </div>
        </form>
      </Modal>

      <Modal
        isOpen={verCheckInId !== null && !!verCheckInObj}
        onClose={() => setVerCheckInId(null)}
        title="Resumo da Atividade"
        variant="dark"
      >
        <div className="space-y-10">
           <div className="flex items-center justify-between pb-8 border-b border-white/10">
              <div>
                <h3 className="text-3xl font-black text-white leading-tight">{sessaoDoVer?.descricao}</h3>
                <p className="text-ink-400 font-bold mt-1 uppercase tracking-widest text-[12px]">
                   {verCheckInObj && new Date(verCheckInObj.horario).toLocaleDateString("pt-BR", { dateStyle: 'full' })}
                </p>
              </div>
              <div className="text-right">
                <p className="text-6xl font-black text-accent">{totalXpCheckIn.toFixed(1)}</p>
                <p className="text-[12px] font-black uppercase tracking-[0.2em] text-ink-500">XP Acumulado</p>
              </div>
           </div>

          {checkInAtividades.length === 0 ? (
            <p className="text-lg text-ink-400 font-medium italic py-10 text-center">Nenhuma atividade registrada ainda.</p>
          ) : (
            <div className="grid grid-cols-1 gap-6">
              {checkInAtividades.map(a => (
                <div key={a.id} className="rounded-[2.5rem] bg-white/5 p-8 border border-white/10 hover:bg-white/[0.08] transition-colors">
                  <div className="flex justify-between items-center mb-8">
                    <Badge tone="accent" className="px-4 py-1.5 bg-accent/20 border-accent/20">{a.sport}</Badge>
                    <span className="text-2xl font-black text-accent">+{a.xpGanho.toFixed(1)} <span className="text-sm font-bold text-ink-500">XP</span></span>
                  </div>
                  
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
                    <div className="space-y-1">
                      <p className="text-[10px] font-black uppercase tracking-widest text-ink-500">Duração</p>
                      <p className="text-lg font-bold text-white">{a.duracao}m</p>
                    </div>
                    
                    {/* Render metrics from the activity record */}
                    {a.metricas?.distancia !== undefined && (
                      <div className="space-y-1">
                        <p className="text-[10px] font-black uppercase tracking-widest text-ink-500">Distância</p>
                        <p className="text-lg font-bold text-white">{a.metricas.distancia}km</p>
                      </div>
                    )}
                    {a.metricas?.ondas !== undefined && (
                      <div className="space-y-1">
                        <p className="text-[10px] font-black uppercase tracking-widest text-ink-500">Ondas</p>
                        <p className="text-lg font-bold text-white">{a.metricas.ondas}</p>
                      </div>
                    )}
                    {a.metricas?.manobras !== undefined && (
                      <div className="space-y-1">
                        <p className="text-[10px] font-black uppercase tracking-widest text-ink-500">Manobras</p>
                        <p className="text-lg font-bold text-white">{a.metricas.manobras}</p>
                      </div>
                    )}
                    {a.metricas?.gols !== undefined && (
                      <div className="space-y-1">
                        <p className="text-[10px] font-black uppercase tracking-widest text-ink-500">Gols</p>
                        <p className="text-lg font-bold text-white">{a.metricas.gols}</p>
                      </div>
                    )}
                    {a.metricas?.assistencias !== undefined && (
                      <div className="space-y-1">
                        <p className="text-[10px] font-black uppercase tracking-widest text-ink-500">Assistências</p>
                        <p className="text-lg font-bold text-white">{a.metricas.assistencias}</p>
                      </div>
                    )}
                    {a.metricas?.velocidadeMax !== undefined && (
                      <div className="space-y-1">
                        <p className="text-[10px] font-black uppercase tracking-widest text-ink-500">Vel. Máx</p>
                        <p className="text-lg font-bold text-white">{a.metricas.velocidadeMax}km/h</p>
                      </div>
                    )}

                    {a.metricas?.custom?.map((m, i) => (
                      <div key={i} className="space-y-1">
                        <p className="text-[10px] font-black uppercase tracking-widest text-ink-500">{m.label}</p>
                        <p className="text-lg font-bold text-white">{m.value}<span className="text-[10px] ml-1 text-ink-500">{m.unit}</span></p>
                      </div>
                    ))}
                  </div>

                  {sessaoDoVer && new Date(sessaoDoVer.periodoFim) > new Date() && !sessaoDoVer.cancelada && !verCheckInObj?.cancelado && (
                    <div className="mt-6 pt-4 border-t border-white/10 flex justify-end">
                      <button
                        type="button"
                        onClick={() => {
                          setVerCheckInId(null);
                          setCheckInSelecionado(verCheckInObj || null);
                          setEditAtividadeId(a.id);
                          setSport(a.sport);
                          setDuracao(a.duracao.toString());
                          setIntensidade(a.intensidade || "media");
                          
                          const values: Record<string, string> = {};
                          const ids: string[] = [];
                          
                          a.metricas?.custom?.forEach((m: any) => {
                            const match = allAvailableMetrics.find(metric => metric.label === m.label);
                            if (match) {
                              values[match.id] = m.value.toString();
                              ids.push(match.id);
                            }
                          });
                          
                          if (a.metricas?.distancia !== undefined) {
                            const match = allAvailableMetrics.find(metric => metric.id === "distance");
                            if (match) {
                              values[match.id] = a.metricas.distancia.toString();
                              if (!ids.includes(match.id)) ids.push(match.id);
                            }
                          }

                          setMetricValues(values);
                          setActiveMetricIds(ids);
                        }}
                        className="rounded-xl bg-accent px-4 py-2 text-[11px] font-black uppercase tracking-wider text-white shadow-md transition-all hover:scale-105 active:scale-95"
                      >
                        Editar Performance
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}

          {checkInAtividades.length > 1 && (
            <div className="mt-10 border-t border-white/10 pt-10">
              <p className="text-[12px] font-black uppercase tracking-[0.2em] text-ink-500 mb-8">Evolução do Treino (XP)</p>
              <div className="w-full bg-[#1c1c1e] rounded-3xl p-6 border border-white/5 relative z-0">
                 <svg viewBox="0 0 1000 250" className="w-full h-auto overflow-visible relative z-10" preserveAspectRatio="xMidYMid meet">
                    <defs>
                      <linearGradient id="areaGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#0a84ff" stopOpacity="0.6" />
                        <stop offset="100%" stopColor="#0a84ff" stopOpacity="0" />
                      </linearGradient>
                    </defs>
                    
                    {/* Path for gradient area fill */}
                    <path
                      d={`M 50,220 L ${checkInAtividades.map((a, i) => {
                         const maxXP = Math.max(...checkInAtividades.map(act => act.xpGanho));
                         const minXP = 0;
                         const range = maxXP - minXP || 1;
                         const x = 50 + (i / (checkInAtividades.length - 1)) * 900;
                         const y = 180 - (((a.xpGanho - minXP) / range) * 130); // scale up to 130px height, base 180
                         return `${x},${y}`;
                      }).join(" L ")} L 950,220 Z`}
                      fill="url(#areaGradient)"
                    />

                    {/* Path for the line stroke */}
                    <polyline 
                      points={checkInAtividades.map((a, i) => {
                         const maxXP = Math.max(...checkInAtividades.map(act => act.xpGanho));
                         const minXP = 0;
                         const range = maxXP - minXP || 1;
                         const x = 50 + (i / (checkInAtividades.length - 1)) * 900;
                         const y = 180 - (((a.xpGanho - minXP) / range) * 130);
                         return `${x},${y}`;
                      }).join(" ")} 
                      fill="none" 
                      stroke="#0a84ff" 
                      strokeWidth="6" 
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className="drop-shadow-[0_4px_12px_rgba(10,132,255,0.5)]"
                    />

                    {/* Interactive Points */}
                    {checkInAtividades.map((a, i) => {
                      const maxXP = Math.max(...checkInAtividades.map(act => act.xpGanho));
                      const minXP = 0;
                      const range = maxXP - minXP || 1;
                      const x = 50 + (i / (checkInAtividades.length - 1)) * 900;
                      const y = 180 - (((a.xpGanho - minXP) / range) * 130);
                      return (
                        <g key={a.id} className="group/point cursor-pointer">
                          {/* Invisible larger hover area */}
                          <circle cx={x} cy={y} r="40" fill="transparent" />
                          <circle 
                            cx={x} 
                            cy={y} 
                            r="10" 
                            fill="#1c1c1e" 
                            stroke="#0a84ff" 
                            strokeWidth="5" 
                            className="transition-all duration-300 group-hover/point:r-14 group-hover/point:fill-[#0a84ff]"
                          />
                          <text x={x} y={y - 30} textAnchor="middle" fill="#fff" fontSize="24" fontWeight="bold" opacity="0" className="group-hover/point:opacity-100 transition-opacity pointer-events-none drop-shadow-xl font-mono">
                             {a.xpGanho.toFixed(1)} XP
                          </text>
                        </g>
                      );
                    })}
                 </svg>
              </div>
            </div>
          )}

          <Button variant="ghost" className="w-full text-ink-400 hover:text-white border-white/5 hover:bg-white/5 py-8 rounded-[2rem]" onClick={() => setVerCheckInId(null)}>
            Fechar Resumo de Performance
          </Button>
        </div>
      </Modal>
    </div>
  );
}
