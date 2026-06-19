type Props = {
  /** Rótulo curto da métrica (ex.: "Treinos", "Distância"). */
  label: string;
  /** Valor principal já formatado (string). */
  value: string;
  /** Unidade auxiliar pequena (ex.: "km", "min"). */
  unit?: string;
  /** Descrição auxiliar exibida abaixo do valor. */
  hint?: string;
  /** Ícone/emoji opcional exibido no topo do card. */
  icon?: string;
  /** Destaque visual da métrica principal. */
  highlight?: boolean;
  className?: string;
};

/**
 * Card de métrica/resumo padronizado.
 * Substitui os blocos de resumo duplicados inline nas páginas.
 */
export function StatCard({ label, value, unit, hint, icon, highlight = false, className = "" }: Props) {
  return (
    <div
      className={`surface rounded-3xl p-6 transition-shadow hover:shadow-md ${highlight ? "ring-1 ring-accent/10" : ""} ${className}`}
    >
      <div className="flex items-center justify-between">
        <p className="text-[10px] font-black uppercase tracking-widest text-ink-400">{label}</p>
        {icon && <span className="text-base leading-none opacity-70">{icon}</span>}
      </div>
      <p
        className={`mt-2 text-3xl font-black tracking-tight ${highlight ? "text-accent" : "text-ink-900"}`}
      >
        {value}
        {unit && <span className="ml-1 text-sm font-bold text-ink-400">{unit}</span>}
      </p>
      {hint && <p className="mt-1 text-[11px] font-bold text-ink-500">{hint}</p>}
    </div>
  );
}
