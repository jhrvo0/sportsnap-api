"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

const linksAtleta = [
  { href: "/atletas", label: "Dashboard" },
  { href: "/atividades/analise", label: "Evolução Real" },
  { href: "/loja", label: "Loja" },
  { href: "/clube", label: "Pass ⭐️" },
  { href: "/favoritos", label: "Favoritos" },
  { href: "/checkin", label: "Check-in" },
  { href: "/social", label: "Social" },
  { href: "/ranking", label: "Ranking" },
  { href: "/spots", label: "Spots" },
  { href: "/sessoes", label: "Sessões" },
];

const linksFotografo = [
  { href: "/lotes", label: "Meus Álbuns" },
  { href: "/upload", label: "Upload" },
  { href: "/social", label: "Social" },
  { href: "/spots", label: "Spots" },
  { href: "/sessoes", label: "Sessões" },
];

const linksPublicos = [
  { href: "/ranking", label: "Ranking" },
  { href: "/spots", label: "Spots" },
];

export function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const { sessao, logout } = useAuth();

  const links =
    sessao?.role === "atleta"
      ? linksAtleta
      : sessao?.role === "fotografo"
        ? linksFotografo
        : linksPublicos;

  return (
    <header className="sticky top-0 z-50 glass border-b border-black/5 px-6">
      <nav className="mx-auto flex max-w-7xl items-center py-4">
        <Link
          href="/"
          className="flex items-center gap-3 text-xl font-black tracking-tighter text-ink-900 group"
        >
          <span className="grid h-9 w-9 place-items-center rounded-xl bg-ink-900 text-[14px] font-black text-white transition-transform group-hover:scale-110 group-active:scale-95">
            S
          </span>
          SportSnap
        </Link>

        <div className="ml-12 hidden items-center gap-2 md:flex">
          {links.map((l) => {
            const active = pathname === l.href;
            return (
              <Link
                key={l.href}
                href={l.href}
                className={`rounded-full px-5 py-2 text-[13px] font-bold transition-all duration-200 ${
                  active
                    ? "bg-ink-900 text-white shadow-md"
                    : "text-ink-500 hover:bg-ink-100/50 hover:text-ink-900"
                }`}
              >
                {l.label}
              </Link>
            );
          })}
        </div>

        <div className="ml-auto flex items-center gap-4">
          {sessao ? (
            <div className="flex items-center gap-4">
              <Link
                href="/perfil"
                className="hidden items-center gap-3 rounded-full bg-ink-50 px-4 py-2 text-[13px] font-bold text-ink-700 hover:bg-ink-100 transition-colors sm:flex"
              >
                <span className="grid h-7 w-7 place-items-center rounded-full bg-accent text-[12px] font-black text-white">
                  {sessao.nome.charAt(0).toUpperCase()}
                </span>
                {sessao.nome.split(" ")[0]}
              </Link>
              <button
                onClick={() => {
                  logout();
                  router.push("/");
                }}
                className="rounded-full px-4 py-2 text-[13px] font-bold text-ink-400 hover:bg-ink-100 hover:text-ink-900 transition-colors"
              >
                Sair
              </button>
            </div>
          ) : (
            <Link
              href="/login"
              className="rounded-full bg-ink-900 px-6 py-2.5 text-[13px] font-bold text-white transition-all hover:bg-ink-800 hover:scale-105 active:scale-95 shadow-md"
            >
              Entrar
            </Link>
          )}
        </div>
      </nav>
    </header>
  );
}
