"use client";

import { useEffect, useState } from "react";
import { db } from "@/lib/db";
import { type Sessao, type Spot } from "@/lib/api";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { Button } from "@/components/Button";
import { Input, Select } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";
import { DynamicMap } from "@/components/DynamicMap";
import { Modal } from "@/components/Modal";
import { listarSpots, listarSessoes, criarSessao, atualizarSessao, cancelarSessao as apiCancelarSessao } from "@/lib/spots";

export default function SessoesPage() {
  const [sessoes, setSessoes] = useState<Sessao[]>([]);
  const [spots, setSpots] = useState<Spot[]>([]);
  
  const [spotId, setSpotId] = useState("");
  const [inicio, setInicio] = useState("");
  const [fim, setFim] = useState("");
  const [descricao, setDescricao] = useState("");
  
  const [editingSessao, setEditingSessao] = useState<Sessao | null>(null);
  const [editSpotId, setEditSpotId] = useState("");
  const [editInicio, setEditInicio] = useState("");
  const [editFim, setEditFim] = useState("");
  const [editDesc, setEditDesc] = useState("");

  const [filtroSpotId, setFiltroSpotId] = useState("");
  const [apenasAtivas, setApenasAtivas] = useState(false);
  const [sessoesFiltradas, setSessoesFiltradas] = useState<Sessao[]>([]);

  const [aviso, setAviso] = useState<string | null>(null);

  async function carregar() {
    try {
      const resSpots = await listarSpots();
      setSpots(resSpots);
      db.set("spots", resSpots);
    } catch (e) {
      console.warn("Erro ao buscar spots da API, usando DB local:", e);
      setSpots(db.get("spots"));
    }

    try {
      const resSessoes = await listarSessoes();
      setSessoes(resSessoes);
      db.set("sessoes", resSessoes);
    } catch (e) {
      console.warn("Erro ao buscar sessoes da API, usando DB local:", e);
      setSessoes(db.get("sessoes"));
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  useEffect(() => {
    let filtradas = [...sessoes];
    const agora = new Date();

    if (filtroSpotId) {
      filtradas = filtradas.filter(s => s.spotId === parseInt(filtroSpotId, 10));
    }
    if (apenasAtivas) {
      filtradas = filtradas.filter(s => {
        if (s.cancelada) return false;
        const start = new Date(s.periodoInicio);
        const end = new Date(s.periodoFim);
        return start <= agora && end >= agora;
      });
    }
    setSessoesFiltradas(filtradas);
  }, [sessoes, filtroSpotId, apenasAtivas]);

  async function cadastrar(e: React.FormEvent) {
    e.preventDefault();
    const startDate = new Date(inicio);
    const endDate = new Date(fim);
    
    if (startDate < new Date()) {
      setAviso("Erro: Não é possível criar sessões no passado.");
      return;
    }
    if (startDate >= endDate) {
      setAviso("Erro: O horário de fim deve ser posterior ao de início.");
      return;
    }

    const payload = {
      spotId: parseInt(spotId, 10),
      periodoInicio: startDate.toISOString(),
      periodoFim: endDate.toISOString(),
      descricao,
    };

    try {
      await criarSessao(payload);
      db.add("sessoes", payload);
      setAviso("Sessão agendada com sucesso globalmente.");
    } catch (err) {
      db.add("sessoes", payload);
      setAviso("Sessão agendada localmente (offline).");
    }

    setInicio("");
    setFim("");
    setDescricao("");
    carregar();
  }

  function prepararEdicao(s: Sessao) {
    const agora = new Date();
    const end = new Date(s.periodoFim);
    if (end <= agora) {
      setAviso("Erro: Não é possível editar uma sessão que já foi encerrada.");
      return;
    }
    if (s.cancelada) {
      setAviso("Erro: Não é possível editar uma sessão cancelada.");
      return;
    }
    setEditingSessao(s);
    setEditSpotId(s.spotId.toString());
    setEditInicio(new Date(s.periodoInicio).toISOString().slice(0, 16));
    setEditFim(new Date(s.periodoFim).toISOString().slice(0, 16));
    setEditDesc(s.descricao);
  }

  async function salvarEdicao(e: React.FormEvent) {
    e.preventDefault();
    if (!editingSessao) return;
    
    const agora = new Date();
    const end = new Date(editingSessao.periodoFim);
    if (end <= agora) {
      setAviso("Erro: Não é possível salvar alterações em uma sessão que já foi encerrada.");
      return;
    }
    if (editingSessao.cancelada) {
      setAviso("Erro: Não é possível salvar alterações em uma sessão cancelada.");
      return;
    }
    
    const originalStart = new Date(editingSessao.periodoInicio);
    const originalEnd = new Date(editingSessao.periodoFim);
    const isOngoing = originalStart <= agora && originalEnd >= agora;
    
    const newStartDate = new Date(editInicio);
    const newEndDate = new Date(editFim);

    if (isOngoing && newStartDate.getTime() !== originalStart.getTime()) {
      setAviso("Erro: Não é possível alterar o início de uma sessão que já está ocorrendo.");
      return;
    }
    if (!isOngoing && originalStart > agora && newStartDate < agora) {
      setAviso("Erro: Não é possível reagendar o início para o passado.");
      return;
    }
    if (newStartDate >= newEndDate) {
      setAviso("Erro: O horário de fim deve ser posterior ao de início.");
      return;
    }

    const payload = {
      spotId: parseInt(editSpotId, 10),
      periodoInicio: newStartDate.toISOString(),
      periodoFim: newEndDate.toISOString(),
      descricao: editDesc
    };

    try {
      await atualizarSessao(editingSessao.id, payload);
      db.update("sessoes", editingSessao.id, payload);
      setAviso(`Sessão #${editingSessao.id} atualizada globalmente.`);
    } catch (err) {
      db.update("sessoes", editingSessao.id, payload);
      setAviso(`Sessão #${editingSessao.id} atualizada localmente.`);
    }
    setEditingSessao(null);
    carregar();
  }

  async function cancelarSessao(s: Sessao) {
    const agora = new Date();
    const start = new Date(s.periodoInicio);
    
    if (start <= agora) {
       setAviso("Erro: Não é possível cancelar uma sessão que já iniciou ou encerrou.");
       return;
    }
    
    if (confirm("Deseja realmente cancelar esta sessão?")) {
      try {
        await apiCancelarSessao(s.id);
        db.update("sessoes", s.id, { cancelada: true });
        setAviso(`Sessão #${s.id} cancelada globalmente.`);
      } catch (err) {
        db.update("sessoes", s.id, { cancelada: true });
        setAviso(`Sessão #${s.id} cancelada localmente.`);
      }
      carregar();
    }
  }

  function excluirSessao(id: number) {
    if (confirm("Deseja realmente excluir esta sessão encerrada?")) {
      db.delete("sessoes", id);
      setAviso(`Sessão #${id} excluída.`);
      carregar();
    }
  }

  function getStatus(s: Sessao) {
    if (s.cancelada) return { text: "Cancelada", tone: "danger" as const };
    const agora = new Date();
    const start = new Date(s.periodoInicio);
    const end = new Date(s.periodoFim);
    if (start <= agora && end >= agora) return { text: "Ativa Agora", tone: "success" as const };
    if (start > agora) return { text: "Agendada", tone: "info" as const };
    return { text: "Encerrada", tone: "warning" as const };
  }

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Admin"
        title="Sessões de Treino"
        subtitle="Agende janelas temporais de atividade em Spots específicos."
      />

      {aviso && <Alert tone="success" className="mb-6">{aviso}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_2fr]">
        <div className="space-y-6">
          <Card title="Nova Sessão">
            <form onSubmit={cadastrar} className="space-y-4">
              <Select
                label="Spot / Local"
                value={spotId}
                onChange={(e) => setSpotId(e.target.value)}
                required
              >
                <option value="">Selecione...</option>
                {spots.map((s) => (
                  <option key={s.id} value={s.id}>#{s.id} – {s.nome}</option>
                ))}
              </Select>
              <div className="grid grid-cols-2 gap-4">
                <Input
                  label="Início"
                  type="datetime-local"
                  value={inicio}
                  onChange={(e) => setInicio(e.target.value)}
                  required
                />
                <Input
                  label="Fim"
                  type="datetime-local"
                  value={fim}
                  onChange={(e) => setFim(e.target.value)}
                  required
                />
              </div>
              <Input
                label="Descrição da Atividade"
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
                placeholder="Ex: Treino de Surf Avançado"
                required
              />
              <div className="flex gap-2">
                <Button type="submit" className="flex-1" size="lg">
                  Agendar Sessão
                </Button>
              </div>
            </form>
          </Card>

          <Card title="Filtrar Lista">
            <div className="space-y-4">
              <Select
                label="Por Spot"
                value={filtroSpotId}
                onChange={(e) => setFiltroSpotId(e.target.value)}
              >
                <option value="">Todos os Locais</option>
                {spots.map((s) => (
                  <option key={s.id} value={s.id}>{s.nome}</option>
                ))}
              </Select>
              <label className="flex items-center gap-3 cursor-pointer group">
                <input
                  type="checkbox"
                  checked={apenasAtivas}
                  onChange={(e) => setApenasAtivas(e.target.checked)}
                  className="h-5 w-5 rounded-lg border-ink-200 text-accent focus:ring-accent"
                />
                <span className="text-sm font-medium text-ink-700 group-hover:text-ink-900 transition-colors">
                  Mostrar apenas sessões acontecendo agora
                </span>
              </label>
            </div>
          </Card>
        </div>

        <Card title={`Sessões Encontradas (${sessoesFiltradas.length})`}>
          {sessoesFiltradas.length === 0 ? (
            <div className="py-16 text-center bg-ink-50 rounded-[2.5rem] border border-dashed border-ink-200">
               <span className="text-4xl opacity-20 mb-4 block">🗓️</span>
               <p className="text-sm text-ink-500 font-medium">Nenhuma sessão agendada.</p>
               <p className="text-xs text-ink-400 mt-1">Ajuste os filtros ou crie uma nova sessão.</p>
            </div>
          ) : (
            <div className="space-y-6">
              {sessoesFiltradas.map((s) => {
                const status = getStatus(s);
                const spotObj = spots.find(sp => sp.id === s.spotId);
                const isAtiva = status.text === "Ativa Agora";
                
                // Calculate duration in hours/minutes
                const start = new Date(s.periodoInicio);
                const end = new Date(s.periodoFim);
                const durationMs = end.getTime() - start.getTime();
                const durationHrs = Math.floor(durationMs / (1000 * 60 * 60));
                const durationMins = Math.round((durationMs % (1000 * 60 * 60)) / (1000 * 60));
                const durationText = `${durationHrs > 0 ? `${durationHrs}h ` : ''}${durationMins > 0 ? `${durationMins}m` : ''}`.trim() || 'N/A';

                return (
                  <div key={s.id} className={`surface relative flex flex-col items-start gap-0 rounded-[2rem] transition-all hover:shadow-lg group overflow-hidden ${isAtiva ? 'ring-2 ring-emerald-500/20 bg-emerald-50/10' : 'border border-ink-100'}`}>
                    
                    {/* Map Header */}
                    {spotObj && (
                      <div className="w-full relative isolate pointer-events-none">
                         <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent z-10" />
                         <DynamicMap latitude={spotObj.latitude} longitude={spotObj.longitude} readOnly={true} height="120px" />
                         <div className="absolute bottom-3 left-4 z-20 flex items-center gap-2">
                           <span className="text-xl drop-shadow-md">📍</span>
                           <span className="text-white font-bold text-sm drop-shadow-md">{spotObj.nome}</span>
                         </div>
                      </div>
                    )}

                    <div className="flex-1 w-full p-6 pt-5">
                      <div className="flex items-center justify-between gap-2 mb-3">
                        <span className="font-mono text-[10px] font-bold text-accent bg-accent/10 px-2 py-0.5 rounded-md">ID #{s.id}</span>
                        <Badge tone={status.tone} className={isAtiva ? "animate-pulse" : ""}>
                          {isAtiva && <span className="mr-1.5 inline-block h-2 w-2 rounded-full bg-emerald-500 animate-ping" />}
                          {status.text}
                        </Badge>
                      </div>
                      
                      <h3 className="text-xl font-bold text-ink-900 leading-tight">{s.descricao}</h3>
                      {!spotObj && (
                         <p className="text-sm font-medium text-ink-500 mt-1 flex items-center gap-1.5">
                           <span className="text-lg">📍</span> Spot #{s.spotId}
                         </p>
                      )}
                      
                      {/* Visual Time Track */}
                      <div className="mt-5 flex items-center gap-4 bg-ink-50/50 p-4 rounded-2xl border border-ink-100">
                         <div className="flex flex-col items-center">
                            <p className="uppercase font-bold text-[9px] text-ink-400 mb-1">Início</p>
                            <span className="text-sm font-bold text-ink-900">{start.toLocaleTimeString("pt-BR", { hour: '2-digit', minute: '2-digit' })}</span>
                            <span className="text-[10px] text-ink-400">{start.toLocaleDateString("pt-BR", { day: '2-digit', month: 'short' })}</span>
                         </div>
                         
                         <div className="flex-1 flex items-center gap-2">
                           <div className="h-px bg-ink-200 flex-1 relative">
                             {isAtiva && (
                               <div className="absolute top-0 left-0 h-full bg-emerald-500 rounded-full" style={{ width: '50%' }} /> // Simulated progress
                             )}
                           </div>
                           <span className="text-[10px] font-bold text-ink-400 bg-white px-2 py-0.5 rounded-full border border-ink-200">
                             {durationText}
                           </span>
                           <div className="h-px bg-ink-200 flex-1" />
                         </div>
                         
                         <div className="flex flex-col items-center">
                            <p className="uppercase font-bold text-[9px] text-ink-400 mb-1">Fim</p>
                            <span className="text-sm font-bold text-ink-900">{end.toLocaleTimeString("pt-BR", { hour: '2-digit', minute: '2-digit' })}</span>
                            <span className="text-[10px] text-ink-400">{end.toLocaleDateString("pt-BR", { day: '2-digit', month: 'short' })}</span>
                         </div>
                      </div>
                      
                      <div className="mt-5 flex sm:flex-row flex-col gap-2 w-full">
                        {status.text === "Encerrada" || status.text === "Cancelada" ? (
                          <>
                            {status.text === "Encerrada" && (
                              <Button 
                                size="md" 
                                variant="ghost" 
                                className="w-full justify-center text-rose-500 hover:bg-rose-50" 
                                onClick={() => excluirSessao(s.id)}
                              >
                                Excluir
                              </Button>
                            )}
                          </>
                        ) : (
                          <>
                            <Button size="md" variant="secondary" className="flex-1 justify-center" onClick={() => prepararEdicao(s)}>Editar Sessão</Button>
                            <Button 
                              size="md" 
                              variant="ghost" 
                              className="sm:flex-none justify-center text-rose-500 hover:bg-rose-50 disabled:opacity-30" 
                              onClick={() => cancelarSessao(s)}
                              disabled={start <= new Date() || s.cancelada}
                            >
                              Cancelar
                            </Button>
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </Card>
      </div>

      <Modal isOpen={!!editingSessao} onClose={() => setEditingSessao(null)} title="Editar Sessão">
        {editingSessao && (
          <form onSubmit={salvarEdicao} className="space-y-4">
            <Select
              label="Spot / Local"
              value={editSpotId}
              onChange={(e) => setEditSpotId(e.target.value)}
              required
            >
              <option value="">Selecione...</option>
              {spots.map((s) => (
                <option key={s.id} value={s.id}>#{s.id} – {s.nome}</option>
              ))}
            </Select>
            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Início"
                type="datetime-local"
                value={editInicio}
                onChange={(e) => setEditInicio(e.target.value)}
                disabled={!!(new Date(editingSessao.periodoInicio) <= new Date() && new Date(editingSessao.periodoFim) >= new Date())}
                required
              />
              <Input
                label="Fim"
                type="datetime-local"
                value={editFim}
                onChange={(e) => setEditFim(e.target.value)}
                required
              />
            </div>
            <Input
              label="Descrição da Atividade"
              value={editDesc}
              onChange={(e) => setEditDesc(e.target.value)}
              placeholder="Ex: Treino de Surf Avançado"
              required
            />
            <div className="flex gap-2">
              <Button type="submit" className="flex-1" size="lg">
                Salvar Alterações
              </Button>
              <Button type="button" variant="ghost" onClick={() => setEditingSessao(null)}>
                Cancelar
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
}
