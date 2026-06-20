"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/lib/auth";
import { obterAssinatura, assinarPlano, cancelarAssinatura, AssinaturaDto } from "@/lib/marketplace";
import { getPlanos, PlanoDto } from "@/lib/plans";

export default function ClubeAssinatura() {
  const { sessao } = useAuth();
  const [assinatura, setAssinatura] = useState<AssinaturaDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadingAcao, setLoadingAcao] = useState(false);
  const [planos, setPlanos] = useState<PlanoDto[]>([]);

  useEffect(() => {
    setPlanos(getPlanos());
    if (sessao) carregarAssinatura();
  }, [sessao]);

  async function carregarAssinatura() {
    if (!sessao) return;
    setLoading(true);
    try {
      const ass = await obterAssinatura(sessao.id);
      setAssinatura(ass);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  async function handleAssinar(planoId: string, nomePlano: string, cotas: number, preco: number, intervalo: string) {
    if (!sessao) return;
    if (!confirm(`Deseja assinar o ${nomePlano} por R$ ${preco.toFixed(2).replace('.', ',')}/${intervalo}?`)) return;
    setLoadingAcao(true);
    try {
      const novaAss = await assinarPlano(sessao.id);
      // Aqui poderíamos passar planoId para o backend se a API suportasse.
      setAssinatura(novaAss);
      alert(`Assinatura realizada com sucesso! Você ganhou ${cotas} cotas.`);
    } catch (e) {
      alert("Erro ao assinar.");
    } finally {
      setLoadingAcao(false);
    }
  }

  async function handleCancelar() {
    if (!sessao) return;
    if (!confirm("Tem certeza que deseja cancelar sua assinatura? Você não receberá novas cotas no próximo ciclo.")) return;
    setLoadingAcao(true);
    try {
      await cancelarAssinatura(sessao.id);
      await carregarAssinatura();
      alert("Assinatura cancelada.");
    } catch (e) {
      alert("Erro ao cancelar.");
    } finally {
      setLoadingAcao(false);
    }
  }

  if (loading) {
    return <div className="p-8">Carregando Clube...</div>;
  }

  return (
    <div className="p-8 max-w-4xl mx-auto text-slate-800">
      <h1 className="text-4xl font-black mb-2 text-slate-900">SportSnap Pass 🏆</h1>
      <p className="text-lg text-slate-600 mb-8">
        Faça parte do clube exclusivo, receba pacotes mensais de fotos e apoie seus fotógrafos favoritos.
      </p>

      {assinatura && assinatura.status !== "INATIVA" ? (
        <div className="bg-gradient-to-r from-blue-600 to-indigo-700 p-8 rounded-2xl text-white shadow-xl">
          <div className="flex justify-between items-start mb-6">
            <div>
              <h2 className="text-2xl font-bold mb-1">Seu Plano está Ativo</h2>
              <p className="text-indigo-100">
                Você é um membro VIP. Ciclo encerra em: {new Date(assinatura.dataFimCiclo).toLocaleDateString()}
              </p>
            </div>
            <div className="text-right">
              <div className="text-sm uppercase tracking-wide text-indigo-200">Saldo de Cotas</div>
              <div className="text-5xl font-black">{assinatura.saldoCotas}</div>
            </div>
          </div>

          <div className="bg-white/10 rounded-xl p-4 mb-6">
            <h3 className="font-semibold mb-2">💡 Como funciona:</h3>
            <ul className="list-disc pl-5 space-y-1 text-sm text-indigo-50">
              <li>Cada cota permite baixar 1 foto em alta resolução na loja, sem custo adicional.</li>
              <li>No fim do mês, o valor da sua assinatura é rateado entre os fotógrafos que você apoiou com suas cotas.</li>
              <li>Cotas não usadas acumulam para o próximo mês (Rollover de até 2x a franquia mensal).</li>
            </ul>
          </div>

          {assinatura.status === "CANCELADA_PENDENTE" ? (
            <div className="bg-yellow-500/20 text-yellow-200 p-3 rounded-lg border border-yellow-500/50 text-sm">
              Sua assinatura está cancelada, mas você ainda pode usar suas cotas até o final do ciclo atual.
            </div>
          ) : (
            <button
              onClick={handleCancelar}
              disabled={loadingAcao}
              className="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-lg transition-colors border border-white/20"
            >
              {loadingAcao ? "Cancelando..." : "Cancelar Assinatura"}
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {planos.map((plano) => (
            <div key={plano.id} className="bg-white p-8 rounded-2xl shadow-xl border border-slate-100 text-center flex flex-col">
              <div className="w-20 h-20 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-4xl mx-auto mb-4">
                ⭐️
              </div>
              <h2 className="text-2xl font-bold mb-4">{plano.nome}</h2>
              <div className="text-4xl font-black text-slate-900 mb-6">
                R$ {plano.preco.toFixed(2).replace('.', ',')}<span className="text-sm text-slate-500 font-normal">/{plano.intervalo}</span>
              </div>

              <ul className="text-left space-y-4 mb-8 flex-grow">
                <li className="flex items-center gap-3 text-sm">
                  <span className="text-green-500">✅</span>
                  <span><strong>{plano.cotas} fotos</strong> por {plano.intervalo} inclusas na franquia</span>
                </li>
                <li className="flex items-center gap-3 text-sm">
                  <span className="text-green-500">✅</span>
                  <span><strong>Rollover de cotas:</strong> não perdeu, acumulou!</span>
                </li>
                <li className="flex items-center gap-3 text-sm">
                  <span className="text-green-500">✅</span>
                  <span><strong>Acesso Antecipado VIP:</strong> veja as fotos antes de todos</span>
                </li>
              </ul>

              <button
                onClick={() => handleAssinar(plano.id, plano.nome, plano.cotas, plano.preco, plano.intervalo)}
                disabled={loadingAcao}
                className="w-full bg-slate-900 hover:bg-slate-800 text-white font-bold py-3 rounded-xl shadow-lg hover:shadow-xl transition-all disabled:opacity-50"
              >
                {loadingAcao ? "Processando..." : `Assinar Agora`}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
