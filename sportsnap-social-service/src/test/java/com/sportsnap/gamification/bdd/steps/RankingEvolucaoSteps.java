package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.ranking.RankingServico;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RankingEvolucaoSteps {

    @Autowired private CartaOficialRepositorio cartaRepositorio;
    @Autowired private RankingServico rankingServico;

    private final Map<Integer, List<AtributoEsportivo>> atributosPorAtleta = new HashMap<>();
    private double overallCalculado;
    private List<CartaOficial> ranking;
    private Optional<Integer> posicao;
    private List<CartaOficial> comparacao;

    @Dado("que o Atleta {int} possui atributo {string} valor {int} peso {int} esporte {string}")
    public void atletaPossuiAtributo(Integer atletaId, String nome, Integer valor, Integer peso,
                                      String esporte) {
        atributosPorAtleta.computeIfAbsent(atletaId, k -> new ArrayList<>())
            .add(new AtributoEsportivo(nome, valor.doubleValue(), peso.doubleValue(), esporte));
    }

    @E("o Atleta {int} possui atributo {string} valor {int} peso {int} esporte {string}")
    public void atletaPossuiAtributoE(Integer atletaId, String nome, Integer valor, Integer peso,
                                       String esporte) {
        atributosPorAtleta.computeIfAbsent(atletaId, k -> new ArrayList<>())
            .add(new AtributoEsportivo(nome, valor.doubleValue(), peso.doubleValue(), esporte));
    }

    @Quando("calculo o Overall do Atleta {int}")
    public void calculoOverall(Integer atletaId) {
        var atributos = atributosPorAtleta.get(atletaId);
        var carta = new CartaOficial(new AtletaId(atletaId), atributos);
        cartaRepositorio.salvar(carta);
        overallCalculado = rankingServico.calcularOverall(new AtletaId(atletaId));
    }

    @Entao("o Overall e {string}")
    public void overallEsperado(String valor) {
        assertEquals(Double.parseDouble(valor), overallCalculado, 0.01);
    }

    @Dado("que o Atleta {int} tem Carta sincronizada com Overall {int}")
    public void atletaCartaSincronizada(Integer atletaId, Integer overall) {
        var atributos = new ArrayList<AtributoEsportivo>();
        atributos.add(new AtributoEsportivo("Geral", overall.doubleValue(), 1.0, "corrida"));
        var carta = new CartaOficial(new AtletaId(atletaId), atributos,
            overall.doubleValue(), LocalDateTime.now());
        cartaRepositorio.salvar(carta);
    }

    @E("o Atleta {int} tem Carta sincronizada com Overall {int}")
    public void atletaCartaSincronizadaE(Integer atletaId, Integer overall) {
        atletaCartaSincronizada(atletaId, overall);
    }

    @Dado("que o Atleta {int} tem Carta nao sincronizada com Overall {int}")
    public void atletaCartaNaoSincronizada(Integer atletaId, Integer overall) {
        var atributos = new ArrayList<AtributoEsportivo>();
        atributos.add(new AtributoEsportivo("Geral", overall.doubleValue(), 1.0, "corrida"));
        var carta = new CartaOficial(new AtletaId(atletaId), atributos,
            overall.doubleValue(), null);
        cartaRepositorio.salvar(carta);
    }

    @E("o Atleta {int} tem Carta nao sincronizada com Overall {int}")
    public void atletaCartaNaoSincronizadaE(Integer atletaId, Integer overall) {
        atletaCartaNaoSincronizada(atletaId, overall);
    }

    @Quando("consulto o ranking global")
    public void consultoRankingGlobal() {
        ranking = rankingServico.consultarGlobal();
    }

    @Entao("o primeiro do ranking e o Atleta {int}")
    public void primeiroDoRanking(Integer atletaId) {
        assertFalse(ranking.isEmpty());
        assertEquals(atletaId.intValue(), ranking.get(0).getAtletaId().getId());
    }

    @E("o segundo do ranking e o Atleta {int}")
    public void segundoDoRanking(Integer atletaId) {
        assertTrue(ranking.size() >= 2);
        assertEquals(atletaId.intValue(), ranking.get(1).getAtletaId().getId());
    }

    @Entao("o ranking tem {int} atleta")
    public void rankingTemAtleta(Integer quantidade) {
        assertEquals(quantidade, ranking.size());
    }

    @Entao("o ranking tem {int} atletas")
    public void rankingTemAtletas(Integer quantidade) {
        assertEquals(quantidade, ranking.size());
    }

    @Quando("consulto a posicao do Atleta {int} no ranking")
    public void consultoPosicao(Integer atletaId) {
        posicao = rankingServico.consultarPosicao(new AtletaId(atletaId));
    }

    @Entao("a posicao e {int}")
    public void posicaoEsperada(Integer valor) {
        assertTrue(posicao.isPresent());
        assertEquals(valor, posicao.get());
    }

    @Entao("a posicao nao esta definida")
    public void posicaoIndefinida() {
        assertTrue(posicao.isEmpty());
    }

    @Quando("comparo as Cartas dos Atletas {int} e {int}")
    public void comparoCartas(Integer a, Integer b) {
        comparacao = rankingServico.compararCartas(new AtletaId(a), new AtletaId(b));
    }

    @Entao("a comparacao retorna {int} cartas")
    public void comparacaoRetornaCartas(Integer quantidade) {
        assertEquals(quantidade, comparacao.size());
    }
}
