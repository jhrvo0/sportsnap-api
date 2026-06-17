import Link from "next/link";

export default function Home() {
  return (
    <>
      <section
        className="relative overflow-hidden rounded-3xl px-8 py-20 text-white sm:px-16 sm:py-28 fade-up"
        style={{
          background: "linear-gradient(135deg, #0a0a0c 0%, #1d1d1f 50%, #0a0a0c 100%)",
          colorScheme: "light",
        }}
      >
        <div className="absolute -right-24 -top-24 h-72 w-72 rounded-full bg-accent/30 blur-3xl" />
        <div className="absolute -bottom-32 -left-20 h-72 w-72 rounded-full bg-violet-500/20 blur-3xl" />
        <div className="relative">
          <p className="mb-3 text-[13px] font-medium uppercase tracking-[0.18em]" style={{ color: "#0a84ff" }}>
            SportSnap
          </p>
          <h1
            className="max-w-3xl text-5xl font-bold leading-[1.05] tracking-tight text-balance sm:text-6xl"
            style={{ color: "#ffffff" }}
          >
            Sua performance.
            <br />
            <span style={{ color: "#e5e5ea" }}>Capturada com perfeição.</span>
          </h1>
          <p className="mt-6 max-w-xl text-lg text-balance" style={{ color: "#a8a8ad" }}>
            Treine, conquiste e veja sua evolução validada por imagens profissionais
            que sincronizam diretamente com sua Carta de Atleta.
          </p>
          <div className="mt-10 flex flex-wrap gap-3">
            <Link
              href="/login"
              className="rounded-full px-6 py-3 text-sm font-semibold transition hover:bg-ink-800"
              style={{ background: "#000000", color: "#ffffff", border: "1px solid rgba(255,255,255,0.18)" }}
            >
              Entrar
            </Link>
            <Link
              href="/ranking"
              className="rounded-full border px-6 py-3 text-sm font-semibold transition hover:bg-white/10"
              style={{ borderColor: "rgba(255,255,255,0.25)", color: "#ffffff" }}
            >
              Ver ranking →
            </Link>
          </div>
        </div>
      </section>

      <section className="mt-20 grid gap-6 md:grid-cols-3 fade-up">
        <FeatureCard
          tone="emerald"
          title="Shadow Stats"
          desc="Cada treino acumula XP oculto. Só uma foto adquirida revela seu progresso oficial."
        />
        <FeatureCard
          tone="violet"
          title="Marketplace"
          desc="Fotógrafos profissionais publicam álbuns. Atletas adquirem licenças e desbloqueiam novas cartas."
        />
        <FeatureCard
          tone="amber"
          title="Ranking ao vivo"
          desc="Posição global atualizada por Overall — somente cartas sincronizadas competem."
        />
      </section>

      <section className="mt-20 grid gap-12 md:grid-cols-2 fade-up">
        <RoleCta
          eyebrow="Para Atletas"
          title="Cada treino, uma conquista visível."
          desc="Faça check-in, treine, compre as fotos do seu dia e dispare a Sincronização para subir no ranking."
          href="/login"
          cta="Entrar como Atleta"
        />
        <RoleCta
          eyebrow="Para Fotógrafos"
          title="Monetize cada clique."
          desc="Publique álbuns vinculados a sessões e spots. Receba split financeiro automático em cada licença vendida."
          href="/login"
          cta="Entrar como Fotógrafo"
        />
      </section>
    </>
  );
}

function FeatureCard({ tone, title, desc }: { tone: "emerald" | "violet" | "amber"; title: string; desc: string }) {
  const dot = {
    emerald: "bg-emerald-500",
    violet: "bg-violet-500",
    amber: "bg-amber-500",
  }[tone];
  return (
    <div className="surface rounded-3xl p-7">
      <div className={`mb-5 h-2.5 w-2.5 rounded-full ${dot}`} />
      <h3 className="text-xl font-semibold text-ink-900">{title}</h3>
      <p className="mt-2 text-[15px] leading-relaxed text-ink-500">{desc}</p>
    </div>
  );
}

function RoleCta({
  eyebrow,
  title,
  desc,
  href,
  cta,
}: {
  eyebrow: string;
  title: string;
  desc: string;
  href: string;
  cta: string;
}) {
  return (
    <div className="surface rounded-3xl p-9">
      <p className="text-[13px] font-medium uppercase tracking-[0.12em] text-accent">{eyebrow}</p>
      <h3 className="mt-2 text-2xl font-bold text-ink-900 text-balance">{title}</h3>
      <p className="mt-3 text-[15px] leading-relaxed text-ink-500">{desc}</p>
      <Link
        href={href}
        className="mt-6 inline-flex items-center gap-1 text-sm font-semibold text-accent hover:text-accent-700"
      >
        {cta} →
      </Link>
    </div>
  );
}
