"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import {
  arquivarLote,
  criarLote,
  desarquivarLote,
  editarLote,
  excluirLote,
  listarLotes,
  type LoteDto,
} from "@/lib/marketplace";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";
import { Button } from "@/components/Button";
import { Card } from "@/components/Card";
import { Input, Select } from "@/components/Input";
import { PageHeader } from "@/components/PageHeader";

const SPOTS_FIXOS = [
  { id: 1, nome: "Praia de Maracaipe" },
  { id: 2, nome: "Ibirapuera" },
  { id: 3, nome: "Rezende" },
];

const SESSOES_FIXAS = [
  { id: 1, descricao: "Sessao Manha" },
  { id: 2, descricao: "Sessao Tarde" },
  { id: 3, descricao: "Sessao Noite" },
];

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
      setErro("Nao foi possivel carregar os albuns. O backend esta rodando?");
    }
  }

  useEffect(() => {
    if (sessao) carregar();
  }, [sessao]);

  async function criar(event: React.FormEvent) {
    event.preventDefault();
    if (!sessao) return;
    setErro(null);
    try {
      await criarLote({
        fotografoId: sessao.id,
        sessaoId: parseInt(sessaoId, 10),
        spotId: parseInt(spotId, 10),
        descricao,
      });
      setAviso("Album criado com sucesso.");
      setSpotId("");
      setSessaoId("");
      setDescricao("");
      carregar();
    } catch {
      setErro("Erro ao criar album.");
    }
  }

  async function salvarEdicao(id: number) {
    if (!novaDescricao.trim()) return;
    setErro(null);
    try {
      await editarLote(id, novaDescricao.trim());
      setEditandoId(null);
      setAviso(`Titulo do album #${id} atualizado.`);
      carregar();
    } catch {
      setErro("Erro ao editar album.");
    }
  }

  async function arquivar(id: number) {
    try {
      await arquivarLote(id);
      setAviso(`Album #${id} arquivado.`);
      carregar();
    } catch {
      setErro("Erro ao arquivar album.");
    }
  }

  async function desarquivar(id: number) {
    try {
      await desarquivarLote(id);
      setAviso(`Album #${id} reativado.`);
      carregar();
    } catch {
      setErro("Erro ao desarquivar album.");
    }
  }

  async function excluir(id: number) {
    if (!confirm(`Excluir album #${id}? Todas as fotos serao removidas.`)) return;
    try {
      await excluirLote(id);
      setAviso(`Album #${id} excluido.`);
      carregar();
    } catch {
      setErro("Erro ao excluir album.");
    }
  }

  if (!sessao) return null;

  return (
    <div className="fade-up">
      <PageHeader eyebrow="Fotografo" title="Gestao de Albuns" subtitle="Organize suas capturas em albuns vinculados a eventos reais." />

      {aviso && <Alert tone="success" className="mb-8">{aviso}</Alert>}
      {erro && <Alert tone="danger" className="mb-8">{erro}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_2fr]">
        <Card title="Criar Novo Album">
          <form onSubmit={criar} className="space-y-5">
            <Select label="Local (Spot)" value={spotId} onChange={(e) => setSpotId(e.target.value)} required>
              <option value="">Selecione...</option>
              {SPOTS_FIXOS.map((spot) => (
                <option key={spot.id} value={spot.id}>{spot.nome}</option>
              ))}
            </Select>
            <Select label="Sessao de Treino" value={sessaoId} onChange={(e) => setSessaoId(e.target.value)} required>
              <option value="">Selecione...</option>
              {SESSOES_FIXAS.map((item) => (
                <option key={item.id} value={item.id}>{item.descricao}</option>
              ))}
            </Select>
            <Input label="Titulo do Album" value={descricao} onChange={(e) => setDescricao(e.target.value)} placeholder="Ex: Surf Matinal Stella Maris" required />
            <Button type="submit" className="w-full" size="lg">Criar Album</Button>
          </form>
        </Card>

        <Card title={`Seus Albuns (${lotes.length})`}>
          {lotes.length === 0 ? (
            <div className="rounded-[2rem] border border-dashed border-ink-200 bg-ink-50 py-20 text-center">
              <p className="text-sm text-ink-500">Nenhum album criado ainda.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {lotes.map((lote) => (
                <div key={lote.id} className="surface rounded-[2rem] p-6 transition-all hover:border-accent/20">
                  {editandoId === lote.id ? (
                    <div className="space-y-3">
                      <Input label="Novo titulo" value={novaDescricao} onChange={(e) => setNovaDescricao(e.target.value)} autoFocus />
                      <div className="flex gap-2">
                        <Button size="sm" onClick={() => salvarEdicao(lote.id)}>Salvar</Button>
                        <Button size="sm" variant="ghost" onClick={() => setEditandoId(null)}>Cancelar</Button>
                      </div>
                    </div>
                  ) : (
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-5">
                        <button
                          type="button"
                          onClick={() => router.push(`/lotes/${lote.id}`)}
                          className="flex h-16 w-16 items-center justify-center rounded-2xl bg-ink-900 font-black text-white transition-colors hover:bg-accent"
                        >
                          {lote.id}
                        </button>
                        <div>
                          <div className="flex items-center gap-2">
                            <button
                              type="button"
                              onClick={() => router.push(`/lotes/${lote.id}`)}
                              className="text-left font-bold text-ink-900 transition-colors hover:text-accent"
                            >
                              {lote.descricao}
                            </button>
                            {lote.arquivado && <Badge tone="warning">Arquivado</Badge>}
                          </div>
                          <p className="mt-1 text-[12px] text-ink-500">
                            Spot #{lote.spotId} - {new Date(lote.criadoEm).toLocaleDateString("pt-BR")}
                          </p>
                        </div>
                      </div>
                      <div className="flex gap-2">
                        {!lote.arquivado && (
                          <Button variant="secondary" size="sm" onClick={() => router.push(`/upload?loteId=${lote.id}`)}>
                            Subir Fotos
                          </Button>
                        )}
                        <Button variant="ghost" size="sm" onClick={() => { setEditandoId(lote.id); setNovaDescricao(lote.descricao); }}>
                          Editar
                        </Button>
                        {lote.arquivado ? (
                          <Button variant="ghost" size="sm" className="text-emerald-600" onClick={() => desarquivar(lote.id)}>Reativar</Button>
                        ) : (
                          <Button variant="ghost" size="sm" className="text-ink-400" onClick={() => arquivar(lote.id)}>Arquivar</Button>
                        )}
                        <Button variant="ghost" size="sm" className="text-rose-500" onClick={() => excluir(lote.id)}>Excluir</Button>
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
