"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import {
  listarLotes, criarLote, editarLote,
  arquivarLote, desarquivarLote, excluirLote,
  type LoteDto,
} from "@/lib/marketplace";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Input, Select } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";

// Spots e sessões vêm do session-service (8083); por ora usamos lista fixa
// igual ao seed do backend para manter consistência
const SPOTS_FIXOS = [{ id: 1, nome: "Praia de Maracaipe" }, { id: 2, nome: "Ibirapuera" }, { id: 3, nome: "Rezende" }];
const SESSOES_FIXAS = [{ id: 1, descricao: "Sessão Manhã" }, { id: 2, descricao: "Sessão Tarde" }, { id: 3, descricao: "Sessão Noite" }];

export default function LotesPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();

  const [lotes, setLotes] = useState<LoteDto[]>([]);
  const [spotId, setSpotId] = useState("");
  const [sessaoId, setSessaoId] = useState("");
  const [descricao, setDescricao] = useState("");
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [novaDescricao, setNovaDescricao] = useState("");
  const [aviso, setAviso] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
    if (sessao && sessao.role !== "fotografo") router.replace("/perfil");
  }, [sessao, carregando, router]);

  async function carregar() {
    if (!sessao) return;
    try {
      setLotes(await listarLotes(sessao.id));
    } catch {
      setErro("Não foi possível carregar os lotes. O backend está rodando?");
    }
  }

  useEffect(() => { if (sessao) carregar(); }, [sessao]);

  async function criar(e: React.FormEvent) {
    e.preventDefault();
    if (!sessao) return;
    setErro(null);
    try {
      await criarLote({
        fotografoId: sessao.id,
        sessaoId: parseInt(sessaoId),
        spotId: parseInt(spotId),
        descricao,
      });
      setAviso("Lote criado com sucesso.");
      setSpotId(""); setSessaoId(""); setDescricao("");
      carregar();
    } catch { setErro("Erro ao criar lote."); }
  }

  async function salvarEdicao(id: number) {
    if (!novaDescricao.trim()) return;
    setErro(null);
    try {
      await editarLote(id, novaDescricao.trim());
      setEditandoId(null);
      setAviso(`Título do lote #${id} atualizado.`);
      carregar();
    } catch { setErro("Erro ao editar lote."); }
  }

  async function arquivar(id: number) {
    try { await arquivarLote(id); setAviso(`Lote #${id} arquivado.`); carregar(); }
    catch { setErro("Erro ao arquivar lote."); }
  }

  async function desarquivar(id: number) {
    try { await desarquivarLote(id); setAviso(`Lote #${id} reativado.`); carregar(); }
    catch { setErro("Erro ao desarquivar lote."); }
  }

  async function excluir(id: number) {
    if (!confirm(`Excluir lote #${id}? Todas as fotos serão removidas.`)) return;
    try { await excluirLote(id); setAviso(`Lote #${id} excluído.`); carregar(); }
    catch { setErro("Erro ao excluir lote."); }
  }

  if (!sessao) return null;

  return (
    <div className="fade-up">
      <PageHeader eyebrow="Fotógrafo" title="Gestão de Álbuns" subtitle="Organize suas capturas em lotes vinculados a eventos reais." />

      {aviso && <Alert tone="success" className="mb-8">{aviso}</Alert>}
      {erro && <Alert tone="danger" className="mb-8">{erro}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_2fr]">
        <Card title="Criar Novo Lote">
          <form onSubmit={criar} className="space-y-5">
            <Select label="Local (Spot)" value={spotId} onChange={(e) => setSpotId(e.target.value)} required>
              <option value="">Selecione...</option>
              {SPOTS_FIXOS.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
            </Select>
            <Select label="Sessão de Treino" value={sessaoId} onChange={(e) => setSessaoId(e.target.value)} required>
              <option value="">Selecione...</option>
              {SESSOES_FIXAS.map((s) => <option key={s.id} value={s.id}>{s.descricao}</option>)}
            </Select>
            <Input label="Título do Álbum" value={descricao} onChange={(e) => setDescricao(e.target.value)} placeholder="Ex: Surf Matinal Stella Maris" required />
            <Button type="submit" className="w-full" size="lg">Gerar Lote</Button>
          </form>
        </Card>

        <Card title={`Seus Álbuns (${lotes.length})`}>
          {lotes.length === 0 ? (
            <div className="py-20 text-center bg-ink-50 rounded-[2rem] border border-dashed border-ink-200">
              <p className="text-sm text-ink-500">Nenhum lote criado ainda.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {lotes.map((l) => (
                <div key={l.id} className="surface p-6 rounded-[2rem] transition-all hover:border-accent/20">
                  {editandoId === l.id ? (
                    <div className="space-y-3">
                      <Input label="Novo título" value={novaDescricao} onChange={(e) => setNovaDescricao(e.target.value)} autoFocus />
                      <div className="flex gap-2">
                        <Button size="sm" onClick={() => salvarEdicao(l.id)}>Salvar</Button>
                        <Button size="sm" variant="ghost" onClick={() => setEditandoId(null)}>Cancelar</Button>
                      </div>
                    </div>
                  ) : (
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-5">
                        <div className="h-16 w-16 rounded-2xl bg-ink-900 flex items-center justify-center text-white font-black">{l.id}</div>
                        <div>
                          <div className="flex items-center gap-2">
                            <h3 className="font-bold text-ink-900">{l.descricao}</h3>
                            {l.arquivado && <Badge tone="warning">Arquivado</Badge>}
                          </div>
                          <p className="mt-1 text-[12px] text-ink-500">
                            Spot #{l.spotId} · {new Date(l.criadoEm).toLocaleDateString("pt-BR")}
                          </p>
                        </div>
                      </div>
                      <div className="flex gap-2">
                        {!l.arquivado && (
                          <Button variant="secondary" size="sm" onClick={() => router.push(`/upload?loteId=${l.id}`)}>
                            Subir Fotos
                          </Button>
                        )}
                        <Button variant="ghost" size="sm" onClick={() => { setEditandoId(l.id); setNovaDescricao(l.descricao); }}>
                          Editar
                        </Button>
                        {l.arquivado ? (
                          <Button variant="ghost" size="sm" className="text-emerald-600" onClick={() => desarquivar(l.id)}>Reativar</Button>
                        ) : (
                          <Button variant="ghost" size="sm" className="text-ink-400" onClick={() => arquivar(l.id)}>Arquivar</Button>
                        )}
                        <Button variant="ghost" size="sm" className="text-rose-500" onClick={() => excluir(l.id)}>Excluir</Button>
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
