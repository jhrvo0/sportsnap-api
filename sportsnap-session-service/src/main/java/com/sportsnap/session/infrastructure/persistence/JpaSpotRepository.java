package com.sportsnap.session.infrastructure.persistence;

import com.sportsnap.session.domain.entities.Spot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSpotRepository extends JpaRepository<Spot, Long> {
}
