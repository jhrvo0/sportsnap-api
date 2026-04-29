package com.sportsnap.session.application;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.repositories.CheckInRepository;
import com.sportsnap.session.domain.repositories.SessionRepository;
import com.sportsnap.session.domain.usecases.ValidarCheckIn;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ValidarCheckInImpl implements ValidarCheckIn {

    private final SessionRepository sessionRepository;
    private final CheckInRepository checkInRepository;

    public ValidarCheckInImpl(SessionRepository sessionRepository,
                               CheckInRepository checkInRepository) {
        this.sessionRepository = sessionRepository;
        this.checkInRepository = checkInRepository;
    }

    @Override
    public void executar(Long atletaId, Long sessionId, Double latitude, Double longitude) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session nao encontrada: " + sessionId));

        LocalDateTime agora = LocalDateTime.now();

        if (agora.isBefore(session.getInicio()) || agora.isAfter(session.getFim())) {
            throw new IllegalStateException("Sess\u00e3o encerrada");
        }

        CheckIn checkIn = new CheckIn(atletaId, agora, latitude, longitude, session);
        checkInRepository.save(checkIn);
    }
}
