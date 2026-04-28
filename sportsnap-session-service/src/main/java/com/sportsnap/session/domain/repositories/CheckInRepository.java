package com.sportsnap.session.domain.repositories;

import com.sportsnap.session.domain.entities.CheckIn;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository {

    CheckIn save(CheckIn checkIn);

    Optional<CheckIn> findById(Long id);

    List<CheckIn> findByAtletaId(Long atletaId);

    List<CheckIn> findBySessionId(Long sessionId);
}
