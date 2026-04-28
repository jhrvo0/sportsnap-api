package com.sportsnap.session.application;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.usecases.ValidarCheckIn;
import com.sportsnap.session.infrastructure.persistence.JpaCheckInRepository;
import com.sportsnap.session.infrastructure.persistence.JpaSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ValidarCheckInImpl implements ValidarCheckIn {

    private final JpaSessionRepository sessionRepository;
    private final JpaCheckInRepository checkInRepository;

    public ValidarCheckInImpl(JpaSessionRepository sessionRepository,
                               JpaCheckInRepository checkInRepository) {
        this.sessionRepository = sessionRepository;
        this.checkInRepository = checkInRepository;
    }

    @Override
    @Transactional
    public void executar(Long atletaId, Long sessionId, Double latitude, Double longitude) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session nao encontrada: " + sessionId));

        LocalDateTime agora = LocalDateTime.now();

        // Validar se a sessao esta ativa
        if (agora.isBefore(session.getInicio()) || agora.isAfter(session.getFim())) {
            throw new IllegalStateException("Sessao encerrada");
        }

        // Registrar check-in
        CheckIn checkIn = new CheckIn(atletaId, agora, latitude, longitude, session);
        checkInRepository.save(checkIn);
    }
}
