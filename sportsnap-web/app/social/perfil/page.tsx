"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import {
  obterPerfilPorUsuario, editarPerfil,
  type PerfilSocial,
} from "@/lib/social";
import { PageHeader } from "@/components/PageHeader";
import { Card } from "@/components/Card";
import { Button } from "@/components/Button";
import { Alert } from "@/components/Alert";
import { Input } from "@/components/Input";

const ESPORTES = ["Corrida", "Surf", "Skate", "Futebol", "Natação", "Ciclismo", "Musculação", "Outro"];

export default function PerfilSocialPage() {
  const { sessao, carregando } = useAuth();
  const router = useRouter();

  const [perfil, setPerfil] = useState<PerfilSocial | null>(null);
  const [nome, setNome] = useState("");
  const [bio, setBio] = useState("");
  const [esporte, setEsporte] = useState("");
  const [localidade, setLocalidade] = useState("");
  const [visibilidade, setVisibilidade] = useState<"PUBLICA" | "PRIVADA">("PUBLICA");
  const [salvando, setSalvando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (carregando) return;
    if (!sessao) { router.replace("/login"); return; }

    obterPerfilPorUsuario(sessao.id).then(p => {
      if (p) {
        setPerfil(p);
        setNome(p.nomeExibicao);
        setBio(p.bio ?? "");
        setEsporte(p.esporte ?? "");
        setLocalidade(p.localidade ?? "");
        setVisibilidade(p.visibilidade);
      } else {
        // Sem perfil social — redireciona para o cadastro criar um
        router.replace("/social");
      }
    });
  }, [sessao, carregando, router]);

  async function handleSalvar() {
    if (!sessao || !perfil?.id) return;
    setSalvando(true);
    setErro(null);
    try {
      const atualizado = await editarPerfil(perfil.id.id, sessao.id, {
        nomeExibicao: nome,
        bio: bio || undefined,
        esporte: esporte || undefined,
        localidade: localidade || undefined,
        visibilidade,
      });
      setPerfil(atualizado);
      setAviso("Perfil salvo com sucesso!");
      setTimeout(() => setAviso(null), 4000);
    } catch {
      setErro("Erro ao salvar perfil. Tente novamente.");
    } finally {
      setSalvando(false);
    }
  }

  if (carregando) return null;
  if (!sessao) return null;

  const bioLength = bio.length;

  return (
    <div className="fade-up">
      <PageHeader
        eyebrow="Social"
        title={perfil ? "Editar perfil" : "Criar perfil"}
        subtitle={perfil ? "Atualize suas informações públicas" : "Configure como você aparece na plataforma"}
      />

      <div className="mx-auto max-w-2xl px-6 py-10 space-y-6">
        {erro && <Alert tone="danger">{erro}</Alert>}
        {aviso && <Alert tone="success">{aviso}</Alert>}

        <Card>
          <div className="space-y-6">
            {/* Avatar preview */}
            <div className="flex items-center gap-4">
              <div className="grid h-16 w-16 place-items-center rounded-full bg-ink-900 text-2xl font-black text-white">
                {nome.charAt(0).toUpperCase() || "?"}
              </div>
              <div>
                <p className="font-bold text-ink-900">{nome || "Seu nome"}</p>
                <p className="text-sm text-ink-500">
                  {sessao.role === "atleta" ? "Atleta" : "Fotógrafo"} ·{" "}
                  {visibilidade === "PUBLICA" ? "Perfil público" : "Perfil privado"}
                </p>
              </div>
            </div>

            {/* Nome */}
            <div>
              <label className="mb-2 block text-sm font-semibold text-ink-700">Nome de exibição *</label>
              <input
                className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-ink-900 placeholder-ink-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                placeholder="Como você quer ser chamado"
                value={nome}
                onChange={e => setNome(e.target.value)}
                maxLength={100}
              />
            </div>

            {/* Bio */}
            <div>
              <div className="mb-2 flex items-center justify-between">
                <label className="text-sm font-semibold text-ink-700">Bio</label>
                <span className={`text-xs ${bioLength > 280 ? "text-danger" : "text-ink-400"}`}>
                  {bioLength}/300
                </span>
              </div>
              <textarea
                className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-ink-900 placeholder-ink-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20 resize-none"
                placeholder="Conte um pouco sobre você..."
                rows={3}
                value={bio}
                onChange={e => setBio(e.target.value)}
                maxLength={300}
              />
            </div>

            {/* Esporte */}
            <div>
              <label className="mb-2 block text-sm font-semibold text-ink-700">Esporte principal</label>
              <select
                className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-ink-900 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                value={esporte}
                onChange={e => setEsporte(e.target.value)}
              >
                <option value="">Selecione um esporte</option>
                {ESPORTES.map(e => (
                  <option key={e} value={e}>{e}</option>
                ))}
              </select>
            </div>

            {/* Localidade */}
            <div>
              <label className="mb-2 block text-sm font-semibold text-ink-700">Cidade / Estado</label>
              <input
                className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-ink-900 placeholder-ink-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
                placeholder="Ex: Recife, PE"
                value={localidade}
                onChange={e => setLocalidade(e.target.value)}
                maxLength={100}
              />
            </div>

            {/* Visibilidade */}
            <div>
              <label className="mb-3 block text-sm font-semibold text-ink-700">Visibilidade do perfil</label>
              <div className="grid grid-cols-2 gap-3">
                {(["PUBLICA", "PRIVADA"] as const).map(v => (
                  <button
                    key={v}
                    onClick={() => setVisibilidade(v)}
                    className={`rounded-2xl border-2 p-4 text-left transition-all ${
                      visibilidade === v
                        ? "border-accent bg-accent-50"
                        : "border-ink-200 bg-white hover:border-ink-300"
                    }`}
                  >
                    <p className="font-bold text-ink-900">{v === "PUBLICA" ? "🌐 Público" : "🔒 Privado"}</p>
                    <p className="mt-1 text-xs text-ink-500">
                      {v === "PUBLICA"
                        ? "Qualquer pessoa pode te seguir"
                        : "Você aprova quem te segue"}
                    </p>
                  </button>
                ))}
              </div>
            </div>

            <Button
              variant="accent"
              size="lg"
              onClick={handleSalvar}
              disabled={salvando || !nome.trim()}
              className="w-full"
            >
              {salvando ? "Salvando..." : "Salvar alterações"}
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
