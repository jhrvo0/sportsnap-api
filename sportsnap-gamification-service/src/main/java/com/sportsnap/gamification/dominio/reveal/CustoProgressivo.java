package com.sportsnap.gamification.dominio.reveal;

/**
 * Curva de custo crescente por faixas (RN13): cada dezena ja acumulada encarece
 * o proximo ponto, produzindo retornos decrescentes. Ex.: elevar de 8 para 9
 * custa 1; de 78 para 79 custa 8.
 */
public class CustoProgressivo implements CustoEvolucaoEstrategia {

    @Override
    public int custoParaElevarUmPonto(double valorAtual) {
        if (valorAtual < 0) {
            throw new IllegalArgumentException("O valor atual nao pode ser negativo");
        }
        return 1 + (int) (valorAtual / 10);
    }
}
