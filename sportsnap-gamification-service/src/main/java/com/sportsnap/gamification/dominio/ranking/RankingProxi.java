package com.sportsnap.gamification.dominio.ranking;

import com.sportsnap.gamification.dominio.carta.CartaOficial;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class RankingProxi {

    private static final Duration TTL = Duration.ofMinutes(5);

    private final RankingServico servico;
    private List<CartaOficial> cache;
    private LocalDateTime ultimaAtualizacao;

    public RankingProxi(RankingServico servico) {
        this.servico = servico;
    }

    public List<CartaOficial> consultarGlobal() {
        if (cache == null || estaExpirado()) {
            cache = servico.consultarGlobal();
            ultimaAtualizacao = LocalDateTime.now();
        }
        return cache;
    }

    public RankingIterador criarIterador() {
        var cartas = consultarGlobal();
        return new CartaRankingIterador(cartas);
    }

    public void invalidarCache() {
        cache = null;
        ultimaAtualizacao = null;
    }

    private boolean estaExpirado() {
        return ultimaAtualizacao == null
            || LocalDateTime.now().isAfter(ultimaAtualizacao.plus(TTL));
    }
}
