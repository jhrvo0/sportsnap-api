package com.sportsnap.gamification.dominio.notificacao;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

import com.sportsnap.gamification.dominio.conexao.PedidoConexaoId;
import com.sportsnap.gamification.dominio.feed.ItemFeedId;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class NotificacaoServico {

    private final NotificacaoRepositorio repositorio;

    public NotificacaoServico(NotificacaoRepositorio repositorio) {
        notNull(repositorio, "O repositorio de Notificacao nao pode ser nulo");
        this.repositorio = repositorio;
    }

    public Notificacao notificarCurtida(PerfilId destinatarioId, ItemFeedId itemId) {
        notNull(destinatarioId, "O destinatarioId nao pode ser nulo");
        notNull(itemId,         "O itemId nao pode ser nulo");
        var existente = repositorio.obterPorTipoERef(
            destinatarioId, TipoNotificacao.CURTIDA, itemId.getId());
        if (existente.isPresent()) {
            var n = existente.get();
            n.incrementarAtores();
            return repositorio.salvar(n);
        }
        return repositorio.salvar(new Notificacao(destinatarioId, TipoNotificacao.CURTIDA, itemId.getId()));
    }

    public Notificacao notificarNovoSeguidor(PerfilId destinatarioId, PerfilId seguidorId) {
        notNull(destinatarioId, "O destinatarioId nao pode ser nulo");
        notNull(seguidorId,     "O seguidorId nao pode ser nulo");
        return repositorio.salvar(
            new Notificacao(destinatarioId, TipoNotificacao.NOVO_SEGUIDOR, seguidorId.getId()));
    }

    public Notificacao notificarPedidoConexao(PerfilId destinatarioId, PedidoConexaoId pedidoId) {
        notNull(destinatarioId, "O destinatarioId nao pode ser nulo");
        notNull(pedidoId,       "O pedidoId nao pode ser nulo");
        return repositorio.salvar(
            new Notificacao(destinatarioId, TipoNotificacao.PEDIDO_CONEXAO, pedidoId.getId()));
    }

    public void removerNotificacaoPedido(PedidoConexaoId pedidoId) {
        notNull(pedidoId, "O pedidoId nao pode ser nulo");
        repositorio.obterPorPedido(pedidoId).ifPresent(n -> repositorio.remover(n.getId()));
    }

    public void marcarComoLida(NotificacaoId id) {
        notNull(id, "O id da Notificacao nao pode ser nulo");
        repositorio.obter(id).ifPresent(n -> { n.marcarComoLida(); repositorio.salvar(n); });
    }

    public void marcarTodasComoLidas(PerfilId destinatarioId) {
        notNull(destinatarioId, "O destinatarioId nao pode ser nulo");
        repositorio.marcarTodasComoLidas(destinatarioId);
    }

    public int contarNaoLidas(PerfilId destinatarioId) {
        notNull(destinatarioId, "O destinatarioId nao pode ser nulo");
        return repositorio.contarNaoLidas(destinatarioId);
    }

    public List<Notificacao> listar(PerfilId destinatarioId) {
        notNull(destinatarioId, "O destinatarioId nao pode ser nulo");
        return repositorio.listarPorDestinatario(destinatarioId);
    }
}
