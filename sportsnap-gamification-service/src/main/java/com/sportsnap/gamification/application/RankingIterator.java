package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.entities.CartaOficial;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Pattern: Iterator
 *
 * Iterador customizado para percorrer o Ranking de atletas.
 * Permite navegar pelas cartas ordenadas por Overall sem expor
 * a estrutura interna da colecao.
 *
 * Adiciona logica de negocio ao iterar: filtra apenas cartas
 * que foram sincronizadas (possuem ultimaSincronizacao != null).
 */
public class RankingIterator implements Iterator<CartaOficial> {

    private final List<CartaOficial> cartas;
    private int posicaoAtual;
    private int ranking;

    public RankingIterator(List<CartaOficial> cartasOrdenadas) {
        // Filtra apenas cartas sincronizadas (regra: ranking so mostra cartas reveladas)
        this.cartas = cartasOrdenadas.stream()
                .filter(c -> c.getUltimaSincronizacao() != null)
                .toList();
        this.posicaoAtual = 0;
        this.ranking = 0;
    }

    @Override
    public boolean hasNext() {
        return posicaoAtual < cartas.size();
    }

    @Override
    public CartaOficial next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Nao ha mais atletas no ranking");
        }
        ranking++;
        return cartas.get(posicaoAtual++);
    }

    public int getPosicaoAtual() {
        return ranking;
    }

    public int getTotalNoRanking() {
        return cartas.size();
    }
}
