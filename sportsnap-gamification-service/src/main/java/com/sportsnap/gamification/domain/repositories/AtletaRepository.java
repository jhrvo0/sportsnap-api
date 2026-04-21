package com.sportsnap.gamification.domain.repositories;

import com.sportsnap.gamification.domain.entities.Atleta;
import java.util.List;
import java.util.Optional;

public interface AtletaRepository {

    Atleta save(Atleta atleta);

    Optional<Atleta> findById(Long id);

    Optional<Atleta> findByEmail(String email);

    List<Atleta> findAll();

    void deleteById(Long id);
}
