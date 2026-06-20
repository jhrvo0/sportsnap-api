package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.aplicacao.perfil.PerfilRepositorioAplicacao;
import com.sportsnap.gamification.aplicacao.perfil.PerfilResumo;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.perfil.Perfil;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.perfil.PerfilRepositorio;
import com.sportsnap.gamification.dominio.perfil.TipoConta;
import com.sportsnap.gamification.dominio.perfil.Visibilidade;

@Entity
@Table(name = "PERFIL_SOCIAL")
class PerfilJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "USUARIO_ID")
    int usuarioId;

    @Column(name = "NOME_EXIBICAO")
    String nomeExibicao;

    @Column(name = "TIPO_CONTA")
    String tipoConta;

    String bio;
    String esporte;
    String localidade;
    String visibilidade;

    @Column(name = "TOTAL_SEGUIDORES")
    int totalSeguidores;

    @Column(name = "TOTAL_SEGUINDO")
    int totalSeguindo;

    @Column(name = "FOTO_PERFIL", length = 1000000)
    String fotoPerfil;
}

interface PerfilJpaRepository extends JpaRepository<PerfilJpa, Integer> {
    Optional<PerfilJpa> findByUsuarioId(int usuarioId);
    List<PerfilResumo> findPerfilResumoBy();
}

@Repository
class PerfilRepositorioImpl implements PerfilRepositorio, PerfilRepositorioAplicacao {

    @Autowired
    PerfilJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public Perfil salvar(Perfil perfil) {
        var jpa  = mapeador.paraJpa(perfil);
        var salvo = repositorio.save(jpa);
        return mapeador.paraDominio(salvo);
    }

    @Override
    public Optional<Perfil> obter(PerfilId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public Optional<Perfil> obterPorUsuario(AtletaId usuarioId) {
        return repositorio.findByUsuarioId(usuarioId.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<Perfil> listarTodos() {
        return repositorio.findAll().stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    @Override
    public List<PerfilResumo> pesquisarResumos() {
        return repositorio.findPerfilResumoBy();
    }
}
