package com.sportsnap.session.infrastructure.persistence;

import com.sportsnap.session.domain.entities.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaCheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByAtletaId(Long atletaId);

    List<CheckIn> findBySessionId(Long sessionId);
}
