package com.sportsnap.session.dominio.atividade;

import java.util.List;

public record AnaliseEvolucao(
    int totalAtividades,
    double distanciaTotal,
    long tempoTotalSegundos,
    double ritmoMedioGeral,
    double melhorRitmo,
    double maiorDistancia,
    double frequenciaSemanal,
    List<PontoEvolucao> evolucaoDistancia,
    List<PontoEvolucao> evolucaoRitmo,
    List<RegistroAtividade> ultimosTreinos
) {}
