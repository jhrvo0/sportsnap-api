"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useSession } from "@/lib/session";

const atletaLinks = [
  { href: "/atleta/dashboard", label: "Dashboard", icon: "🏠" },
  { href: "/atleta/sessoes", label: "Sessoes", icon: "📅" },
  { href: "/atleta/fotos", label: "Fotos", icon: "📸" },
  { href: "/atleta/ranking", label: "Ranking", icon: "🏆" },
  { href: "/atleta/perfil", label: "Perfil", icon: "👤" },
];

const fotografoLinks = [
  { href: "/fotografo/dashboard", label: "Dashboard", icon: "🏠" },
  { href: "/fotografo/upload", label: "Upload", icon: "📤" },
  { href: "/fotografo/lotes", label: "Meus Lotes", icon: "📁" },
  { href: "/fotografo/perfil", label: "Perfil", icon: "👤" },
];

export function Sidebar() {
  const pathname = usePathname();
  const { userType, userName, clearSession } = useSession();
  const links = userType === "FOTOGRAFO" ? fotografoLinks : atletaLinks;

  return (
    <aside className="fixed left-0 top-0 h-full w-64 bg-dark-800 border-r border-dark-600 flex flex-col">
      <div className="p-6 border-b border-dark-600">
        <h1 className="text-2xl font-bold">
          Sport<span className="text-brand">Snap</span>
        </h1>
        <p className="text-sm text-gray-400 mt-1">{userName}</p>
        <span className="text-xs px-2 py-0.5 rounded-full bg-dark-600 text-brand">
          {userType}
        </span>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        {links.map((link) => (
          <Link
            key={link.href}
            href={link.href}
            className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
              pathname === link.href
                ? "bg-brand/10 text-brand border border-brand/20"
                : "text-gray-300 hover:bg-dark-600 hover:text-white"
            }`}
          >
            <span>{link.icon}</span>
            <span>{link.label}</span>
          </Link>
        ))}
      </nav>

      <div className="p-4 border-t border-dark-600">
        <Link
          href="/"
          onClick={clearSession}
          className="flex items-center gap-3 px-4 py-3 rounded-lg text-red-400 hover:bg-red-400/10 transition-colors"
        >
          Sair
        </Link>
      </div>
    </aside>
  );
}
