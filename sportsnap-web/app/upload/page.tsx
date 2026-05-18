"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api, MARKETPLACE_BASE, type Foto, type Lote } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Select, Textarea } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";

export default function UploadPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [loteId, setLoteId] = useState("");
  const [caminhos, setCaminhos] = useState("");
  const [fotos, setFotos] = useState<Foto[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  useEffect(() => {
    if (!carregando && !sessao) router.replace("/login");
    if (sessao && sessao.role !== "fotografo") router.replace("/perfil");
  }, [sessao, carregando, router]);

  async function carregar() {
    if (!sessao || sessao.role !== "fotografo") return;
    try {
      const data = await api.get<Lote[]>(
        `${MARKETPLACE_BASE}/api/lotes?fotografoId=${sessao.id}`,
      );
      setLotes((data ?? []).filter((l) => !l.arquivado));
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  useEffect(() => {
    if (sessao) carregar();
  }, [sessao]);

  async function carregarFotosLote(lid: string) {
    if (!lid) {
      setFotos([]);
      return;
    }
    try {
      const data = await api.get<Foto[]>(`${MARKETPLACE_BASE}/api/fotos?loteId=${lid}`);
      setFotos(data ?? []);
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  async function enviar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setAviso(null);
    const lista = caminhos
      .split("\n")
      .map((s) => s.trim())
      .filter(Boolean);
    if (!loteId || lista.length === 0) {
      setErro("Escolha um lote e informe ao menos um caminho.");
      return;
    }
    try {
      const novas = await api.post<Foto[]>(`${MARKETPLACE_BASE}/api/fotos`, {
        loteId: parseInt(loteId, 10),
        caminhos: lista,
      });
      setAviso(`${novas.length} foto(s) enviadas. Metadados EXIF extraídos.`);
      setCaminhos("");
      await carregarFotosLote(loteId);
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  async function remover(id: number) {
    setErro(null);
    setAviso(null);
    try {
      await api.post(`${MARKETPLACE_BASE}/api/fotos/${id}/remover`, {});
      setAviso(`Foto #${id} removida.`);
      await carregarFotosLote(loteId);
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  if (!sessao) return null;

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Fotógrafo"
        title="Upload de Fotos"
        subtitle="Envie fotos para um lote ativo. O EXIF é extraído automaticamente."
      />

      {erro && <Alert tone="danger">{erro}</Alert>}
      {aviso && <Alert tone="success">{aviso}</Alert>}

      <div className="grid gap-6 lg:grid-cols-[1fr_2fr]">
        <Card title="Enviar fotos">
          <form onSubmit={enviar} className="space-y-4">
            <Select
              label="Lote ativo"
              value={loteId}
              onChange={(e) => {
                setLoteId(e.target.value);
                carregarFotosLote(e.target.value);
              }}
              required
            >
              <option value="">Selecione...</option>
              {lotes.map((l) => (
                <option key={l.id} value={l.id}>
                  #{l.id} – {l.descricao}
                </option>
              ))}
            </Select>
            <Textarea
              label="Caminhos das fotos"
              value={caminhos}
              onChange={(e) => setCaminhos(e.target.value)}
              placeholder={"/fotos/IMG_001.jpg\n/fotos/IMG_002.jpg\n/fotos/IMG_003.jpg"}
              hint="Um caminho por linha"
              rows={6}
            />
            <Button type="submit" className="w-full" size="lg">
              Enviar
            </Button>
          </form>
        </Card>

        <Card title={`Fotos do lote (${fotos.length})`}>
          {!loteId ? (
            <p className="text-sm text-ink-500">Selecione um lote para ver suas fotos.</p>
          ) : fotos.length === 0 ? (
            <p className="text-sm text-ink-500">Nenhuma foto neste lote.</p>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2">
              {fotos.map((f) => (
                <div
                  key={f.id}
                  className="overflow-hidden rounded-2xl border border-ink-100"
                >
                  <div className="aspect-video bg-gradient-to-br from-blue-300 to-emerald-300 p-3 text-[10px] font-mono text-white">
                    foto #{f.id}
                  </div>
                  <div className="p-3">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-semibold">#{f.id}</span>
                      {f.licenciada ? (
                        <Badge tone="success">Vendida</Badge>
                      ) : f.removida ? (
                        <Badge tone="warning">Removida</Badge>
                      ) : (
                        <Badge>Disponível</Badge>
                      )}
                    </div>
                    {!f.licenciada && !f.removida && (
                      <button
                        onClick={() => remover(f.id)}
                        className="mt-2 text-[11px] font-medium text-rose-500 hover:text-rose-600"
                      >
                        Remover
                      </button>
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
