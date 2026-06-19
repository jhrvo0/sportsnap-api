package com.sportsnap.gamification.dominio.notificacao;

import com.sportsnap.gamification.dominio.conexao.PedidoConexaoId;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepositorio {

    Notificacao salvar(Notificacao notificacao);

    void remover(NotificacaoId id);

    Optional<Notificacao> obter(NotificacaoId id);

    Optional<Notificacao> obterPorTipoERef(PerfilId destinatarioId, TipoNotificacao tipo, int referenciaId);

    Optional<Notificacao> obterPorPedido(PedidoConexaoId pedidoId);

    List<Notificacao> listarPorDestinatario(PerfilId destinatarioId);

    int contarNaoLidas(PerfilId destinatarioId);

    void marcarTodasComoLidas(PerfilId destinatarioId);

    void limpar();
}

