"use client";

import Link from "next/link";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { PageHeader } from "@/components/PageHeader";

const features = [
  {
    href: "/gamificacao/carta",
    emoji: "🃏",
    titulo: "Minha Carta",
    desc: "Sua Carta Oficial: tier, Overall e atributos. Use o Reveal para transformar o XP dos treinos em evolução real.",
  },
  {
    href: "/gamificacao/competicao",
    emoji: "⚔️",
    titulo: "Arena",
    desc: "Enfrente outros atletas, suba no ranking por pontos, mude de liga e descubra adversários do seu nível.",
  },
  {
    href: "/gamificacao/analise",
    emoji: "📊",
    titulo: "Análise",
    desc: "Onde você está forte, onde dá pra crescer, como você se compara com a base e para onde sua evolução aponta.",
  },
  {
    href: "/gamificacao/desafios",
    emoji: "🎯",
    titulo: "Desafios",
    desc: "Missões que recompensam consistência. Conclua objetivos e colecione insígnias no seu perfil.",
  },
  {
    href: "/gamificacao/temporadas",
    emoji: "📅",
    titulo: "Temporadas",
    desc: "O calendário competitivo. Acompanhe a temporada vigente e o histórico de cada disputa.",
  },
];

export default function GamificacaoHub() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
  }, [sessao, carregando, router]);

  if (!sessao) return null;
  const primeiroNome = sessao.nome.split(" ")[0];

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Gamificação"
        title={`Sua jornada, ${primeiroNome}`}
        subtitle="Evolua sua Carta, compita na arena, entenda seu desempenho e conquiste desafios — tudo o que transforma seus treinos em progresso."
      />

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {features.map((f) => (
          <Link
            key={f.href}
            href={f.href}
            className="surface group flex flex-col rounded-[2.5rem] p-8 transition-all hover:-translate-y-1 hover:border-accent/30 hover:shadow-soft"
          >
            <span className="mb-6 grid h-14 w-14 place-items-center rounded-2xl bg-ink-50 text-3xl transition-transform group-hover:scale-110">
              {f.emoji}
            </span>
            <h3 className="text-xl font-black text-ink-900">{f.titulo}</h3>
            <p className="mt-2 flex-1 text-[13px] font-medium leading-relaxed text-ink-500">{f.desc}</p>
            <span className="mt-6 text-sm font-bold text-accent transition-transform group-hover:translate-x-1">
              Abrir →
            </span>
          </Link>
        ))}
      </div>
    </div>
  );
}
