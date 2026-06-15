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
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucao;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucaoRepositorio;

@Entity
@Table(name = "REGISTRO_EVOLUCAO")
class RegistroEvolucaoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    int atletaId;
    double overallAnterior;
    double overallNovo;
    LocalDateTime ocorridoEm;
}

interface RegistroEvolucaoJpaRepository extends JpaRepository<RegistroEvolucaoJpa, Integer> {
    List<RegistroEvolucaoJpa> findByAtletaIdOrderByOcorridoEmAsc(int atletaId);
    long countByAtletaId(int atletaId);
}

@Repository
class RegistroEvolucaoRepositorioImpl implements RegistroEvolucaoRepositorio {

    @Autowired
    RegistroEvolucaoJpaRepository repositorio;

    @Override
    public RegistroEvolucao inserir(RegistroEvolucao registro) {
        return paraDominio(repositorio.save(paraJpa(registro)));
    }

    @Override
    public List<RegistroEvolucao> listarPorAtleta(AtletaId atletaId) {
        return repositorio.findByAtletaIdOrderByOcorridoEmAsc(atletaId.getId()).stream()
            .map(this::paraDominio)
            .toList();
    }

    @Override
    public long contarPorAtleta(AtletaId atletaId) {
        return repositorio.countByAtletaId(atletaId.getId());
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    private RegistroEvolucao paraDominio(RegistroEvolucaoJpa jpa) {
        return new RegistroEvolucao(jpa.id, new AtletaId(jpa.atletaId),
            jpa.overallAnterior, jpa.overallNovo, jpa.ocorridoEm);
    }

    private RegistroEvolucaoJpa paraJpa(RegistroEvolucao dominio) {
        var jpa = new RegistroEvolucaoJpa();
        jpa.id = dominio.getId();
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.overallAnterior = dominio.getOverallAnterior();
        jpa.overallNovo = dominio.getOverallNovo();
        jpa.ocorridoEm = dominio.getOcorridoEm();
        return jpa;
    }
}
