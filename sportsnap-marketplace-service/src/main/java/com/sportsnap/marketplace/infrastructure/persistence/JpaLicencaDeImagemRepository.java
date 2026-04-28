package com.sportsnap.marketplace.infrastructure.persistence;

import com.sportsnap.marketplace.domain.entities.LicencaDeImagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaLicencaDeImagemRepository extends JpaRepository<LicencaDeImagem, Long> {

    List<LicencaDeImagem> findByAtletaId(Long atletaId);

    List<LicencaDeImagem> findByFotoId(Long fotoId);
}
