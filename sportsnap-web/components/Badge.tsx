type Props = {
  tone?: "neutral" | "success" | "warning" | "info" | "accent";
  children: React.ReactNode;
};

const tones = {
  neutral: "bg-ink-100 text-ink-700",
  success: "bg-emerald-100 text-emerald-700",
  warning: "bg-amber-100 text-amber-700",
  info: "bg-blue-100 text-blue-700",
  accent: "bg-accent/10 text-accent",
};

export function Badge({ tone = "neutral", children }: Props) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-[11px] font-medium ${tones[tone]}`}>
      {children}
    </span>
  );
}
