type Props = {
  /** Ícone (emoji ou texto) exibido no topo. */
  icon?: string;
  /** Título curto do estado vazio. */
  title: string;
  /** Descrição auxiliar, exibida abaixo do título. */
  description?: string;
  /** Ação opcional (ex.: botão de criar). */
  action?: React.ReactNode;
  className?: string;
};

/**
 * Estado vazio padronizado para listas/grids sem dados.
 * Usa a superfície tracejada consistente em todo o app.
 */
export function EmptyState({ icon, title, description, action, className = "" }: Props) {
  return (
    <div
      className={`flex flex-col items-center justify-center rounded-3xl border-2 border-dashed border-ink-200 bg-ink-50/40 px-6 py-14 text-center ${className}`}
    >
      {icon && (
        <span className="mb-4 grid h-16 w-16 place-items-center rounded-2xl bg-white text-4xl shadow-soft grayscale-[0.2]">
          {icon}
        </span>
      )}
      <p className="text-lg font-bold text-ink-800">{title}</p>
      {description && (
        <p className="mt-2 max-w-sm text-[13px] font-medium leading-relaxed text-ink-500">
          {description}
        </p>
      )}
      {action && <div className="mt-6">{action}</div>}
    </div>
  );
}
