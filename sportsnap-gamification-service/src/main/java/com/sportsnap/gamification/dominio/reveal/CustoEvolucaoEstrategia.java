package com.sportsnap.gamification.dominio.reveal;

/**
 * Estrategia (padrao Strategy) que define o custo, em pontos do orcamento, para
 * elevar um atributo em uma unidade a partir do seu valor atual (RN13). Permite
 * trocar a curva de custo crescente sem alterar o motor de alocacao.
 */
public interface CustoEvolucaoEstrategia {

    int custoParaElevarUmPonto(double valorAtual);
}
