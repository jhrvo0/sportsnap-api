import assert from "node:assert/strict";
import test from "node:test";

import { fromDateTimeLocalValue, toDateTimeLocalValue } from "./session-datetime.mjs";

test("mantem horario local ao preparar payload da API", () => {
  assert.equal(fromDateTimeLocalValue("2026-06-21T09:30"), "2026-06-21T09:30:00");
});

test("mantem horario da API local ao abrir campo datetime-local", () => {
  assert.equal(toDateTimeLocalValue("2026-06-21T09:30:00"), "2026-06-21T09:30");
});

test("converte ISO com timezone para datetime-local do navegador", () => {
  const date = new Date("2026-06-21T12:30:00Z");
  const expected = [
    date.getFullYear(),
    "-",
    String(date.getMonth() + 1).padStart(2, "0"),
    "-",
    String(date.getDate()).padStart(2, "0"),
    "T",
    String(date.getHours()).padStart(2, "0"),
    ":",
    String(date.getMinutes()).padStart(2, "0"),
  ].join("");

  assert.equal(toDateTimeLocalValue("2026-06-21T12:30:00Z"), expected);
});
