package com.sportsnap.gamification.domain.repositories;

import com.sportsnap.gamification.domain.entities.CartaOficial;
import java.util.List;
import java.util.Optional;

public interface CartaOficialRepository {

    CartaOficial save(CartaOficial cartaOficial);

    Optional<CartaOficial> findById(Long id);

    Optional<CartaOficial> findByAtletaId(Long atletaId);

    List<CartaOficial> findAllOrderByOverallDesc();
}
