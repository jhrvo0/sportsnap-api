package com.sportsnap.session.application;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.usecases.MotorDeMatchAutomatico;
import com.sportsnap.session.infrastructure.persistence.JpaCheckInRepository;
import com.sportsnap.session.infrastructure.persistence.JpaSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MotorDeMatchAutomaticoImpl implements MotorDeMatchAutomatico {

    private final JpaSessionRepository sessionRepository;
    private final JpaCheckInRepository checkInRepository;

    public MotorDeMatchAutomaticoImpl(JpaSessionRepository sessionRepository,
                                       JpaCheckInRepository checkInRepository) {
        this.sessionRepository = sessionRepository;
        this.checkInRepository = checkInRepository;
    }

    /**
     * RN02 — Regra do Match:
     * Retorna os IDs dos atletas que fizeram check-in dentro do intervalo da Session.
     * Esses IDs serao usados pelo Marketplace para cruzar com timestamps EXIF das fotos.
     */
    @Override
    public List<Long> executar(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session nao encontrada: " + sessionId));

        List<CheckIn> checkIns = checkInRepository.findBySessionId(sessionId);

        return checkIns.stream()
                .filter(checkIn ->
                        !checkIn.getHorario().isBefore(session.getInicio()) &&
                        !checkIn.getHorario().isAfter(session.getFim()))
                .map(CheckIn::getAtletaId)
                .distinct()
                .collect(Collectors.toList());
    }
}
