import type { ComponentPropsWithoutRef } from "react";

type Props = ComponentPropsWithoutRef<"section"> & {
  title?: string;
  description?: string;
  children: React.ReactNode;
};

export function Card({ title, description, children, className = "", ...props }: Props) {
  return (
    <section className={`surface rounded-[2.5rem] p-8 ${className}`} {...props}>
      {(title || description) && (
        <div className="mb-6">
          {title && <h2 className="text-xl font-black text-ink-900 leading-tight">{title}</h2>}
          {description && <p className="mt-1.5 text-[13px] font-medium text-ink-500 leading-relaxed">{description}</p>}
        </div>
      )}
      {children}
    </section>
  );
}
