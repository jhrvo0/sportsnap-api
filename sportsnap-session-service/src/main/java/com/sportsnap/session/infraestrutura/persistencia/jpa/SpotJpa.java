package com.sportsnap.session.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
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

import com.sportsnap.session.aplicacao.spot.SpotRepositorioAplicacao;
import com.sportsnap.session.aplicacao.spot.SpotResumo;
import com.sportsnap.session.dominio.spot.Coordenada;
import com.sportsnap.session.dominio.spot.Spot;
import com.sportsnap.session.dominio.spot.SpotId;
import com.sportsnap.session.dominio.spot.SpotRepositorio;

@Entity
@Table(name = "SPOT")
class SpotJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String nome;
    double latitude;
    double longitude;
    String descricao;

    @Override
    public String toString() {
        return nome;
    }
}

interface SpotJpaRepository extends JpaRepository<SpotJpa, Integer> {
    List<SpotResumo> findSpotResumoBy();
}

@Repository
class SpotRepositorioImpl implements SpotRepositorio, SpotRepositorioAplicacao {

    @Autowired
    SpotJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public Spot salvar(Spot spot) {
        var salvo = repositorio.save(mapeador.paraJpa(spot));
        return mapeador.paraDominio(salvo);
    }

    @Override
    public Optional<Spot> obter(SpotId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<Spot> listarTodos() {
        return repositorio.findAll().stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    @Override
    public void remover(SpotId id) {
        repositorio.deleteById(id.getId());
    }

    @Override
    public List<SpotResumo> pesquisarResumos() {
        return repositorio.findSpotResumoBy();
    }
}
