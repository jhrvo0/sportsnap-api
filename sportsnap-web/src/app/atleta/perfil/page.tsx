"use client";

import { useSession } from "@/lib/session";

export default function PerfilPage() {
  const { userName, userType, userId } = useSession();

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Perfil</h1>

      <div className="bg-dark-700 rounded-2xl p-8 border border-dark-600 max-w-md">
        <div className="w-20 h-20 rounded-full bg-brand/20 flex items-center justify-center text-3xl mb-6">
          👤
        </div>
        <div className="space-y-4">
          <div>
            <label className="text-sm text-gray-400 uppercase tracking-wider">Nome</label>
            <p className="text-xl font-semibold mt-1">{userName || "—"}</p>
          </div>
          <div>
            <label className="text-sm text-gray-400 uppercase tracking-wider">Tipo</label>
            <p className="mt-1">
              <span className="px-3 py-1 rounded-full bg-brand/10 text-brand text-sm border border-brand/20">
                {userType || "—"}
              </span>
            </p>
          </div>
          <div>
            <label className="text-sm text-gray-400 uppercase tracking-wider">ID</label>
            <p className="text-lg font-mono text-gray-300 mt-1">{userId || "—"}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
