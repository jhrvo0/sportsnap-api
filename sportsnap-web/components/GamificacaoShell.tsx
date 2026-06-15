"use client";

import { ReactNode, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { gamificacao, AtletaApi } from "@/lib/gamificacao";
import { PageHeader } from "@/components/PageHeader";

type ShellCtx = { atletaId: number; atletas: AtletaApi[]; nome: string };

type Props = {
  eyebrow: string;
  title: string;
  subtitle?: string;
  children: (ctx: ShellCtx) => ReactNode;
};

/**
 * Casca comum das telas de gamificacao do atleta. Usa sempre o atleta logado
 * (como o Dashboard e o Perfil), resolve o nome real e expoe a lista de atletas
 * para consultas (ex.: adversarios). Sem seletor de impersonacao.
 */
export function GamificacaoShell({ eyebrow, title, subtitle, children }: Props) {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const [atletas, setAtletas] = useState<AtletaApi[]>([]);
  const [carregandoAtletas, setCarregandoAtletas] = useState(true);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
    if (!carregando && sessao && sessao.role !== "atleta") router.replace("/perfil");
  }, [sessao, carregando, router]);

  useEffect(() => {
    let ativo = true;
    gamificacao
      .listarAtletas()
      .then((lista) => ativo && setAtletas(lista))
      .catch(() => ativo && setAtletas([]))
      .finally(() => ativo && setCarregandoAtletas(false));
    return () => {
      ativo = false;
    };
  }, []);

  if (carregando || !sessao || sessao.role !== "atleta") return null;

  const atletaId = sessao.id;
  const nome = sessao.nome;
  const inicial = nome.charAt(0).toUpperCase();

  return (
    <div className="fade-up">
      <PageHeader eyebrow={eyebrow} title={title} subtitle={subtitle}>
        <div className="flex items-center gap-3 rounded-full border border-white/10 bg-white/5 px-4 py-2.5 backdrop-blur-sm">
          <span className="grid h-9 w-9 place-items-center rounded-full bg-gradient-to-br from-accent to-violet-600 text-sm font-black text-white">
            {inicial}
          </span>
          <div className="pr-1 text-left">
            <p className="text-[10px] font-bold uppercase tracking-widest text-accent">Atleta</p>
            <p className="text-sm font-bold leading-none text-white">{nome}</p>
          </div>
        </div>
      </PageHeader>

      {carregandoAtletas ? (
        <div className="surface rounded-[2rem] p-12 text-center text-ink-400">Carregando…</div>
      ) : (
        children({ atletaId, atletas, nome })
      )}
    </div>
  );
}
