"use client";

import { useEffect, useState } from "react";
import { db } from "@/lib/db";
import { type Spot } from "@/lib/api";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { Button } from "@/components/Button";
import { Input, Textarea } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { Badge } from "@/components/Badge";
import { DynamicMap } from "@/components/DynamicMap";
import { Modal } from "@/components/Modal";
import { EmptyState } from "@/components/EmptyState";
import { Loading } from "@/components/StateView";
import { listarSpots, criarSpot, atualizarSpot, removerSpot } from "@/lib/spots";

export default function SpotsPage() {
  const [spots, setSpots] = useState<Spot[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [nome, setNome] = useState("");
  const [latitude, setLatitude] = useState(-23.5505);
  const [longitude, setLongitude] = useState(-46.6333);
  const [descricao, setDescricao] = useState("");

  const [editingSpot, setEditingSpot] = useState<Spot | null>(null);
  const [editNome, setEditNome] = useState("");
  const [editLat, setEditLat] = useState(-23.5505);
  const [editLong, setEditLong] = useState(-46.6333);
  const [editDesc, setEditDesc] = useState("");

  const [aviso, setAviso] = useState<{ tone: "success" | "danger"; msg: string } | null>(null);

  async function carregar() {
    setCarregando(true);
    try {
      const res = await listarSpots();
      setSpots(res);
      db.set("spots", res);
    } catch (e) {
      console.warn("Erro ao buscar spots da API, usando DB local:", e);
      setSpots(db.get("spots"));
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  async function cadastrar(e: React.FormEvent) {
    e.preventDefault();
    if (latitude < -90 || latitude > 90) {
      setAviso({ tone: "danger", msg: "Erro: Latitude deve estar entre -90 e 90." });
      return;
    }
    if (longitude < -180 || longitude > 180) {
      setAviso({ tone: "danger", msg: "Erro: Longitude deve estar entre -180 e 180." });
      return;
    }
    const payload = {
      nome,
      latitude,
      longitude,
      descricao,
    };
    try {
      await criarSpot(payload);
      db.add("spots", payload);
      setAviso({ tone: "success", msg: "Spot cadastrado com sucesso globalmente." });
    } catch (err) {
      db.add("spots", payload);
      setAviso({ tone: "success", msg: "Spot cadastrado localmente (offline)." });
    }
    setNome("");
    setLatitude(-23.5505);
    setLongitude(-46.6333);
    setDescricao("");
    carregar();
  }

  function prepararEdicao(s: Spot) {
    setEditingSpot(s);
    setEditNome(s.nome);
    setEditLat(s.latitude);
    setEditLong(s.longitude);
    setEditDesc(s.descricao || "");
  }

  async function salvarEdicao(e: React.FormEvent) {
    e.preventDefault();
    if (!editingSpot) return;
    if (editLat < -90 || editLat > 90) {
      setAviso({ tone: "danger", msg: "Erro: Latitude deve estar entre -90 e 90." });
      return;
    }
    if (editLong < -180 || editLong > 180) {
      setAviso({ tone: "danger", msg: "Erro: Longitude deve estar entre -180 e 180." });
      return;
    }
    const payload = {
      nome: editNome,
      latitude: editLat,
      longitude: editLong,
      descricao: editDesc,
    };
    try {
      await atualizarSpot(editingSpot.id, payload);
      db.update("spots", editingSpot.id, payload);
      setAviso({ tone: "success", msg: `Spot #${editingSpot.id} atualizado globalmente.` });
    } catch (err) {
      db.update("spots", editingSpot.id, payload);
      setAviso({ tone: "success", msg: `Spot #${editingSpot.id} atualizado localmente.` });
    }
    setEditingSpot(null);
    carregar();
  }

  async function excluirSpot(id: number) {
    if (confirm("Deseja excluir este spot?")) {
      try {
        await removerSpot(id);
        db.delete("spots", id);
        setAviso({ tone: "success", msg: `Spot #${id} excluído globalmente.` });
      } catch (err) {
        db.delete("spots", id);
        setAviso({ tone: "success", msg: `Spot #${id} excluído localmente.` });
      }
      carregar();
    }
  }

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Admin"
        title="Gerenciar Spots"
        subtitle="Configure os locais geográficos onde os eventos acontecem."
      >
        <Badge tone="accent">{spots.length} cadastrados</Badge>
      </PageHeader>

      {aviso && (
        <Alert tone={aviso.tone} className="mb-6">
          {aviso.msg}
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.6fr)]">
        {/* Formulário de criação */}
        <Card title="Novo Spot" description="Cadastre um novo local de treino.">
          <form onSubmit={cadastrar} className="space-y-4">
            <Input
              label="Nome do Local"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="Ex.: Parque Ibirapuera"
              required
            />

            <div>
              <span className="mb-1.5 block text-[13px] font-medium text-ink-700">
                Localização no Mapa
              </span>
              <div className="overflow-hidden rounded-2xl border border-ink-200">
                <DynamicMap
                  latitude={latitude}
                  longitude={longitude}
                  onChange={(lat, lng) => {
                    setLatitude(lat);
                    setLongitude(lng);
                  }}
                  onAddressFound={(addr) => {
                    setNome(addr);
                  }}
                />
              </div>
            </div>

            <Textarea
              label="Descrição / Dicas"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              rows={3}
              placeholder="Informações úteis sobre o local..."
            />
            <Button type="submit" className="w-full" size="lg">
              Cadastrar Spot
            </Button>
          </form>
        </Card>

        {/* Listagem */}
        <Card
          title="Spots Ativos"
          description="Edite ou remova os locais cadastrados."
        >
          {carregando ? (
            <Loading message="Carregando spots..." />
          ) : spots.length === 0 ? (
            <EmptyState
              icon="🗺️"
              title="Nenhum spot configurado"
              description="Cadastre seu primeiro local usando o formulário ao lado para que as sessões possam ser associadas a ele."
            />
          ) : (
            <div className="card-grid">
              {spots.map((s) => (
                <article
                  key={s.id}
                  className="surface flex flex-col overflow-hidden rounded-3xl transition-shadow hover:shadow-soft-lg"
                >
                  <div className="relative isolate overflow-hidden">
                    <DynamicMap
                      latitude={s.latitude}
                      longitude={s.longitude}
                      readOnly={true}
                      height="140px"
                    />
                    <div className="absolute left-3 top-3">
                      <span className="rounded-md bg-white/90 px-2 py-0.5 font-mono text-[10px] font-black text-accent shadow-sm backdrop-blur">
                        ID #{s.id}
                      </span>
                    </div>
                  </div>

                  <div className="flex flex-1 flex-col p-5">
                    <h3 className="text-lg font-black tracking-tight text-ink-900">{s.nome}</h3>
                    <p className="mt-1 font-mono text-[10px] font-bold uppercase tracking-widest text-ink-400">
                      LAT {s.latitude.toFixed(4)} · LNG {s.longitude.toFixed(4)}
                    </p>
                    {s.descricao && (
                      <p className="mt-3 rounded-2xl bg-ink-50 p-3 text-[13px] italic leading-relaxed text-ink-600">
                        {s.descricao}
                      </p>
                    )}

                    <div className="mt-auto flex gap-2 pt-5">
                      <Button
                        variant="secondary"
                        size="sm"
                        className="flex-1"
                        onClick={() => prepararEdicao(s)}
                      >
                        ✏️ Editar
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="text-rose-600 hover:bg-rose-50"
                        onClick={() => excluirSpot(s.id)}
                      >
                        🗑️ Excluir
                      </Button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </Card>
      </div>

      <Modal isOpen={!!editingSpot} onClose={() => setEditingSpot(null)} title="Editar Spot">
        {editingSpot && (
          <form onSubmit={salvarEdicao} className="space-y-4">
            <Input
              label="Nome do Local"
              value={editNome}
              onChange={(e) => setEditNome(e.target.value)}
              required
            />

            <div>
              <span className="mb-1.5 block text-[13px] font-medium text-ink-700">
                Localização no Mapa
              </span>
              <div className="overflow-hidden rounded-2xl border border-ink-200">
                <DynamicMap
                  latitude={editLat}
                  longitude={editLong}
                  onChange={(lat, lng) => {
                    setEditLat(lat);
                    setEditLong(lng);
                  }}
                  onAddressFound={(addr) => {
                    setEditNome(addr);
                  }}
                />
              </div>
            </div>

            <Textarea
              label="Descrição / Dicas"
              value={editDesc}
              onChange={(e) => setEditDesc(e.target.value)}
              rows={3}
            />
            <div className="flex flex-col gap-2 sm:flex-row-reverse">
              <Button type="submit" className="flex-1" size="lg">
                Salvar Alterações
              </Button>
              <Button type="button" variant="ghost" onClick={() => setEditingSpot(null)}>
                Cancelar
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
}
