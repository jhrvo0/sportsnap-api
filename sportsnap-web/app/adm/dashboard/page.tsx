"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { getPlanos, adicionarPlano, removerPlano, PlanoDto } from "@/lib/plans";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Input, Select } from "@/components/Input";

export default function AdminDashboardPage() {
  const router = useRouter();
  const { sessao, logout } = useAuth();
  
  const [planos, setPlanos] = useState<PlanoDto[]>([]);
  const [nome, setNome] = useState("");
  const [preco, setPreco] = useState("");
  const [cotas, setCotas] = useState("");
  const [intervalo, setIntervalo] = useState("mês");

  useEffect(() => {
    if (!sessao || sessao.role !== "adm") {
      router.replace("/adm");
      return;
    }
    carregarPlanos();
  }, [sessao, router]);

  function carregarPlanos() {
    setPlanos(getPlanos());
  }

  function handleAddPlano(e: React.FormEvent) {
    e.preventDefault();
    if (!nome || !preco || !cotas) return;

    const novoPlano: PlanoDto = {
      id: "plano_" + Date.now(),
      nome,
      preco: parseFloat(preco),
      cotas: parseInt(cotas, 10),
      intervalo
    };

    adicionarPlano(novoPlano);
    setNome("");
    setPreco("");
    setCotas("");
    setIntervalo("mês");
    carregarPlanos();
  }

  function handleRemoverPlano(id: string) {
    if (confirm("Tem certeza que deseja remover este plano?")) {
      removerPlano(id);
      carregarPlanos();
    }
  }

  if (!sessao || sessao.role !== "adm") return null;

  return (
    <div className="p-8 max-w-5xl mx-auto text-slate-800">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-4xl font-black text-slate-900">Dashboard Administrativo</h1>
          <p className="text-lg text-slate-600">Gestão de Planos de Assinatura</p>
        </div>
        <Button onClick={() => { logout(); router.push("/adm"); }} tone="danger">
          Sair
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div className="md:col-span-1">
          <Card>
            <h2 className="text-xl font-bold mb-4">Adicionar Novo Plano</h2>
            <form onSubmit={handleAddPlano} className="space-y-4">
              <Input
                label="Nome do Plano"
                value={nome}
                onChange={e => setNome(e.target.value)}
                placeholder="Ex: SportSnap Pass Semestral"
                required
              />
              <Input
                label="Preço (R$)"
                type="number"
                step="0.01"
                value={preco}
                onChange={e => setPreco(e.target.value)}
                placeholder="Ex: 499.90"
                required
              />
              <Input
                label="Quantidade de Cotas"
                type="number"
                value={cotas}
                onChange={e => setCotas(e.target.value)}
                placeholder="Ex: 60"
                required
              />
              <Select
                label="Intervalo de Cobrança"
                value={intervalo}
                onChange={e => setIntervalo(e.target.value)}
              >
                <option value="mês">Mensal</option>
                <option value="quinzena">Quinzenal</option>
                <option value="semestre">Semestral</option>
                <option value="ano">Anual</option>
              </Select>
              
              <Button type="submit" className="w-full">
                Adicionar Plano
              </Button>
            </form>
          </Card>
        </div>

        <div className="md:col-span-2">
          <Card>
            <h2 className="text-xl font-bold mb-4">Planos Existentes</h2>
            
            {planos.length === 0 ? (
              <p className="text-slate-500">Nenhum plano cadastrado.</p>
            ) : (
              <div className="space-y-4">
                {planos.map(plano => (
                  <div key={plano.id} className="flex justify-between items-center p-4 border border-slate-200 rounded-xl bg-slate-50">
                    <div>
                      <h3 className="font-bold text-lg">{plano.nome}</h3>
                      <p className="text-slate-600 text-sm">
                        R$ {plano.preco.toFixed(2).replace('.', ',')} / {plano.intervalo} &bull; {plano.cotas} cotas
                      </p>
                    </div>
                    <Button 
                      tone="danger" 
                      onClick={() => handleRemoverPlano(plano.id)}
                    >
                      Remover
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
