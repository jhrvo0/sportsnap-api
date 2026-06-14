package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.util.ArrayList;
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

import com.sportsnap.gamification.aplicacao.atleta.AtletaRepositorioAplicacao;
import com.sportsnap.gamification.aplicacao.atleta.AtletaResumo;
import com.sportsnap.gamification.dominio.atleta.Atleta;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.atleta.AtletaRepositorio;

@Entity
@Table(name = "ATLETA")
class AtletaJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String nome;
    String email;

    @Override
    public String toString() {
        return nome;
    }
}

interface AtletaJpaRepository extends JpaRepository<AtletaJpa, Integer> {
    List<AtletaResumo> findAtletaResumoBy();
}

@Repository
class AtletaRepositorioImpl implements AtletaRepositorio, AtletaRepositorioAplicacao {

    @Autowired
    AtletaJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public Atleta salvar(Atleta atleta) {
        var jpa = mapeador.paraJpa(atleta);
        var salvo = repositorio.save(jpa);
        return mapeador.paraDominio(salvo);
    }

    @Override
    public Optional<Atleta> obter(AtletaId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<Atleta> listarTodos() {
        return repositorio.findAll().stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    @Override
    public List<AtletaResumo> pesquisarResumos() {
        return repositorio.findAtletaResumoBy();
    }
}
