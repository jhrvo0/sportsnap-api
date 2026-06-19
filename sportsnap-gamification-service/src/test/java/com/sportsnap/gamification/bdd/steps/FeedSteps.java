package com.sportsnap.gamification.bdd.steps;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.bloqueio.BloqueioRepositorio;
import com.sportsnap.gamification.dominio.conexao.ConexaoRepositorio;
import com.sportsnap.gamification.dominio.conexao.ConexaoServico;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoRepositorio;
import com.sportsnap.gamification.dominio.feed.CurtidaRepositorio;
import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.feed.ItemFeed;
import com.sportsnap.gamification.dominio.feed.ItemFeedId;
import com.sportsnap.gamification.dominio.feed.ItemFeedRepositorio;
import com.sportsnap.gamification.dominio.feed.TipoItemFeed;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.perfil.PerfilRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilServico;
import com.sportsnap.gamification.dominio.perfil.TipoConta;
import com.sportsnap.gamification.dominio.perfil.Visibilidade;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FeedSteps {

    @Autowired private PerfilServico perfilServico;
    @Autowired private PerfilRepositorio perfilRepositorio;
    @Autowired private ConexaoServico conexaoServico;
    @Autowired private ConexaoRepositorio conexaoRepositorio;
    @Autowired private FeedServico feedServico;
    @Autowired private ItemFeedRepositorio itemFeedRepositorio;
    @Autowired private CurtidaRepositorio curtidaRepositorio;
    @Autowired private BloqueioRepositorio bloqueioRepositorio;

    private List<ItemFeed> feedResult;
    private ItemFeed ultimoItem;
    private double pontuacaoCalculada;

    private PerfilId idPerfil(int usuarioId) {
        return perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow().getId();
    }

    @Dado("o Usuario {int} ja possui Perfil publico {string}")
    public void criarPerfilPublico(Integer usuarioId, String nome) {
        perfilServico.criar(new AtletaId(usuarioId), nome, TipoConta.ATLETA);
    }

    @E("o Usuario {int} publicou um item no feed")
    public void publicarItemFeed(Integer usuarioId) {
        ultimoItem = feedServico.publicarItem(idPerfil(usuarioId), TipoItemFeed.FOTO, 1);
    }

    @E("o Usuario {int} publicou um item recente no feed")
    public void publicarItemRecenteFeed(Integer usuarioId) {
        ultimoItem = feedServico.publicarItem(idPerfil(usuarioId), TipoItemFeed.FOTO, 1);
    }

    @E("o Usuario {int} curtiu o item do feed")
    public void curtirItemFeed(Integer usuarioId) {
        feedServico.curtir(idPerfil(usuarioId), ultimoItem.getId());
    }

    @E("o Usuario {int} segue o Usuario {int}")
    public void seguir(Integer seguidorId, Integer seguidoId) {
        conexaoServico.seguir(idPerfil(seguidorId), idPerfil(seguidoId));
    }

    @Quando("o Usuario {int} consulta o feed pagina {int}")
    public void consultarFeed(Integer usuarioId, Integer pagina) {
        feedResult = feedServico.consultarFeed(idPerfil(usuarioId), pagina);
    }

    @Quando("o Usuario {int} curte o item do feed")
    public void curtir(Integer usuarioId) {
        feedServico.curtir(idPerfil(usuarioId), ultimoItem.getId());
    }

    @Quando("o Usuario {int} curte o item do feed novamente")
    public void curtirNovamente(Integer usuarioId) {
        feedServico.curtir(idPerfil(usuarioId), ultimoItem.getId());
    }

    @Quando("o Usuario {int} descurte o item do feed")
    public void descurtir(Integer usuarioId) {
        feedServico.descurtir(idPerfil(usuarioId), ultimoItem.getId());
    }

    @Quando("o Usuario {int} bloqueia o Usuario {int}")
    public void bloquear(Integer bloqueadorId, Integer bloqueadoId) {
        conexaoServico.bloquear(idPerfil(bloqueadorId), idPerfil(bloqueadoId));
    }

    @Quando("consulto a pontuacao do item do Usuario {int} sem curtidas")
    public void consultarPontuacao(Integer usuarioId) {
        pontuacaoCalculada = ultimoItem.calcularPontuacao(0, 1.0);
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
        assertEquals(esperado, curtidaRepositorio.contarPorItem(ultimoItem.getId()));
    }

    @Entao("a pontuacao e maior que zero")
    public void pontuacaoMaiorQueZero() {
        assertTrue(pontuacaoCalculada > 0);
    }
}
