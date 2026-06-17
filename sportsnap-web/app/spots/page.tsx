"use client";

import { useEffect, useState } from "react";
import { db } from "@/lib/db";
import { type Spot } from "@/lib/api";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { Button } from "@/components/Button";
import { Input, Textarea } from "@/components/Input";
import { Alert } from "@/components/Alert";
import { DynamicMap } from "@/components/DynamicMap";
import { Modal } from "@/components/Modal";
import { listarSpots, criarSpot, atualizarSpot, removerSpot } from "@/lib/spots";

export default function SpotsPage() {
  const [spots, setSpots] = useState<Spot[]>([]);
  const [nome, setNome] = useState("");
  const [latitude, setLatitude] = useState(-23.5505);
  const [longitude, setLongitude] = useState(-46.6333);
  const [descricao, setDescricao] = useState("");
  
  const [editingSpot, setEditingSpot] = useState<Spot | null>(null);
  const [editNome, setEditNome] = useState("");
  const [editLat, setEditLat] = useState(-23.5505);
  const [editLong, setEditLong] = useState(-46.6333);
  const [editDesc, setEditDesc] = useState("");

  const [aviso, setAviso] = useState<string | null>(null);

  async function carregar() {
    try {
      const res = await listarSpots();
      setSpots(res);
      db.set("spots", res);
    } catch (e) {
      console.warn("Erro ao buscar spots da API, usando DB local:", e);
      setSpots(db.get("spots"));
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  async function cadastrar(e: React.FormEvent) {
    e.preventDefault();
    if (latitude < -90 || latitude > 90) {
      setAviso("Erro: Latitude deve estar entre -90 e 90.");
      return;
    }
    if (longitude < -180 || longitude > 180) {
      setAviso("Erro: Longitude deve estar entre -180 e 180.");
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
      setAviso("Spot cadastrado com sucesso globalmente.");
    } catch (err) {
      db.add("spots", payload);
      setAviso("Spot cadastrado localmente (offline).");
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
      setAviso("Erro: Latitude deve estar entre -90 e 90.");
      return;
    }
    if (editLong < -180 || editLong > 180) {
      setAviso("Erro: Longitude deve estar entre -180 e 180.");
      return;
    }
    const payload = {
      nome: editNome,
      latitude: editLat,
      longitude: editLong,
      descricao: editDesc
    };
    try {
      await atualizarSpot(editingSpot.id, payload);
      db.update("spots", editingSpot.id, payload);
      setAviso(`Spot #${editingSpot.id} atualizado globalmente.`);
    } catch (err) {
      db.update("spots", editingSpot.id, payload);
      setAviso(`Spot #${editingSpot.id} atualizado localmente.`);
    }
    setEditingSpot(null);
    carregar();
  }

  async function excluirSpot(id: number) {
    if (confirm("Deseja excluir este spot?")) {
      try {
        await removerSpot(id);
        db.delete("spots", id);
        setAviso(`Spot #${id} excluído globalmente.`);
      } catch (err) {
        db.delete("spots", id);
        setAviso(`Spot #${id} excluído localmente.`);
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
      />

      {aviso && <Alert tone="success" className="mb-6">{aviso}</Alert>}

      <div className="grid gap-8 lg:grid-cols-[1fr_2fr]">
        <div className="space-y-6">
          <Card title="Novo Spot">
            <form onSubmit={cadastrar} className="space-y-4">
              <Input 
                label="Nome do Local" 
                value={nome} 
                onChange={(e) => setNome(e.target.value)} 
                required 
              />
              
              <div>
                 <label className="mb-1 block text-[13px] font-medium text-ink-700">
                    Localização no Mapa
                 </label>
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

              <Textarea
                label="Descrição / Dicas"
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
                rows={3}
              />
              <div className="flex gap-2">
                <Button type="submit" className="flex-1" size="lg">
                  Cadastrar Spot
                </Button>
              </div>
            </form>
          </Card>
        </div>

        <Card title={`Spots Ativos (${spots.length})`}>
          {spots.length === 0 ? (
            <div className="py-16 text-center bg-ink-50 rounded-[2.5rem] border border-dashed border-ink-200">
               <span className="text-4xl opacity-20 mb-4 block">🗺️</span>
               <p className="text-sm text-ink-500">Nenhum spot configurado.</p>
            </div>
          ) : (
            <div className="grid gap-8 sm:grid-cols-2">
              {spots.map((s) => (
                <div key={s.id} className="surface flex flex-col justify-between rounded-[2.5rem] p-3 transition-all hover:border-accent/30 hover:shadow-xl group">
                  <div className="relative rounded-[2rem] overflow-hidden isolate">
                     <DynamicMap 
                       latitude={s.latitude} 
                       longitude={s.longitude} 
                       readOnly={true} 
                       height="160px" 
                     />
                     <div className="absolute top-3 right-3 flex gap-1 bg-white/80 backdrop-blur-md rounded-full p-1 shadow-sm opacity-0 group-hover:opacity-100 transition-opacity z-10">
                        <Button size="icon" variant="ghost" className="h-7 w-7 rounded-full bg-white hover:bg-ink-50" onClick={() => prepararEdicao(s)}>✏️</Button>
                        <Button size="icon" variant="ghost" className="h-7 w-7 rounded-full bg-white hover:bg-rose-50 text-rose-500" onClick={() => excluirSpot(s.id)}>🗑️</Button>
                     </div>
                  </div>
                  
                  <div className="p-5">
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-mono text-[10px] font-bold text-accent bg-accent/10 px-2 py-0.5 rounded-md">ID #{s.id}</span>
                    </div>
                    <h3 className="text-xl font-bold text-ink-900 mt-1">{s.nome}</h3>
                    <p className="text-[11px] font-medium text-ink-400 uppercase tracking-widest mt-1 font-mono">
                       LAT {s.latitude.toFixed(4)} / LNG {s.longitude.toFixed(4)}
                    </p>
                    {s.descricao && (
                      <p className="mt-4 text-[13px] text-ink-600 leading-relaxed bg-ink-50 p-3 rounded-2xl italic">
                         {s.descricao}
                      </p>
                    )}
                  </div>
                </div>
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
               <label className="mb-1 block text-[13px] font-medium text-ink-700">
                  Localização no Mapa
               </label>
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

            <Textarea
              label="Descrição / Dicas"
              value={editDesc}
              onChange={(e) => setEditDesc(e.target.value)}
              rows={3}
            />
            <div className="flex gap-2">
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
