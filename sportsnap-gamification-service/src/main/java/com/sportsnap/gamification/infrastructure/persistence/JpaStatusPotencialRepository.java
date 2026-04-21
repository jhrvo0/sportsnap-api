package com.sportsnap.gamification.infrastructure.persistence;

import com.sportsnap.gamification.domain.entities.StatusPotencial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaStatusPotencialRepository extends JpaRepository<StatusPotencial, Long> {

    Optional<StatusPotencial> findByAtletaId(Long atletaId);
}
