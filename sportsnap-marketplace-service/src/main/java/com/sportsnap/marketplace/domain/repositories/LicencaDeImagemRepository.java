package com.sportsnap.marketplace.domain.repositories;

import com.sportsnap.marketplace.domain.entities.LicencaDeImagem;
import java.util.List;
import java.util.Optional;

public interface LicencaDeImagemRepository {

    LicencaDeImagem save(LicencaDeImagem licenca);

    Optional<LicencaDeImagem> findById(Long id);

    List<LicencaDeImagem> findByAtletaId(Long atletaId);

    List<LicencaDeImagem> findByFotoId(Long fotoId);
}
