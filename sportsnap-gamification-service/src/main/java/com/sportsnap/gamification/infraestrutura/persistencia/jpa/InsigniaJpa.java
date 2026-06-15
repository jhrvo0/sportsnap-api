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
import com.sportsnap.gamification.dominio.desafio.Insignia;
import com.sportsnap.gamification.dominio.desafio.InsigniaRepositorio;

@Entity
@Table(name = "INSIGNIA")
class InsigniaJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    int atletaId;
    String codigo;
    int desafioId;
    LocalDateTime concedidaEm;
}

interface InsigniaJpaRepository extends JpaRepository<InsigniaJpa, Integer> {
    List<InsigniaJpa> findByAtletaIdOrderByConcedidaEmAsc(int atletaId);
    boolean existsByAtletaIdAndDesafioId(int atletaId, int desafioId);
}

@Repository
class InsigniaRepositorioImpl implements InsigniaRepositorio {

    @Autowired
    InsigniaJpaRepository repositorio;

    @Override
    public Insignia salvar(Insignia insignia) {
        return paraDominio(repositorio.save(paraJpa(insignia)));
    }

    @Override
    public List<Insignia> listarPorAtleta(AtletaId atletaId) {
        return repositorio.findByAtletaIdOrderByConcedidaEmAsc(atletaId.getId()).stream()
            .map(this::paraDominio)
            .toList();
    }

    @Override
    public boolean existePorAtletaEDesafio(AtletaId atletaId, int desafioId) {
        return repositorio.existsByAtletaIdAndDesafioId(atletaId.getId(), desafioId);
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    private Insignia paraDominio(InsigniaJpa jpa) {
        return new Insignia(jpa.id, new AtletaId(jpa.atletaId), jpa.codigo, jpa.desafioId, jpa.concedidaEm);
    }

    private InsigniaJpa paraJpa(Insignia dominio) {
        var jpa = new InsigniaJpa();
        jpa.id = dominio.getId();
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.codigo = dominio.getCodigo();
        jpa.desafioId = dominio.getDesafioId();
        jpa.concedidaEm = dominio.getConcedidaEm();
        return jpa;
    }
}
