import { Spot, Sessao } from "./api";
import { fromDateTimeLocalValue } from "./session-datetime.mjs";

const SESSION_URL = process.env.NEXT_PUBLIC_SESSION_URL ?? "http://localhost:8083";

export type CheckInDto = {
  id?: number;
  atletaId: number;
  sessaoId: number;
  horario?: string;
  latitude: number;
  longitude: number;
  cancelado?: boolean;
  atividadeRegistrada?: boolean;
  checkoutHorario?: string | null;
};

export async function listarSpots(): Promise<Spot[]> {
  const r = await fetch(`${SESSION_URL}/api/spots`);
  if (!r.ok) throw new Error("Erro ao listar spots");
  return r.json();
}

export async function criarSpot(spot: Omit<Spot, "id">): Promise<void> {
  const r = await fetch(`${SESSION_URL}/api/spots`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(spot),
  });
  if (!r.ok) throw new Error("Erro ao criar spot");
}

export async function atualizarSpot(id: number, spot: Omit<Spot, "id">): Promise<void> {
  const r = await fetch(`${SESSION_URL}/api/spots/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(spot),
  });
  if (!r.ok) throw new Error("Erro ao atualizar spot");
}

export async function removerSpot(id: number): Promise<void> {
  const r = await fetch(`${SESSION_URL}/api/spots/${id}`, {
    method: "DELETE",
  });
  if (!r.ok) throw new Error("Erro ao remover spot");
}

export async function listarSessoes(): Promise<Sessao[]> {
  const r = await fetch(`${SESSION_URL}/api/sessoes`);
  if (!r.ok) throw new Error("Erro ao listar sessoes");
  const data = await r.json();
  // Map fields for cancelada if projection uses isCancelada
  return data.map((s: any) => ({
    id: s.id,
    spotId: s.spotId,
    periodoInicio: s.periodoInicio,
    periodoFim: s.periodoFim,
    descricao: s.descricao,
    cancelada: s.cancelada !== undefined ? s.cancelada : false
  }));
}

export async function criarSessao(sessao: Omit<Sessao, "id">): Promise<void> {
  const body = {
    spotId: sessao.spotId,
    inicio: fromDateTimeLocalValue(sessao.periodoInicio),
    fim: fromDateTimeLocalValue(sessao.periodoFim),
    descricao: sessao.descricao,
  };
  const r = await fetch(`${SESSION_URL}/api/sessoes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!r.ok) throw new Error("Erro ao criar sessao");
}

export async function atualizarSessao(id: number, sessao: Omit<Sessao, "id">): Promise<void> {
  const body = {
    spotId: sessao.spotId,
    inicio: fromDateTimeLocalValue(sessao.periodoInicio),
    fim: fromDateTimeLocalValue(sessao.periodoFim),
    descricao: sessao.descricao,
  };
  const r = await fetch(`${SESSION_URL}/api/sessoes/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!r.ok) throw new Error("Erro ao atualizar sessao");
}

export async function cancelarSessao(id: number): Promise<void> {
  const r = await fetch(`${SESSION_URL}/api/sessoes/${id}/cancelar`, {
    method: "POST",
  });
  if (!r.ok) throw new Error("Erro ao cancelar sessao");
}

export async function realizarCheckIn(dto: CheckInDto): Promise<CheckInDto> {
  const r = await fetch(`${SESSION_URL}/api/checkins`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
  if (!r.ok) throw new Error("Erro ao realizar check-in");
  return r.json();
}

export async function listarCheckIns(atletaId?: number, sessaoId?: number): Promise<CheckInDto[]> {
  let url = `${SESSION_URL}/api/checkins`;
  if (atletaId) url += `?atletaId=${atletaId}`;
  else if (sessaoId) url += `?sessaoId=${sessaoId}`;
  
  const r = await fetch(url);
  if (!r.ok) throw new Error("Erro ao listar check-ins");
  return r.json();
}

export async function cancelarCheckIn(id: number): Promise<void> {
  const r = await fetch(`${SESSION_URL}/api/checkins/${id}/cancelar`, {
    method: "POST",
  });
  if (!r.ok) throw new Error("Erro ao cancelar check-in");
}

export async function fazerCheckoutCheckIn(id: number): Promise<CheckInDto> {
  const r = await fetch(`${SESSION_URL}/api/checkins/${id}/checkout`, {
    method: "POST",
  });
  if (!r.ok) throw new Error("Erro ao fazer checkout");
  return r.json();
}
