"use client";

import { useState } from "react";
import { useSession } from "@/lib/session";
import { api } from "@/lib/api";

export default function UploadFotosPage() {
  const { userId } = useSession();
  const [sessionId, setSessionId] = useState("");
  const [spotId, setSpotId] = useState("");
  const [caminhos, setCaminhos] = useState("");
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState("");

  const handleUpload = async () => {
    if (!userId || !sessionId || !spotId || !caminhos) return;
    setLoading(true);
    setMsg("");
    try {
      const lote = await api.criarLote(userId, parseInt(sessionId), parseInt(spotId));
      const paths = caminhos.split("\n").map((p) => p.trim()).filter(Boolean);
      await api.uploadFotos(lote.id, paths);
      setMsg(`Lote #${lote.id} criado com ${paths.length} foto(s)!`);
      setSessionId("");
      setSpotId("");
      setCaminhos("");
    } catch {
      setMsg("Erro ao criar lote ou enviar fotos.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Upload de Fotos</h1>

      <div className="bg-dark-700 rounded-2xl p-8 border border-dark-600 max-w-lg space-y-5">
        <div>
          <label className="text-sm text-gray-400 uppercase tracking-wider">ID da Session</label>
          <input
            type="text"
            value={sessionId}
            onChange={(e) => setSessionId(e.target.value)}
            className="mt-2 w-full px-4 py-3 rounded-xl bg-dark-800 border border-dark-600 text-white placeholder-gray-500 focus:border-brand focus:outline-none"
          />
        </div>

        <div>
          <label className="text-sm text-gray-400 uppercase tracking-wider">ID do Spot</label>
          <input
            type="text"
            value={spotId}
            onChange={(e) => setSpotId(e.target.value)}
            className="mt-2 w-full px-4 py-3 rounded-xl bg-dark-800 border border-dark-600 text-white placeholder-gray-500 focus:border-brand focus:outline-none"
          />
        </div>

        <div>
          <label className="text-sm text-gray-400 uppercase tracking-wider">Caminhos das fotos (um por linha)</label>
          <textarea
            value={caminhos}
            onChange={(e) => setCaminhos(e.target.value)}
            placeholder={"foto1.jpg\nfoto2.jpg\nfoto3.jpg"}
            rows={4}
            className="mt-2 w-full px-4 py-3 rounded-xl bg-dark-800 border border-dark-600 text-white placeholder-gray-500 focus:border-brand focus:outline-none resize-none"
          />
        </div>

        <button
          onClick={handleUpload}
          disabled={loading || !sessionId || !spotId || !caminhos}
          className="w-full py-4 rounded-xl bg-brand text-dark-900 font-bold text-lg hover:bg-brand/90 disabled:opacity-40 transition-all"
        >
          {loading ? "Enviando..." : "Criar Lote e Upload"}
        </button>

        {msg && <p className="text-brand font-medium">{msg}</p>}
      </div>
    </div>
  );
}
