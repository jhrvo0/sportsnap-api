package com.sportsnap.gamification.bdd.steps;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.gamification.dominio.mensagem.Mensagem;
import com.sportsnap.gamification.dominio.mensagem.MensagemRepositorio;
import com.sportsnap.gamification.dominio.mensagem.MensagemServico;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class MensagemSteps {

    @Autowired private MensagemServico mensagemServico;
    @Autowired private MensagemRepositorio mensagemRepositorio;
    @Autowired private SocialComumSteps comumSteps;

    private Mensagem ultimaMensagem;
    private List<Mensagem> inbox;
    private Exception excecao;
    private int contador;

    @E("o Usuario {int} enviou mensagem para o Usuario {int} com texto {string}")
    public void enviarMensagem(Integer remetenteId, Integer destinatarioId, String texto) {
        ultimaMensagem = mensagemServico.enviar(
            comumSteps.idPerfil(remetenteId),
            comumSteps.idPerfil(destinatarioId),
            texto);
    }

    @Quando("o Usuario {int} envia mensagem para o Usuario {int} com texto {string}")
    public void enviarMensagemQuando(Integer remetenteId, Integer destinatarioId, String texto) {
        ultimaMensagem = mensagemServico.enviar(
            comumSteps.idPerfil(remetenteId),
            comumSteps.idPerfil(destinatarioId),
            texto);
    }

    @Quando("o Usuario {int} tenta enviar mensagem para si mesmo")
    public void tentarEnviarParaSiMesmo(Integer usuarioId) {
        try {
            var id = comumSteps.idPerfil(usuarioId);
            mensagemServico.enviar(id, id, "mensagem para si mesmo");
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o Usuario {int} marca a mensagem como lida")
    public void marcarComoLida(Integer usuarioId) {
        mensagemServico.marcarComoLida(ultimaMensagem.getId(), comumSteps.idPerfil(usuarioId));
    }

    @Quando("consulto mensagens nao lidas do Usuario {int}")
    public void consultarNaoLidas(Integer usuarioId) {
        contador = mensagemServico.contarNaoLidas(comumSteps.idPerfil(usuarioId));
    }

    @Quando("consulto o inbox do Usuario {int}")
    public void consultarInbox(Integer usuarioId) {
        inbox = mensagemServico.listarUltimasMensagensPorConversa(comumSteps.idPerfil(usuarioId));
    }

    @Entao("a conversa entre os Usuarios {int} e {int} tem {int} mensagem")
    public void conversaTemMensagem(Integer id1, Integer id2, Integer quantidade) {
        var conversa = mensagemServico.listarConversa(
            comumSteps.idPerfil(id1), comumSteps.idPerfil(id2));
        assertEquals(quantidade, conversa.size());
    }

    @Entao("a conversa entre os Usuarios {int} e {int} tem {int} mensagens")
    public void conversaTemMensagens(Integer id1, Integer id2, Integer quantidade) {
        conversaTemMensagem(id1, id2, quantidade);
    }

    @E("a mensagem e do remetente {int} para o destinatario {int}")
    public void mensagemRemetente(Integer remetenteId, Integer destinatarioId) {
        assertNotNull(ultimaMensagem);
        assertEquals(comumSteps.idPerfil(remetenteId), ultimaMensagem.getRemetenteId());
        assertEquals(comumSteps.idPerfil(destinatarioId), ultimaMensagem.getDestinatarioId());
    }

    @Entao("o contador de nao lidas e {int}")
    public void contadorNaoLidas(Integer esperado) {
        assertEquals(esperado, contador);
    }

    @Entao("o envio e rejeitado")
    public void envioRejeitado() {
        assertNotNull(excecao);
    }

    @Entao("o inbox tem {int} conversas")
    public void inboxTemConversas(Integer quantidade) {
        assertEquals(quantidade, inbox.size());
    }
}
