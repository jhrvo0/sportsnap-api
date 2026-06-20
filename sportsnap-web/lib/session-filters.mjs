function dateAtStartOfDay(value) {
  if (!value) return null;
  const date = new Date(`${value}T00:00:00`);
  return Number.isNaN(date.getTime()) ? null : date;
}

function dateAtEndOfDay(value) {
  if (!value) return null;
  const date = new Date(`${value}T23:59:59.999`);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function filtrarSessoesPorPeriodo(sessoes, periodo) {
  const inicio = dateAtStartOfDay(periodo.inicio);
  const fim = dateAtEndOfDay(periodo.fim);

  if (!inicio && !fim) return sessoes;
  if (inicio && fim && inicio > fim) return [];

  return sessoes.filter((sessao) => {
    const sessaoInicio = new Date(sessao.periodoInicio);
    const sessaoFim = new Date(sessao.periodoFim);

    if (Number.isNaN(sessaoInicio.getTime()) || Number.isNaN(sessaoFim.getTime())) {
      return false;
    }

    if (inicio && sessaoFim < inicio) return false;
    if (fim && sessaoInicio > fim) return false;
    return true;
  });
}
