package com.sportsnap.gamification.bdd.steps;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.gamification.dominio.notificacao.NotificacaoRepositorio;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoServico;
import com.sportsnap.gamification.dominio.notificacao.TipoNotificacao;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

public class NotificacaoSteps {

    @Autowired private NotificacaoServico notificacaoServico;
    @Autowired private NotificacaoRepositorio notificacaoRepositorio;
    @Autowired private SocialComumSteps comumSteps;

    @Quando("o Usuario {int} marca todas as notificacoes como lidas")
    public void marcarTodasComoLidas(Integer usuarioId) {
        notificacaoServico.marcarTodasComoLidas(comumSteps.idPerfil(usuarioId));
    }

    @Entao("o Usuario {int} tem {int} notificacao nao lida do tipo {word}")
    public void temNotificacaoNaoLida(Integer usuarioId, Integer quantidade, String tipo) {
        var notifs = notificacaoRepositorio.listarPorDestinatario(comumSteps.idPerfil(usuarioId)).stream()
            .filter(n -> !n.isLida() && n.getTipo().name().equals(tipo))
            .toList();
        assertEquals(quantidade, notifs.size());
    }

    @E("a notificacao tem {int} atores")
    public void notificacaoTemAtores(Integer numAtores) {
        var notif = notificacaoRepositorio.listarPorDestinatario(comumSteps.idPerfil(2)).stream()
            .filter(n -> n.getTipo() == TipoNotificacao.CURTIDA)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Nenhuma notificacao de CURTIDA encontrada para usuario 2"));
        assertEquals(numAtores, notif.getNumAtores());
    }

    @Entao("o contador de nao lidas do Usuario {int} e {int}")
    public void contadorNaoLidas(Integer usuarioId, Integer esperado) {
        assertEquals(esperado, notificacaoServico.contarNaoLidas(comumSteps.idPerfil(usuarioId)));
    }

    @Entao("o Usuario {int} nao tem notificacao de pedido de conexao")
    public void semNotificacaoPedido(Integer usuarioId) {
        var notifs = notificacaoRepositorio.listarPorDestinatario(comumSteps.idPerfil(usuarioId)).stream()
            .filter(n -> n.getTipo() == TipoNotificacao.PEDIDO_CONEXAO)
            .toList();
        assertTrue(notifs.isEmpty());
    }
}
