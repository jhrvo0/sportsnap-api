package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.sincronizacao.Licenca;
import com.sportsnap.gamification.dominio.sincronizacao.LicencaRepositorio;

@Entity
@Table(name = "LICENCA_GAMIFICATION")
class LicencaJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int atletaId;
    LocalDateTime adquiridaEm;
}

interface LicencaJpaRepository extends JpaRepository<LicencaJpa, Integer> {
    List<LicencaJpa> findByAtletaId(int atletaId);
    boolean existsByAtletaIdAndAdquiridaEmAfter(int atletaId, LocalDateTime instante);
}

@Repository
class LicencaRepositorioImpl implements LicencaRepositorio {

    @Autowired
    LicencaJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public void registrar(Licenca licenca) {
        repositorio.save(mapeador.paraJpa(licenca));
    }

    @Override
    public List<Licenca> listarPorAtleta(AtletaId atletaId) {
        return repositorio.findByAtletaId(atletaId.getId()).stream()
            .map(mapeador::paraDominio)
            .toList();
    }

    @Override
    public boolean existeLicencaPosterior(AtletaId atletaId, LocalDateTime instante) {
        return repositorio.existsByAtletaIdAndAdquiridaEmAfter(atletaId.getId(), instante);
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}
