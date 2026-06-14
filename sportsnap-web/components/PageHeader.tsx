type Props = {
  title: string;
  subtitle?: string;
  eyebrow?: string;
  children?: React.ReactNode;
};

export function PageHeader({ title, subtitle, eyebrow, children }: Props) {
  return (
    <section
      className="relative mb-12 overflow-hidden rounded-[3rem] px-10 py-16 sm:px-14 sm:py-20 fade-up"
      style={{
        background: "linear-gradient(145deg, #0a0a0c 0%, #1c1c1e 100%)",
        colorScheme: "dark",
      }}
    >
      <div className="absolute -right-24 -top-24 h-80 w-80 rounded-full bg-accent/20 blur-[100px]" />
      <div className="absolute -bottom-40 -left-20 h-80 w-80 rounded-full bg-violet-600/10 blur-[100px]" />
      
      <div className="relative flex flex-col md:flex-row md:items-end justify-between gap-8">
        <div className="max-w-2xl">
          {eyebrow && (
            <p className="mb-4 text-[12px] font-black uppercase tracking-[0.25em] text-accent">
              {eyebrow}
            </p>
          )}
          <h1 className="text-4xl font-black leading-[1.1] tracking-tight text-white sm:text-6xl">
            {title}
          </h1>
          {subtitle && (
            <p className="mt-6 text-lg font-medium text-ink-300 leading-relaxed max-w-xl">
              {subtitle}
            </p>
          )}
        </div>
        {children && <div className="flex shrink-0 gap-3">{children}</div>}
      </div>
    </section>
  );
}
