package com.sportsnap.gamification.infraestrutura.evento;

import com.sportsnap.gamification.dominio.conexao.ConexaoServico.ConexaoCriadaEvento;
import com.sportsnap.gamification.dominio.conexao.ConexaoServico.PedidoCanceladoEvento;
import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.feed.FeedServico.ItemCurtidoEvento;
import com.sportsnap.gamification.dominio.feed.TipoItemFeed;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoServico;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SocialEventoListener {

    private final FeedServico feedServico;
    private final NotificacaoServico notificacaoServico;

    public SocialEventoListener(FeedServico feedServico, NotificacaoServico notificacaoServico) {
        this.feedServico         = feedServico;
        this.notificacaoServico  = notificacaoServico;
    }

    @EventListener
    public void onConexaoCriada(ConexaoCriadaEvento evento) {
        var conexao = evento.getConexao();
        feedServico.publicarItem(conexao.getSeguidorId(), TipoItemFeed.NOVA_CONEXAO,
            conexao.getSeguidoId().getId());
        notificacaoServico.notificarNovoSeguidor(conexao.getSeguidoId(), conexao.getSeguidorId());
    }

    @EventListener
    public void onItemCurtido(ItemCurtidoEvento evento) {
        var curtida    = evento.getCurtida();
        var autorItemId = evento.getAutorItemId();
        if (!curtida.getUsuarioId().equals(autorItemId)) {
            notificacaoServico.notificarCurtida(autorItemId, curtida.getItemId());
        }
    }

    @EventListener
    public void onPedidoCancelado(PedidoCanceladoEvento evento) {
        notificacaoServico.removerNotificacaoPedido(evento.getPedidoId());
    }
}
