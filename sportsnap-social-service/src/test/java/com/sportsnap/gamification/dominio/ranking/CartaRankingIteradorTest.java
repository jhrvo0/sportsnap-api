package com.sportsnap.gamification.dominio.ranking;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

class CartaRankingIteradorTest {

    private CartaOficial criarCarta(AtletaId atletaId, double overall) {
        var atributos = List.of(
            new AtributoEsportivo("Velocidade", overall, 1.0, "CORRIDA")
        );
        return new CartaOficial(atletaId, atributos, overall, LocalDateTime.now());
    }

    @Test
    void iterarListaVazia() {
        var iterador = new CartaRankingIterador(new ArrayList<>());

        assertFalse(iterador.temProximo());
    }

    @Test
    void iterarUmaCarta() {
        var cartas = List.of(criarCarta(new AtletaId(1), 80.0));
        var iterador = new CartaRankingIterador(cartas);

        assertTrue(iterador.temProximo());
        var carta = iterador.proximo();
        assertEquals(80.0, carta.getOverall(), 0.01);
        assertFalse(iterador.temProximo());
    }

    @Test
    void iterarMultiplasCartasOrdenadas() {
        var cartas = List.of(
            criarCarta(new AtletaId(1), 70.0),
            criarCarta(new AtletaId(2), 90.0),
            criarCarta(new AtletaId(3), 80.0)
        );
        var iterador = new CartaRankingIterador(cartas);

        assertEquals(90.0, iterador.proximo().getOverall(), 0.01);
        assertEquals(80.0, iterador.proximo().getOverall(), 0.01);
        assertEquals(70.0, iterador.proximo().getOverall(), 0.01);
        assertFalse(iterador.temProximo());
    }

    @Test
    void proximoSemMaisCartas() {
        var cartas = List.of(criarCarta(new AtletaId(1), 80.0));
        var iterador = new CartaRankingIterador(cartas);

        iterador.proximo();

        assertThrows(NoSuchElementException.class, iterador::proximo);
    }

    @Test
    void posicaoAtualIncrementaCorretamente() {
        var cartas = List.of(
            criarCarta(new AtletaId(1), 70.0),
            criarCarta(new AtletaId(2), 90.0)
        );
        var iterador = new CartaRankingIterador(cartas);

        assertEquals(0, iterador.posicaoAtual());
        iterador.proximo();
        assertEquals(1, iterador.posicaoAtual());
        iterador.proximo();
        assertEquals(2, iterador.posicaoAtual());
    }
}
