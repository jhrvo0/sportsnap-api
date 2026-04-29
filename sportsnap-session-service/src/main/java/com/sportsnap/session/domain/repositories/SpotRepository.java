package com.sportsnap.session.domain.repositories;

import com.sportsnap.session.domain.entities.Spot;
import java.util.List;
import java.util.Optional;

public interface SpotRepository {

    Spot save(Spot spot);

    Optional<Spot> findById(Long id);

    List<Spot> findAll();

    void deleteAll();
}
