package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.reveal.RegistroSincronizacao;
import com.sportsnap.gamification.dominio.reveal.RegistroSincronizacaoRepositorio;

@Entity
@Table(name = "REGISTRO_SINCRONIZACAO")
class RegistroSincronizacaoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    int atletaId;
    LocalDateTime ocorridoEm;
    int orcamentoPontos;
    int custoTotal;
    double overallAnterior;
    double overallNovo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "REGISTRO_SINC_ALOCACAO", joinColumns = @JoinColumn(name = "REGISTRO_ID"))
    @MapKeyColumn(name = "ATRIBUTO")
    @Column(name = "INCREMENTO")
    Map<String, Integer> alocacao = new LinkedHashMap<>();
}

interface RegistroSincronizacaoJpaRepository extends JpaRepository<RegistroSincronizacaoJpa, Integer> {
    List<RegistroSincronizacaoJpa> findByAtletaIdOrderByOcorridoEmAsc(int atletaId);
}

@Repository
class RegistroSincronizacaoRepositorioImpl implements RegistroSincronizacaoRepositorio {

    @Autowired
    RegistroSincronizacaoJpaRepository repositorio;

    @Override
    public RegistroSincronizacao salvar(RegistroSincronizacao registro) {
        return paraDominio(repositorio.save(paraJpa(registro)));
    }

    @Override
    public List<RegistroSincronizacao> listarPorAtleta(AtletaId atletaId) {
        return repositorio.findByAtletaIdOrderByOcorridoEmAsc(atletaId.getId()).stream()
            .map(this::paraDominio)
            .toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    private RegistroSincronizacao paraDominio(RegistroSincronizacaoJpa jpa) {
        return new RegistroSincronizacao(jpa.id, new AtletaId(jpa.atletaId), jpa.ocorridoEm,
            jpa.orcamentoPontos, jpa.custoTotal, jpa.overallAnterior, jpa.overallNovo,
            new LinkedHashMap<>(jpa.alocacao));
    }

    private RegistroSincronizacaoJpa paraJpa(RegistroSincronizacao dominio) {
        var jpa = new RegistroSincronizacaoJpa();
        jpa.id = dominio.getId();
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.ocorridoEm = dominio.getOcorridoEm();
        jpa.orcamentoPontos = dominio.getOrcamentoPontos();
        jpa.custoTotal = dominio.getCustoTotal();
        jpa.overallAnterior = dominio.getOverallAnterior();
        jpa.overallNovo = dominio.getOverallNovo();
        jpa.alocacao = new LinkedHashMap<>(dominio.getAlocacao());
        return jpa;
    }
}
