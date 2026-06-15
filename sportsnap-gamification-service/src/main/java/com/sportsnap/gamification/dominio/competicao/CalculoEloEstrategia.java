package com.sportsnap.gamification.dominio.competicao;

/**
 * Estrategia (padrao Strategy) de calculo da variacao de PR em um confronto.
 * Permite trocar a formula de pontuacao (ex.: Elo) sem alterar o servico.
 */
public interface CalculoEloEstrategia {

    /**
     * Variacao de PR (sempre positiva) a ser somada ao vencedor e subtraida do
     * perdedor, em funcao da diferenca de PR entre eles (RN31).
     */
    double variacao(double prVencedor, double prPerdedor);
}
