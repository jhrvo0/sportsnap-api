package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.competicao.EntradaSnapshot;
import com.sportsnap.gamification.dominio.competicao.StatusTemporada;
import com.sportsnap.gamification.dominio.competicao.Temporada;
import com.sportsnap.gamification.dominio.competicao.TemporadaRepositorio;

@Entity
@Table(name = "TEMPORADA")
class TemporadaJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String modalidade;
    LocalDateTime inicio;
    LocalDateTime fim;
    String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TEMPORADA_SNAPSHOT", joinColumns = @JoinColumn(name = "TEMPORADA_ID"))
    List<EntradaSnapshotJpa> snapshot = new ArrayList<>();
}

@Embeddable
class EntradaSnapshotJpa {
    int atletaId;
    int posicao;
    double pr;
}

interface TemporadaJpaRepository extends JpaRepository<TemporadaJpa, Integer> {
    List<TemporadaJpa> findByModalidadeIgnoreCase(String modalidade);
}

@Repository
class TemporadaRepositorioImpl implements TemporadaRepositorio {

    @Autowired
    TemporadaJpaRepository repositorio;

    @Override
    public Temporada salvar(Temporada temporada) {
        return paraDominio(repositorio.save(paraJpa(temporada)));
    }

    @Override
    public Optional<Temporada> obterPorId(int id) {
        return repositorio.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Temporada> listarTodas() {
        return repositorio.findAll().stream().map(this::paraDominio).toList();
    }

    @Override
    public List<Temporada> listarPorModalidade(String modalidade) {
        return repositorio.findByModalidadeIgnoreCase(modalidade).stream().map(this::paraDominio).toList();
    }

    @Override
    public Optional<Temporada> obterVigente(String modalidade, LocalDateTime instante) {
        return repositorio.findByModalidadeIgnoreCase(modalidade).stream()
            .map(this::paraDominio)
            .filter(t -> t.estaAtivaEm(instante))
            .findFirst();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    private Temporada paraDominio(TemporadaJpa jpa) {
        List<EntradaSnapshot> snapshot = jpa.snapshot.stream()
            .map(e -> new EntradaSnapshot(new AtletaId(e.atletaId), e.posicao, e.pr))
            .toList();
        return new Temporada(jpa.id, jpa.modalidade, jpa.inicio, jpa.fim,
            StatusTemporada.valueOf(jpa.status), snapshot);
    }

    private TemporadaJpa paraJpa(Temporada dominio) {
        var jpa = new TemporadaJpa();
        jpa.id = dominio.getId();
        jpa.modalidade = dominio.getModalidade();
        jpa.inicio = dominio.getInicio();
        jpa.fim = dominio.getFim();
        jpa.status = dominio.getStatus().name();
        jpa.snapshot = new ArrayList<>();
        for (EntradaSnapshot entrada : dominio.getSnapshotFinal()) {
            var e = new EntradaSnapshotJpa();
            e.atletaId = entrada.getAtletaId().getId();
            e.posicao = entrada.getPosicao();
            e.pr = entrada.getPr();
            jpa.snapshot.add(e);
        }
        return jpa;
    }
}
