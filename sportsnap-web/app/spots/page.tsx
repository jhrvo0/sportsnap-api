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

  function carregar() {
    setSpots(db.get("spots"));
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
    db.add("spots", {
      nome,
      latitude,
      longitude,
      descricao,
    });
    setNome("");
    setLatitude(-23.5505);
    setLongitude(-46.6333);
    setDescricao("");
    setAviso("Spot cadastrado com sucesso.");
    carregar();
  }

  function prepararEdicao(s: Spot) {
    setEditingSpot(s);
    setEditNome(s.nome);
    setEditLat(s.latitude);
    setEditLong(s.longitude);
    setEditDesc(s.descricao || "");
  }

  function salvarEdicao(e: React.FormEvent) {
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
    db.update("spots", editingSpot.id, {
      nome: editNome,
      latitude: editLat,
      longitude: editLong,
      descricao: editDesc
    });
    setEditingSpot(null);
    setAviso(`Spot #${editingSpot.id} atualizado.`);
    carregar();
  }

  function excluirSpot(id: number) {
    if (confirm("Deseja excluir este spot?")) {
      db.delete("spots", id);
      setAviso(`Spot #${id} excluído.`);
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
          <Card title={editingSpot ? "Editar Spot" : "Novo Spot"}>
            <form onSubmit={editingSpot ? salvarEdicao : cadastrar} className="space-y-4">
              <Input 
                label="Nome do Local" 
                value={editingSpot ? editNome : nome} 
                onChange={(e) => editingSpot ? setEditNome(e.target.value) : setNome(e.target.value)} 
                required 
              />
              
              <div>
                 <label className="mb-1 block text-[13px] font-medium text-ink-700">
                    Localização no Mapa
                 </label>
                 <DynamicMap 
                   latitude={editingSpot ? editLat : latitude}
                   longitude={editingSpot ? editLong : longitude}
                   onChange={(lat, lng) => {
                      if (editingSpot) {
                         setEditLat(lat);
                         setEditLong(lng);
                      } else {
                         setLatitude(lat);
                         setLongitude(lng);
                      }
                   }}
                   onAddressFound={(addr) => {
                      // Optionally update the name if it's currently empty, or always update it
                      // Updating it always provides better feedback as requested
                      if (editingSpot) {
                         setEditNome(addr);
                      } else {
                         setNome(addr);
                      }
                   }}
                 />
              </div>

              <Textarea
                label="Descrição / Dicas"
                value={editingSpot ? editDesc : descricao}
                onChange={(e) => editingSpot ? setEditDesc(e.target.value) : setDescricao(e.target.value)}
                rows={3}
              />
              <div className="flex gap-2">
                <Button type="submit" className="flex-1" size="lg">
                  {editingSpot ? "Salvar Alterações" : "Cadastrar Spot"}
                </Button>
                {editingSpot && (
                  <Button type="button" variant="ghost" onClick={() => setEditingSpot(null)}>Cancelar</Button>
                )}
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
    </div>
  );
}
