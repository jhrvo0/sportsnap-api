package com.sportsnap.session.infrastructure.persistence;

import com.sportsnap.session.domain.entities.RegistroDeAtividade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRegistroDeAtividadeRepository extends JpaRepository<RegistroDeAtividade, Long> {

    List<RegistroDeAtividade> findByCheckInId(Long checkInId);
}
