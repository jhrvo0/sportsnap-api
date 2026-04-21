package com.sportsnap.marketplace.infrastructure.persistence;

import com.sportsnap.marketplace.domain.entities.Fotografo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaFotografoRepository extends JpaRepository<Fotografo, Long> {

    Optional<Fotografo> findByEmail(String email);
}
