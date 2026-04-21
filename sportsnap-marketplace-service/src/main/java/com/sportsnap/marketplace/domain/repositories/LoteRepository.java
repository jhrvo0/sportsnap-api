package com.sportsnap.marketplace.domain.repositories;

import com.sportsnap.marketplace.domain.entities.Lote;
import java.util.List;
import java.util.Optional;

public interface LoteRepository {

    Lote save(Lote lote);

    Optional<Lote> findById(Long id);

    List<Lote> findBySessionId(Long sessionId);

    List<Lote> findByFotografoId(Long fotografoId);
}
