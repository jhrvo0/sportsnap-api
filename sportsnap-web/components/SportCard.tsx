"use client";

import { Badge } from "./Badge";

type Props = {
  nome: string;
  overall: number;
  resistencia: number;
  velocidade: number;
  tecnica: number;
  explosao: number;
  imagemUrl?: string;
  className?: string;
};

export function SportCard({ 
  nome, 
  overall, 
  resistencia, 
  velocidade, 
  tecnica, 
  explosao, 
  imagemUrl,
  className = "" 
}: Props) {
  return (
    <div className={`relative overflow-hidden rounded-[3rem] bg-[#0a0a0c] p-8 shadow-2xl transition-all duration-500 hover:scale-[1.02] border border-white/5 ${className}`}>
      {/* Background Glow */}
      <div className="absolute -right-24 -top-24 h-72 w-72 rounded-full bg-accent opacity-10 blur-[100px]" />
      <div className="absolute -bottom-24 -left-24 h-72 w-72 rounded-full bg-violet-600 opacity-10 blur-[100px]" />

      <div className="relative flex flex-col gap-8">
        <div className="flex items-center justify-between">
          <div>
            <Badge tone="accent" className="mb-3 bg-white/10 text-white border-white/10">Carta Oficial</Badge>
            <h2 className="text-3xl font-black tracking-tight text-white leading-tight">{nome}</h2>
          </div>
          <div className="text-right">
            <div className="text-6xl font-black italic tracking-tighter text-accent leading-none">
              {overall.toFixed(1)}
            </div>
            <div className="text-[10px] font-black uppercase tracking-[0.25em] text-ink-500 mt-2">Overall</div>
          </div>
        </div>

        {/* Card Image Area */}
        <div className="aspect-[4/5] overflow-hidden rounded-[2.5rem] bg-gradient-to-br from-ink-900 to-black border border-white/5 shadow-inner">
          {imagemUrl ? (
            <img src={imagemUrl} alt={nome} className="h-full w-full object-cover grayscale hover:grayscale-0 transition-all duration-1000" />
          ) : (
            <div className="flex h-full w-full items-center justify-center opacity-10">
              <span className="text-9xl">👤</span>
            </div>
          )}
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-2 gap-x-8 gap-y-6 px-2 pb-2">
          <Stat label="Resistência" value={resistencia} />
          <Stat label="Velocidade" value={velocidade} />
          <Stat label="Técnica" value={tecnica} />
          <Stat label="Explosão" value={explosao} />
        </div>
      </div>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: number }) {
  const percent = Math.min(100, Math.max(0, value));
  return (
    <div className="space-y-2.5">
      <div className="flex items-center justify-between text-[10px] font-black uppercase tracking-[0.15em] text-ink-500">
        <span>{label}</span>
        <span className="text-white font-bold">{value.toFixed(0)}</span>
      </div>
      <div className="h-1 w-full overflow-hidden rounded-full bg-white/5">
        <div 
          className="h-full rounded-full bg-accent transition-all duration-[1500ms] ease-out" 
          style={{ width: `${percent}%` }}
        />
      </div>
    </div>
  );
}
