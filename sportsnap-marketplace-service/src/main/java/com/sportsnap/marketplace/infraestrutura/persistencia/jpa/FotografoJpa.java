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

import com.sportsnap.marketplace.aplicacao.fotografo.FotografoRepositorioAplicacao;
import com.sportsnap.marketplace.aplicacao.fotografo.FotografoResumo;
import com.sportsnap.marketplace.dominio.fotografo.Fotografo;
import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.fotografo.FotografoRepositorio;

@Entity
@Table(name = "FOTOGRAFO")
class FotografoJpa {
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

interface FotografoJpaRepository extends JpaRepository<FotografoJpa, Integer> {
    List<FotografoResumo> findFotografoResumoBy();
}

@Repository
class FotografoRepositorioImpl implements FotografoRepositorio, FotografoRepositorioAplicacao {

    @Autowired
    FotografoJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public Fotografo salvar(Fotografo fotografo) {
        var salvo = repositorio.save(mapeador.paraJpa(fotografo));
        return mapeador.paraDominio(salvo);
    }

    @Override
    public Optional<Fotografo> obter(FotografoId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<Fotografo> listarTodos() {
        return repositorio.findAll().stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    @Override
    public List<FotografoResumo> pesquisarResumos() {
        return repositorio.findFotografoResumoBy();
    }
}
