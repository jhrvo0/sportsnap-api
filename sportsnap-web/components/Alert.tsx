type Props = {
  tone?: "info" | "success" | "danger" | "warning";
  children: React.ReactNode;
  className?: string;
};

const tones = {
  info: "bg-accent/10 text-accent-700",
  success: "bg-emerald-100 text-emerald-700",
  danger: "bg-rose-100 text-rose-700",
  warning: "bg-amber-100 text-amber-700",
};

export function Alert({ tone = "info", children, className = "" }: Props) {
  return (
    <div className={`mb-6 rounded-2xl px-4 py-3 text-sm ${tones[tone]} ${className}`}>{children}</div>
  );
}
