package com.sportsnap.marketplace.infrastructure.persistence;

import com.sportsnap.marketplace.domain.entities.Foto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaFotoRepository extends JpaRepository<Foto, Long> {

    List<Foto> findByLoteId(Long loteId);
}
