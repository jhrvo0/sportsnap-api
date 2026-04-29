package com.sportsnap.gamification.domain.repositories;

import com.sportsnap.gamification.domain.entities.StatusPotencial;
import java.util.Optional;

public interface StatusPotencialRepository {

    StatusPotencial save(StatusPotencial statusPotencial);

    Optional<StatusPotencial> findById(Long id);

    Optional<StatusPotencial> findByAtletaId(Long atletaId);

    void deleteAll();
}
