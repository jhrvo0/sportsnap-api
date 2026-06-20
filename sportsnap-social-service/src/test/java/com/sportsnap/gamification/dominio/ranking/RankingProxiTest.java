package com.sportsnap.gamification.dominio.ranking;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.infraestrutura.memoria.CartaOficialRepositorioMemoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class RankingProxiTest {

    private final CartaOficialRepositorioMemoria cartaRepositorio = new CartaOficialRepositorioMemoria();
    private RankingServico servico;
    private RankingProxi proxi;

    @BeforeEach
    void setUp() {
        servico = new RankingServico(cartaRepositorio);
        proxi = new RankingProxi(servico);

        criarCartaSincronizada(new AtletaId(1), 80.0);
        criarCartaSincronizada(new AtletaId(2), 90.0);
    }

    private CartaOficial criarCartaSincronizada(AtletaId atletaId, double valor) {
        var atributos = List.of(
            new AtributoEsportivo("Velocidade", valor, 1.0, "CORRIDA")
        );
        return cartaRepositorio.salvar(new CartaOficial(atletaId, atributos, valor, LocalDateTime.now()));
    }

    @Test
    void primeiraConsultaBuscaDoServicoReal() {
        var resultado = proxi.consultarGlobal();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void segundaConsultaRetornaCache() {
        var primeira = proxi.consultarGlobal();
        var segunda = proxi.consultarGlobal();

        assertSame(primeira, segunda);
    }

    @Test
    void invalidarCacheForcaRecarga() {
        var primeira = proxi.consultarGlobal();
        proxi.invalidarCache();
        var segunda = proxi.consultarGlobal();

        assertNotSame(primeira, segunda);
        assertEquals(primeira.size(), segunda.size());
    }
}
