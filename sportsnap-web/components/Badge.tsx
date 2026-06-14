type Props = {
  tone?: "neutral" | "success" | "warning" | "info" | "accent" | "danger";
  children: React.ReactNode;
  className?: string;
};

const tones = {
  neutral: "bg-ink-100/50 text-ink-600 border-ink-100",
  success: "bg-emerald-50 text-emerald-600 border-emerald-100",
  warning: "bg-amber-50 text-amber-600 border-amber-100",
  info: "bg-blue-50 text-blue-600 border-blue-100",
  accent: "bg-accent/5 text-accent border-accent/10",
  danger: "bg-rose-50 text-rose-600 border-rose-100",
};

export function Badge({ tone = "neutral", children, className = "" }: Props) {
  return (
    <span className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-[10px] font-black uppercase tracking-widest ${tones[tone]} ${className}`}>
      {children}
    </span>
  );
}
