package com.sportsnap.session.application;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.repositories.CheckInRepository;
import com.sportsnap.session.domain.repositories.SessionRepository;
import com.sportsnap.session.domain.usecases.MotorDeMatchAutomatico;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MotorDeMatchAutomaticoImpl implements MotorDeMatchAutomatico {

    private final SessionRepository sessionRepository;
    private final CheckInRepository checkInRepository;

    public MotorDeMatchAutomaticoImpl(SessionRepository sessionRepository,
                                       CheckInRepository checkInRepository) {
        this.sessionRepository = sessionRepository;
        this.checkInRepository = checkInRepository;
    }

    @Override
    public List<Long> executar(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session nao encontrada: " + sessionId));

        List<CheckIn> checkIns = checkInRepository.findBySessionId(sessionId);

        if (checkIns.isEmpty()) {
            return List.of();
        }

        Set<Long> atletasComMatch = new HashSet<>();

        for (CheckIn checkIn : checkIns) {
            if (!checkIn.getHorario().isBefore(session.getInicio()) &&
                !checkIn.getHorario().isAfter(session.getFim())) {
                atletasComMatch.add(checkIn.getAtletaId());
            }
        }

        return new ArrayList<>(atletasComMatch);
    }
}
