"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/lib/auth";
import {
  obterPerfilPorUsuario, listarInbox, listarConversa, listarPerfis, enviarMensagem,
  marcarMensagemComoLida, contarMensagensNaoLidas,
  type PerfilSocial, type Mensagem, type PerfilResumo,
} from "@/lib/social";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";

function dataRelativa(iso: string) {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1)  return "agora";
  if (mins < 60) return `${mins}m`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)  return `${hrs}h`;
  return `${Math.floor(hrs / 24)}d`;
}

export default function MensagensPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const inputRef = useRef<HTMLInputElement>(null);

  // Lê ?com=perfilId da URL sem useSearchParams (evita Suspense no Next.js 14)
  const [comParam] = useState(() => {
    if (typeof window === "undefined") return null;
    return new URLSearchParams(window.location.search).get("com");
  });

  const [meuPerfil, setMeuPerfil]         = useState<PerfilSocial | null>(null);
  const [inbox, setInbox]                 = useState<Mensagem[]>([]);
  const [todos, setTodos]                 = useState<PerfilResumo[]>([]);
  const [conversaAtual, setConversaAtual] = useState<number | null>(null);
  const [mensagens, setMensagens]         = useState<Mensagem[]>([]);
  const [texto, setTexto]                 = useState("");
  const [enviando, setEnviando]           = useState(false);
  const [busca, setBusca]                 = useState("");
  const [erro, setErro]                   = useState<string | null>(null);
  const endRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (carregando) return;
    if (!sessao) { router.replace("/login"); return; }

    obterPerfilPorUsuario(sessao.id).then(async p => {
      if (!p) { router.replace("/perfil"); return; }
      setMeuPerfil(p);
      const [inboxData, todosPerfis] = await Promise.all([
        listarInbox(p.id!.id),
        listarPerfis(),
      ]);
      setInbox(inboxData);
      setTodos(todosPerfis);

      // Abre conversa via query param ?com=perfilId
      try {
        if (comParam) abrirConversa(parseInt(comParam), p.id!.id);
      } catch {}
    }).catch(() => setErro("Erro ao carregar mensagens"));
  }, [sessao, carregando, router]);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [mensagens]);

  async function abrirConversa(outroPerfilId: number, meuId?: number) {
    const pid = meuId ?? meuPerfil?.id?.id;
    if (!pid) return;
    setConversaAtual(outroPerfilId);
    const conv = await listarConversa(pid, outroPerfilId);
    setMensagens(conv);
    // Marca não lidas como lidas
    conv.filter(m => !m.lida && m.destinatarioId.id === pid)
        .forEach(m => marcarMensagemComoLida(m.id.id, pid).catch(() => {}));
    inputRef.current?.focus();
  }

  async function handleEnviar() {
    if (!meuPerfil?.id || !conversaAtual || !texto.trim()) return;
    setEnviando(true);
    try {
      const nova = await enviarMensagem(meuPerfil.id.id, conversaAtual, texto.trim());
      setMensagens(prev => [...prev, nova]);
      setTexto("");
      // Atualiza inbox
      listarInbox(meuPerfil.id.id).then(setInbox).catch(() => {});
    } catch { setErro("Erro ao enviar mensagem"); }
    finally { setEnviando(false); }
  }

  function nomeDoId(id: number) {
    return todos.find(p => p.id === id)?.nomeExibicao ?? `Usuário #${id}`;
  }

  const resultadosBusca = busca.length >= 2
    ? todos.filter(p => p.nomeExibicao.toLowerCase().includes(busca.toLowerCase()) && p.id !== meuPerfil?.id?.id)
    : [];

  if (carregando || !sessao) return null;

  return (
    <div className="fade-up max-w-4xl mx-auto">
      {erro && <Alert tone="danger" className="mb-4">{erro}</Alert>}

      <div className="grid grid-cols-3 gap-4 h-[75vh]">
        {/* Coluna esquerda: inbox + busca */}
        <div className="col-span-1 flex flex-col rounded-3xl border border-ink-100 bg-white overflow-hidden shadow-sm">
          <div className="p-4 border-b border-ink-100">
            <h2 className="font-bold text-ink-900 mb-3">Mensagens</h2>
            <input
              className="w-full rounded-2xl border border-ink-200 bg-ink-50 px-3 py-2 text-sm placeholder-ink-400 focus:border-accent focus:bg-white focus:outline-none focus:ring-2 focus:ring-accent/20"
              placeholder="🔍 Buscar pessoa..."
              value={busca}
              onChange={e => setBusca(e.target.value)}
            />
            {resultadosBusca.length > 0 && (
              <div className="mt-2 rounded-2xl border border-ink-100 overflow-hidden">
                {resultadosBusca.slice(0, 4).map(p => (
                  <button
                    key={p.id}
                    onClick={() => { setBusca(""); abrirConversa(p.id); }}
                    className="w-full flex items-center gap-3 px-3 py-2.5 hover:bg-ink-50 text-left border-b border-ink-50 last:border-0"
                  >
                    <div className="h-8 w-8 shrink-0 rounded-full bg-gradient-to-br from-accent to-violet-600 flex items-center justify-center text-xs font-bold text-white">
                      {p.nomeExibicao.charAt(0).toUpperCase()}
                    </div>
                    <span className="text-sm font-medium text-ink-900 truncate">{p.nomeExibicao}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Lista de conversas */}
          <div className="flex-1 overflow-y-auto">
            {inbox.length === 0 ? (
              <div className="py-12 text-center px-4">
                <span className="text-3xl opacity-20 block mb-2">💬</span>
                <p className="text-sm text-ink-400">Nenhuma conversa ainda</p>
                <p className="text-xs text-ink-300 mt-1">Use a busca para iniciar</p>
              </div>
            ) : inbox.map(m => {
              const outroId = meuPerfil?.id?.id === m.remetenteId.id ? m.destinatarioId.id : m.remetenteId.id;
              const ativo = conversaAtual === outroId;
              const naoLida = !m.lida && m.destinatarioId.id === meuPerfil?.id?.id;
              return (
                <button
                  key={m.id.id}
                  onClick={() => abrirConversa(outroId)}
                  className={`w-full flex items-center gap-3 px-4 py-3 text-left transition-colors border-b border-ink-50 last:border-0 ${ativo ? "bg-accent-50" : "hover:bg-ink-50"}`}
                >
                  <div className="h-10 w-10 shrink-0 rounded-full bg-gradient-to-br from-accent to-violet-600 flex items-center justify-center font-bold text-white text-sm">
                    {nomeDoId(outroId).charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className={`text-sm truncate ${naoLida ? "font-bold text-ink-900" : "font-medium text-ink-700"}`}>
                      {nomeDoId(outroId)}
                    </p>
                    <p className="text-xs text-ink-400 truncate">{m.conteudo}</p>
                  </div>
                  <div className="flex flex-col items-end gap-1 shrink-0">
                    <span className="text-[10px] text-ink-400">{dataRelativa(m.criadaEm)}</span>
                    {naoLida && <div className="h-2 w-2 rounded-full bg-accent" />}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Coluna direita: conversa */}
        <div className="col-span-2 flex flex-col rounded-3xl border border-ink-100 bg-white overflow-hidden shadow-sm">
          {!conversaAtual ? (
            <div className="flex-1 flex flex-col items-center justify-center text-center px-8">
              <span className="text-5xl opacity-20 block mb-4">💬</span>
              <p className="font-bold text-ink-600">Selecione uma conversa</p>
              <p className="text-sm text-ink-400 mt-1">ou busque uma pessoa para começar</p>
            </div>
          ) : (
            <>
              {/* Header da conversa */}
              <div className="flex items-center gap-3 px-5 py-4 border-b border-ink-100">
                <div className="h-9 w-9 rounded-full bg-gradient-to-br from-accent to-violet-600 flex items-center justify-center font-bold text-white text-sm">
                  {nomeDoId(conversaAtual).charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="font-bold text-ink-900">{nomeDoId(conversaAtual)}</p>
                </div>
                <Link href={`/social/usuario/${conversaAtual}`} className="ml-auto text-xs text-accent hover:underline">
                  Ver perfil
                </Link>
              </div>

              {/* Balões de mensagem */}
              <div className="flex-1 overflow-y-auto p-4 space-y-3">
                {mensagens.length === 0 ? (
                  <div className="flex items-center justify-center h-full">
                    <p className="text-sm text-ink-400">Nenhuma mensagem ainda. Diga olá! 👋</p>
                  </div>
                ) : mensagens.map(m => {
                  const minha = m.remetenteId.id === meuPerfil?.id?.id;
                  return (
                    <div key={m.id.id} className={`flex ${minha ? "justify-end" : "justify-start"}`}>
                      <div className={`max-w-[70%] rounded-2xl px-4 py-2.5 ${
                        minha
                          ? "bg-accent text-white rounded-br-sm"
                          : "bg-ink-100 text-ink-900 rounded-bl-sm"
                      }`}>
                        <p className="text-sm leading-relaxed break-words">{m.conteudo}</p>
                        <p className={`text-[10px] mt-1 ${minha ? "text-white/70" : "text-ink-400"}`}>
                          {dataRelativa(m.criadaEm)}{minha && m.lida ? " · lida" : ""}
                        </p>
                      </div>
                    </div>
                  );
                })}
                <div ref={endRef} />
              </div>

              {/* Input de mensagem */}
              <div className="p-4 border-t border-ink-100">
                <div className="flex gap-3">
                  <input
                    ref={inputRef}
                    className="flex-1 rounded-2xl border border-ink-200 bg-ink-50 px-4 py-2.5 text-sm focus:border-accent focus:bg-white focus:outline-none focus:ring-2 focus:ring-accent/20"
                    placeholder="Escreva uma mensagem..."
                    value={texto}
                    onChange={e => setTexto(e.target.value)}
                    onKeyDown={e => { if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); handleEnviar(); } }}
                    maxLength={1000}
                  />
                  <Button variant="accent" onClick={handleEnviar} disabled={enviando || !texto.trim()}>
                    {enviando ? "..." : "Enviar"}
                  </Button>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
