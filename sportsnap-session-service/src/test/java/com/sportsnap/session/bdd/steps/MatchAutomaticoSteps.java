package com.sportsnap.session.bdd.steps;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.entities.Spot;
import com.sportsnap.session.domain.repositories.CheckInRepository;
import com.sportsnap.session.domain.repositories.SessionRepository;
import com.sportsnap.session.domain.repositories.SpotRepository;
import com.sportsnap.session.domain.usecases.MotorDeMatchAutomatico;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchAutomaticoSteps {

    @Autowired private SpotRepository spotRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private MotorDeMatchAutomatico motorDeMatchAutomatico;

    private Session session;
    private Spot spot;
    private List<Long> resultadoMatch;

    @Before
    public void setUp() {
        checkInRepository.deleteAll();
        sessionRepository.deleteAll();
        spotRepository.deleteAll();
        resultadoMatch = null;
    }

    @Dado("que existe uma sessao ativa no Spot {string}")
    public void existeSessaoAtivaNoSpot(String nomeSpot) {
        spot = new Spot(nomeSpot, -8.0631, -34.8711, "Local esportivo");
        spot = spotRepository.save(spot);

        LocalDateTime agora = LocalDateTime.now();
        session = new Session(agora.minusHours(1), agora.plusHours(2), spot);
        session = sessionRepository.save(session);
    }

    @E("o Atleta com id {long} realizou check-in durante a sessao")
    public void atletaRealizouCheckInDuranteASessao(Long atletaId) {
        LocalDateTime agora = LocalDateTime.now();
        CheckIn checkIn = new CheckIn(atletaId, agora, spot.getLatitude(), spot.getLongitude(), session);
        checkInRepository.save(checkIn);
    }

    @E("nenhum atleta realizou check-in na sessao")
    public void nenhumAtletaRealizouCheckIn() {
        // Nenhum check-in foi adicionado, estado ja esta limpo pelo setUp
    }

    @Quando("o Motor de Match e executado para a sessao")
    public void motorDeMatchExecutado() {
        resultadoMatch = motorDeMatchAutomatico.executar(session.getId());
    }

    @Entao("o Atleta com id {long} aparece na lista de matches")
    public void atletaApareceNaListaDeMatches(Long atletaId) {
        assertNotNull(resultadoMatch);
        assertTrue(resultadoMatch.contains(atletaId),
                "Atleta com id " + atletaId + " deveria estar nos resultados de match");
    }

    @Entao("a lista de matches esta vazia")
    public void listaDeMatchesVazia() {
        assertNotNull(resultadoMatch);
        assertTrue(resultadoMatch.isEmpty(), "Nao deveria haver atletas nos resultados de match");
    }

    @Entao("ambos os atletas aparecem na lista de matches")
    public void ambosOsAtletasAparecemNaListaDeMatches() {
        assertNotNull(resultadoMatch);
        assertEquals(2, resultadoMatch.size());
        assertTrue(resultadoMatch.contains(1L));
        assertTrue(resultadoMatch.contains(2L));
    }
}
