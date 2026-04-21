package com.sportsnap.gamification.infrastructure.persistence;

import com.sportsnap.gamification.domain.entities.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaAtletaRepository extends JpaRepository<Atleta, Long> {

    Optional<Atleta> findByEmail(String email);
}
