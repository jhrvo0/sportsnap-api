package com.sportsnap.session.domain.repositories;

import com.sportsnap.session.domain.entities.Session;
import java.util.List;
import java.util.Optional;

public interface SessionRepository {

    Session save(Session session);

    Optional<Session> findById(Long id);

    List<Session> findBySpotId(Long spotId);

    void deleteAll();
}
