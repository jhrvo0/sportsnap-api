"use client";

import { useEffect, useState } from "react";
import { useSession } from "@/lib/session";
import { api } from "@/lib/api";

export default function FotosPage() {
  const { userId } = useSession();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [licencas, setLicencas] = useState<any[]>([]);

  useEffect(() => {
    if (!userId) return;
    api.listarLicencas(userId).then(setLicencas).catch(() => setLicencas([]));
  }, [userId]);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Fotos & Licencas</h1>

      {licencas.length === 0 ? (
        <div className="bg-dark-700 rounded-2xl p-8 border border-dark-600 text-center">
          <p className="text-gray-400">Nenhuma licenca adquirida. Faca check-in em uma sessao primeiro.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {licencas.map((lic, i) => (
            <div key={i} className="bg-dark-700 rounded-2xl p-6 border border-dark-600 hover:border-brand/30 transition-colors">
              <div className="flex justify-between items-start mb-4">
                <span className="text-sm text-gray-400">Licenca #{lic.id || i + 1}</span>
                <span className="text-brand font-bold">R$ {lic.preco || "29,90"}</span>
              </div>
              <div className="h-32 bg-dark-600 rounded-lg flex items-center justify-center mb-4">
                <span className="text-4xl">📸</span>
              </div>
              {lic.adquiridaEm && (
                <p className="text-xs text-gray-500">
                  Adquirida: {new Date(lic.adquiridaEm).toLocaleDateString("pt-BR")}
                </p>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
