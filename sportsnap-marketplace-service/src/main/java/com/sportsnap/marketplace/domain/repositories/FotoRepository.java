package com.sportsnap.marketplace.domain.repositories;

import com.sportsnap.marketplace.domain.entities.Foto;
import java.util.List;
import java.util.Optional;

public interface FotoRepository {

    Foto save(Foto foto);

    Optional<Foto> findById(Long id);

    List<Foto> findByLoteId(Long loteId);
}
