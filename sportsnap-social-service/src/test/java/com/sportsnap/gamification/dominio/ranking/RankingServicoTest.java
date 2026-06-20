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

class RankingServicoTest {

    private final CartaOficialRepositorioMemoria cartaRepositorio = new CartaOficialRepositorioMemoria();
    private RankingServico servico;

    @BeforeEach
    void setUp() {
        servico = new RankingServico(cartaRepositorio);
    }

    private CartaOficial criarCartaSincronizada(AtletaId atletaId, double valor) {
        var atributos = List.of(
            new AtributoEsportivo("Velocidade", valor, 1.0, "CORRIDA"),
            new AtributoEsportivo("Forca", valor, 1.0, "MUSCULACAO")
        );
        return cartaRepositorio.salvar(new CartaOficial(atletaId, atributos, valor, LocalDateTime.now()));
    }

    @Test
    void calcularOverallDeAtletaExistente() {
        var atletaId = new AtletaId(1);
        criarCartaSincronizada(atletaId, 80.0);

        double overall = servico.calcularOverall(atletaId);

        assertEquals(80.0, overall, 0.01);
    }

    @Test
    void calcularOverallDeAtletaSemCarta() {
        var atletaId = new AtletaId(999);

        var erro = assertThrows(IllegalStateException.class, () -> servico.calcularOverall(atletaId));
        assertTrue(erro.getMessage().contains("CartaOficial nao encontrada"));
    }

    @Test
    void consultarGlobalOrdenadoPorOverall() {
        criarCartaSincronizada(new AtletaId(1), 70.0);
        criarCartaSincronizada(new AtletaId(2), 90.0);
        criarCartaSincronizada(new AtletaId(3), 80.0);

        var ranking = servico.consultarGlobal();

        assertEquals(3, ranking.size());
        assertEquals(90.0, ranking.get(0).getOverall(), 0.01);
        assertEquals(80.0, ranking.get(1).getOverall(), 0.01);
        assertEquals(70.0, ranking.get(2).getOverall(), 0.01);
    }

    @Test
    void consultarPorModalidadeFiltraEsporte() {
        criarCartaSincronizada(new AtletaId(1), 80.0);

        var ranking = servico.consultarPorModalidade("CORRIDA");

        assertFalse(ranking.isEmpty());
        assertTrue(ranking.stream().allMatch(c -> !c.filtrarAtributosPorEsporte("CORRIDA").isEmpty()));
    }

    @Test
    void consultarPosicaoDeAtleta() {
        criarCartaSincronizada(new AtletaId(1), 70.0);
        criarCartaSincronizada(new AtletaId(2), 90.0);

        var posicao = servico.consultarPosicao(new AtletaId(2));

        assertTrue(posicao.isPresent());
        assertEquals(1, posicao.get());
    }

    @Test
    void consultarPosicaoDeAtletaForaDoRanking() {
        var posicao = servico.consultarPosicao(new AtletaId(999));

        assertTrue(posicao.isEmpty());
    }

    @Test
    void compararDuasCartas() {
        var cartaA = criarCartaSincronizada(new AtletaId(1), 70.0);
        var cartaB = criarCartaSincronizada(new AtletaId(2), 90.0);

        var resultado = servico.compararCartas(new AtletaId(1), new AtletaId(2));

        assertEquals(2, resultado.size());
    }
}
