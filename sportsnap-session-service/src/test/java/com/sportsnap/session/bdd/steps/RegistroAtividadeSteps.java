package com.sportsnap.session.bdd.steps;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.RegistroDeAtividade;
import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.entities.Spot;
import com.sportsnap.session.domain.repositories.CheckInRepository;
import com.sportsnap.session.domain.repositories.RegistroDeAtividadeRepository;
import com.sportsnap.session.domain.repositories.SessionRepository;
import com.sportsnap.session.domain.repositories.SpotRepository;
import com.sportsnap.session.domain.usecases.RegistrarAtividade;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RegistroAtividadeSteps {

    @Autowired private SpotRepository spotRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private RegistroDeAtividadeRepository registroDeAtividadeRepository;
    @Autowired private RegistrarAtividade registrarAtividade;

    private Spot spot;
    private Session session;
    private CheckIn checkIn;

    @Before
    public void setUp() {
        registroDeAtividadeRepository.deleteAll();
        checkInRepository.deleteAll();
        sessionRepository.deleteAll();
        spotRepository.deleteAll();
    }

    @Dado("que o Atleta realizou check-in na sessao ativa")
    public void atletaRealizouCheckInNaSessaoAtiva() {
        spot = new Spot("Spot Teste", -8.0631, -34.8711, "Local esportivo");
        spot = spotRepository.save(spot);

        LocalDateTime agora = LocalDateTime.now();
        session = new Session(agora.minusHours(1), agora.plusHours(2), spot);
        session = sessionRepository.save(session);

        Long atletaId = 1L;
        checkIn = new CheckIn(atletaId, agora, spot.getLatitude(), spot.getLongitude(), session);
        checkIn = checkInRepository.save(checkIn);
    }

    @Quando("o Atleta registra atividade com distancia {int} e duracao {int} e intensidade {string}")
    public void atletaRegistraAtividade(Integer distancia, Integer duracao, String intensidade) {
        registrarAtividade.executar(checkIn.getId(), distancia.doubleValue(), duracao, intensidade);
    }

    @Entao("o RegistroDeAtividade e criado com sucesso")
    public void registroCriadoComSucesso() {
        List<RegistroDeAtividade> registros = registroDeAtividadeRepository.findByCheckInId(checkIn.getId());
        assertFalse(registros.isEmpty(), "RegistroDeAtividade deveria ter sido criado");
    }

    @E("o XP calculado e proporcional a distancia e intensidade")
    public void xpCalculadoProporcional() {
        List<RegistroDeAtividade> registros = registroDeAtividadeRepository.findByCheckInId(checkIn.getId());
        RegistroDeAtividade registro = registros.get(0);
        // distancia 5, intensidade alta (fator 3): XP = 5.0 * 3 = 15.0
        assertTrue(registro.getXpCalculado() > 0, "XP deveria ser maior que zero");
        assertEquals(15.0, registro.getXpCalculado(), 0.01);
    }

    @Entao("o XP calculado deve ser {int}")
    public void xpCalculadoDeveSer(Integer xpEsperado) {
        List<RegistroDeAtividade> registros = registroDeAtividadeRepository.findByCheckInId(checkIn.getId());
        RegistroDeAtividade registro = registros.get(0);
        assertEquals(xpEsperado.doubleValue(), registro.getXpCalculado(), 0.01);
    }

    @Entao("o RegistroDeAtividade esta vinculado ao CheckIn do Atleta")
    public void registroVinculadoAoCheckIn() {
        List<RegistroDeAtividade> registros = registroDeAtividadeRepository.findByCheckInId(checkIn.getId());
        assertFalse(registros.isEmpty(), "RegistroDeAtividade deveria ter sido criado");
        RegistroDeAtividade registro = registros.get(0);
        assertEquals(checkIn.getId(), registro.getCheckIn().getId());
    }
}
