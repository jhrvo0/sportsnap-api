package com.sportsnap.gamification.bdd.steps;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.gamification.dominio.conexao.ConexaoServico;
import com.sportsnap.gamification.dominio.feed.CurtidaRepositorio;
import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.feed.ItemFeed;
import com.sportsnap.gamification.dominio.feed.TipoItemFeed;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FeedSteps {

    @Autowired private ConexaoServico conexaoServico;
    @Autowired private FeedServico feedServico;
    @Autowired private CurtidaRepositorio curtidaRepositorio;
    @Autowired private SocialComumSteps comumSteps;

    private List<ItemFeed> feedResult;
    private double pontuacaoCalculada;

    @E("o Usuario {int} publicou um item recente no feed")
    public void publicarItemRecenteFeed(Integer usuarioId) {
        comumSteps.ultimoItem = feedServico.publicarItem(
            comumSteps.idPerfil(usuarioId), TipoItemFeed.FOTO, 1);
    }

    @E("o Usuario {int} curtiu o item do feed")
    public void curtirItemFeed(Integer usuarioId) {
        feedServico.curtir(comumSteps.idPerfil(usuarioId), comumSteps.ultimoItem.getId());
    }

    @Quando("o Usuario {int} consulta o feed pagina {int}")
    public void consultarFeed(Integer usuarioId, Integer pagina) {
        feedResult = feedServico.consultarFeed(comumSteps.idPerfil(usuarioId), pagina);
    }

    @Quando("o Usuario {int} curte o item do feed novamente")
    public void curtirNovamente(Integer usuarioId) {
        feedServico.curtir(comumSteps.idPerfil(usuarioId), comumSteps.ultimoItem.getId());
    }

    @Quando("o Usuario {int} descurte o item do feed")
    public void descurtir(Integer usuarioId) {
        feedServico.descurtir(comumSteps.idPerfil(usuarioId), comumSteps.ultimoItem.getId());
    }

    @Quando("consulto a pontuacao do item do Usuario {int} sem curtidas")
    public void consultarPontuacao(Integer usuarioId) {
        pontuacaoCalculada = comumSteps.ultimoItem.calcularPontuacao(0, 1.0);
    }

    @Entao("o feed do Usuario {int} esta vazio")
    public void feedVazio(Integer usuarioId) {
        assertTrue(feedResult == null || feedResult.isEmpty());
    }

    @Entao("o feed do Usuario {int} tem {int} item")
    public void feedTemItem(Integer usuarioId, Integer quantidade) {
        assertEquals(quantidade, feedResult.size());
    }

    @Entao("o numero de curtidas do item e {int}")
    public void numeroCurtidas(Integer esperado) {
        assertEquals(esperado, curtidaRepositorio.contarPorItem(comumSteps.ultimoItem.getId()));
    }

    @Entao("a pontuacao e maior que zero")
    public void pontuacaoMaiorQueZero() {
        assertTrue(pontuacaoCalculada > 0);
    }
}
