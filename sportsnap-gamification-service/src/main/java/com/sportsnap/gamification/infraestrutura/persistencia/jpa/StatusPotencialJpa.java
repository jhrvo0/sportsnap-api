package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.potencial.StatusPotencial;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;

@Entity
@Table(name = "STATUS_POTENCIAL")
class StatusPotencialJpa {
    @Id
    @Column(name = "ATLETA_ID")
    int atletaId;
    double xpAcumulado;
    int streakConsistencia;
    LocalDateTime ultimaAtividade;
}

interface StatusPotencialJpaRepository extends JpaRepository<StatusPotencialJpa, Integer> {
}

@Repository
class StatusPotencialRepositorioImpl implements StatusPotencialRepositorio {

    @Autowired
    StatusPotencialJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public StatusPotencial salvar(StatusPotencial status) {
        var jpa = mapeador.paraJpa(status);
        var salvo = repositorio.save(jpa);
        return mapeador.paraDominio(salvo);
    }

    @Override
    public Optional<StatusPotencial> obterPorAtleta(AtletaId atletaId) {
        return repositorio.findById(atletaId.getId()).map(mapeador::paraDominio);
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}
