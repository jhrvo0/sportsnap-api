type Props = {
  title: string;
  subtitle?: string;
  eyebrow?: string;
  children?: React.ReactNode;
};

export function PageHeader({ title, subtitle, eyebrow, children }: Props) {
  return (
    <section
      className="relative mb-10 overflow-hidden rounded-3xl px-8 py-14 sm:px-12 sm:py-16 fade-up"
      style={{
        background: "linear-gradient(135deg, #0a0a0c 0%, #1d1d1f 50%, #0a0a0c 100%)",
        colorScheme: "light",
      }}
    >
      <div className="absolute -right-24 -top-24 h-64 w-64 rounded-full bg-accent/30 blur-3xl" />
      <div className="absolute -bottom-32 -left-20 h-64 w-64 rounded-full bg-violet-500/20 blur-3xl" />
      <div className="relative flex flex-wrap items-end justify-between gap-4">
        <div className="max-w-2xl">
          {eyebrow && (
            <p
              className="mb-2 text-[13px] font-medium uppercase tracking-[0.16em]"
              style={{ color: "#0a84ff" }}
            >
              {eyebrow}
            </p>
          )}
          <h1
            className="text-3xl font-bold leading-[1.1] tracking-tight text-balance sm:text-5xl"
            style={{ color: "#ffffff" }}
          >
            {title}
          </h1>
          {subtitle && (
            <p
              className="mt-3 text-base text-balance sm:text-lg"
              style={{ color: "#a8a8ad" }}
            >
              {subtitle}
            </p>
          )}
        </div>
        {children && <div className="flex gap-2">{children}</div>}
      </div>
    </section>
  );
}
