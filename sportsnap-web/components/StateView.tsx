type Tone = "default" | "danger" | "success" | "warning";

type Props = {
  /** Mensagem opcional exibida ao lado do spinner. */
  message?: string;
  className?: string;
};

/**
 * Estado de carregamento padronizado.
 * Usar dentro de qualquer área de conteúdo enquanto dados são buscados.
 */
export function Loading({ message = "Carregando...", className = "" }: Props) {
  return (
    <div
      className={`flex flex-col items-center justify-center gap-4 rounded-3xl px-6 py-16 text-center ${className}`}
    >
      <span className="h-12 w-12 rounded-full border-[5px] border-ink-100 border-t-accent animate-spin" />
      <p className="text-[13px] font-semibold tracking-tight text-ink-500">{message}</p>
    </div>
  );
}

type ErrorProps = {
  title?: string;
  description?: string;
  /** Ação opcional (ex.: botão "Tentar novamente"). */
  action?: React.ReactNode;
  tone?: Tone;
  className?: string;
};

const errorToneStyles: Record<Tone, string> = {
  default: "border-ink-200 bg-ink-50/50",
  danger: "border-rose-100 bg-rose-50/60",
  success: "border-emerald-100 bg-emerald-50/60",
  warning: "border-amber-100 bg-amber-50/60",
};

const errorIconStyles: Record<Tone, string> = {
  default: "bg-ink-100 text-ink-500",
  danger: "bg-rose-100 text-rose-600",
  success: "bg-emerald-100 text-emerald-600",
  warning: "bg-amber-100 text-amber-600",
};

const iconByTone: Record<Tone, string> = {
  default: "ⓘ",
  danger: "⚠",
  success: "✓",
  warning: "!",
};

/**
 * Estado de erro padronizado. Substitui mensagens inline cruas.
 */
export function ErrorState({
  title = "Algo deu errado",
  description,
  action,
  tone = "danger",
  className = "",
}: ErrorProps) {
  return (
    <div
      className={`flex flex-col items-center justify-center gap-4 rounded-3xl border px-6 py-14 text-center ${errorToneStyles[tone]} ${className}`}
    >
      <span
        className={`grid h-12 w-12 place-items-center rounded-2xl text-xl font-black ${errorIconStyles[tone]}`}
      >
        {iconByTone[tone]}
      </span>
      <div className="max-w-sm">
        <p className="text-lg font-bold text-ink-900">{title}</p>
        {description && (
          <p className="mt-1.5 text-[13px] font-medium leading-relaxed text-ink-500">
            {description}
          </p>
        )}
      </div>
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}
