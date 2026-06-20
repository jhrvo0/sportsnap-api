package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.conexao.ConexaoServico;
import com.sportsnap.gamification.dominio.conexao.PedidoConexao;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoRepositorio;
import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.feed.ItemFeed;
import com.sportsnap.gamification.dominio.feed.TipoItemFeed;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.perfil.PerfilRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilServico;
import com.sportsnap.gamification.dominio.perfil.TipoConta;
import com.sportsnap.gamification.dominio.perfil.Visibilidade;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Steps compartilhados entre as features h09–h12. Centraliza as definições de setup e
 * ação que aparecem em múltiplos arquivos de feature, evitando DuplicateStepDefinitionException.
 * Também expõe estado compartilhado (ultimoItem, ultimoPedido) para as classes de steps
 * que precisam acessá-lo nas suas asserções.
 */
public class SocialComumSteps {

    @Autowired private PerfilServico perfilServico;
    @Autowired private PerfilRepositorio perfilRepositorio;
    @Autowired private ConexaoServico conexaoServico;
    @Autowired private PedidoConexaoRepositorio pedidoRepositorio;
    @Autowired private FeedServico feedServico;

    public ItemFeed ultimoItem;
    public PedidoConexao ultimoPedido;

    public PerfilId idPerfil(int usuarioId) {
        return perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow().getId();
    }

    @Dado("o Usuario {int} ja possui Perfil publico {string}")
    public void criarPerfilPublico(Integer usuarioId, String nome) {
        perfilServico.criar(new AtletaId(usuarioId), nome, TipoConta.ATLETA);
    }

    @Dado("o Usuario {int} ja possui Perfil privado {string}")
    public void criarPerfilPrivado(Integer usuarioId, String nome) {
        var p = perfilServico.criar(new AtletaId(usuarioId), nome, TipoConta.ATLETA);
        perfilServico.editar(p.getId(), new AtletaId(usuarioId), nome, null, null, null, Visibilidade.PRIVADA, null);
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

    @E("o Usuario {int} publicou um item no feed")
    public void publicarItemFeed(Integer usuarioId) {
        ultimoItem = feedServico.publicarItem(idPerfil(usuarioId), TipoItemFeed.FOTO, 1);
    }

    @Quando("o Usuario {int} curte o item do feed")
    public void curtir(Integer usuarioId) {
        feedServico.curtir(idPerfil(usuarioId), ultimoItem.getId());
    }

    @Quando("o Usuario {int} cancela o proprio pedido")
    public void cancelarPedido(Integer solicitanteId) {
        conexaoServico.cancelar(ultimoPedido.getId(), idPerfil(solicitanteId));
    }
}
