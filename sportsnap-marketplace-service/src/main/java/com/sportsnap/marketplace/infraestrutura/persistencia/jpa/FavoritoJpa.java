package com.sportsnap.marketplace.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.sugestao.FavoritoRepositorio;

@Entity
@Table(name = "FAVORITO")
class FavoritoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int atletaId;
    int fotoId;
}

interface FavoritoJpaRepository extends JpaRepository<FavoritoJpa, Integer> {
    List<FavoritoJpa> findByAtletaId(int atletaId);
    Optional<FavoritoJpa> findByAtletaIdAndFotoId(int atletaId, int fotoId);
    void deleteByAtletaIdAndFotoId(int atletaId, int fotoId);
}

@Repository
class FavoritoRepositorioImpl implements FavoritoRepositorio {

    @Autowired
    FavoritoJpaRepository repositorio;

    @Override
    public void adicionar(AtletaId atletaId, FotoId fotoId) {
        var jpa = new FavoritoJpa();
        jpa.atletaId = atletaId.getId();
        jpa.fotoId = fotoId.getId();
        repositorio.save(jpa);
    }

    @Transactional
    @Override
    public void remover(AtletaId atletaId, FotoId fotoId) {
        repositorio.deleteByAtletaIdAndFotoId(atletaId.getId(), fotoId.getId());
    }

    @Override
    public List<FotoId> listarPorAtleta(AtletaId atletaId) {
        return repositorio.findByAtletaId(atletaId.getId()).stream()
            .map(f -> new FotoId(f.fotoId))
            .toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}
