export const SESSION_URL =
  process.env.NEXT_PUBLIC_SESSION_URL ?? "http://localhost:8083";

export type RegistroAtividadeDto = {
  id?: number;
  atletaId: number;
  checkInId?: number | null;
  esporte: string;
  data: string;
  distancia: number;
  duracaoSegundos: number;
  intensidade?: string | null;
  xpCalculado?: number;
  esforcoPercebido?: number | null;
  observacoes?: string | null;
  origemRegistro?: string;
  ritmoMedio?: number | null;
  caloriasEstimadas?: number | null;
  criadoEm?: string;
  atualizadoEm?: string;
  metricas?: string | null;
};

export type PontoEvolucaoDto = {
  data: string;
  valor: number;
};

export type AnaliseEvolucaoDto = {
  totalAtividades: number;
  distanciaTotal: number;
  tempoTotalSegundos: number;
  ritmoMedioGeral: number;
  melhorRitmo: number;
  maiorDistancia: number;
  frequenciaSemanal: number;
  evolucaoDistancia: PontoEvolucaoDto[];
  evolucaoRitmo: PontoEvolucaoDto[];
  ultimosTreinos: RegistroAtividadeDto[];
};

export async function registrarAtividade(dto: RegistroAtividadeDto): Promise<RegistroAtividadeDto> {
  const r = await fetch(`${SESSION_URL}/api/atividades`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
  if (!r.ok) throw new Error("Erro ao registrar atividade");
  return r.json();
}

export async function listarAtividades(
  atletaId: number,
  esporte?: string,
  inicio?: string,
  fim?: string
): Promise<RegistroAtividadeDto[]> {
  let url = `${SESSION_URL}/api/atividades?atletaId=${atletaId}`;
  if (esporte) url += `&esporte=${esporte}`;
  if (inicio) url += `&inicio=${inicio}`;
  if (fim) url += `&fim=${fim}`;
  
  const r = await fetch(url);
  if (!r.ok) throw new Error("Erro ao listar atividades");
  return r.json();
}

export async function obterAtividade(id: number): Promise<RegistroAtividadeDto> {
  const r = await fetch(`${SESSION_URL}/api/atividades/${id}`);
  if (!r.ok) throw new Error("Erro ao obter atividade");
  return r.json();
}

export async function atualizarAtividade(
  id: number,
  dto: RegistroAtividadeDto
): Promise<RegistroAtividadeDto> {
  const r = await fetch(`${SESSION_URL}/api/atividades/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
  if (!r.ok) throw new Error("Erro ao atualizar atividade");
  return r.json();
}

export async function removerAtividade(id: number): Promise<void> {
  const r = await fetch(`${SESSION_URL}/api/atividades/${id}`, {
    method: "DELETE",
  });
  if (!r.ok) throw new Error("Erro ao remover atividade");
}

export async function obterAnalise(
  atletaId: number,
  esporte: string,
  periodo: string
): Promise<AnaliseEvolucaoDto> {
  const r = await fetch(
    `${SESSION_URL}/api/atividades/analise?atletaId=${atletaId}&esporte=${esporte}&periodo=${periodo}`
  );
  if (!r.ok) throw new Error("Erro ao obter analise de atividades");
  return r.json();
}
