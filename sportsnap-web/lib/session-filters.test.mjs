import assert from "node:assert/strict";
import test from "node:test";

import { filtrarSessoesPorPeriodo } from "./session-filters.mjs";

const sessoes = [
  {
    id: 1,
    spotId: 1,
    descricao: "Antes do periodo",
    periodoInicio: "2026-06-19T08:00:00",
    periodoFim: "2026-06-19T10:00:00",
  },
  {
    id: 2,
    spotId: 1,
    descricao: "Dentro do periodo",
    periodoInicio: "2026-06-20T10:00:00",
    periodoFim: "2026-06-20T12:00:00",
  },
  {
    id: 3,
    spotId: 2,
    descricao: "Sobrepoe fim do periodo",
    periodoInicio: "2026-06-21T22:00:00",
    periodoFim: "2026-06-22T02:00:00",
  },
  {
    id: 4,
    spotId: 2,
    descricao: "Depois do periodo",
    periodoInicio: "2026-06-23T08:00:00",
    periodoFim: "2026-06-23T10:00:00",
  },
];

test("filtra sessoes que se sobrepoem ao intervalo informado", () => {
  const filtradas = filtrarSessoesPorPeriodo(sessoes, {
    inicio: "2026-06-20",
    fim: "2026-06-21",
  });

  assert.deepEqual(filtradas.map((sessao) => sessao.id), [2, 3]);
});

test("mantem todas as sessoes quando o periodo nao foi preenchido", () => {
  const filtradas = filtrarSessoesPorPeriodo(sessoes, {
    inicio: "",
    fim: "",
  });

  assert.deepEqual(filtradas.map((sessao) => sessao.id), [1, 2, 3, 4]);
});
