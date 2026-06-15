package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.atleta.AtletaRepositorio;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.competicao.ConfrontoRepositorio;
import com.sportsnap.gamification.dominio.competicao.PontuacaoRankingRepositorio;
import com.sportsnap.gamification.dominio.competicao.TemporadaRepositorio;
import com.sportsnap.gamification.dominio.desafio.DesafioRepositorio;
import com.sportsnap.gamification.dominio.desafio.InsigniaRepositorio;
import com.sportsnap.gamification.dominio.desafio.ProgressoDesafioRepositorio;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucaoRepositorio;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;
import com.sportsnap.gamification.dominio.reveal.RegistroSincronizacaoRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.LicencaRepositorio;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import org.springframework.beans.factory.annotation.Autowired;

public class ContextoGamification {

    @Autowired private AtletaRepositorio atletaRepositorio;
    @Autowired private CartaOficialRepositorio cartaRepositorio;
    @Autowired private StatusPotencialRepositorio statusRepositorio;
    @Autowired private LicencaRepositorio licencaRepositorio;
    @Autowired private RegistroEvolucaoRepositorio evolucaoRepositorio;
    @Autowired private RegistroSincronizacaoRepositorio sincronizacaoRepositorio;
    @Autowired private PontuacaoRankingRepositorio pontuacaoRepositorio;
    @Autowired private ConfrontoRepositorio confrontoRepositorio;
    @Autowired private TemporadaRepositorio temporadaRepositorio;
    @Autowired private DesafioRepositorio desafioRepositorio;
    @Autowired private ProgressoDesafioRepositorio progressoRepositorio;
    @Autowired private InsigniaRepositorio insigniaRepositorio;
    @Autowired private ColetorDeEventos coletorDeEventos;

    @Before
    public void limpar() {
        insigniaRepositorio.limpar();
        progressoRepositorio.limpar();
        desafioRepositorio.limpar();
        confrontoRepositorio.limpar();
        pontuacaoRepositorio.limpar();
        temporadaRepositorio.limpar();
        sincronizacaoRepositorio.limpar();
        evolucaoRepositorio.limpar();
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
