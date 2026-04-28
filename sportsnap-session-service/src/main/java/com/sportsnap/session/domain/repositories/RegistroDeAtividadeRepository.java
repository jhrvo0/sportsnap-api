package com.sportsnap.session.domain.repositories;

import com.sportsnap.session.domain.entities.RegistroDeAtividade;
import java.util.List;
import java.util.Optional;

public interface RegistroDeAtividadeRepository {

    RegistroDeAtividade save(RegistroDeAtividade registro);

    Optional<RegistroDeAtividade> findById(Long id);

    List<RegistroDeAtividade> findByCheckInId(Long checkInId);
}
