package com.sportsnap.gamification.infrastructure.persistence;

import com.sportsnap.gamification.domain.entities.AtributoEsportivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaAtributoEsportivoRepository extends JpaRepository<AtributoEsportivo, Long> {

    List<AtributoEsportivo> findByCartaOficialId(Long cartaOficialId);
}
