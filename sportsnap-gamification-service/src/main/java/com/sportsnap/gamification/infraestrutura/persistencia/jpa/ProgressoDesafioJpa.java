package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.sportsnap.gamification.dominio.desafio.ProgressoDesafio;
import com.sportsnap.gamification.dominio.desafio.ProgressoDesafioRepositorio;
import com.sportsnap.gamification.dominio.desafio.StatusProgresso;

@Entity
@Table(name = "PROGRESSO_DESAFIO")
class ProgressoDesafioJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    int atletaId;
    int desafioId;
    String status;
    boolean insigniaConcedida;
    LocalDate cicloReferencia;
    LocalDateTime iniciadoEm;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PROGRESSO_CONTADOR", joinColumns = @JoinColumn(name = "PROGRESSO_ID"))
    @MapKeyColumn(name = "CRITERIO_INDICE")
    @Column(name = "CONTADOR")
    Map<Integer, Integer> contadores = new LinkedHashMap<>();
}

interface ProgressoDesafioJpaRepository extends JpaRepository<ProgressoDesafioJpa, Integer> {
    List<ProgressoDesafioJpa> findByAtletaId(int atletaId);
    Optional<ProgressoDesafioJpa> findByAtletaIdAndDesafioIdAndStatus(int atletaId, int desafioId, String status);
}

@Repository
class ProgressoDesafioRepositorioImpl implements ProgressoDesafioRepositorio {

    @Autowired
    ProgressoDesafioJpaRepository repositorio;

    @Override
    public ProgressoDesafio salvar(ProgressoDesafio progresso) {
        return paraDominio(repositorio.save(paraJpa(progresso)));
    }

    @Override
    public Optional<ProgressoDesafio> obterPorId(int id) {
        return repositorio.findById(id).map(this::paraDominio);
    }

    @Override
    public Optional<ProgressoDesafio> obterAtivo(AtletaId atletaId, int desafioId) {
        return repositorio.findByAtletaIdAndDesafioIdAndStatus(
            atletaId.getId(), desafioId, StatusProgresso.ATIVO.name()).map(this::paraDominio);
    }

    @Override
    public List<ProgressoDesafio> listarPorAtleta(AtletaId atletaId) {
        return repositorio.findByAtletaId(atletaId.getId()).stream().map(this::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    private ProgressoDesafio paraDominio(ProgressoDesafioJpa jpa) {
        return new ProgressoDesafio(jpa.id, new AtletaId(jpa.atletaId), jpa.desafioId,
            new LinkedHashMap<>(jpa.contadores), StatusProgresso.valueOf(jpa.status),
            jpa.insigniaConcedida, jpa.cicloReferencia, jpa.iniciadoEm);
    }

    private ProgressoDesafioJpa paraJpa(ProgressoDesafio dominio) {
        var jpa = new ProgressoDesafioJpa();
        jpa.id = dominio.getId();
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.desafioId = dominio.getDesafioId();
        jpa.status = dominio.getStatus().name();
        jpa.insigniaConcedida = dominio.isInsigniaConcedida();
        jpa.cicloReferencia = dominio.getCicloReferencia();
        jpa.iniciadoEm = dominio.getIniciadoEm();
        jpa.contadores = new LinkedHashMap<>(dominio.getContadores());
        return jpa;
    }
}
