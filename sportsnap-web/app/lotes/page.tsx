"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { db } from "@/lib/db";
import { useAuth } from "@/lib/auth";
import { type Lote, type Sessao, type Spot } from "@/lib/api";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Input, Select } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";

export default function LotesPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [spots, setSpots] = useState<Spot[]>([]);
  const [sessoes, setSessoes] = useState<Sessao[]>([]);
  
  const [spotId, setSpotId] = useState("");
  const [sessaoId, setSessaoId] = useState("");
  const [descricao, setDescricao] = useState("");
  
  const [aviso, setAviso] = useState<string | null>(null);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
    if (sessao && sessao.role !== "fotografo") router.replace("/perfil");
  }, [sessao, carregando, router]);

  function carregar() {
    if (!sessao) return;
    setLotes(db.filter("lotes", l => l.fotografoId === sessao.id));
    setSessoes(db.get("sessoes"));
    setSpots(db.get("spots"));
  }

  useEffect(() => {
    if (sessao) carregar();
  }, [sessao]);

  async function criar(e: React.FormEvent) {
    e.preventDefault();
    if (!sessao) return;
    
    db.add("lotes", {
      fotografoId: sessao.id,
      sessaoId: parseInt(sessaoId, 10),
      spotId: parseInt(spotId, 10),
      descricao,
      criadoEm: new Date().toISOString(),
      arquivado: false,
    });

    setAviso("Lote criado! Agora você pode fazer o upload das fotos.");
    setSpotId("");
    setSessaoId("");
    setDescricao("");
    carregar();
  }

  function arquivar(id: number) {
    db.update("lotes", id, { arquivado: true });
    setAviso(`Lote #${id} arquivado. Novas fotos não podem ser adicionadas.`);
    carregar();
  }

  if (!sessao) return null;

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Fotógrafo"
        title="Gestão de Álbuns"
        subtitle="Organize suas capturas em lotes vinculados a eventos reais."
      />

      {aviso && <Alert tone="success" className="mb-8">{aviso}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_2fr]">
        <Card title="Criar Novo Lote">
          <form onSubmit={criar} className="space-y-5">
            <Select
              label="Local (Spot)"
              value={spotId}
              onChange={(e) => setSpotId(e.target.value)}
              required
            >
              <option value="">Selecione...</option>
              {spots.map((s) => (
                <option key={s.id} value={s.id}>{s.nome}</option>
              ))}
            </Select>
            <Select
              label="Sessão de Treino"
              value={sessaoId}
              onChange={(e) => setSessaoId(e.target.value)}
              required
            >
              <option value="">Selecione...</option>
              {sessoes.map((s) => (
                <option key={s.id} value={s.id}>{s.descricao}</option>
              ))}
            </Select>
            <Input
              label="Título do Álbum"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              placeholder="Ex: Surf Matinal Stella Maris"
              required
            />
            <Button type="submit" className="w-full" size="lg">
              Gerar Lote
            </Button>
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
                <div key={l.id} className="surface flex items-center justify-between p-6 rounded-[2rem] transition-all hover:border-accent/20">
                  <div className="flex items-center gap-5">
                    <div className="h-16 w-16 rounded-2xl bg-ink-900 flex items-center justify-center text-white font-black">
                       {l.id}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="font-bold text-ink-900">{l.descricao}</h3>
                        {l.arquivado && <Badge tone="warning">Arquivado</Badge>}
                      </div>
                      <p className="mt-1 text-[12px] text-ink-500">
                        {spots.find(s => s.id === l.spotId)?.nome} · {new Date(l.criadoEm).toLocaleDateString("pt-BR")}
                      </p>
                    </div>
                  </div>
                  
                  <div className="flex gap-2">
                    <Button variant="secondary" size="sm" onClick={() => router.push(`/upload?loteId=${l.id}`)}>
                       Subir Fotos
                    </Button>
                    {!l.arquivado && (
                      <Button variant="ghost" size="sm" className="text-ink-400" onClick={() => arquivar(l.id)}>
                        Arquivar
                      </Button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
