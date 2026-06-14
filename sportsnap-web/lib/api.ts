export type Atleta = { id: number; nome: string; email: string };
export type CartaResumo = {
  atletaId: number;
  overall: number;
  ultimaSincronizacao: string | null;
  sincronizada: boolean;
};
export type Spot = { id: number; nome: string; latitude: number; longitude: number; descricao?: string };
export type Sessao = {
  id: number;
  spotId: number;
  periodoInicio: string;
  periodoFim: string;
  descricao: string;
  cancelada?: boolean;
};
export type Fotografo = { id: number; nome: string; email: string };
export type Lote = {
  id: number;
  fotografoId: number;
  sessaoId: number;
  spotId: number;
  descricao: string;
  criadoEm: string;
  arquivado: boolean;
};
export type Foto = {
  id: number;
  loteId: number;
  urlPreview: string;
  urlOriginal: string;
  exifTimestamp: string;
  exifDetalhes: string;
  licenciada: boolean;
  removida: boolean;
};
export type Licenca = {
  id: number;
  atletaId: number;
  fotoId: number;
  preco: number;
  adquiridaEm: string;
  cancelada: boolean;
};
