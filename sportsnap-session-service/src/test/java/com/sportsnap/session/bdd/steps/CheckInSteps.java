package com.sportsnap.session.bdd.steps;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.entities.Spot;
import com.sportsnap.session.domain.repositories.CheckInRepository;
import com.sportsnap.session.domain.repositories.SessionRepository;
import com.sportsnap.session.domain.repositories.SpotRepository;
import com.sportsnap.session.domain.usecases.ValidarCheckIn;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Ent\u00e3o;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CheckInSteps {

    @Autowired private SpotRepository spotRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private ValidarCheckIn validarCheckIn;

    private Spot spot;
    private Session session;
    private Long atletaId;
    private Long atletaId2;
    private Exception excecaoCapturada;

    @Before
    public void setUp() {
        checkInRepository.deleteAll();
        sessionRepository.deleteAll();
        spotRepository.deleteAll();
        excecaoCapturada = null;
        atletaId = null;
        atletaId2 = null;
    }

    @Dado("que existe um Spot {string} com coordenadas v\u00e1lidas")
    public void existeSpotComCoordenadas(String nome) {
        spot = new Spot(nome, -8.0631, -34.8711, "Local esportivo em Recife");
        spot = spotRepository.save(spot);
    }

    @E("existe uma Session ativa neste Spot")
    public void existeSessionAtiva() {
        LocalDateTime agora = LocalDateTime.now();
        session = new Session(agora.minusHours(1), agora.plusHours(2), spot);
        session = sessionRepository.save(session);
    }

    @E("existe uma Session que j\u00e1 foi encerrada neste Spot")
    public void existeSessionEncerrada() {
        LocalDateTime ontem = LocalDateTime.now().minusDays(1);
        session = new Session(ontem.minusHours(2), ontem.minusHours(1), spot);
        session = sessionRepository.save(session);
    }

    @Quando("o Atleta {string} realiza o CheckIn com localiza\u00e7\u00e3o pr\u00f3xima ao Spot")
    public void atletaRealizaCheckIn(String nome) {
        if (atletaId == null) {
            atletaId = 1L;
            validarCheckIn.executar(atletaId, session.getId(), spot.getLatitude(), spot.getLongitude());
        } else {
            atletaId2 = 3L;
            validarCheckIn.executar(atletaId2, session.getId(), spot.getLatitude(), spot.getLongitude());
        }
    }

    @Quando("o Atleta {string} tenta realizar o CheckIn")
    public void atletaTentaRealizarCheckIn(String nome) {
        atletaId = 2L;
        try {
            validarCheckIn.executar(atletaId, session.getId(), spot.getLatitude(), spot.getLongitude());
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Ent\u00e3o("o CheckIn \u00e9 registrado com sucesso")
    public void checkInRegistradoComSucesso() {
        List<CheckIn> checkIns = checkInRepository.findByAtletaId(atletaId);
        assertFalse(checkIns.isEmpty(), "CheckIn deveria ter sido registrado");
    }

    @E("o hor\u00e1rio do CheckIn est\u00e1 dentro do intervalo da Session")
    public void horarioCheckInDentroDoIntervalo() {
        List<CheckIn> checkIns = checkInRepository.findByAtletaId(atletaId);
        CheckIn checkIn = checkIns.get(0);
        assertTrue(
                !checkIn.getHorario().isBefore(session.getInicio()) &&
                !checkIn.getHorario().isAfter(session.getFim()),
                "Horario do CheckIn deve estar dentro do intervalo da Session"
        );
    }

    @Ent\u00e3o("o CheckIn \u00e9 rejeitado")
    public void checkInRejeitado() {
        assertNotNull(excecaoCapturada, "Deveria ter lancado excecao");
    }

    @E("uma mensagem de erro {string} \u00e9 exibida")
    public void mensagemDeErroExibida(String mensagem) {
        assertNotNull(excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains(mensagem));
    }

    @Ent\u00e3o("ambos os CheckIns s\u00e3o registrados com sucesso")
    public void ambosCheckInsRegistrados() {
        List<CheckIn> checkIns1 = checkInRepository.findByAtletaId(atletaId);
        List<CheckIn> checkIns2 = checkInRepository.findByAtletaId(atletaId2);
        assertFalse(checkIns1.isEmpty());
        assertFalse(checkIns2.isEmpty());
    }
}
