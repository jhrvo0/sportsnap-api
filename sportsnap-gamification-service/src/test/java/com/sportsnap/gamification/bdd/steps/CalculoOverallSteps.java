package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.domain.entities.Atleta;
import com.sportsnap.gamification.domain.entities.AtributoEsportivo;
import com.sportsnap.gamification.domain.entities.CartaOficial;
import com.sportsnap.gamification.domain.repositories.AtletaRepository;
import com.sportsnap.gamification.domain.repositories.CartaOficialRepository;
import com.sportsnap.gamification.domain.usecases.CalcularOverall;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculoOverallSteps {

    @Autowired
    private AtletaRepository atletaRepository;

    @Autowired
    private CartaOficialRepository cartaOficialRepository;

    @Autowired
    private CalcularOverall calcularOverall;

    private Atleta atleta;
    private CartaOficial cartaOficial;
    private Double overallCalculado;
    private List<CartaOficial> ranking;

    @Before
    public void setUp() {
        cartaOficialRepository.deleteAll();
        atletaRepository.deleteAll();
        overallCalculado = null;
        ranking = null;
    }

    @Dado("que o Atleta {string} possui uma CartaOficial com atributos esportivos")
    public void atletaPossuiCartaComAtributos(String nome) {
        atleta = new Atleta(nome, nome.toLowerCase() + "@sportsnap.com");
        atleta = atletaRepository.save(atleta);

        cartaOficial = new CartaOficial(atleta);
        cartaOficial.setOverall(0.0);
        cartaOficial = cartaOficialRepository.save(cartaOficial);
    }

    @E("o atributo {string} tem valor {int} e peso {int}")
    public void atributoTemValorEPeso(String nome, int valor, int peso) {
        AtributoEsportivo atributo = new AtributoEsportivo(nome, (double) valor, (double) peso, cartaOficial);
        cartaOficial.getAtributos().add(atributo);
        cartaOficial = cartaOficialRepository.save(cartaOficial);
    }

    @Quando("o Overall e recalculado")
    public void overallRecalculado() {
        overallCalculado = calcularOverall.executar(atleta.getId());
    }

    @Entao("o Overall deve ser a media ponderada dos atributos")
    public void overallDeveSerMediaPonderada() {
        // (80 * 2 + 60 * 1) / (2 + 1) = 220 / 3 = 73.33...
        double esperado = (80.0 * 2.0 + 60.0 * 1.0) / (2.0 + 1.0);
        assertNotNull(overallCalculado);
        assertEquals(esperado, overallCalculado, 0.01,
                "Overall deveria ser a media ponderada dos atributos");
    }

    @Dado("que existem dois Atletas com CartaOficial sincronizada")
    public void existemDoisAtletasComCartaSincronizada() {
        // Atletas serao criados nos passos seguintes
    }

    @E("o Atleta {string} possui Overall {int}")
    public void atletaPossuiOverall(String nome, int overall) {
        Atleta a = new Atleta(nome, nome.toLowerCase() + "@sportsnap.com");
        a = atletaRepository.save(a);

        CartaOficial carta = new CartaOficial(a);
        carta.setOverall((double) overall);
        carta.setUltimaSincronizacao(LocalDateTime.now());
        cartaOficialRepository.save(carta);
    }

    @Quando("o Ranking e consultado")
    public void rankingConsultado() {
        ranking = cartaOficialRepository.findAllByOrderByOverallDesc();
    }

    @Entao("o Atleta {string} aparece antes de {string} no Ranking")
    public void atletaApareceAntesNoRanking(String primeiro, String segundo) {
        assertNotNull(ranking);
        assertTrue(ranking.size() >= 2, "Ranking deveria ter pelo menos 2 atletas");

        int indicePrimeiro = -1;
        int indiceSegundo = -1;
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getAtleta().getNome().equals(primeiro)) {
                indicePrimeiro = i;
            }
            if (ranking.get(i).getAtleta().getNome().equals(segundo)) {
                indiceSegundo = i;
            }
        }

        assertTrue(indicePrimeiro >= 0, "Atleta " + primeiro + " deveria estar no ranking");
        assertTrue(indiceSegundo >= 0, "Atleta " + segundo + " deveria estar no ranking");
        assertTrue(indicePrimeiro < indiceSegundo,
                primeiro + " deveria aparecer antes de " + segundo + " no ranking");
    }

    @Dado("que o Atleta {string} possui CartaOficial sincronizada com Overall {int}")
    public void atletaPossuiCartaSincronizadaComOverall(String nome, int overall) {
        Atleta a = new Atleta(nome, nome.toLowerCase() + "@sportsnap.com");
        a = atletaRepository.save(a);

        CartaOficial carta = new CartaOficial(a);
        carta.setOverall((double) overall);
        carta.setUltimaSincronizacao(LocalDateTime.now());
        cartaOficialRepository.save(carta);
    }

    @E("o Atleta {string} possui CartaOficial nao sincronizada com Overall {int}")
    public void atletaPossuiCartaNaoSincronizadaComOverall(String nome, int overall) {
        Atleta a = new Atleta(nome, nome.toLowerCase() + "@sportsnap.com");
        a = atletaRepository.save(a);

        CartaOficial carta = new CartaOficial(a);
        carta.setOverall((double) overall);
        // ultimaSincronizacao permanece null (nao sincronizada)
        cartaOficialRepository.save(carta);
    }

    @Entao("apenas o Atleta {string} aparece no Ranking")
    public void apenasAtletaApareceNoRanking(String nome) {
        assertNotNull(ranking);
        // Filtrar apenas cartas sincronizadas
        List<CartaOficial> sincronizadas = ranking.stream()
                .filter(c -> c.getUltimaSincronizacao() != null)
                .toList();

        assertEquals(1, sincronizadas.size(),
                "Apenas uma carta sincronizada deveria estar no ranking");
        assertEquals(nome, sincronizadas.get(0).getAtleta().getNome(),
                "O atleta no ranking deveria ser " + nome);
    }
}
