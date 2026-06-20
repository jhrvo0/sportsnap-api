package com.sportsnap.gamification.dominio.xp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CalculoXpEstrategiaTest {

    @Test
    void xpCorridaComDistanciaEDuracao() {
        var estrategia = new EstrategiaXpCorrida();
        double xp = estrategia.calcular(5.0, 3600);

        assertEquals(5.0 * 10.0 + (3600 / 60.0) * 0.5, xp, 0.01);
    }

    @Test
    void xpMusculacaoComDuracao() {
        var estrategia = new EstrategiaXpMusculacao();
        double xp = estrategia.calcular(0.0, 3600);

        assertEquals((3600 / 60.0) * 2.0, xp, 0.01);
    }

    @Test
    void xpCorridaComDistanciaZero() {
        var estrategia = new EstrategiaXpCorrida();
        double xp = estrategia.calcular(0.0, 3600);

        assertEquals((3600 / 60.0) * 0.5, xp, 0.01);
    }

    @Test
    void xpMusculacaoIgnoraDistancia() {
        var estrategia = new EstrategiaXpMusculacao();
        double xpComDistancia = estrategia.calcular(10.0, 3600);
        double xpSemDistancia = estrategia.calcular(0.0, 3600);

        assertEquals(xpSemDistancia, xpComDistancia, 0.01);
    }

    @Test
    void tipoEsporteCorreto() {
        assertEquals("CORRIDA", new EstrategiaXpCorrida().getTipoEsporte());
        assertEquals("MUSCULACAO", new EstrategiaXpMusculacao().getTipoEsporte());
    }
}
