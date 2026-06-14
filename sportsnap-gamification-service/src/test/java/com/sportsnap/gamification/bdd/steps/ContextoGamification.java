package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.atleta.AtletaRepositorio;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
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
    @Autowired private ColetorDeEventos coletorDeEventos;

    @Before
    public void limpar() {
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
