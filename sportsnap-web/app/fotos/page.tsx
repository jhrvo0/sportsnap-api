"use client";

import { useEffect, useState } from "react";
import { db } from "@/lib/db";
import { type Foto } from "@/lib/api";
import { Card } from "@/components/Card";
import { PageHeader } from "@/components/PageHeader";
import { Badge } from "@/components/Badge";

export default function FotosPage() {
  const [fotos, setFotos] = useState<Foto[]>([]);

  function carregar() {
    setFotos(db.get("fotos"));
  }

  useEffect(() => {
    carregar();
  }, []);

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Catálogo"
        title="Fotos"
        subtitle={`Total no marketplace: ${fotos.length}`}
      />

      {fotos.length === 0 ? (
        <Card>
          <p className="text-sm text-ink-500">
            Nenhuma foto cadastrada. Fotógrafos podem subir fotos pela página{" "}
            <a href="/upload" className="font-medium text-accent">
              Upload
            </a>
            .
          </p>
        </Card>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {fotos.map((f) => (
            <article key={f.id} className="surface-elev overflow-hidden rounded-3xl">
              <div className="aspect-[4/3] bg-gradient-to-br from-purple-300 via-pink-300 to-rose-300 p-3 text-[10px] font-mono text-white">
                prévia #{f.id}
              </div>
              <div className="p-5">
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold text-ink-900">Foto #{f.id}</h3>
                  {f.licenciada ? (
                    <Badge tone="success">Licenciada</Badge>
                  ) : f.removida ? (
                    <Badge tone="warning">Removida</Badge>
                  ) : (
                    <Badge>Disponível</Badge>
                  )}
                </div>
                <p className="mt-1 text-[12px] text-ink-500">
                  Lote #{f.loteId} · {new Date(f.exifTimestamp).toLocaleString("pt-BR")}
                </p>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
