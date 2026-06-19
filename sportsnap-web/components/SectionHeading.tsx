import type { ReactNode } from "react";
import { Badge } from "./Badge";

type Props = {
  /** Título curto da seção. */
  title: string;
  /** Descrição auxiliar opcional. */
  description?: string;
  /** Badge opcional exibido à direita (ex.: "Ao Vivo"). */
  badge?: { tone?: "neutral" | "success" | "warning" | "info" | "accent" | "danger"; label: string };
  /** Ação(ões) alinhadas à direita. */
  actions?: ReactNode;
  className?: string;
};

/**
 * Cabeçalho interno de seção padronizado.
 * Substitui os diversos <div className="flex items-center justify-between"> espalhados.
 */
export function SectionHeading({ title, description, badge, actions, className = "" }: Props) {
  return (
    <div className={`flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between ${className}`}>
      <div>
        <div className="flex items-center gap-3">
          <h2 className="text-2xl font-black tracking-tight text-ink-900 sm:text-[28px]">{title}</h2>
          {badge && <Badge tone={badge.tone}>{badge.label}</Badge>}
        </div>
        {description && (
          <p className="mt-1.5 text-[13px] font-medium leading-relaxed text-ink-500">{description}</p>
        )}
      </div>
      {actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
    </div>
  );
}
