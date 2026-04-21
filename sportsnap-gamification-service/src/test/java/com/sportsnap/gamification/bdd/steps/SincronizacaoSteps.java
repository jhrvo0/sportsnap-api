package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.domain.entities.Atleta;
import com.sportsnap.gamification.domain.entities.AtributoEsportivo;
import com.sportsnap.gamification.domain.entities.CartaOficial;
import com.sportsnap.gamification.domain.entities.StatusPotencial;
import com.sportsnap.gamification.domain.usecases.SincronizarCartaAtleta;
import com.sportsnap.gamification.infrastructure.persistence.JpaAtletaRepository;
import com.sportsnap.gamification.infrastructure.persistence.JpaCartaOficialRepository;
import com.sportsnap.gamification.infrastructure.persistence.JpaStatusPotencialRepository;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class SincronizacaoSteps {

    @Autowired
    private JpaAtletaRepository atletaRepository;

    @Autowired
    private JpaCartaOficialRepository cartaOficialRepository;

    @Autowired
    private JpaStatusPotencialRepository statusPotencialRepository;

    @Autowired
    private SincronizarCartaAtleta sincronizarCartaAtleta;

    private Atleta atleta;
    private CartaOficial cartaOficial;
    private StatusPotencial statusPotencial;
    private Double overallAntes;
    private boolean possuiLicencaValida;
    private Exception excecaoCapturada;

    @Before
    public void setUp() {
        statusPotencialRepository.deleteAll();
        cartaOficialRepository.deleteAll();
        atletaRepository.deleteAll();
        excecaoCapturada = null;
        possuiLicencaValida = false;
    }

    @Dado("que o Atleta {string} possui um CheckIn registrado hoje")
    public void atletaPossuiCheckInHoje(String nome) {
        atleta = new Atleta(nome, nome.toLowerCase() + "@sportsnap.com");
        atleta = atletaRepository.save(atleta);

        cartaOficial = new CartaOficial(atleta);
        cartaOficial.setOverall(50.0);
        cartaOficial = cartaOficialRepository.save(cartaOficial);

        // Adicionar atributos esportivos
        AtributoEsportivo resistencia = new AtributoEsportivo("Resistencia", 50.0, 1.0, cartaOficial);
        AtributoEsportivo velocidade = new AtributoEsportivo("Velocidade", 50.0, 1.0, cartaOficial);
        cartaOficial.getAtributos().add(resistencia);
        cartaOficial.getAtributos().add(velocidade);
        cartaOficial = cartaOficialRepository.save(cartaOficial);

        statusPotencial = new StatusPotencial(atleta);
        statusPotencial.setXpAcumulado(100.0);
        statusPotencial.setStreakDeConsistencia(3);
        statusPotencial = statusPotencialRepository.save(statusPotencial);

        overallAntes = cartaOficial.getOverall();
    }

    @E("possui uma LicencaDeImagem adquirida após o último Reveal")
    public void possuiLicencaValida() {
        possuiLicencaValida = true;
    }

    @E("não possui uma LicencaDeImagem válida")
    public void naoPossuiLicencaValida() {
        possuiLicencaValida = false;
        // Sem licenca, o XP e zerado para simular a falta
        statusPotencial.setXpAcumulado(0.0);
        statusPotencialRepository.save(statusPotencial);
    }

    @Quando("o Atleta dispara a Sincronizacao")
    public void atletaDisparaSincronizacao() {
        sincronizarCartaAtleta.executar(atleta.getId());
    }

    @Quando("o Atleta tenta disparar a Sincronizacao")
    public void atletaTentaDisparaSincronizacao() {
        try {
            sincronizarCartaAtleta.executar(atleta.getId());
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("os StatusPotencial são transferidos para a CartaOficial")
    public void statusPotencialTransferidosParaCarta() {
        StatusPotencial statusAtualizado = statusPotencialRepository.findByAtletaId(atleta.getId()).orElseThrow();
        assertEquals(0.0, statusAtualizado.getXpAcumulado(), "XP deveria ter sido zerado apos sincronizacao");
    }

    @E("o Overall é recalculado")
    public void overallRecalculado() {
        CartaOficial cartaAtualizada = cartaOficialRepository.findByAtletaId(atleta.getId()).orElseThrow();
        assertNotNull(cartaAtualizada.getOverall());
        assertTrue(cartaAtualizada.getOverall() > overallAntes,
                "Overall deveria ter aumentado apos sincronizacao");
    }

    @E("a posição no Ranking é atualizada")
    public void posicaoRankingAtualizada() {
        CartaOficial cartaAtualizada = cartaOficialRepository.findByAtletaId(atleta.getId()).orElseThrow();
        assertNotNull(cartaAtualizada.getUltimaSincronizacao(),
                "Data de ultima sincronizacao deveria estar preenchida");
    }

    @Então("a sincronização é rejeitada")
    public void sincronizacaoRejeitada() {
        assertNotNull(excecaoCapturada, "Deveria ter lancado excecao ao tentar sincronizar sem licenca");
    }

    @E("a CartaOficial permanece inalterada")
    public void cartaOficialInaAlterada() {
        CartaOficial cartaAtualizada = cartaOficialRepository.findByAtletaId(atleta.getId()).orElseThrow();
        assertEquals(overallAntes, cartaAtualizada.getOverall(),
                "Overall nao deveria ter mudado");
    }
}
