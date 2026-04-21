package com.sportsnap.gamification.infrastructure.persistence;

import com.sportsnap.gamification.domain.entities.CartaOficial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCartaOficialRepository extends JpaRepository<CartaOficial, Long> {

    Optional<CartaOficial> findByAtletaId(Long atletaId);

    List<CartaOficial> findAllByOrderByOverallDesc();
}
