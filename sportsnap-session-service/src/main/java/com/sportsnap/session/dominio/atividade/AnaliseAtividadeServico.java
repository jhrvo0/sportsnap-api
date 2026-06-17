package com.sportsnap.session.dominio.atividade;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.notBlank;

import com.sportsnap.session.dominio.atleta.AtletaId;
import java.time.LocalDateTime;
import java.util.List;

public class AnaliseAtividadeServico {

    private final RegistroAtividadeRepositorio repositorio;

    public AnaliseAtividadeServico(RegistroAtividadeRepositorio repositorio) {
        notNull(repositorio, "O repositorio de RegistroAtividade nao pode ser nulo");
        this.repositorio = repositorio;
    }

    public AnaliseEvolucao gerarAnalise(AtletaId atletaId, String esporte, int dias) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notBlank(esporte, "O esporte nao pode estar em branco");

        var agora = LocalDateTime.now();
        var inicio = agora.minusDays(dias);

        var atividades = repositorio.buscarPorAtletaEsporteEPeriodo(atletaId, esporte.toUpperCase().trim(), inicio, agora);

        var ordenadosPorDataAsc = atividades.stream()
            .sorted((a, b) -> a.getData().compareTo(b.getData()))
            .toList();

        var ultimosTreinos = atividades.stream()
            .sorted((a, b) -> b.getData().compareTo(a.getData()))
            .limit(10)
            .toList();

        int totalAtividades = atividades.size();
        double distanciaTotal = atividades.stream()
            .mapToDouble(RegistroAtividade::getDistancia)
            .sum();

        long tempoTotalSegundos = atividades.stream()
            .mapToLong(RegistroAtividade::getDuracaoSegundos)
            .sum();

        double ritmoMedioGeral = 0.0;
        if (distanciaTotal > 0) {
            ritmoMedioGeral = (tempoTotalSegundos / 60.0) / distanciaTotal;
        }

        double melhorRitmo = atividades.stream()
            .map(RegistroAtividade::getRitmoMedio)
            .filter(r -> r != null && r > 0)
            .mapToDouble(Double::doubleValue)
            .min()
            .orElse(0.0);

        double maiorDistancia = atividades.stream()
            .mapToDouble(RegistroAtividade::getDistancia)
            .max()
            .orElse(0.0);

        double semanas = dias / 7.0;
        double frequenciaSemanal = totalAtividades / semanas;

        List<PontoEvolucao> evolucaoDistancia = ordenadosPorDataAsc.stream()
            .map(a -> new PontoEvolucao(a.getData().toLocalDate(), a.getDistancia()))
            .toList();

        List<PontoEvolucao> evolucaoRitmo = ordenadosPorDataAsc.stream()
            .filter(a -> a.getRitmoMedio() != null && a.getRitmoMedio() > 0)
            .map(a -> new PontoEvolucao(a.getData().toLocalDate(), a.getRitmoMedio()))
            .toList();

        return new AnaliseEvolucao(
            totalAtividades,
            distanciaTotal,
            tempoTotalSegundos,
            ritmoMedioGeral,
            melhorRitmo,
            maiorDistancia,
            frequenciaSemanal,
            evolucaoDistancia,
            evolucaoRitmo,
            ultimosTreinos
        );
    }
}
