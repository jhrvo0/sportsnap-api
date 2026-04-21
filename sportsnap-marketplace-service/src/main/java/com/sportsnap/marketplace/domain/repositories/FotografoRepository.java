package com.sportsnap.marketplace.domain.repositories;

import com.sportsnap.marketplace.domain.entities.Fotografo;
import java.util.List;
import java.util.Optional;

public interface FotografoRepository {

    Fotografo save(Fotografo fotografo);

    Optional<Fotografo> findById(Long id);

    Optional<Fotografo> findByEmail(String email);

    List<Fotografo> findAll();
}
