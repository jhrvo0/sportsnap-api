export type PlanoDto = {
  id: string;
  nome: string;
  preco: number;
  cotas: number;
  intervalo: string;
};

const STORAGE_KEY = "sportsnap.planos";

const PLANO_PADRAO: PlanoDto = {
  id: "mensal",
  nome: "SportSnap Pass",
  preco: 99.9,
  cotas: 10,
  intervalo: "mês",
};

export function getPlanos(): PlanoDto[] {
  if (typeof window === "undefined") return [PLANO_PADRAO];
  
  const raw = localStorage.getItem(STORAGE_KEY);
  if (raw) {
    try {
      const planos = JSON.parse(raw) as PlanoDto[];
      if (planos.length > 0) return planos;
    } catch (e) {
      console.error("Erro ao ler planos do localStorage", e);
    }
  }
  
  // Initialize with default plan if empty or parse failed
  localStorage.setItem(STORAGE_KEY, JSON.stringify([PLANO_PADRAO]));
  return [PLANO_PADRAO];
}

export function adicionarPlano(plano: PlanoDto): void {
  if (typeof window === "undefined") return;
  
  const planos = getPlanos();
  planos.push(plano);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(planos));
}

export function removerPlano(id: string): void {
  if (typeof window === "undefined") return;
  
  const planos = getPlanos();
  const novosPlanos = planos.filter((p) => p.id !== id);
  localStorage.setItem(STORAGE_KEY, JSON.stringify(novosPlanos));
}
