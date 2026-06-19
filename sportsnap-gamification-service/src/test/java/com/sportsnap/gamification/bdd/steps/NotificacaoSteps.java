package com.sportsnap.gamification.bdd.steps;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.conexao.ConexaoServico;
import com.sportsnap.gamification.dominio.conexao.PedidoConexao;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoRepositorio;
import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.feed.ItemFeed;
import com.sportsnap.gamification.dominio.feed.TipoItemFeed;
import com.sportsnap.gamification.dominio.notificacao.Notificacao;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoRepositorio;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoServico;
import com.sportsnap.gamification.dominio.notificacao.TipoNotificacao;
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

public class NotificacaoSteps {

    @Autowired private PerfilServico perfilServico;
    @Autowired private PerfilRepositorio perfilRepositorio;
    @Autowired private ConexaoServico conexaoServico;
    @Autowired private PedidoConexaoRepositorio pedidoRepositorio;
    @Autowired private FeedServico feedServico;
    @Autowired private NotificacaoServico notificacaoServico;
    @Autowired private NotificacaoRepositorio notificacaoRepositorio;

    private ItemFeed ultimoItem;
    private PedidoConexao ultimoPedido;

    private PerfilId idPerfil(int usuarioId) {
        return perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow().getId();
    }

    @Dado("o Usuario {int} ja possui Perfil publico {string}")
    public void criarPerfilPublico(Integer usuarioId, String nome) {
        perfilServico.criar(new AtletaId(usuarioId), nome, TipoConta.ATLETA);
    }

    @Dado("o Usuario {int} ja possui Perfil privado {string}")
    public void criarPerfilPrivado(Integer usuarioId, String nome) {
        var p = perfilServico.criar(new AtletaId(usuarioId), nome, TipoConta.ATLETA);
        perfilServico.editar(p.getId(), new AtletaId(usuarioId), nome, null, null, null, Visibilidade.PRIVADA);
    }

    @E("o Usuario {int} publicou um item no feed")
    public void publicarItem(Integer usuarioId) {
        ultimoItem = feedServico.publicarItem(idPerfil(usuarioId), TipoItemFeed.FOTO, 1);
    }

    @E("o Usuario {int} segue o Usuario {int}")
    public void seguir(Integer seguidorId, Integer seguidoId) {
        conexaoServico.seguir(idPerfil(seguidorId), idPerfil(seguidoId));
    }

    @E("o Usuario {int} enviou pedido para o Usuario {int}")
    public void enviarPedido(Integer solicitanteId, Integer alvoId) {
        conexaoServico.seguir(idPerfil(solicitanteId), idPerfil(alvoId));
        ultimoPedido = pedidoRepositorio.listarPendentesPorAlvo(idPerfil(alvoId)).get(0);
    }

    @Quando("o Usuario {int} curte o item do feed")
    public void curtir(Integer usuarioId) {
        feedServico.curtir(idPerfil(usuarioId), ultimoItem.getId());
    }

    @Quando("o Usuario {int} marca todas as notificacoes como lidas")
    public void marcarTodasComoLidas(Integer usuarioId) {
        notificacaoServico.marcarTodasComoLidas(idPerfil(usuarioId));
    }

    @Quando("o Usuario {int} cancela o proprio pedido")
    public void cancelarPedido(Integer solicitanteId) {
        conexaoServico.cancelar(ultimoPedido.getId(), idPerfil(solicitanteId));
    }

    @Entao("o Usuario {int} tem {int} notificacao nao lida do tipo {word}")
    public void temNotificacaoNaoLida(Integer usuarioId, Integer quantidade, String tipo) {
        var notifs = notificacaoRepositorio.listarPorDestinatario(idPerfil(usuarioId)).stream()
            .filter(n -> !n.isLida() && n.getTipo().name().equals(tipo))
            .toList();
        assertEquals(quantidade, notifs.size());
    }

    @E("a notificacao tem {int} atores")
    public void notificacaoTemAtores(Integer numAtores) {
        var ultimoDestinatario = notificacaoRepositorio.listarPorDestinatario(idPerfil(2)).stream()
            .filter(n -> n.getTipo() == TipoNotificacao.CURTIDA)
            .findFirst()
            .orElseThrow(() ->
                new AssertionError("Nenhuma notificacao de CURTIDA encontrada para o usuario 2"));
        assertEquals(numAtores, ultimoDestinatario.getNumAtores());
    }

    @E("o Usuario {int} tem {int} atores na notificacao de curtida")
    public void notificacaoAtores(Integer usuarioId, Integer numAtores) {
        var notif = notificacaoRepositorio.listarPorDestinatario(idPerfil(usuarioId)).stream()
            .filter(n -> n.getTipo() == TipoNotificacao.CURTIDA)
            .findFirst().orElseThrow();
        assertEquals(numAtores, notif.getNumAtores());
    }

    @Entao("o contador de nao lidas do Usuario {int} e {int}")
    public void contadorNaoLidas(Integer usuarioId, Integer esperado) {
        assertEquals(esperado, notificacaoServico.contarNaoLidas(idPerfil(usuarioId)));
    }

    @Entao("o Usuario {int} nao tem notificacao de pedido de conexao")
    public void semNotificacaoPedido(Integer usuarioId) {
        var notifs = notificacaoRepositorio.listarPorDestinatario(idPerfil(usuarioId)).stream()
            .filter(n -> n.getTipo() == TipoNotificacao.PEDIDO_CONEXAO)
            .toList();
        assertTrue(notifs.isEmpty());
    }
}
