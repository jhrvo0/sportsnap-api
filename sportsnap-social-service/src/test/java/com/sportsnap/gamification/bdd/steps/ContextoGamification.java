package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.atleta.AtletaRepositorio;
import com.sportsnap.gamification.dominio.bloqueio.BloqueioRepositorio;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.conexao.ConexaoRepositorio;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoRepositorio;
import com.sportsnap.gamification.dominio.feed.CurtidaRepositorio;
import com.sportsnap.gamification.dominio.feed.ItemFeedRepositorio;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilRepositorio;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.LicencaRepositorio;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import org.springframework.beans.factory.annotation.Autowired;

public class ContextoGamification {

    @Autowired private AtletaRepositorio atletaRepositorio;
    @Autowired private CartaOficialRepositorio cartaRepositorio;
    @Autowired private StatusPotencialRepositorio statusRepositorio;
    @Autowired private LicencaRepositorio licencaRepositorio;
    @Autowired private PerfilRepositorio perfilRepositorio;
    @Autowired private ConexaoRepositorio conexaoRepositorio;
    @Autowired private PedidoConexaoRepositorio pedidoRepositorio;
    @Autowired private BloqueioRepositorio bloqueioRepositorio;
    @Autowired private ItemFeedRepositorio itemFeedRepositorio;
    @Autowired private CurtidaRepositorio curtidaRepositorio;
    @Autowired private NotificacaoRepositorio notificacaoRepositorio;
    @Autowired private ColetorDeEventos coletorDeEventos;

    @Before
    public void limpar() {
        notificacaoRepositorio.limpar();
        curtidaRepositorio.limpar();
        itemFeedRepositorio.limpar();
        bloqueioRepositorio.limpar();
        pedidoRepositorio.limpar();
        conexaoRepositorio.limpar();
        perfilRepositorio.limpar();
        licencaRepositorio.limpar();
        statusRepositorio.limpar();
        cartaRepositorio.limpar();
        atletaRepositorio.limpar();
        coletorDeEventos.limpar();
    }

    @Dado("que o sistema de Gamification esta limpo")
    public void sistemaLimpo() {
        // noop — @Before cuida
    }
}
