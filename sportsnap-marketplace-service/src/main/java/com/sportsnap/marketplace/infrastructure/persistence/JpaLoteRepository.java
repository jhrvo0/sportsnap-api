package com.sportsnap.marketplace.infrastructure.persistence;

import com.sportsnap.marketplace.domain.entities.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaLoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findBySessionId(Long sessionId);

    List<Lote> findByFotografoId(Long fotografoId);
}
