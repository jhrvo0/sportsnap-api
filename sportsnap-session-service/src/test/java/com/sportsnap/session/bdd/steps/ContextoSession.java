package com.sportsnap.session.bdd.steps;

import com.sportsnap.session.dominio.atividade.RegistroAtividadeRepositorio;
import com.sportsnap.session.dominio.checkin.CheckInRepositorio;
import com.sportsnap.session.dominio.sessao.SessaoRepositorio;
import com.sportsnap.session.dominio.spot.SpotRepositorio;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ContextoSession {

    @Autowired private SpotRepositorio spotRepositorio;
    @Autowired private SessaoRepositorio sessaoRepositorio;
    @Autowired private CheckInRepositorio checkInRepositorio;
    @Autowired private RegistroAtividadeRepositorio registroAtividadeRepositorio;
    @Autowired private ColetorDeEventos coletorDeEventos;

    public static final List<Object> EVENTOS = new ArrayList<>();

    @Before
    public void limparAmbiente() {
        spotRepositorio.limpar();
        sessaoRepositorio.limpar();
        checkInRepositorio.limpar();
        registroAtividadeRepositorio.limpar();
        coletorDeEventos.limpar();
    }

    @Dado("que o sistema de Sessoes esta limpo")
    public void sistemaLimpo() {
        // o @Before ja limpa todos os repositorios
    }
}
