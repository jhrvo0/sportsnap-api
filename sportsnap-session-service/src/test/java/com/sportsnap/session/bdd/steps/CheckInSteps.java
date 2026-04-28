package com.sportsnap.session.bdd.steps;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.entities.Spot;
import com.sportsnap.session.domain.usecases.ValidarCheckIn;
import com.sportsnap.session.infrastructure.persistence.JpaCheckInRepository;
import com.sportsnap.session.infrastructure.persistence.JpaSessionRepository;
import com.sportsnap.session.infrastructure.persistence.JpaSpotRepository;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CheckInSteps {

    @Autowired
    private JpaSpotRepository spotRepository;

    @Autowired
    private JpaSessionRepository sessionRepository;

    @Autowired
    private JpaCheckInRepository checkInRepository;

    @Autowired
    private ValidarCheckIn validarCheckIn;

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
    }

    @Dado("que existe um Spot {string} com coordenadas válidas")
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

    @E("existe uma Session que já foi encerrada neste Spot")
    public void existeSessionEncerrada() {
        LocalDateTime ontem = LocalDateTime.now().minusDays(1);
        session = new Session(ontem.minusHours(2), ontem.minusHours(1), spot);
        session = sessionRepository.save(session);
    }

    @Quando("o Atleta {string} realiza o CheckIn com localização próxima ao Spot")
    public void atletaRealizaCheckIn(String nome) {
        atletaId = 1L;
        validarCheckIn.executar(atletaId, session.getId(), spot.getLatitude(), spot.getLongitude());
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

    @Então("o CheckIn é registrado com sucesso")
    public void checkInRegistradoComSucesso() {
        List<CheckIn> checkIns = checkInRepository.findByAtletaId(atletaId);
        assertFalse(checkIns.isEmpty(), "CheckIn deveria ter sido registrado");
    }

    @E("o horário do CheckIn está dentro do intervalo da Session")
    public void horarioCheckInDentroDoIntervalo() {
        List<CheckIn> checkIns = checkInRepository.findByAtletaId(atletaId);
        CheckIn checkIn = checkIns.get(0);

        assertTrue(
                !checkIn.getHorario().isBefore(session.getInicio()) &&
                !checkIn.getHorario().isAfter(session.getFim()),
                "Horario do CheckIn deve estar dentro do intervalo da Session"
        );
    }

    @Então("o CheckIn é rejeitado")
    public void checkInRejeitado() {
        assertNotNull(excecaoCapturada, "Deveria ter lancado excecao ao tentar check-in em sessao encerrada");
    }

    @E("uma mensagem de erro {string} é exibida")
    public void mensagemDeErroExibida(String mensagem) {
        assertNotNull(excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains(mensagem),
                "Mensagem de erro deveria conter: " + mensagem);
    }

    @E("o Atleta {string} realiza o CheckIn com localização próxima ao Spot")
    public void outroAtletaRealizaCheckIn(String nome) {
        atletaId2 = 3L;
        validarCheckIn.executar(atletaId2, session.getId(), spot.getLatitude(), spot.getLongitude());
    }

    @Então("ambos os CheckIns são registrados com sucesso")
    public void ambosCheckInsRegistrados() {
        List<CheckIn> checkIns1 = checkInRepository.findByAtletaId(atletaId);
        List<CheckIn> checkIns2 = checkInRepository.findByAtletaId(atletaId2);
        assertFalse(checkIns1.isEmpty(), "CheckIn do primeiro atleta deveria existir");
        assertFalse(checkIns2.isEmpty(), "CheckIn do segundo atleta deveria existir");
    }
}
