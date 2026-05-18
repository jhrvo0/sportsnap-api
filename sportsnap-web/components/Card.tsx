type Props = {
  title?: string;
  description?: string;
  children: React.ReactNode;
  className?: string;
};

export function Card({ title, description, children, className = "" }: Props) {
  return (
    <section className={`surface rounded-3xl p-7 ${className}`}>
      {title && <h2 className="text-xl font-semibold text-ink-900">{title}</h2>}
      {description && <p className="mt-1 text-sm text-ink-500">{description}</p>}
      {(title || description) && <div className="my-5 h-px bg-ink-100" />}
      {children}
    </section>
  );
}
