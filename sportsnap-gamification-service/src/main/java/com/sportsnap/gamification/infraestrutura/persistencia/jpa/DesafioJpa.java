package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.desafio.Cadencia;
import com.sportsnap.gamification.dominio.desafio.CriterioDesafio;
import com.sportsnap.gamification.dominio.desafio.Desafio;
import com.sportsnap.gamification.dominio.desafio.DesafioRepositorio;
import com.sportsnap.gamification.dominio.desafio.TipoCriterio;

@Entity
@Table(name = "DESAFIO")
class DesafioJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String titulo;
    LocalDateTime inicio;
    LocalDateTime fim;
    boolean permanente;
    String insigniaCodigo;
    String cadencia;
    boolean repetivel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "DESAFIO_CRITERIO", joinColumns = @JoinColumn(name = "DESAFIO_ID"))
    @OrderColumn(name = "ORDEM")
    List<CriterioDesafioJpa> criterios = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "DESAFIO_PREREQUISITO", joinColumns = @JoinColumn(name = "DESAFIO_ID"))
    @Column(name = "PREREQUISITO_ID")
    Set<Integer> prerequisitos = new LinkedHashSet<>();
}

@Embeddable
class CriterioDesafioJpa {
    String tipo;
    int meta;
    String alvoAtributo;
}

interface DesafioJpaRepository extends JpaRepository<DesafioJpa, Integer> {
}

@Repository
class DesafioRepositorioImpl implements DesafioRepositorio {

    @Autowired
    DesafioJpaRepository repositorio;

    @Override
    public Desafio salvar(Desafio desafio) {
        return paraDominio(repositorio.save(paraJpa(desafio)));
    }

    @Override
    public Optional<Desafio> obterPorId(int id) {
        return repositorio.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Desafio> listarTodos() {
        return repositorio.findAll().stream().map(this::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    private Desafio paraDominio(DesafioJpa jpa) {
        List<CriterioDesafio> criterios = jpa.criterios.stream()
            .map(c -> new CriterioDesafio(TipoCriterio.valueOf(c.tipo), c.meta, c.alvoAtributo))
            .toList();
        return new Desafio(jpa.id, jpa.titulo, criterios, jpa.inicio, jpa.fim, jpa.permanente,
            jpa.insigniaCodigo, new ArrayList<>(jpa.prerequisitos), Cadencia.valueOf(jpa.cadencia),
            jpa.repetivel);
    }

    private DesafioJpa paraJpa(Desafio dominio) {
        var jpa = new DesafioJpa();
        jpa.id = dominio.getId();
        jpa.titulo = dominio.getTitulo();
        jpa.inicio = dominio.getInicio();
        jpa.fim = dominio.getFim();
        jpa.permanente = dominio.isPermanente();
        jpa.insigniaCodigo = dominio.getInsigniaCodigo();
        jpa.cadencia = dominio.getCadencia().name();
        jpa.repetivel = dominio.isRepetivel();
        jpa.criterios = new ArrayList<>();
        for (CriterioDesafio c : dominio.getCriterios()) {
            var cJpa = new CriterioDesafioJpa();
            cJpa.tipo = c.getTipo().name();
            cJpa.meta = c.getMeta();
            cJpa.alvoAtributo = c.getAlvoAtributo();
            jpa.criterios.add(cJpa);
        }
        jpa.prerequisitos = new LinkedHashSet<>(dominio.getPrerequisitos());
        return jpa;
    }
}
