package com.sportsnap.gamification.dominio.ranking;

import com.sportsnap.gamification.dominio.carta.CartaOficial;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

public interface RankingIterador {
    boolean temProximo();
    CartaOficial proximo();
    int posicaoAtual();
}

class CartaRankingIterador implements RankingIterador {

    private final List<CartaOficial> cartas;
    private int posicao = 0;

    CartaRankingIterador(List<CartaOficial> cartas) {
        this.cartas = new ArrayList<>(cartas);
        this.cartas.sort(Comparator.comparingDouble(CartaOficial::getOverall).reversed());
    }

    @Override
    public boolean temProximo() {
        return posicao < cartas.size();
    }

    @Override
    public CartaOficial proximo() {
        if (!temProximo()) {
            throw new NoSuchElementException("Nao ha mais cartas no ranking");
        }
        return cartas.get(posicao++);
    }

    @Override
    public int posicaoAtual() {
        return posicao;
    }
}
