"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

const linksAtleta = [
  { href: "/loja", label: "Loja" },
  { href: "/ranking", label: "Ranking" },
  { href: "/spots", label: "Spots" },
  { href: "/sessoes", label: "Sessões" },
  { href: "/perfil", label: "Perfil" },
];

const linksFotografo = [
  { href: "/lotes", label: "Meus Lotes" },
  { href: "/upload", label: "Upload" },
  { href: "/spots", label: "Spots" },
  { href: "/sessoes", label: "Sessões" },
  { href: "/perfil", label: "Perfil" },
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
    <header className="sticky top-0 z-50 glass border-b border-black/5">
      <nav className="mx-auto flex max-w-6xl items-center px-6 py-3">
        <Link
          href="/"
          className="flex items-center gap-2 text-base font-semibold tracking-tight text-ink-900"
        >
          <span className="grid h-7 w-7 place-items-center rounded-lg bg-ink-900 text-[11px] font-bold text-white">
            S
          </span>
          SportSnap
        </Link>

        <div className="ml-8 hidden items-center gap-1 md:flex">
          {links.map((l) => {
            const active = pathname === l.href;
            return (
              <Link
                key={l.href}
                href={l.href}
                className={`rounded-full px-3.5 py-1.5 text-[13px] font-medium transition ${
                  active
                    ? "bg-ink-900 text-white"
                    : "text-ink-600 hover:bg-ink-100 hover:text-ink-900"
                }`}
              >
                {l.label}
              </Link>
            );
          })}
        </div>

        <div className="ml-auto flex items-center gap-2">
          {sessao ? (
            <div className="flex items-center gap-2">
              <Link
                href="/perfil"
                className="hidden items-center gap-2 rounded-full bg-ink-50 px-3 py-1.5 text-[13px] font-medium text-ink-700 hover:bg-ink-100 sm:flex"
              >
                <span className="grid h-6 w-6 place-items-center rounded-full bg-accent text-[11px] font-semibold text-white">
                  {sessao.nome.charAt(0).toUpperCase()}
                </span>
                {sessao.nome.split(" ")[0]}
              </Link>
              <button
                onClick={() => {
                  logout();
                  router.push("/");
                }}
                className="rounded-full px-3 py-1.5 text-[13px] font-medium text-ink-500 hover:bg-ink-100 hover:text-ink-900"
              >
                Sair
              </button>
            </div>
          ) : (
            <Link
              href="/login"
              className="rounded-full bg-ink-900 px-4 py-1.5 text-[13px] font-medium text-white transition hover:bg-ink-800"
            >
              Entrar
            </Link>
          )}
        </div>
      </nav>
    </header>
  );
}
