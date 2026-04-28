package com.sportsnap.session.infrastructure.persistence;

import com.sportsnap.session.domain.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaSessionRepository extends JpaRepository<Session, Long> {

    List<Session> findBySpotId(Long spotId);
}
